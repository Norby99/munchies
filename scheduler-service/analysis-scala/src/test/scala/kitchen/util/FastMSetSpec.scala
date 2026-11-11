package kitchen.util

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import u07.utils.{MSet => VendoredMSet}
import scala.util.Random

/** `FastMSet` gives the same `asMap` as the vendored `MSet` for every operation. */
class FastMSetSpec extends AnyFunSuite:

  private def vendored(l: Int*): VendoredMSet[Int] = VendoredMSet.ofList(l.toList)
  private def fast(l: Int*): VendoredMSet[Int] = FastMSet.ofList(l.toList)

  test("apply/size agree on a hand-picked multiset"):
    val vs = vendored(1, 1, 2, 3, 3, 3)
    val fs = fast(1, 1, 2, 3, 3, 3)
    vs.asMap shouldBe fs.asMap
    vs.size shouldBe fs.size
    for v <- Seq(1, 2, 3, 4) do vs(v) shouldBe fs(v)

  test("union/++ agree"):
    val v = vendored(1, 1, 2) union vendored(2, 3)
    val f = fast(1, 1, 2) union fast(2, 3)
    v.asMap shouldBe f.asMap

  test("diff agrees, including dropping a key that hits zero"):
    val v = vendored(1, 1, 2, 3) diff vendored(1, 3)
    val f = fast(1, 1, 2, 3) diff fast(1, 3)
    v.asMap shouldBe f.asMap
    v.asMap.contains(3) shouldBe false
    f.asMap.contains(3) shouldBe false

  test("disjoined agrees, both when sharing a key and when not"):
    vendored(1, 2).disjoined(vendored(3, 4)) shouldBe fast(1, 2).disjoined(fast(3, 4))
    vendored(1, 2).disjoined(vendored(2, 4)) shouldBe fast(1, 2).disjoined(fast(2, 4))

  test("extract agrees: success case removes exactly the matched counts"):
    val v = vendored(1, 1, 2, 3).extract(vendored(1, 2))
    val f = fast(1, 1, 2, 3).extract(fast(1, 2))
    v.map(_.asMap) shouldBe f.map(_.asMap)

  test("extract agrees: failure case (not enough tokens) yields None on both"):
    vendored(1, 2).extract(vendored(1, 1)) shouldBe None
    fast(1, 2).extract(fast(1, 1)) shouldBe None

  test("matches agrees"):
    vendored(1, 1, 2).matches(vendored(1, 2)) shouldBe fast(1, 1, 2).matches(fast(1, 2))
    vendored(1).matches(vendored(1, 1)) shouldBe fast(1).matches(fast(1, 1))

  test("map/flatMap/filter/collect agree"):
    vendored(1, 2, 3).map(_ * 2).asMap shouldBe fast(1, 2, 3).map(_ * 2).asMap
    vendored(1, 2).flatMap(x => VendoredMSet(x, x)).asMap shouldBe
      fast(1, 2).flatMap(x => FastMSet(x, x)).asMap
    vendored(1, 2, 3, 4).filter(_ % 2 == 0).asMap shouldBe fast(1, 2, 3, 4).filter(_ % 2 == 0).asMap
    vendored(1, 2, 3).collect { case x if x > 1 => x * 10 }.asMap shouldBe
      fast(1, 2, 3).collect { case x if x > 1 => x * 10 }.asMap

  test("iterator yields the same distinct-key set as the vendored implementation"):
    vendored(1, 1, 2, 3).iterator.toSet shouldBe fast(1, 1, 2, 3).iterator.toSet

  test("randomized cross-check: union/diff/disjoined/extract/size agree over many random multisets"):
    val rnd = new Random(2026)
    for _ <- 0 until 500 do
      val a = List.fill(rnd.nextInt(10))(rnd.nextInt(6))
      val b = List.fill(rnd.nextInt(10))(rnd.nextInt(6))
      val (va, fa) = (VendoredMSet.ofList(a), FastMSet.ofList(a))
      val (vb, fb) = (VendoredMSet.ofList(b), FastMSet.ofList(b))
      (va union vb).asMap shouldBe (fa union fb).asMap
      (va diff vb).asMap shouldBe (fa diff fb).asMap
      va.disjoined(vb) shouldBe fa.disjoined(fb)
      va.extract(vb).map(_.asMap) shouldBe fa.extract(fb).map(_.asMap)
      va.size shouldBe fa.size
