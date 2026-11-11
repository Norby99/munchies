package kitchen.util

import u07.utils.MSet

/** A map-backed `MSet`, faster than the vendored list-based one. */
object FastMSet:
  def apply[A](l: A*): MSet[A] = new FastMSetImpl(l.toList)
  def ofList[A](l: List[A]): MSet[A] = new FastMSetImpl(l)
  def ofMap[A](m: Map[A, Int]): MSet[A] = FastMSetImpl(m.filter(_._2 > 0))
  def fill[A](n: Int)(elem: => A): MSet[A] = ofList(List.fill(n)(elem))

  private case class FastMSetImpl[A](asMap: Map[A, Int]) extends MSet[A]:
    def this(list: List[A]) =
      this(list.groupBy(identity).view.mapValues(_.size).toMap)

    override def asList: List[A] = asMap.toList.flatMap { case (a, n) => List.fill(n)(a) }

    override def apply(v1: A) = asMap.getOrElse(v1, 0)

    override infix def ++(m: MSet[A]): MSet[A] = this.union(m)

    override def union(m: MSet[A]): MSet[A] =
      FastMSetImpl(m.asMap.foldLeft(asMap) { case (acc, (k, v)) => acc.updated(k, acc.getOrElse(k, 0) + v) })

    override def diff(m: MSet[A]): MSet[A] =
      FastMSetImpl(m.asMap.foldLeft(asMap) { case (acc, (k, v)) =>
        val remaining = acc.getOrElse(k, 0) - v
        if remaining > 0 then acc.updated(k, remaining) else acc.removed(k)
      })

    override def disjoined(m: MSet[A]): Boolean =
      val (smaller, larger) = if asMap.size <= m.asMap.size then (asMap, m.asMap) else (m.asMap, asMap)
      !smaller.keysIterator.exists(larger.contains)

    override def size: Int = asMap.valuesIterator.sum

    override def matches(m: MSet[A]): Boolean = extract(m).isDefined

    override def extract(m: MSet[A]): Option[MSet[A]] =
      m.asMap.foldLeft(Option(asMap)) { case (accOpt, (k, v)) =>
        accOpt.flatMap { acc =>
          val have = acc.getOrElse(k, 0)
          if have >= v then Some(if have == v then acc.removed(k) else acc.updated(k, have - v)) else None
        }
      }.map(FastMSetImpl.apply)

    override def iterator: Iterator[A] = asMap.keysIterator

    override def map[B](f: A => B): MSet[B] =
      FastMSetImpl(asMap.foldLeft(Map.empty[B, Int]) { case (acc, (a, n)) =>
        val b = f(a)
        acc.updated(b, acc.getOrElse(b, 0) + n)
      })

    override def flatMap[B](f: A => MSet[B]): MSet[B] =
      FastMSetImpl(asMap.foldLeft(Map.empty[B, Int]) { case (acc, (a, n)) =>
        f(a).asMap.foldLeft(acc) { case (acc2, (b, m2)) => acc2.updated(b, acc2.getOrElse(b, 0) + m2 * n) }
      })

    override def filter(f: A => Boolean): MSet[A] =
      FastMSetImpl(asMap.filter { case (a, _) => f(a) })

    override def collect[B](f: PartialFunction[A, B]): MSet[B] =
      FastMSetImpl(asMap.foldLeft(Map.empty[B, Int]) { case (acc, (a, n)) =>
        f.lift(a) match
          case Some(b) => acc.updated(b, acc.getOrElse(b, 0) + n)
          case None    => acc
      })

    override def toString = s"{${asList.mkString("|")}}"
