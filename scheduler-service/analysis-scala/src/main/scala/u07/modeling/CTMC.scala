// Vendored verbatim from ../../../asmd-public-models (src/main/scala/u07/modelling/CTMC.scala,
// commit 3297344). Not modified — see analysis-scala/README.md for the reuse rationale.
package u07.modeling

import scala.util.Random

trait CTMC[S]:
  import CTMC.Action
  def transitions(a: S): Set[Action[S]] // rate + state

object CTMC:

  case class Action[S](rate: Double, state: S)
  extension [S](rate: Double)
    def -->(state: S) = Action(rate, state)

  case class Transition[S](state: S, action: Action[S])

  def ofFunction[S](f: PartialFunction[S, Set[Action[S]]]): CTMC[S] =
    s => f.applyOrElse(s, x => Set[Action[S]]())

  def ofRelation[S](rel: Set[Transition[S]]): CTMC[S] =
    ofFunction(s => rel filter (_.state == s) map (_.action))

  def ofTransitions[S](rel: Transition[S]*): CTMC[S] = ofRelation(rel.toSet)
