package utils

import translators.{CustomErrorType, NonRecoverable}
import exceptions.HttpCallCustomException
import logging.ApplicationLogger

case class RetryMechanismResult[T](result: T, retries: Int)

class RetryMechanism() {

  def execute[T <: AnyRef](connectionName: String,
                           action: () => Either[Exception, T]): Either[String, RetryMechanismResult[T]] = {
    try {
      executeImpl(connectionName, Configuration.maxRetries, List.empty, action)
    }
    catch {
      case ex: Exception => Left(ex.toString)
    }
  }


  private def executeImpl[T <: AnyRef](connectionName: String,
                                       retry: Int,
                                       listOfErrors: List[Exception],
                                       action: () => Either[Exception, T]): Either[String, RetryMechanismResult[T]] = {
    if(retry == 0)  Left(s"Could not bring result for $connectionName connection\nReasons\n" + (listOfErrors.map(_.getMessage) mkString "\n"))
    else {
      action() match {
        case Right(result) => Right(RetryMechanismResult(result, retry))
        case Left(exception) =>
          exception match {
            case custom: HttpCallCustomException => custom.errorType match {
              case NonRecoverable =>
                ApplicationLogger.error(custom.getMessage())
                Left(s"Had an unrecoverable problem ${custom.getMessage()}")
              case _ => executeImpl(connectionName, retry - 1, listOfErrors ::: List(custom), action)
            }
            case ex: Exception =>
              ApplicationLogger.error(ex)
              executeImpl(connectionName, retry - 1, listOfErrors ::: List(ex), action)
          }
      }
    }
  }
}