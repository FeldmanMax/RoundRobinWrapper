package utils

import com.google.inject.Inject
import translators.{NoReduction, NormalReduction}
import exceptions.{HttpCallCustomException, HttpCallGeneralException}
import logging.ApplicationLogger

import scalaj.http.{Http, HttpOptions, HttpResponse}

case class HttpRequestMetadata(uri: String, connTimeoutInMillis: Int, readTimeoutInMillis: Int, params: Map[String, String])

trait HttpCallApi {
  def get(request: HttpRequestMetadata): Either[Exception, HttpResponse[String]]
}

class HttpCall @Inject()() extends HttpCallApi {
  def get(request: HttpRequestMetadata): Either[Exception, HttpResponse[String]] = {
    try {
      isValidRequest(request) match {
        case Left(message) => Left(HttpCallCustomException(NoReduction, message))
        case Right(_) => {
          val asString: HttpResponse[String] = getImpl(request)
          Right(asString)
        }
      }
    }
    catch {
      case ex: Exception =>
        ApplicationLogger.error(ex)
        Left(HttpCallGeneralException(NormalReduction, ex))
    }
  }

  private def getImpl(request: HttpRequestMetadata) = {
    val asString: HttpResponse[String] = Http(request.uri).params(request.params).
      options(List(HttpOptions.connTimeout(request.connTimeoutInMillis), HttpOptions.readTimeout(request.readTimeoutInMillis))).asString
    asString
  }

  private def isValidRequest(requestMetadata: HttpRequestMetadata): Either[String, Unit] = {
    if(!Validators.isValidUrl(uri = requestMetadata.uri))     Left("Uri was not supplied")
    else  if(requestMetadata.connTimeoutInMillis <= 0)        Left(s"Connection timeout is ${requestMetadata.connTimeoutInMillis}")
    else  if(requestMetadata.readTimeoutInMillis <= 0)        Left(s"Read timeout is ${requestMetadata.readTimeoutInMillis}")
    else                                                      Right()
  }
}

class HttpCallMock extends HttpCallApi {
  def get(request: HttpRequestMetadata): Either[Exception, HttpResponse[String]] = {
    Right(HttpResponse[String]("All Good", 200, Map.empty))
  }
}
