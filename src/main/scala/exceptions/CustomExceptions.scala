package exceptions

import translators.CustomErrorType

abstract class CustomException(errorType: CustomErrorType, message: String) extends Exception {
  override def getMessage(): String = s"Custom Message: $message, ErrorType: ${errorType.getClass}"
}

abstract class GeneralException (errorType: CustomErrorType, ex: Exception, params: Option[Map[String, String]]) extends Exception {
  override def getMessage(): String = s"Error Type: ${errorType.getClass}, Exception: ${ex.getMessage}"
}

case class WrapperException(errorType: CustomErrorType, ex: Exception, params: Option[Map[String, String]]) extends GeneralException(errorType, ex, params)