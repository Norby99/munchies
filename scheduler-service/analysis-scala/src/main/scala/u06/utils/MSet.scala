// Vendored verbatim from ../../../asmd-public-models (src/main/scala/u06/utils/MSet.scala,
// commit 3297344). Not modified — see analysis-scala/README.md for the reuse rationale.
package u06.utils

// A multiset datatype
trait MSet[A] extends (A => Int):
  infix def union(m: MSet[A]): MSet[A]
  def diff(m: MSet[A]): MSet[A]
  infix def disjoined(m: MSet[A]): Boolean
  def size: Int
  def matches(m: MSet[A]): Boolean
  infix def extract(m: MSet[A]): Option[MSet[A]]
  def asList: List[A]
  def asMap: Map[A,Int]
  def iterator: Iterator[A]
  def printMap: String

// Functional-style helpers/implementation
object MSet:
  // Factories
  def apply[A](l: A*): MSet[A] = new MSetImpl(l.toList)
  def ofList[A](l: List[A]): MSet[A] = new MSetImpl(l)
  def ofMap[A](m: Map[A,Int]): MSet[A] = MSetImpl(m)
  def ofTuple[A, T <: Tuple](t: T)(using Tuple.Union[T] <:< A): MSet[A] =
    ofList(t.toList.map(_.asInstanceOf[A]))

  // Hidden reference implementation
  private case class MSetImpl[A](asMap: Map[A,Int]) extends MSet[A]:
    def this(list: List[A]) = this(list.groupBy(a => a).map((a,n) => (a, n.size)))
    override val asList =
      asMap.toList.flatMap((a,n) => List.fill(n)(a))

    override def apply(v1: A): Int = asMap.getOrElse(v1,0)
    override def union(m: MSet[A]) = new MSetImpl[A](asList ++ m.asList)
    override def diff(m: MSet[A]) = new MSetImpl[A](asList diff m.asList)
    override def disjoined(m: MSet[A]) = (asList intersect m.asList).isEmpty
    override def size = asList.size
    override def matches(m: MSet[A]) = extract(m).isDefined
    override def extract(m: MSet[A]) =
      Some(this diff m) filter (_.size == size - m.size)
    override def iterator = asMap.keysIterator
    override def toString = s"{${asList.mkString("|")}}"
    override def printMap = s"$asMap"
