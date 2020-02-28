---
id: introduction
title: Introduction
---

Tapiro parses your Scala controllers to generate HTTP endpoints.

A Scala controller is a trait defined as follows:

```scala mdoc
import scala.annotation.StaticAnnotation

class query extends StaticAnnotation
class command extends StaticAnnotation

case class Cat(name: String)
case class Error(msg: String)

trait Cats[F[_], AuthToken] {
  @query //translate this to a GET
  def findCutestCat(): F[Either[Error, Cat]]

  @command //translate this to a POST
  def doSomethingWithTheCat(catId: Int, token: AuthToken): F[Either[Error, Unit]]
}
```

For each controller tapiro generates two files:
- `CatsEndpoints.scala` containing the HTTP api description using https://tapir-scala.readthedocs.io/
- `CatsHttp4sEndpoints.scala` or `CatsAkkaHttpEndpoints.scala` depeneding on the HTTP server the user is using.

The resulting routes can be simply used like this (http4s example):
```scala
val routes = CatsHttp4sEndpoints.routes(catsImplementation)

override def run(args: List[String]): IO[ExitCode] =
  BlazeServerBuilder[IO]
    .bindHttp(port, host)
    .withHttpApp(routes.orNotFound)
    .serve
    .compile
    .drain
    .as(ExitCode.Success)
```

The resulting server can be queried as follows:
```
/GET /Cats/findCutestCat
/POST /Cats/doSomethingWithTheCat -d '{ "catId": 1 }'
```

## Authentication

An `AuthToken` type argument is expected in each controller and is added as authorization header.

`trait Cats[F[_], AuthToken]`

The acutal implementation of the `AuthToken` is left to the user. All tapiro requires is a proper tapir `PlainCodec` such as:

```scala mdoc
import sttp.tapir._
import sttp.tapir.Codec._

case class CustomAuth(token: String)

def decodeAuth(s: String): DecodeResult[CustomAuth] = {
  val TokenPattern = "Token token=(.+)".r
  s match {
    case TokenPattern(token) => DecodeResult.Value(CustomAuth(token))
    case _                   => DecodeResult.Error(s, new Exception("token not found"))
  }
}

def encodeAuth(auth: CustomAuth): String = auth.token

implicit val authCodec: PlainCodec[CustomAuth] = Codec.stringPlainCodecUtf8
  .mapDecode(decodeAuth)(encodeAuth)
```

The user will find the decoded token as the last argument of the method in the trait.

```scala
@command //translate this to a POST
def doSomethingWithTheCat(catId: Int, token: AuthToken): F[Either[Error, Unit]]
```