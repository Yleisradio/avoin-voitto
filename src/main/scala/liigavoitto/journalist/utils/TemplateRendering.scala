package liigavoitto.journalist.utils

import liigavoitto.util.Logging

import scala.util.{Failure, Success, Try}

case class Template(template: String, weight: Double = 1.0) {
  require(weight > 0.0)
}

case class RenderedTemplate(text: String, weight: Double) {
  require(weight > 0.0)
}

object TemplateRendering extends Logging{
  def render(template: Template,
             attributes: Map[String, Any]): Option[RenderedTemplate] = {
    Try {
      RenderedTemplate(Mustache(template.template).apply(attributes), template.weight)
    } match {
      case Success(rendered) => Some(rendered)
      case Failure(e) =>
        log.warn(s"Could not render '$template': " + e.getMessage)
        None
    }
  }
}


