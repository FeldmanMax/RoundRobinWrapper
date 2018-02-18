package wrappers

import com.google.inject.Inject
import com.google.inject.name.Named
import translators._
import exceptions.{GeneralException, HttpCallCustomException, WrapperException}
import models.EndpointResponse
import responses.{ConnectionAndHttpResponses, HttpWrapperResponse}
import roundrobin.models.api.EndpointWeight
import services.HttpService
import utils._

import scalaj.http.HttpResponse

class HttpWrapper @Inject() (@Named("retry_mechanism")       val retryMechanism: RetryMechanism,
                             @Named("connection_retriever")  val connectionApi: ConnectionWrapper,
                             @Named("http_service")          val httpService: HttpService
                            ) extends BaseWrapper[HttpResponse[String]] {

  def get[T](connectionName: String,
             params: Map[String, String],
             translate: (HttpResponse[String]) => Either[Exception, T]): Either[String, HttpWrapperResponse[T]] = {
    retryMechanism.execute[EndpointResponse[T]](connectionName, () => {
      getApiAction(connectionName, params)() match {
        case Left(endpointToException) => {
          val endpointName: String = endpointToException.params.getOrElse(Map.empty).getOrElse("endpoint_name", "")
          val exception: Exception = endpointToException.ex
          if(endpointName.nonEmpty) updateWeight(endpointName, exception)
          Left(exception)
        }
        case Right(connectionHttpResponse) => responseTranslationAction(connectionHttpResponse, translate)
      }
    }).right.flatMap { retryMechanism => Right(HttpWrapperResponse(retryMechanism.result, retryMechanism)) }
  }

  private def responseTranslationAction[T](connectionHttpResponse: ConnectionAndHttpResponses,
                                           translate: (HttpResponse[String]) => Either[Exception, T]): Either[Exception, EndpointResponse[T]] = {
    httpService.getHttpResponseStatus(connectionHttpResponse.http_response) match {
      case Success =>
        updateWeight(connectionHttpResponse.connection_response.endpointName, Success)
        translate(connectionHttpResponse.http_response).right.flatMap { endpointResult =>
          Right(EndpointResponse(connectionHttpResponse.connection_response, endpointResult))
        } match {
          case Left(ex) => Left(ex)
          case Right(instance) => Right(instance)
        }
      case default =>
        updateWeight(connectionHttpResponse.connection_response.endpointName, default.asInstanceOf[ResultType])
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

  private def getApiAction[T](connectionName: String, params: Map[String, String]): () => Either[WrapperException, ConnectionAndHttpResponses] = {
    () => {
      connectionApi.next(connectionName) match {
        case Left(connectionErrorMessage) => Left(
          WrapperException(NonRecoverable,
                           HttpCallCustomException(NonRecoverable, connectionErrorMessage),
                           Option(Map("connection_name" -> connectionName))))
        case Right(connection) => prepareHttpRequestMetadata(connection.value, params) match {
          case Left(prepareHttpRequestErrorMessage) => Left(
            WrapperException(NonRecoverable,
              HttpCallCustomException(NonRecoverable, prepareHttpRequestErrorMessage),
              Option(Map("endpoint_name" -> connection.endpointName))))
          case Right(httpRequestMetadata) =>
            httpService.httpCall.get(httpRequestMetadata) match {
              case Left(exception) => Left(WrapperException(NonRecoverable, exception, Option(Map("endpoint_name" -> connection.endpointName))))
              case Right(httpResponse) => Right(ConnectionAndHttpResponses(connection, httpResponse))
            }
        }
      }
    }
  }

  private def prepareHttpRequestMetadata(uri: String, params: Map[String, String]): Either[String, HttpRequestMetadata] = {
    Right(HttpRequestMetadata(uri, Configuration.httpConnectionTimeoutInMillis, Configuration.httpReadTimeoutInMillis, params))
  }
}