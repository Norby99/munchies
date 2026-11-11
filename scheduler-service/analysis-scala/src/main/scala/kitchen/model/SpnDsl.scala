package kitchen.model

import u07.utils.MSet
import u07.modeling.SPN

/** `cond ~ rate ~> eff ^^^ inh`, like the vendored Petri net DSL with a rate added. */
object SpnDsl:

  /** A place, a tuple of places, or a marking. */
  trait AsMarking[X, P]:
    def apply(x: X): MSet[P]

  object AsMarking extends LowPriorityAsMarking:
    given tuple[P, T <: Tuple](using Tuple.Union[T] <:< P): AsMarking[T, P] =
      t => MSet.ofList(t.toList.map(_.asInstanceOf[P]))
    given marking[P]: AsMarking[MSet[P], P] = identity(_)

  trait LowPriorityAsMarking:
    given single[P]: AsMarking[P, P] = MSet(_)

  case class PartialTrn[P](cond: MSet[P], rate: MSet[P] => Double)

  // Two overloads rather than a generic rate, so that a bare lambda's type can be inferred.
  extension [X, P](self: X)(using c: AsMarking[X, P])
    def ~(rate: MSet[P] => Double): PartialTrn[P] = PartialTrn(c(self), rate)
    def ~(rate: Double): PartialTrn[P] = PartialTrn(c(self), _ => rate)

  extension [P](self: PartialTrn[P])
    def ~>[Y](y: Y)(using e: AsMarking[Y, P]): SPN.Trn[P] =
      SPN.Trn(self.cond, self.rate, e(y), MSet())

  extension [P](self: SPN.Trn[P])
    def ^^^(z: MSet[P]): SPN.Trn[P] = self.copy(inh = z)
    def ^^^[Z](z: Z)(using i: AsMarking[Z, P]): SPN.Trn[P] = self.copy(inh = i(z))
