package services

import com.google.inject.Inject
import com.google.inject.name.Named
import translators._
import http.HttpCallApi

import scalaj.http.HttpResponse

class HttpService @Inject()(@Named("http_call_api")           val httpCall: HttpCallApi,
                            @Named("http_error_translator")   val errorTranslator: HttpErrorTranslator) {

  def getHttpResponseStatus(response: HttpResponse[String]): ResultType = {
    def isInRange(code: Int, fromInclusive: Int, to: Int): Boolean = code >= fromInclusive && code < to
    if(isInRange(response.code, 200, 300))  Success
    else if(isInRange(response.code, 300, 400)) Redirection
    else if(isInRange(response.code, 400, 500)) ClientErrors
    else if(isInRange(response.code, 500, 600)) ServerErrors
    else Success
  }
}