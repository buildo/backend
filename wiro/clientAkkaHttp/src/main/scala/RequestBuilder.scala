package wiro
package client.akkaHttp

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.Uri.{ Host, Authority, Path, Query }
import akka.http.scaladsl.model.Uri

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

class RequestBuilder(
  config: Config,
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
    val uri = buildUri(operationName)

    val httpRequest = methodMetaData.operationType match {
      case _: OperationType.Command => commandHttpRequest(nonTokenArgs, uri)
      case _: OperationType.Query => queryHttpRequest(nonTokenArgs, uri)
    }
    handlingToken(tokenArgs, httpRequest)
  }

  private[this] def buildUri(operationName: String) = Uri(
    scheme = "http", path = Path / ctx.path / operationName,
    authority = Authority(host = Host(config.host), port = config.port)
  )

  private[this] def splitTokenArgs(args: Map[String, Json]): (List[String], Map[String, Json]) = {
    val tokenCandidates = args.map { case (_, v) => v.as[wiro.Auth] }.collect { case Right(result) => result.token }.toList
    val nonTokenArgs = args.filter { case (_, v) => v.as[wiro.Auth].isLeft }
    (tokenCandidates, nonTokenArgs)
  }

  private[this] def handlingToken(
    tokenCandidates: List[String],
    httpRequest: HttpRequest
  ): HttpRequest = {
    val maybeToken =
      if (tokenCandidates.length > 1) throw new Exception("Only one parameter of wiro.Auth type should be provided")
      else tokenCandidates.headOption

    maybeToken match {
      case Some(token) => httpRequest.addHeader(RawHeader("Authorization", s"Token token=$token"))
      case None => httpRequest
    }
  }

  private[this] def commandHttpRequest(nonTokenArgs: Map[String, Json], uri: Uri) = HttpRequest(
    uri = uri,
    method = HttpMethods.POST,
    entity = HttpEntity(
      contentType = ContentTypes.`application/json`,
      string = nonTokenArgs.asJson.noSpaces
    )
  )

  private[this] def queryHttpRequest(nonTokenArgs: Map[String, Json], uri: Uri): HttpRequest = {
    val args = nonTokenArgs.mapValues(_.noSpaces)
    val completeUri = uri.withQuery(Query(args))
    val method = HttpMethods.GET

    HttpRequest(
      uri = completeUri,
      method = method
    )
  }
}
