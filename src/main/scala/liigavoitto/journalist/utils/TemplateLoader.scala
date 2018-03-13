package liigavoitto.journalist.utils

import liigavoitto.util.Logging
import scaledn.parser.parseEDN
import scaledn.{EDN, EDNKeyword, EDNSymbol}

import scala.io.Source
import scala.reflect.ClassTag
import scala.util.{Failure, Try}

trait TemplateLoader extends Logging {

  type FileContent = Map[EDNKeyword, Map[EDNKeyword, List[TemplateVector]]]
  type TemplateVector = Vector[Any]
  type TemplateSettings = Map[EDNKeyword, Any]
  val WeightKey = EDNKeyword(EDNSymbol("weight"))

  def load(filePath: String, templatesName: String, language: String) = {
    val content = loadResource(filePath)
    val parsed = parseEDN(content)
    logErrors(parsed, filePath)
    val mapped = parsed.get.asInstanceOf[FileContent]
    getTemplates(mapped, templatesName, language)
  }

  private def getTemplates(parsed: FileContent, name: String, language: String) = {
    val templatesName = EDNKeyword(EDNSymbol(name))
    val languageKey = EDNKeyword(EDNSymbol(language))
    parsed(templatesName)(languageKey).map(parseTemplate)
  }

  private def parseTemplate(vector: TemplateVector) = {
    val tmpl = vector(0).asInstanceOf[String]
    val weight = getWeight(vector)
    if (weight.isDefined)
      Template(tmpl, weight.get)
    else
      Template(tmpl)
  }

  private def asInstanceOfOption[T: ClassTag](o: Any): Option[T] =
    Some(o) collect { case m: T => m }

  private def getWeight(vector: Vector[Any]) = for {
      opts <- vector.lift(1)
      settings <- asInstanceOfOption[TemplateSettings](opts)
      value <- settings.get(WeightKey)
      asDouble <- asInstanceOfOption[Double](value)
    } yield asDouble

  private def loadResource(path: String) = {
    val resourcePath =  path
    val res = getClass.getClassLoader.getResource(resourcePath)
    val source = Source.fromURL(res)
    source.mkString
  }

  private def logErrors(parsed: Try[EDN], filePath: String) = parsed match {
      case Failure(f : org.parboiled2.ParseError) => {
        log.error(s"$filePath ParseError at line " + f.position.line + " col " + f.position.column)
      }
      case _ =>
    }
}
