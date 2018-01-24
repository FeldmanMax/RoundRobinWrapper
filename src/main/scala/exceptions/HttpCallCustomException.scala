package exceptions

import translators.CustomErrorType

case class HttpCallCustomException(errorType: CustomErrorType, message: String) extends Exception {
  override def getMessage(): String = s"Custom Message: $message, ErrorType: ${errorType.getClass}"
}