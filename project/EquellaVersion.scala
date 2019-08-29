case class EquellaVersion(majorMinor: String, releaseType: String, sha: String) {
  def fullVersion = s"$majorMinor-$releaseType-$sha"
}

object EquellaVersion {
  val RegEx = """(.*)-(.*)-(.*)""".r
  def apply(s: String): EquellaVersion = s match {
    case RegEx(mm, rt, sha) => EquellaVersion(mm, rt, sha)
    case _                  => EquellaVersion("6.5", "Unknown", "00000")
  }
}
