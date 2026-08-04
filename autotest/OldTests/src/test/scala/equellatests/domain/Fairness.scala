package equellatests.domain

import org.scalacheck.Gen

object Fairness {

  def favourIncomplete[A](
      incompleteRatio: Int,
      completeRatio: Int
  )(vals: Seq[A], complete: A => Boolean): Gen[A] = {
    Gen.frequency(
      vals.map(a => (if (complete(a)) completeRatio else incompleteRatio, Gen.const(a))): _*
    )
  }

  def favour3to1[A]: (Seq[A], A => Boolean) => Gen[A] = favourIncomplete(3, 1)
}
