package liigavoitto.journalist.values

import liigavoitto.journalist.utils._
import liigavoitto.scores.{Feed, Match, Player, PlayerStatsEntry, Team}

trait ImportantPlayerValues {
  implicit val lang: String
  implicit val mtch: Match
  implicit val home: Team
  implicit val away: Team
  implicit val winner: Team
  implicit val loser: Team
  implicit val endPeriod: Int
  implicit val goalDif: Int
  implicit val previousHomeTeamMatches: List[Match]
  implicit val previousAwayTeamMatches: List[Match]
  implicit val playerStats: List[PlayerStatsEntry]
  implicit val winningGoal: Feed
  implicit val winningTeamPlayers: List[Player]

  lazy val winningGoalPlayer = winningTeamPlayers.find(_.id == winningGoal.player.get.id).get
  lazy val bestPlayerScoresWinningGoal = winningTeamBestPlayer.id == winningGoal.player.get.id

  lazy val allPlayers = skaters(home.players ++ away.players)
  lazy val bestPlayer = getPlayerWithMostPoints(allPlayers)
  lazy val bestPlayerStats = Stats.getFrom(bestPlayer)
  lazy val winningTeamBestPlayer = getPlayerWithMostPoints(skaters(winner.players))
  lazy val winningTeamBestPlayerStats = Stats.getFrom(winningTeamBestPlayer)
  lazy val winningTeamBestPlayerTookPartInMostGoals = (winningTeamBestPlayerStats.points.toDouble / winner.score.get.now) >= 0.5

  lazy val isShutout = loser.score.get.now == 0
  lazy val winningTeamBestGoalie = goalies(winner.players).sortBy(Stats.getGoalieStats(_).saves).reverse.head
  lazy val winningTeamBestGoalieStats = Stats.getGoalieStats(winningTeamBestGoalie)

  lazy val losingTeamBestPlayer = getPlayerWithMostPoints(skaters(loser.players))
  lazy val losingTeamBestPlayerStats = Stats.getFrom(losingTeamBestPlayer)

  def skaters(players: List[Player]) = players.filterNot(isGoalie).filter(hasStats)
  def goalies(players: List[Player]) = players.filter(isGoalie)

  def isGoalie(player: Player) = player.position.contains("g")
  def isUndefined(player: Player) = player.position.contains("")
  def hasStats(player: Player) = Stats.hasStats(player)

  def hasExceptionalPoints(p: Player): Boolean = {
    val stats = Stats.getFrom(p)
    stats.goals >= 3 || stats.points >= 4
  }

  def hasSeasonsFirstPoints(p: Player): Boolean = !hasPreviousPoints(p) && hasPoints(p)

  def hasPreviousPoints(p: Player) =
    playerStats.find(ps => ps.player.id == p.id)
      .map(stats => stats.teamStats.map(_.points).sum)
      .exists(points => points > 0)

  def hasPoints(p: Player) = Stats.getFrom(p).points > 0

  def isFirstMatch(player: Player) = {
    if (isHomeTeamPlayer(player)) previousHomeTeamMatches.isEmpty
    else previousAwayTeamMatches.isEmpty
  }

  def isHomeTeamPlayer(player: Player) = home.players.exists(p => p.id == player.id)

  def getPlayerTeam(player: Option[Player]): Option[Team] = {
    player.map(p => if (isHomeTeamPlayer(p)) home else away)
  }

  def getPlayerHomeOrAway(player: Option[Player]): Option[String] = {
    player.map(p => if (isHomeTeamPlayer(p)) "kotijoukkueen" else "vierasjoukkueen")
  }

  def points(player: Player) = Stats.getFrom(player).points

  def goals(player: Player) = Stats.getFrom(player).goals

  def getPlayerWithMostPoints(players: List[Player]) =
    players.reduceLeft((p1, p2) => if (compareStats(p1, p2)) p1 else p2)

  private def compareStats(p1: Player, p2: Player) = {
    val p1Goals = goals(p1)
    val p1Points = points(p1)
    val p2Goals = goals(p2)
    val p2Points = points(p2)

    if (p1Points == p2Points) {
      if (p1Goals == p2Goals) winningGoal.player.get.id == p1.id
      else p1Goals > p2Goals
    }
    else p1Points > p2Points
  }
}
