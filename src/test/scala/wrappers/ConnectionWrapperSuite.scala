package wrappers

import org.scalatest.{BeforeAndAfter, FunSuite}
import utils.Configuration

class ConnectionWrapperSuite extends FunSuite with BeforeAndAfter {
  private var localConnection: LocalConnection = _
  before {
    localConnection = new LocalConnection(Configuration.increaseWeightRate(), Configuration.decreaseWeightRate())
  }

  test("LocalConnection - next connection") {
    localConnection.next("search_google") match {
      case Left(message) => fail(message)
      case Right(result) => assert(result.endpointName == "google_com" || result.endpointName == "google_th")
    }
  }

  test("Local Connection - update as failed once and then increase once") {
    localConnection.next("search_bing") match {
      case Left(message) => fail(message)
      case Right(result) => localConnection.update(result.endpointName, isSuccess = false) match {
        case Left(updateMessage) => fail(updateMessage)
        case Right(updateResult) =>
          assert(updateResult.size == 80)
          localConnection.connectionWeight("search_bing") match {
            case Left(weightMessage) => fail(weightMessage)
            case Right(weightResult) =>
              assert(weightResult.totalWeight == 80)
              localConnection.update(result.endpointName, isSuccess = true)
              assert(localConnection.connectionWeight("search_bing").right.get.totalWeight == 90)
          }
      }
    }
  }
}
