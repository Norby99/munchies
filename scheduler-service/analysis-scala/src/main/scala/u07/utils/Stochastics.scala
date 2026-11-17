// Vendored verbatim from ../../../asmd-public-models (src/main/scala/u07/utils/Stochastics.scala,
// commit 3297344). Not modified — see analysis-scala/README.md for the reuse rationale.
package u07.utils

import scala.util.Random

object Stochastics:

  given Random = new Random()

  // (p1,a1),...,(pn,an) --> (p1,a1),(p1+p2,a2),..,(p1+..+pn,an)
  def cumulative[A](l: List[(Double, A)]): List[(Double, A)] =
    l.tail.scanLeft(l.head):
      case ((r, _), (r2, a2)) => (r + r2, a2)

  // (p1,a1),...,(pn,an) --> ai, selected randomly and fairly
  def draw[A](cumulativeList: List[(Double,A)])(using rnd: Random): A =
    val rndVal = rnd.nextDouble() * cumulativeList.last._1
    cumulativeList.collectFirst:
      case (r, a) if r >= rndVal => a
    .get

  // (p1,a1),...,(pn,an) --> {a1 -> P1%,...,an -> Pn%}
  def statistics[A](choices: Set[(Double,A)], size: Int)
                   (using rnd: Random): Map[A, Int] =
    (1 to size).map(_ => draw(cumulative(choices.toList)))
                .groupBy(identity).view.mapValues(_.size).toMap
