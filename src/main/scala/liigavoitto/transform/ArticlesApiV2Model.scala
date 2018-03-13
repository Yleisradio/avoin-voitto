package liigavoitto.transform

import org.joda.time.DateTime

case class Article(
  id: String,
  title: String,
  lead: String,
  body: List[String],
  datePublished: DateTime,
  escenicSections: List[EscenicSection],
  externalContent: List[ExternalContent],
  gameEvents: List[TextBlock],
  gameStats: List[ContentBlock],
  conceptIds: List[String] = List(),
  author: Option[liigavoitto.journalist.author.Author] = None,
  surveyLink: Option[String] = None,
  footNote: Option[String] = None,
  language: String = "fi",
  publisher: String = "Yle Urheilu",
  coverage: String = "national",
  dateModified: Option[DateTime] = None,
  properties: List[String] = List(),
  shortSummary: String)

case class ExternalContent(html: String, css: List[String], scripts: List[String], embedLocation: Option[String] = None)

case class ArticleV2(
  id: String,
  language: String,
  url: Url,
  headline: Headline,
  lead: String,
  publisher: Publisher,
  datePublished: String,
  dateContentModified: String,
  dateJsonModified: String,
  coverage: String,
  content: List[ContentBlock],
  subjects: List[Subject],
  mainMedia: List[ImageBlock],
  authors: Option[List[Author]],
  properties: List[String] = List(),
  shortSummary: String
)
case class Url(full: String, short: Option[String] = None)
case class Headline(full: String, image: Option[ImageBlock] = None)
case class Publisher(name: String)
case class Subject(id: String)
case class Author(`type`: String, id: String, name: String, organization: String, image: Option[ImageBlock])
case class EscenicSection(uniqueName: String, homeSection: Boolean, publication: String)

trait ContentBlock {
  val `type`: String
}
case class HeadingBlock(level: Int, text: String, `type`: String) extends ContentBlock
case class ImageBlock(id: String, aspect: Double, `type`: String, alt: Option[String] = None, url: Option[String] = None) extends ContentBlock
case class TextBlock(text: String, `type`: String) extends ContentBlock
case class ExternalContentV2(`type`: String, html: String, css: List[String] = List(), scripts: List[String] = List()) extends ContentBlock
