package services

import com.google.inject.Inject
import com.google.inject.name.Named
import translators._
import utils.HttpCallApi

import scalaj.http.HttpResponse

class HttpService @Inject()(@Named("http_executor")         val httpCall: HttpCallApi,
                            @Named("error_handling")        val errorTranslator: HttpErrorTranslator) {

  def getHttpResponseStatus(response: HttpResponse[String]): ResultType = {
    def isInRange(code: Int, fromInclusive: Int, to: Int): Boolean = code >= fromInclusive && code < to
    if(isInRange(response.code, 200, 300))  Success
    else if(isInRange(response.code, 300, 400)) Redirection
    else if(isInRange(response.code, 400, 500)) ClientErrors
    else if(isInRange(response.code, 500, 600)) ServerErrors
    else Success
  }
}