package kitchen.model

import u07.utils.MSet
import u07.modeling.SPN

/**
 * Builds SPN.Trn values via `cond ~ rate ~> eff ^^^ inh`, parallel to
 * u06.modeling.PetriNet's `~~>`/`^^^` but with a rate slot in between.
 */
object SpnDsl:

  /** Anything that can stand on a side of `~`/`~>`: a single place, a tuple of places,
   * or an already-built marking. */
  trait AsMarking[X, P]:
    def apply(x: X): MSet[P]

  object AsMarking extends LowPriorityAsMarking:
    given tuple[P, T <: Tuple](using Tuple.Union[T] <:< P): AsMarking[T, P] =
      t => MSet.ofList(t.toList.map(_.asInstanceOf[P]))
    given marking[P]: AsMarking[MSet[P], P] = identity(_)

  trait LowPriorityAsMarking:
    given single[P]: AsMarking[P, P] = MSet(_)

  /** `cond ~ rate`: not yet a full transition -- missing the effect. */
  case class PartialTrn[P](cond: MSet[P], rate: MSet[P] => Double)

  // Two concrete overloads, not one generic `r: R` -- a generic rate parameter can't be
  // inferred from an unannotated lambda (nothing pins its type before Scala elaborates
  // it). Resolving `AsMarking[X, P]` on the *extension's own* clause, rather than inside
  // each method, fixes `P` before either overload is considered, so ordinary overload
  // resolution can push the concrete expected type `MSet[P] => Double` into a bare
  // lambda -- the same mechanism that already lets `xs.map(x => ...)` infer `x`'s type
  // against a known receiver.
  extension [X, P](self: X)(using c: AsMarking[X, P])
    def ~(rate: MSet[P] => Double): PartialTrn[P] = PartialTrn(c(self), rate)
    def ~(rate: Double): PartialTrn[P] = PartialTrn(c(self), _ => rate)

  extension [P](self: PartialTrn[P])
    def ~>[Y](y: Y)(using e: AsMarking[Y, P]): SPN.Trn[P] =
      SPN.Trn(self.cond, self.rate, e(y), MSet())

  extension [P](self: SPN.Trn[P])
    def ^^^(z: MSet[P]): SPN.Trn[P] = self.copy(inh = z)
    def ^^^[Z](z: Z)(using i: AsMarking[Z, P]): SPN.Trn[P] = self.copy(inh = i(z))
