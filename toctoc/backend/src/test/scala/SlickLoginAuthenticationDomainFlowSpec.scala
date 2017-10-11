package io.buildo.toctoc.authentication

import org.scalatest._

import slick.jdbc.PostgresProfile.api._
import slick.jdbc.JdbcBackend.Database

import io.buildo.toctoc.authentication.TokenBasedAuthentication._
import io.buildo.toctoc.authentication.login._
import io.buildo.toctoc.authentication.token._

import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.ExecutionContext.Implicits.global

class SlickLoginAuthenticationDomainFlowSpec extends FlatSpec with BeforeAndAfterAll with ScalaFutures with EitherValues {

  val db = Database.forConfig("db")
  val loginAuthDomain = new SlickLoginAuthenticationDomain(db)
  val accessTokenAuthDomain = new SlickAccessTokenAuthenticationDomain(db)

  val loginTable = loginAuthDomain.loginTable
  val accessTokenTable = accessTokenAuthDomain.accessTokenTable
  val schema = loginTable.schema ++ accessTokenTable.schema

  val authFlow = new SlickTokenBasedAuthenticationFlow(24 * 60 * 60, db)

  override def beforeAll() = {
    db.run((schema).create).futureValue
  }

  override def afterAll() = {
    db.run((schema).drop).futureValue
  }

  case class User(ref: String) extends Subject
  val login = Login("username", "password")
  val subject = User("test")

  "unregistered login credentials" should "not be accepted when exchanging for token" in {
    authFlow.exchangeForTokens(login).futureValue.left.get
  }

  "registered login credentials" should "be accepted when exchanging for token" in {
    authFlow.registerSubjectCredentials(subject, login).futureValue.right.get
    authFlow.exchangeForTokens(login).futureValue.right.get
  }

  "token obtained by login" should "be validated" in {
    val token = authFlow.exchangeForTokens(login).futureValue.right.get
    authFlow.validateToken(token).futureValue.right.get
  }

  "multiple login with same values" should "not be accepted in registration" in {
    authFlow.registerSubjectCredentials(subject, login).futureValue.left.get
  }

}
