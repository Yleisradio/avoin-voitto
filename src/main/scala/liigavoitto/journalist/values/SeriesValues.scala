package liigavoitto.journalist.values

class SeriesValues(seriesId: String) {

  case class PlayoffRound(name: String, winsRequired: Int, teamStandings: List[Int])
  case class SeriesStage(stage: liigavoitto.journalist.values.StageEnum.Value, id: Option[String])
  case class SeriesAttributes(stages: List[SeriesStage], playoffRounds: List[PlayoffRound])

  val seriesValues: Map[String, SeriesAttributes] = Map(
    "sm-liiga" -> SeriesAttributes(
      List(
        SeriesStage(StageEnum.RegularSeason, Some("1311")),
        SeriesStage(StageEnum.Playoffs, Some("1312")),
        SeriesStage(StageEnum.Training, Some("1313"))
      ),
      List(
        PlayoffRound("1. Round", 2, List.range(7, 11)),
        PlayoffRound("Quarterfinal", 4, List.range(1,11)),
        PlayoffRound("Semifinal", 4, List.range(1,11)),
        PlayoffRound("Final", 4, List.range(1,11))
      )
    ),
    "liiga" -> SeriesAttributes(
      List(
        SeriesStage(StageEnum.RegularSeason, Some("1311")),
        SeriesStage(StageEnum.Playoffs, Some("1312")),
        SeriesStage(StageEnum.Training, Some("1313"))
      ),
      List(
        PlayoffRound("1. Round", 2, List.range(7, 11)),
        PlayoffRound("Quarterfinal", 4, List.range(1,11)),
        PlayoffRound("Semifinal", 4, List.range(1,11)),
        PlayoffRound("Final", 4, List.range(1,11))
      )
    ),
    "mestis" -> SeriesAttributes(
      List(
        SeriesStage(StageEnum.RegularSeason, Some("171")),
        SeriesStage(StageEnum.Playoffs, Some("2164")),
        SeriesStage(StageEnum.Training, Some("150"))
      ),
      List(
        PlayoffRound("Quarterfinal", 4, List.range(1,9)),
        PlayoffRound("Semifinal", 4, List.range(1,9)),
        PlayoffRound("Final", 4, List.range(1,9))
      )
    ),
    "naisten-liiga" -> SeriesAttributes(
      List(
        SeriesStage(StageEnum.RegularSeason, Some("191")),
        SeriesStage(StageEnum.Playoffs, Some("2202")),
        SeriesStage(StageEnum.Training, None)
      ),
      List(
        PlayoffRound("Quarterfinal", 3, List.range(3,7)),
        PlayoffRound("Semifinal", 3, List.range(1,7)),
        PlayoffRound("Final", 3, List.range(1,7))
      )
    )
  ).withDefaultValue(
    SeriesAttributes(
      List(
        SeriesStage(StageEnum.RegularSeason, None),
        SeriesStage(StageEnum.Playoffs, None),
        SeriesStage(StageEnum.Training, None)
      ),
      List(
        PlayoffRound("Quarterfinal", 3, List.range(1,11)),
        PlayoffRound("Semifinal", 3, List.range(1,11)),
        PlayoffRound("Final", 3, List.range(1,11))
      )
    )
  )

  val series: SeriesAttributes = seriesValues(seriesId)
  val playoffRounds: List[PlayoffRound] = series.playoffRounds
  val regularSeasonStageId: Option[String] = series.stages.find(_.stage == StageEnum.RegularSeason).get.id
  val playoffsStageId: Option[String] = series.stages.find(_.stage == StageEnum.Playoffs).get.id
  val trainingStageId: Option[String] = series.stages.find(_.stage == StageEnum.Training).get.id
}
