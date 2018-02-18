package utils

import org.scalatest.FunSuite

class HttpCallSuite extends FunSuite {
  test("bad request - 1") {
    val call: HttpCall = new HttpCall()
    call.get(HttpRequestMetadata("", 1, 1, Map.empty)) match {
      case Right(_) => fail()
      case Left(message) => assert(message.getMessage.contains("Custom Message: Uri was not supplied, ErrorType: class"))
    }
  }

  test("bad request - 2") {
    val call: HttpCall = new HttpCall()

    call.get(HttpRequestMetadata("http://www.google.com/", -1, 1, Map.empty)) match {
      case Right(_) => fail()
      case Left(message) => assert(message.getMessage.contains("Custom Message: Connection timeout is -1, ErrorType: class"))
    }
  }

  test("bad request - 3") {
    val call: HttpCall = new HttpCall()
    call.get(HttpRequestMetadata("http://www.google.com", 1, 0, Map.empty)) match {
      case Right(_) => fail()
      case Left(message) => assert(message.getMessage.contains("Custom Message: Read timeout is 0, ErrorType: class "))
    }
  }
}
