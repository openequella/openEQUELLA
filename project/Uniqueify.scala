import scala.annotation.tailrec

object Uniqueify {

  def uniqueify[A](mkUniqueAttempt: (Int, A) => A)(exists: A => Boolean, v: A): A = {
    @tailrec
    def rec(attempt: Int): A = {
      val c = if (attempt == 1) v else mkUniqueAttempt(attempt, v)
      if (exists(c)) rec(attempt + 1) else c
    }
    rec(1)
  }

  def uniqueSeq[A](mkUniqueAttempt: (Int, A) => A, contains: List[A] => A => Boolean)(list: Seq[A]): Seq[A] = {
    val uniqf = uniqueify(mkUniqueAttempt) _
    list.foldRight(List.empty[A])((a, already) => uniqf(contains(already), a) :: already)
  }
}