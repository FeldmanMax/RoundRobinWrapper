package wrappers

import translators.HttpErrorTranslator
import models.WeightRate
import org.scalatest.FunSuite
import services.HttpService
import utils.{HttpCall, HttpRequestMetadata, RetryMechanism}

import scalaj.http.HttpResponse

class HttpWrapperIntegrationSuite extends FunSuite {
  test("google search - bitcoin info") {
    val searchKeyWord: String = "bitcoin"
    val connectionRetriever: ConnectionWrapper = new LocalConnection(WeightRate(isSuccess = true, isPercent = false, 10), WeightRate(isSuccess = false, isPercent = false, 20))
    val httpService: HttpService = new HttpService(new HttpCall, new HttpErrorTranslator)
    val httpWrapper: HttpWrapper = new HttpWrapper(new RetryMechanism(), connectionRetriever, httpService)
    val result = httpWrapper.get[String]("search_google", Map("q" -> searchKeyWord), getHttpResponseBody)
    result match {
      case Left(message) => fail(message)
      case Right(data) =>
        val pattern = searchKeyWord.r
        val amountOfOccurences = (pattern findAllIn data.response.toLowerCase).toList.size
        assert(amountOfOccurences > 1, data.response)
    }
  }

  test("HTTP 500") {
    val httpCall: HttpCall = new HttpCall
    val guid: String = s"http://localhost:9000/"
    val response = httpCall.get(HttpRequestMetadata(guid, 10000, 1000, Map.empty))
    val s = 10
  }

  private def getHttpResponseBody(response: HttpResponse[String]): Either[Exception, String] = {
    try {
      Right(response.body)
    }
    catch {
      case ex: Exception =>
        val retMessage: String = ex.getMessage
        Left(ex)
    }
  }
}
