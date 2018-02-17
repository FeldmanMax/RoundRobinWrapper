package dockers

import dwrapper.container.api.DockerContainer
import dwrapper.image.traits.DockerLoader
import dwrapper.models.api.Image
import org.scalatest.FunSuite

class DockersSuite extends FunSuite with DockerLoader {
  test("load api response docker") {
    val myImage: Image = Image("", "image/web-api", "2.0.0", "", "")
    val container_name: String = "web-api-2.0.0"
    val dockerFilePath: String = "/Users/maksik1/IdeaProjects/ApiResponse"

    loadContainer(container_name, "image/web-api", "2.0.0", dockerFilePath, Some(Map("80" -> "9000"))) match {
      case Left(left) => fail(left)
      case Right(container) => assert(container.image.nameWithTag == myImage.nameWithTag)
        DockerContainer.stop(container.id) match { //  Stop of the Container
          case Left(left) => fail(left)
          case Right(stopped_container) => assert(container.id == stopped_container.id)
            DockerContainer.remove(stopped_container.id) match {
              case Left(left) => fail(left)
              case Right(removed_container) => assert(stopped_container.id == removed_container.id)
                DockerContainer.container(removed_container.id) match {
                  case Left(left) => assert(left == s"Container ${removed_container.id} does not exist")
                  case Right(_) => fail(s"Container ${removed_container.id} was not deleted")
                }
            }
        }
    }
  }
}
