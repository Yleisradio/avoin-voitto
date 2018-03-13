package liigavoitto.transform

import liigavoitto.journalist.author.AuthorImage
import org.joda.time.format.ISODateTimeFormat

import scala.util.Properties

trait ArticlesV2Transformer {

  val dateFormat = ISODateTimeFormat.dateTimeNoMillis()
  val urlBase = Properties.envOrElse("ARTICLE_BASE_URL", "http://urheilu.test.c1t.yle.fi/urheilu/")

  def getV2Article(createArticle: Article): ArticleV2 = {
    val published = dateFormat.print(createArticle.datePublished)
    val url = urlBase + createArticle.id
    val externalContent = createArticle.externalContent
    val content = getContent(createArticle, externalContent, createArticle.surveyLink, createArticle.footNote)
    val subjects = createArticle.conceptIds.map(c => Subject(c))

    ArticleV2(
      createArticle.id,
      createArticle.language,
      Url(url),
      Headline(createArticle.title, None),
      createArticle.lead,
      Publisher("Yle Urheilu"),
      published,
      published,
      published,
      createArticle.coverage,
      content,
      subjects,
      List(),
      Some(List(author(createArticle.author)).flatten),
      createArticle.properties,
      createArticle.shortSummary
    )
  }

  private def getContent(createArticle: Article, externalContent: List[ExternalContent], surveyLink: Option[String], footNote: Option[String]) = {
    List(
      HeadingBlock(level = 1, text = createArticle.title, "heading"),
      textBlock(createArticle.lead)
    ) ++
      mainExternalContent(externalContent) ++
      (bodyTextBlocks(createArticle.body) ++ createArticle.gameEvents) ++
      createArticle.gameStats ++
      surveyLinkBlock(surveyLink) ++ footNoteBlock(footNote) ++
      bottomExternalContent(externalContent)
  }

  private def withFootNote(content: List[ContentBlock], footNote: Option[String]) = footNote match {
    case Some(t) => content :+ textBlock(t)
    case None => content
  }


  private def mainExternalContent(ec: List[ExternalContent]) = ec.filter(_.embedLocation.isEmpty).map(externalContentBlock)
  private def bottomExternalContent(ec: List[ExternalContent]) = ec.filter(_.embedLocation.contains("bottom")).map(externalContentBlock)
  private def externalContentBlock(ec: ExternalContent) = ExternalContentV2("external-content", ec.html, ec.css, ec.scripts)
  private def surveyLinkBlock(sl: Option[String]) = sl.map(f => List(textBlock(f))).getOrElse(List())
  private def footNoteBlock(footNote: Option[String]) = footNote.map(f => List(textBlock(f))).getOrElse(List())

  private def textBlock(text: String) = TextBlock(text, "text")
  private def imageBlock(a: AuthorImage) = Some(ImageBlock(a.id, a.aspect, "image", a.alt))
  private def bodyTextBlocks(paragraphs: List[String]) = paragraphs.map(textBlock)

  private def author(a: Option[liigavoitto.journalist.author.Author]) = a match {
    case Some(s) => Some(Author("Person", s.id, s.name, s.organization, imageBlock(s.image)))
    case None => None
  }
}
