package liigavoitto.journalist.values

import java.util.Locale

import liigavoitto.scores.Match
import org.joda.time.format.DateTimeFormat

trait DateValues {
  implicit val mtch: Match
  implicit val lang: String
  private lazy val locale = lang match {
    case "fi" => new Locale("fi", "FI")
    case "sv" => new Locale("sv", "SE")
  }
  
  private lazy val formatter = DateTimeFormat.forPattern("EEEE").withLocale(locale)
  lazy val day = mtch.date.toString(formatter)

}
