import org.scalatest.{BeforeAndAfter, FunSuite}
import utils.{Configuration, RetryMechanism}

class RetryMechanismSuite extends FunSuite with BeforeAndAfter {
  private var retryMechanism: RetryMechanism = _
  before {
    retryMechanism = new RetryMechanism()
  }

  test("All failures should get an error") {
    retryMechanism.execute("connection", () => Left(new Exception("failed"))) match {
      case Right(_) => fail("Should have failed")
      case Left(message) => assert("Could not bring result for connection connection" == message)
    }
  }

  test("Success on the first iteration") {
    var currentRun: Int = 0
    val func: () => Either[Exception, String] = () => {
      if(currentRun != 0) Left(new Exception("Not on the First Run"))
      else                {
        currentRun += 1
        Right("All Good")
      }
    }

    retryMechanism.execute("connection", func) match {
      case Left(_) => fail("Could not get the result on the first run")
      case Right(_) => assert(1 == 1)
    }
  }

  test("Success on the last iteration") {
    var currentRun: Int = 0
    val func: () => Either[Exception, String] = () => {
      if(currentRun != Configuration.maxRetries - 1) {
        currentRun += 1
        Left(new Exception("Not on the Last Run"))
      }
      else Right("All Good")
    }

    retryMechanism.execute("connection", func) match {
      case Left(_) => fail("Could not get the result on the last run")
      case Right(_) => assert(1 == 1)
    }
  }

  test("execution throws an exception") {
    retryMechanism.execute("connection", () => Left(new Exception("some exception"))) match {
      case Right(_) => fail("Exception was not thrown")
      case Left(message) => assert("Could not bring result for connection connection" == message)
    }
  }
}
