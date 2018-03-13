package liigavoitto.util

import org.joda.time.{ DateTime, DateTimeZone }
import org.joda.time.format.ISODateTimeFormat
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{ JNull, JString }

case object DateTimeNoMillisSerializer extends CustomSerializer[DateTime](format => (
  {
    case JString(s) => ISODateTimeFormat.dateTimeNoMillis().withZone(Time.zone).parseDateTime(s)
    case JNull => null
  },
  {
    case d: DateTime => JString(ISODateTimeFormat.dateTimeNoMillis().withZone(Time.zone).print(d))
  }
))

object Time {
  val zone = DateTimeZone.forID("Europe/Helsinki")
}