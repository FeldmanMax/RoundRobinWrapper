package utils

import translators._

import scalaj.http.HttpResponse

object HttpUtils {

  def getHttpResponseStatus(response: HttpResponse[String]): ResultType = {
    def isInRange(code: Int, fromInclusive: Int, to: Int): Boolean = code >= fromInclusive && code < to
    if(isInRange(response.code, 200, 300))  Success
    else if(isInRange(response.code, 300, 400)) Redirection
    else if(isInRange(response.code, 400, 500)) ClientErrors
    else if(isInRange(response.code, 500, 600)) ServerErrors
    else Success
  }
}
