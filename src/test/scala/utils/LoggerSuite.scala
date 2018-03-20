package utils


import logger.ApplicationLogger
import org.scalatest.FunSuite
import services.FileSystemService

class LoggerSuite extends FunSuite {

  test("write application info") {
    val infoFile: String = "/tmp/log_info.log"
    ApplicationLogger.info("my data")
    val fileSystemService: FileSystemService = new FileSystemService
    fileSystemService.loadFile(infoFile) match {
      case Left(left) =>
        fileSystemService.deleteFile(infoFile)
        fail(left)
      case Right(result) =>
        assert(result.contains("my data"))
        fileSystemService.deleteFile(infoFile)
    }
  }
}
