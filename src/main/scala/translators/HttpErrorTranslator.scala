package translators

import java.net.{SocketTimeoutException, UnknownHostException}

import com.google.inject.Inject
import exceptions.HttpCallCustomException

class HttpErrorTranslator @Inject()() {

  def getErrorType(exception: Exception): ResultType = {
    exception match {
          case _: SocketTimeoutException =>       NormalReduction
          case _: UnknownHostException =>         NonRecoverable
          case custom: HttpCallCustomException => custom.errorType
          case _: Exception =>                    Critical
    }
  }


}

trait ResultType

abstract class CustomErrorType extends ResultType
object NonUpdatable     extends CustomErrorType
object NonRecoverable   extends CustomErrorType
object Critical         extends CustomErrorType
object NormalReduction  extends CustomErrorType
object NoReduction      extends CustomErrorType

abstract class HttpErrorType extends ResultType
object Success              extends HttpErrorType // 2xx
object Redirection          extends HttpErrorType // 3xx
object ClientErrors         extends HttpErrorType // 4xx
object ServerErrors         extends HttpErrorType // 5xx