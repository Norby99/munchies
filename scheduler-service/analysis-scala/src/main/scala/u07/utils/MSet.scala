// Vendored verbatim from ../../../asmd-public-models (src/main/scala/u07/utils/MSet.scala,
// commit 3297344). Not modified — see analysis-scala/README.md for the reuse rationale.
// This is the MSet u07.modelling.SPN actually imports (not u06's) — the course keeps each
// lesson's utils self-contained rather than reusing the prior lesson's copy.
package u07.utils

import scala.collection.immutable

// A multiset datatype
trait MSet[A] extends (A => Int):
  infix def ++(m: MSet[A]): MSet[A]
  def union(m: MSet[A]): MSet[A]
  def diff(m: MSet[A]): MSet[A]
  def disjoined(m: MSet[A]): Boolean
  def size: Int
  def matches(m: MSet[A]): Boolean
  def extract(m: MSet[A]): Option[MSet[A]]
  def asList: List[A]
  def asMap: Map[A,Int]
  def iterator: Iterator[A]
  def map[B](f: (A) => B): MSet[B]
  def flatMap[B](f: (A) => MSet[B]): MSet[B]
  def filter(f: (A) => Boolean): MSet[A]
  def collect[B](f: PartialFunction[A, B]): MSet[B]

// Functional-style helpers/implementation
object MSet:
  // Factories
  def apply[A](l: A*): MSet[A] = new MSetImpl(l.toList)
  def ofList[A](l: List[A]): MSet[A] = new MSetImpl(l)
  def ofMap[A](m: Map[A,Int]): MSet[A] = MSetImpl(m)
  def fill[A](n: Int)(elem: => A): MSet[A] = ofList(List.fill(n)(elem))

  // Hidden reference implementation
  private case class MSetImpl[A](asMap: Map[A,Int]) extends MSet[A]:
    def this(list: List[A]) =
      this(list.groupBy(a=>a).map{case (a,n) => (a, n.size)})
    override val asList =
      asMap.toList.flatMap{case (a,n) => immutable.List.fill(n)(a)}

    override def apply(v1: A) = asMap.getOrElse(v1,0)

    override infix def ++(m: MSet[A]): MSet[A] = this.union(m)
    override def union(m: MSet[A]) = new MSetImpl[A](asList ++ m.asList)
    override def diff(m: MSet[A]) = new MSetImpl[A](asList diff m.asList)
    override def disjoined(m: MSet[A]) = (asList intersect m.asList).isEmpty
    override def size = asList.size
    override def matches(m: MSet[A]) = extract(m).isDefined
    override def extract(m: MSet[A]) =
      Some(this diff m) filter (_.size == size - m.size)
    override def iterator = asMap.keysIterator
    override def map[B](f: (A) => B) = new MSetImpl[B](asList.map(f))
    override def flatMap[B](f: (A) => MSet[B]) =
      new MSetImpl[B](asList.flatMap(x => f(x).asList))
    override def filter(f: (A) => Boolean) =
      new MSetImpl[A](asList.filter(f))
    override def collect[B](f: PartialFunction[A, B]) =
      new MSetImpl[B](asList.collect(f))
    override def toString = s"{${asList.mkString("|")}}"
