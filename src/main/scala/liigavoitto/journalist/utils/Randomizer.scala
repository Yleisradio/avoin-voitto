package liigavoitto.journalist.utils

import scala.util.Random

trait Randomizer {
  def random[T](from: List[T]): Option[T] = if (from.length <= 0) None else from.lift(Random.nextInt(from.size))
}


trait WeightedRandomizer {
  def weightedRandom(list: List[RenderedTemplate]): Option[String] = {
    if (list.isEmpty) None
    else {
      val sumOfWeights = list.map(_.weight).sum
      val normalizedWeights = list.map(_.weight / sumOfWeights)

      // cumulatedWeights ranges from 0.0 to 1.0
      val cumulatedWeights = normalizedWeights.scanLeft(0.0)(_+_)

      // Random.nextDouble returns a value in between 0.0 and 1.0
      val random = Random.nextDouble()

      // Walk the cumulated weights until the random value is exceeded
      val index = cumulatedWeights.indexWhere(cw => cw >= random) - 1

      list.lift(index).map(_.text)
    }
  }
}