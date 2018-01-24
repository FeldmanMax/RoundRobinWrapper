package utils

import translators.{CustomErrorType, NonRecoverable}
import exceptions.HttpCallCustomException
import logging.ApplicationLogger

class RetryMechanism() {

  def execute[T <: AnyRef](connectionName: String,
                           action: () => Either[Exception, T]): Either[String, T] = {
    try {
      executeImpl(connectionName, Configuration.maxRetries, action)
    }
    catch {
      case ex: Exception => Left(ex.toString)
    }
  }


  private def executeImpl[T <: AnyRef](connectionName: String,
                                       retry: Int,
                                       action: () => Either[Exception, T]): Either[String, T] = {
    if(retry == 0)  Left(s"Could not bring result for $connectionName connection")
    else {
      action() match {
        case Right(result) => Right(result)
        case Left(exception) =>
          exception match {
            case custom: HttpCallCustomException => custom.errorType match {
              case NonRecoverable =>
                ApplicationLogger.error(custom.getMessage())
                Left(s"Had an unrecoverable problem ${custom.getMessage()}")
            }
            case _: Exception =>
              ApplicationLogger.error(exception)
              executeImpl(connectionName, retry - 1, action)
          }
      }
    }
  }
}