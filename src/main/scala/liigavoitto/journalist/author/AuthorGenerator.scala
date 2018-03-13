package liigavoitto.journalist.author

import scala.util.Properties

case class Author(id: String, name: String, organization: String, image: AuthorImage)
case class AuthorImage(id: String, aspect: Double, alt: Option[String])

object AuthorGenerator {

  def getAuthor = Author(authorId, name, organization, image)

  def authorId = Properties.envOrElse("ESCENIC_AUTHOR_ID", "")
  def imageId = ""
  def name = "Voitto-robotti"
  def organization = "Yle"

  def image = AuthorImage(
    imageId,
    500.toDouble / 500.toDouble,
    Some(name)
  )
}
