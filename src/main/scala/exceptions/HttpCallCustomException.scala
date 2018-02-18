package exceptions

import translators.CustomErrorType

case class HttpCallCustomException(errorType: CustomErrorType, message: String) extends CustomException(errorType, message)
case class HttpCallGeneralException(errorType: CustomErrorType, ex: Exception) extends GeneralException(errorType, ex, None)