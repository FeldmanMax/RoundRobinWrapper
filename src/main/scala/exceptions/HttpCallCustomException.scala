package exceptions

import translators.CustomErrorType

case class HttpCallCustomException(errorType: CustomErrorType, message: String) extends Exception {
  override def getMessage(): String = s"Custom Message: $message, ErrorType: ${errorType.getClass}"
}

case class HttpCallGeneralException(errorType: CustomErrorType, ex: Exception) extends Exception {
  override def getMessage(): String = s"Error Type: ${errorType.getClass}, Exception: ${ex.getMessage}"
}