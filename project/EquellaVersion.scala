case class EquellaVersion(majorMinor: String, releaseType: String, commits: Int, sha: String) {
  def fullVersion = s"$majorMinor-$releaseType-r$commits-$sha"
}

object EquellaVersion {
  val RegEx = """(.*)-(.*)-r(\d*)-(.*)""".r
  def apply(s: String): EquellaVersion = s match {
    case RegEx(mm, rt, c, sha) => EquellaVersion(mm, rt, c.toInt, sha)
    case _                     => EquellaVersion("6.5", "Unknown", 0, "00000")
  }
}
