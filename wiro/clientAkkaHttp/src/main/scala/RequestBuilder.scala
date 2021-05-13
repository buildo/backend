package wiro
package client.akkaHttp

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpMethods, HttpRequest, Uri }
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.Uri.{ Host, Authority, Path, Query }

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

import cats.syntax.either._

class RequestBuilder(
  config: Config,
  prefix: Option[String],
  scheme: String,
  ctx: RPCClientContext[_]
) {
  def build(path : Seq[String], args: Map[String, Json]): HttpRequest = {
    val completePath = path.mkString(".")
    //we're trying to match here the paths generated by two different macros
    //if it fails at runtime it means something is wrong in the implementation
    val methodMetaData = ctx.methodsMetaData
      .getOrElse(completePath, throw new Exception(s"Couldn't find metadata about method $completePath"))
    val operationName = methodMetaData.operationType.name
      .getOrElse(path.lastOption.getOrElse(throw new Exception("Couldn't find appropriate method path")))

    val (tokenArgs, nonTokenArgs) = splitTokenArgs(args)
    val (headersArgs, remainingArgs) = splitHeaderArgs(nonTokenArgs)
    val tokenHeader = handleAuth(tokenArgs.values.toList)
    val headers = handleHeaders(headersArgs.values.toList) ++ tokenHeader
    val uri = buildUri(operationName)
    val httpRequest = methodMetaData.operationType match {
      case _: OperationType.Command => commandHttpRequest(remainingArgs, uri)
      case _: OperationType.Query => queryHttpRequest(remainingArgs, uri)
    }

    httpRequest.withHeaders(headers)
  }

  private[this] def buildUri(operationName: String) = {
    val path = prefix match {
      case None => Path / ctx.path / operationName
      case Some(prefix) => Path / prefix /ctx.path / operationName
    }

    Uri(
      scheme = scheme,
      path = path,
      authority = Authority(host = Host(config.host), port = config.port)
    )
  }

  private[this] def splitTokenArgs(args: Map[String, Json]): (Map[String, Json], Map[String, Json]) =
    args.partition { case (_, value) => value.as[wiro.Auth].isRight }

  private[this] def splitHeaderArgs(args: Map[String, Json]): (Map[String, Json], Map[String, Json]) =
    args.partition { case (_, value) => value.as[wiro.OperationParameters].isRight }

  private[this] def handleAuth(tokenCandidates: List[Json]): List[RawHeader] =
    if (tokenCandidates.length > 1)
      throw new Exception("Only one parameter of wiro.Auth type should be provided")
    else tokenCandidates.map(_.as[wiro.Auth]).collect {
      case Right(wiro.Auth(token)) => RawHeader("Authorization", s"Token token=$token")
    }

  private[this] val stringPairToHeader: PartialFunction[(String, String), RawHeader] = {
    case (headerName: String, headerValue: String) => RawHeader(headerName, headerValue)
  }

  private[this] def handleHeaders(headersCandidates: List[Json]): List[RawHeader] = {
    val headers: Option[List[RawHeader]] = for {
      parameters: Decoder.Result[OperationParameters] <- headersCandidates.headOption.map(_.as[wiro.OperationParameters])
      headers: List[RawHeader] <- parameters.toOption.map(_.parameters.map(stringPairToHeader).toList)
    } yield headers

    headers.getOrElse(Nil)
  }

  private[this] def commandHttpRequest(nonTokenArgs: Map[String, Json], uri: Uri) = HttpRequest(
    method = HttpMethods.POST, uri = uri, entity = HttpEntity(
      contentType = ContentTypes.`application/json`,
      string = nonTokenArgs.asJson.noSpaces
    )
  )

  private[this] def queryHttpRequest(nonTokenArgs: Map[String, Json], uri: Uri) = HttpRequest(
    method = HttpMethods.GET, uri = uri.withQuery(Query(nonTokenArgs.mapValues(_.noSpaces).toMap))
  )
}