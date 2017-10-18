package equellatests.domain

import java.io.File
import java.nio.file.{Files, Path, Paths}

import scala.annotation.tailrec

object Uniqueify {

  def uniqueify[A](mkUniqueAttempt: (Int,A) => A)(exists: A => Boolean, v: A): A = {
    @tailrec
    def rec(attempt: Int): A = {
      val c = if (attempt == 1) v else mkUniqueAttempt(attempt,v)
      if (exists(c)) rec(attempt+1) else c
    }
    rec(1)
  }

  val uniquelyNumbered = uniqueify(numberAfter) _

  def numberAfter(n: Int, v: String) : String =
    s"$v ($n)"

  def numberBeforeExtension(n: Int, v: String) : String = {
    val (before,after) = v.lastIndexOf('.') match {
      case -1 => (v, "")
      case o => (v.substring(0, o), v.substring(o))
    }
    s"$before($n)$after"
  }

  def uniqueFile(dir: Path): String => Path =
    name => dir.resolve(uniqueify(numberBeforeExtension)(fn => Files.exists(dir.resolve(fn)), name))
}
