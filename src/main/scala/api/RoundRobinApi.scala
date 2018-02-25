package roundrobinwrapper.api

import com.google.inject.{Guice, Injector}
import dependencyInjection.RoundRobinWrapperInjection
import logger.ApplicationLogger
import roundrobinwrapper.responses.HttpWrapperResponse
import wrappers.HttpWrapper

import scalaj.http.HttpResponse

object RoundRobinApi {
  private lazy val injector: Injector = Guice.createInjector(new RoundRobinWrapperInjection)

  def get[T](connectionName: String,
             params: Map[String, String],
             translate: (HttpResponse[String]) => Either[Exception, T]): Either[String, HttpWrapperResponse[T]] = {
    ApplicationLogger.info(s"get $connectionName")
    injector.getInstance(classOf[HttpWrapper]).get[T](connectionName, params, translate) match {
      case Left(error) => ApplicationLogger.errorLeft(error)
      case Right(response) => Right(response)
    }
  }
}
