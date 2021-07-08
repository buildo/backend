import sbt._
import sbt.Keys._
import scala.language.reflectiveCalls

object Dependencies {

  val V = new {
    val circe = "0.14.1"
    val scalacheck = "1.15.4"
    val mockito = "1.9.5"
    val akka = "2.6.15"
    val akkaHttp = "10.2.4"
    val akkaHttpCirce = "1.37.0"
    val awscala = "0.8.5"
    val cats = "2.6.1"
    val catsEffect = "2.5.1"
    val monixCatnap = "3.4.0"
    val config = "1.4.1"
    val scalacacheGuava = "0.28.0"
    val scalaLogging = "3.9.4"
    val logback = "1.2.3"
    val leveldb = "1.8"
    val mailin = "3.0.1"
    val jakartaMail = "2.0.1"
    val slick = "3.3.3"
    val postgresql = "42.2.21"
    val mysql = "8.0.25"
    val ldap = "6.0.0"
    val flyway = "5.2.4"
    val bcrypt = "0.4"
    val slf4j = "1.7.31"
    val scalameta = "4.4.22"
    val scalafmtCore = "2.0.0-RC5"
    val plantuml = "8059"
    val pprint = "0.6.6"
    val tapir = "0.14.5"
    val munit = "0.7.26"
  }

  val circeCore = "io.circe" %% "circe-core" % V.circe
  val circeParser = "io.circe" %% "circe-parser" % V.circe
  val circeGeneric = "io.circe" %% "circe-generic" % V.circe
  val circeGenericExtras = "io.circe" %% "circe-generic-extras" % V.circe
  val scalacheck = "org.scalacheck" %% "scalacheck" % V.scalacheck
  val mockito = "org.mockito" % "mockito-all" % V.mockito
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % V.akka
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % V.akka
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % V.akkaHttp
  val akkaHttpTestKitBase = "com.typesafe.akka" %% "akka-http-testkit" % V.akkaHttp
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % V.akka
  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % V.akka
  val awscala = "com.github.seratch" %% "awscala" % V.awscala
  val catsCore = "org.typelevel" %% "cats-core" % V.cats
  val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
  val alleyCatsCore = "org.typelevel" %% "alleycats-core" % V.cats
  val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % V.akkaHttpCirce
  val typesafeConfig = "com.typesafe" % "config" % V.config
  val scalaCacheGuava = "com.github.cb372" %% "scalacache-guava" % V.scalacacheGuava
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % V.scalaLogging
  val logback = "ch.qos.logback" % "logback-classic" % V.logback
  val levelDb = "org.fusesource.leveldbjni" % "leveldbjni-all" % V.leveldb
  val mailin = "com.sendinblue" % "sib-api-v3-sdk" % V.mailin
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % V.akka
  val jakartaMail = "com.sun.mail" % "jakarta.mail" % V.jakartaMail
  val slick = "com.typesafe.slick" %% "slick" % V.slick
  val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % V.slick
  val postgresql = "org.postgresql" % "postgresql" % V.postgresql
  val bcrypt = "org.mindrot" % "jbcrypt" % V.bcrypt
  val flywayCore = "org.flywaydb" % "flyway-core" % V.flyway
  val mysql = "mysql" % "mysql-connector-java" % V.mysql
  val ldap = "com.unboundid" % "unboundid-ldapsdk" % V.ldap
  val monixCatnap = "io.monix" %% "monix-catnap" % V.monixCatnap
  val slf4jNop = "org.slf4j" % "slf4j-nop" % V.slf4j
  val scalameta = "org.scalameta" %% "scalameta" % V.scalameta
  val scalafmtCore = "org.scalameta" %% "scalafmt-core" % V.scalafmtCore
  val plantuml = "net.sourceforge.plantuml" % "plantuml" % V.plantuml
  val pprint = "com.lihaoyi" %% "pprint" % V.pprint
  val tapir = "com.softwaremill.sttp.tapir" %% "tapir-core" % V.tapir
  val tapirJsonCirce = "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.tapir
  val tapirCore = "com.softwaremill.sttp.tapir" %% "tapir-core" % V.tapir
  val tapirHttp4s = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % V.tapir
  val munit = "org.scalameta" %% "munit" % V.munit
  val munitScalaCheck = "org.scalameta" %% "munit-scalacheck" % V.munit
  val log4j = "org.apache.logging.log4j" % "log4j-api" % "2.14.1"
  val autowire = "com.lihaoyi" %% "autowire" % "0.3.3"
  val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.11.1"
  val metarpheusCore = "io.buildo" %% "metarpheus-core" % "1.0.0"

  val enumeroDependencies = List(
    munit,
    mockito,
  ).map(_ % Test)

  val enumeroCirceDependencies = List(
    circeCore,
  ) ++ List(
    circeParser,
    munit,
  ).map(_ % Test)

  val mailoDependencies = List(
    akkaStream,
    akkaHttp,
    akkaPersistence,
    akkaRemote,
    awscala,
    catsCore,
    alleyCatsCore,
    circeCore,
    circeGeneric,
    circeParser,
    typesafeConfig,
    akkaHttpCirce,
    scalaCacheGuava,
    scalaLogging,
    mailin,
    levelDb,
    jakartaMail,
  ) ++ List(
    munit,
    logback,
    akkaTestkit,
  ).map(_ % Test)

  val toctocCoreDependencies = List(
    bcrypt,
    catsCore,
  ) ++ List(
    munit,
    munitScalaCheck,
    scalacheck,
    slf4jNop,
    catsEffect,
  ).map(_ % Test)

  val toctocSlickPostgresDependencies = List(
    postgresql,
    slick,
    slickHikari,
    catsEffect,
    monixCatnap,
  ) ++ List(
    munit,
    slf4jNop,
  ).map(_ % Test)

  val toctocSlickMySqlDependencies = List(
    mysql,
    slick,
    slickHikari,
    catsEffect,
    monixCatnap,
  ) ++ List(
    munit,
    slf4jNop,
  ).map(_ % Test)

  val toctocLdapDependencies = List(
    ldap,
    mysql,
    slf4jNop,
    catsEffect,
  )

  val toctocCirceDependencies = List(
    circeCore,
    circeGeneric,
  ) ++ List(
    scalacheck,
    munit,
    munitScalaCheck,
  ).map(_ % Test)

  val metarpheusCoreDependencies = List(
    scalameta,
  ) ++ List(
    munit,
  ).map(_ % Test)

  val metarpheusJsFacadeDependencies = List(
    circeCore,
    circeParser,
    circeGenericExtras,
  )

  val tapiroCoreDependencies = List(
    log4j,
    scalameta,
    scalafmtCore,
    circeCore,
    pprint,
    metarpheusCore,
  ) ++ List(
    munit,
  ).map(_ % Test)

  val javaTimeCirceCodecsDependencies = List(
    circeCore,
  ) ++ List(
    munit,
  ).map(_ % Test)

  val wiroCoreDependencies = List(
    autowire,
    akkaActor,
    catsCore,
    pureConfig,
    circeCore,
    circeGeneric,
    circeParser,
  )

  val wiroHttpServerDependencies = List(
    scalaLogging,
    akkaStream,
    akkaHttp,
    akkaHttpCirce,
  ) ++ List(
    munit,
    akkaTestkit,
    akkaHttpTestKitBase,
  ).map(_ % Test)

  val wiroHttpClientDependencies = List(
    scalaLogging,
    akkaStream,
    akkaHttp,
    akkaHttpCirce,
  ) ++ List(
    munit,
    akkaTestkit,
    akkaHttpTestKitBase,
  ).map(_ % Test)

  val docsDependencies = List(
    plantuml,
    tapir,
    tapirJsonCirce,
    tapirCore,
    tapirHttp4s,
  )

}
