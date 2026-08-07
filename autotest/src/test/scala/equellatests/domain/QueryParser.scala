package equellatests.domain

object QueryParser {

  def titlesForOp(
      w1: Set[String],
      w2: Set[String],
      op: BooleanOp,
      biasLeft: Boolean,
      succeed: Boolean
  ): Set[String] =
    if (succeed)
      op match {
        case And => w1 ++ w2
        case Or  => if (biasLeft) w1 else w2
      }
    else
      op match {
        case And => if (biasLeft) w1 else w2
        case Or  => (w1 ++ w2).map(w => "Z" + w)
      }

  def boolQuery(w1: String, w2: String, op: BooleanOp): String = op match {
    case And => s"$w1 AND $w2"
    case Or  => s"$w1 OR $w2"
  }
}
