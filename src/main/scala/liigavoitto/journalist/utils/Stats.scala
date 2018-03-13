package liigavoitto.journalist.utils

import liigavoitto.scores.{ Player, PlayerStatsEntry }


case class Stats(goals: Int, assists: Int, points: Int, shotsOnGoal: Int = 0, missedShots: Int = 0, saves: Int = 0, penaltyMins: Int = 0, faceOffWins: Int = 0, faceOffLosses: Int = 0)
case class GoalieStats(saves: Int)

object Stats {

  def hasStats(p: Player) = {
    lazy val map = p.specific("stats").asInstanceOf[Map[String, Any]]("total").asInstanceOf[Map[String, Any]]
    p.specific.contains("stats") && map.contains("goals") && map.contains("assists") && map.contains("points")
  }

  def getFrom(p: Player) = {
    val map = p.specific("stats").asInstanceOf[Map[String, Any]]("total").asInstanceOf[Map[String, Any]]
    mapToStats(map)
  }

  private def mapToStats(map: Map[String, Any]) = {
    Stats(
      map.get("goals").map(_.asInstanceOf[BigInt].toInt).getOrElse(0),
      map.get("assists").map(_.asInstanceOf[BigInt].toInt).getOrElse(0),
      map.get("points").map(_.asInstanceOf[BigInt].toInt).getOrElse(0),
      map.get("shotsOnGoal").map(_.asInstanceOf[BigInt].toInt).getOrElse(0),
      map.get("missedShots").map(_.asInstanceOf[BigInt].toInt).getOrElse(0),
      map.get("saves").map(_.asInstanceOf[BigInt].toInt).getOrElse(0),
      map.get("penaltyMins").map(_.asInstanceOf[BigInt].toInt).getOrElse(0),
      map.get("faceOffWins").map(_.asInstanceOf[BigInt].toInt).getOrElse(0),
      map.get("faceOffLosses").map(_.asInstanceOf[BigInt].toInt).getOrElse(0)
    )
  }

  def getPerPeriod(p: Player) = {
    val perPeriod = p.specific("stats").asInstanceOf[Map[String, Any]]("perPeriod").asInstanceOf[Map[String, Map[String, Any]]]
    perPeriod.mapValues(map => mapToStats(map))
  }

  def getFrom(playerStatsEntry: PlayerStatsEntry) = {
    combine(playerStatsEntry.teamStats.map(teamStats =>
      Stats(
        teamStats.goals,
        teamStats.assists,
        teamStats.points
      )))
  }

  def getGoalieStats(p: Player) = {
    val map = p.specific("stats").asInstanceOf[Map[String, Any]]("total").asInstanceOf[Map[String, Any]]
    GoalieStats(
      map.get("saves").map(_.asInstanceOf[BigInt].toInt).getOrElse(0)
    )
  }

  def combine(list: List[Stats]) = Stats(list.map(_.goals).sum, list.map(_.assists).sum, list.map(_.points).sum)

}
