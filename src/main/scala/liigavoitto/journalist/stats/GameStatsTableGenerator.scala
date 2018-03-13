package liigavoitto.journalist.stats

import liigavoitto.journalist.MatchData
import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.values.MatchDataValues
import liigavoitto.transform._

import scala.collection.mutable.ListBuffer

case class TableBlock(rows: List[TableRow], `type`: String) extends ContentBlock

case class TableRow(cells: List[TableCell], `type`: String = "table-row")

case class TableCell(text: String, `type`: String = "text")

class GameStatsTableGenerator(matchData: MatchData, language: String) extends CommonImplicits {

  val lang = language
  val values = MatchDataValues(matchData, lang)

  private val filePath = "template/stats/stats.edn"

  private def tmpl = getTemplateFn(filePath)

  def getTable: List[ContentBlock] =
    List(heading, table, attendance)

  val homeGroupedPenalties = createPenaltiesText(values.homePenaltyMinutesGrouped)
  val awayGroupedPenalties = createPenaltiesText(values.awayPenaltyMinutesGrouped)

  private def createPenaltiesText(minutes: Map[String, Int]): String = {
    if (!minutes.isEmpty) minutes.map(m => s"${m._2} x ${m._1} min + ").mkString.dropRight(3) + " = "
    else ""
  }

  def heading = HeadingBlock(3, tmpl("heading").head.template, "heading")

  def attendance = TextBlock(attendanceText, "text")

  def table =
    TableBlock(
      List(titleRow) ++
      List(shotsRowOption).flatten ++
      List(penaltyMinutesRow, savesRow) ++
      List(faceoffsRowOption).flatten
    , "table")

  def titleRow = TableRow(List(
    TableCell("**" + values.home.name + "**"),
    TableCell(""),
    TableCell("**" + values.away.name + "**")
  ))

  def shotsRow = TableRow(List(
    TableCell(values.homeShotsOnGoal.toString),
    TableCell("**" + tmpl("shots").head.template + "**"),
    TableCell(values.awayShotsOnGoal.toString)
  ))

  def shotsRowOption: Option[TableRow] = values.homeShotsOnGoal + values.awayShotsOnGoal match {
    case 0 => None
    case _ => Some(shotsRow)
  }

  def penaltyMinutesRow = TableRow(List(
    TableCell(s"${homeGroupedPenalties}${values.homePenaltyMinutesTotal} min"),
    TableCell("**" + tmpl("penalties").head.template + "**"),
    TableCell(s"${awayGroupedPenalties}${values.awayPenaltyMinutesTotal} min")
  ))

  def savesRow = TableRow(List(
    TableCell(values.homeGoalieSavesCombinedByPeriod.mkString(" + ") + " = " + values.homeGoalieSavesTotal.toString),
    TableCell("**" + tmpl("saves").head.template + "**"),
    TableCell(values.awayGoalieSavesCombinedByPeriod.mkString(" + ") + " = " + values.awayGoalieSavesTotal.toString)
  ))

  def faceoffsRow = TableRow(List(
    TableCell(values.homeFaceOffWins.toString + " (" + values.homeFaceOffPercentage.round.toString + " %)"),
    TableCell("**" + tmpl("faceoffs").head.template + "**"),
    TableCell(values.awayFaceOffWins.toString + " (" + values.awayFaceOffPercentage.round.toString + " %)")
  ))

  def faceoffsRowOption: Option[TableRow] = values.homeFaceOffWins + values.awayFaceOffWins match {
    case 0 => None
    case _ => Some(faceoffsRow)
  }

  def attendanceText = "**" + tmpl("attendance").head.template + ":** " + values.attendance.toString + " (" + values.venue.toString + ")"
}
