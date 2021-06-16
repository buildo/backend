package io.buildo.toctoc
package slick
package authentication
package test

import core.authentication.TokenBasedAuthentication._
import _root_.slick.jdbc.MySQLProfile.api._
import _root_.slick.jdbc.JdbcBackend.Database
import io.buildo.toctoc.slick.authentication.login.MySqlSlickLoginAuthenticationDomain
import io.buildo.toctoc.slick.authentication.token.MySqlSlickAccessTokenAuthenticationDomain
import cats.effect.IO
import java.time.Duration

class MySqlSlickLoginAuthenticationDomainFlowSpec extends munit.FunSuite {

  val loginTableName = "login"
  val tokenTableName = "token"

  val db = Database.forConfig("db")
  val loginAuthDomain = new MySqlSlickLoginAuthenticationDomain[IO](db, loginTableName)
  val accessTokenAuthDomain = new MySqlSlickAccessTokenAuthenticationDomain[IO](db, tokenTableName)

  val loginTable = loginAuthDomain.loginTable
  val accessTokenTable = accessTokenAuthDomain.accessTokenTable
  val schema = loginTable.schema ++ accessTokenTable.schema

  val authFlow =
    new TokenBasedAuthenticationFlow[IO](loginAuthDomain, accessTokenAuthDomain, Duration.ofDays(1))

  implicit val contextShift = IO.contextShift(munitExecutionContext)

  override def beforeAll(): Unit = {
    IO.fromFuture(IO(db.run(schema.create))).unsafeRunSync
  }

  override def afterEach(context: AfterEach): Unit =
    IO.fromFuture(IO(db.run(schema.truncate))).unsafeRunSync

  override def afterAll(): Unit = {
    IO.fromFuture(IO(db.run(schema.drop))).unsafeRunSync
    db.close()
  }

  val login = Login("username", "password")
  val login2 = Login("usernameoso", "password")
  val login3 = Login("usernameosone", "password")
  val subject = UserSubject("test")
  val subject2 = UserSubject("test2")

  test("unregistered login credentials should not be accepted when exchanging for token") {
    assert(authFlow.exchangeForTokens(login).unsafeRunSync.isLeft)
  }

  test("registered login credentials should be accepted when exchanging for token") {
    assert(authFlow.registerSubjectLogin(subject, login).unsafeRunSync.isRight)
    assert(authFlow.exchangeForTokens(login).unsafeRunSync.isRight)
  }

  test("token obtained by login should be validated") {
    assert(authFlow.registerSubjectLogin(subject, login).unsafeRunSync.isRight)
    val token = authFlow.exchangeForTokens(login).unsafeRunSync.right.get
    assertEquals(authFlow.validateToken(token).unsafeRunSync.right.get, subject)
  }

  test("multiple login with same values should not be accepted in registration") {
    assert(authFlow.registerSubjectLogin(subject, login).unsafeRunSync.isRight)
    assert(authFlow.registerSubjectLogin(subject, login).unsafeRunSync.isLeft)
  }

  test("multiple login with different values should be accepted in registration") {
    assert(authFlow.registerSubjectLogin(subject, login).unsafeRunSync.isRight)
    assert(authFlow.registerSubjectLogin(subject2, login2).unsafeRunSync.isRight)
    val token2 = authFlow.exchangeForTokens(login2).unsafeRunSync.right.get
    assertEquals(authFlow.validateToken(token2).unsafeRunSync.right.get, subject2)
  }

  test("single token unregistration should unregister only the specific token") {
    assert(authFlow.registerSubjectLogin(subject, login).unsafeRunSync.isRight)
    assert(authFlow.registerSubjectLogin(subject2, login2).unsafeRunSync.isRight)
    val token = authFlow.exchangeForTokens(login).unsafeRunSync.right.get
    val token2 = authFlow.exchangeForTokens(login2).unsafeRunSync.right.get
    authFlow.unregisterToken(token).unsafeRunSync
    assert(authFlow.validateToken(token).unsafeRunSync.isLeft)
    assert(authFlow.validateToken(token2).unsafeRunSync.isRight)
  }

  test("token unregistration should unregister all subject's tokens") {
    assert(authFlow.registerSubjectLogin(subject, login).unsafeRunSync.isRight)
    assert(authFlow.registerSubjectLogin(subject2, login2).unsafeRunSync.isRight)
    val token = authFlow.exchangeForTokens(login).unsafeRunSync.right.get
    val token2 = authFlow.exchangeForTokens(login).unsafeRunSync.right.get
    val token3 = authFlow.exchangeForTokens(login2).unsafeRunSync.right.get
    authFlow.unregisterAllSubjectTokens(subject).unsafeRunSync
    assert(authFlow.validateToken(token).unsafeRunSync.isLeft)
    assert(authFlow.validateToken(token2).unsafeRunSync.isLeft)
    assert(authFlow.validateToken(token3).unsafeRunSync.isRight)
  }

  test("single subject credentials unregistration should take effect") {
    assert(authFlow.registerSubjectLogin(subject, login).unsafeRunSync.isRight)
    authFlow.unregisterLogin(login).unsafeRunSync
    assert(authFlow.exchangeForTokens(login).unsafeRunSync.isLeft)
  }

  test("subject credentials unregistration should take effect") {
    assert(authFlow.registerSubjectLogin(subject, login).unsafeRunSync.isRight)
    assert(authFlow.registerSubjectLogin(subject, login3).unsafeRunSync.isRight)
    authFlow.unregisterAllSubjectLogins(subject).unsafeRunSync
    assert(authFlow.exchangeForTokens(login).unsafeRunSync.isLeft)
    assert(authFlow.exchangeForTokens(login3).unsafeRunSync.isLeft)
  }

}
