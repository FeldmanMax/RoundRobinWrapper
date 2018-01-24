package utils

import scala.util.matching.Regex

object Validators {
  private val urlPattern: Regex = """^(http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([\-\.]{1}[a-z0-9]+)*(\.|\/|\:)[a-z0-9]{2,5}(:[0-9]{1,5})?(\/.*)?$""".r

  def isValidUrl(uri: String): Boolean = uri match {
    case urlPattern(_*) => true
    case _ => false
  }
}