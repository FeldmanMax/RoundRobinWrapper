package wrappers

import org.scalatest.FunSuite
import utils.{HttpCallMock, RetryMechanism}

import scalaj.http.HttpResponse

class HttpWrapperSuite extends FunSuite {
//  test("http request for search engines - google") {
//    val httpWrapper: HttpWrapper = new HttpWrapper(new HttpCallMock, new RetryMechanism())
//    httpWrapper.get[String]("search_google", Map("q" -> "dream theater"), parseMockResponse) match {
//      case Left(message) => fail(message)
//      case Right(data) => assert(data.response == "All Good")
//    }
//  }

  private def parseMockResponse(response: HttpResponse[String]): Either[String, String] = Right(response.body)
}
