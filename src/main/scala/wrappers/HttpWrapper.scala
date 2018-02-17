package wrappers

import com.google.inject.Inject
import com.google.inject.name.Named
import translators._
import exceptions.HttpCallCustomException
import models.EndpointResponse
import roundrobin.models.api.{ConnectionResponse, EndpointWeight}
import services.HttpService
import utils._

import scalaj.http.HttpResponse

class HttpWrapper @Inject() (@Named("retry_mechanism")       val retryMechanism: RetryMechanism,
                             @Named("connection_retriever")  val connectionApi: ConnectionWrapper,
                             @Named("http_service")          val httpService: HttpService
                            ) extends BaseWrapper[HttpResponse[String]] {

  private type ConnectionAndHttpResponses = (ConnectionResponse, HttpResponse[String])

  def get[T](connectionName: String,
             params: Map[String, String],
             translate: (HttpResponse[String]) => Either[Exception, T]): Either[String, EndpointResponse[T]] = {
    retryMechanism.execute[EndpointResponse[T]](connectionName, () => {
      getApiAction(connectionName, params)() match {
        case Left(endpointToException) => {
          val endpointName: String = endpointToException._1
          val exception: Exception = endpointToException._2
          updateWeight(endpointName, exception)
          Left(exception)
        }
        case Right(connectionHttpResponse) => responseTranslationAction(connectionHttpResponse, translate)
      }
    })
  }

  private def responseTranslationAction[T](connectionHttpResponse: ConnectionAndHttpResponses,
                                           translate: (HttpResponse[String]) => Either[Exception, T]): Either[Exception, EndpointResponse[T]] = {
    val httpResponse: HttpResponse[String] = connectionHttpResponse._2
    httpService.getHttpResponseStatus(httpResponse) match {
      case Success =>
        val connectionResponse: ConnectionResponse = connectionHttpResponse._1
        updateWeight(connectionResponse.endpointName, Success)
        translate(httpResponse).right.flatMap { endpointResult =>
          Right(EndpointResponse(connectionResponse, endpointResult))
        } match {
          case Left(ex) => Left(ex)
          case Right(instance) => Right(instance)
        }
      case default =>
        updateWeight(connectionHttpResponse._1.endpointName, default.asInstanceOf[ResultType])
        Left(HttpCallCustomException(NormalReduction, default.toString))
    }
  }

  private def updateWeight[T](endpointName: String, exception: Exception): Either[String, EndpointWeight] = {
    updateWeight(endpointName, httpService.errorTranslator.getErrorType(exception))
  }

  // IF YOU WANT TO ADD MORE RESULTTYPES TO UPDATE THE CACHE THEN DO IT HERE!
  private def updateWeight[T](endpoint_name: String, errorType: ResultType): Either[String, EndpointWeight] = {
    errorType match {
      case NormalReduction => connectionApi.update(endpoint_name, isSuccess = false)
      case NonRecoverable => connectionApi.updateToMin(endpoint_name)
      case Critical => connectionApi.update(endpoint_name, isSuccess = false)
      case NoReduction => connectionApi.update(endpoint_name, isSuccess = true)
      case ServerErrors => connectionApi.update(endpoint_name, isSuccess = false)
      case Success => connectionApi.update(endpoint_name, isSuccess = true)
    }
  }

  private def getApiAction[T](connectionName: String, params: Map[String, String]): () => Either[(String, Exception), ConnectionAndHttpResponses] = {
    () => {
      connectionApi.next(connectionName) match {
        case Left(connectionErrorMessage) => Left((connectionName, HttpCallCustomException(NonRecoverable, connectionErrorMessage)))
        case Right(connection) => prepareHttpRequestMetadata(connection.value, params) match {
          case Left(prepareHttpRequestErrorMessage) => Left((connection.endpointName, HttpCallCustomException(NonRecoverable, prepareHttpRequestErrorMessage)))
          case Right(httpRequestMetadata) =>
            httpService.httpCall.get(httpRequestMetadata) match {
              case Left(exception) =>     Left((connection.endpointName, exception))
              case Right(httpResponse) => Right((connection, httpResponse))
            }
        }
      }
    }
  }

  private def prepareHttpRequestMetadata(uri: String, params: Map[String, String]): Either[String, HttpRequestMetadata] = {
    Right(HttpRequestMetadata(uri, Configuration.httpConnectionTimeoutInMillis, Configuration.httpReadTimeoutInMillis, params))
  }
}