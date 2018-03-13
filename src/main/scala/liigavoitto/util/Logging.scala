package liigavoitto.util

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

trait Logging {
  def log = Logger(LoggerFactory.getLogger(getClass))
}
