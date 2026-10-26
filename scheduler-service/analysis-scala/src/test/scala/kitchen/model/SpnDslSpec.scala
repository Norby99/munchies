package kitchen.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import u07.utils.MSet
import u07.modeling.SPN
import SpnDsl.*

class SpnDslSpec extends AnyFunSuite:

  enum P:
    case A, B, C

  import P.*

  test("a single place on each side, with a constant rate, builds the expected Trn"):
    val trn = A ~ 2.0 ~> B
    trn.cond shouldBe MSet(A)
    trn.rate(MSet(A)) shouldBe 2.0
    trn.eff shouldBe MSet(B)
    trn.inh shouldBe MSet()

  test("a tuple of places on the cond side becomes a multi-token precondition"):
    val trn = (A, A) ~ 1.0 ~> B
    trn.cond shouldBe MSet(A, A)

  test("a tuple of places on the eff side becomes a multi-token effect"):
    val trn = A ~ 1.0 ~> (B, B, C)
    trn.eff shouldBe MSet(B, B, C)

  test("a marking-dependent rate function is used as-is, not wrapped as a constant"):
    val trn = A ~ ((m: MSet[P]) => m(A) * 3.0) ~> B
    trn.rate(MSet(A, A)) shouldBe 6.0
    trn.rate(MSet(A, A, A)) shouldBe 9.0

  test("a marking-dependent rate can be written as a bare lambda, no annotation needed"):
    // Overload resolution on a concrete P (fixed by AsMarking on the extension's own
    // clause, from A's type) pushes MSet[P] => Double as the expected type here --
    // the same mechanism xs.map(x => ...) already relies on against a known receiver.
    val trn = A ~ (m => m(A) * 3.0) ~> B
    trn.rate(MSet(A, A)) shouldBe 6.0

  test("^ with a single place sets the inhibitor"):
    val trn = A ~ 1.0 ~> B ^^^ C
    trn.inh shouldBe MSet(C)

  test("^ with a tuple sets a multi-place inhibitor"):
    val trn = A ~ 1.0 ~> B ^^^ (C, C)
    trn.inh shouldBe MSet(C, C)

  test("an already-built marking can stand on any side, unchanged"):
    val cond = MSet(A, B)
    val trn = cond ~ 1.0 ~> MSet(C) ^^^ MSet(A)
    trn.cond shouldBe cond
    trn.eff shouldBe MSet(C)
    trn.inh shouldBe MSet(A)

  test("the resulting Trn behaves correctly under SPN.toCTMC, inhibitor included"):
    val ctmc = SPN.toCTMC(SPN(A ~ 5.0 ~> B ^^^ C))
    ctmc.transitions(MSet(A)).map(_.state) should contain(MSet(B))
    ctmc.transitions(MSet(A, C)) shouldBe empty
