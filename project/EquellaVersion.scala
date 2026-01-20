case class EquellaVersion(major: Int, minor: Int, patch: Int, releaseType: String, sha: String) {
  def semanticVersion = s"$major.$minor.$patch"
  def fullVersion     = s"$semanticVersion-$releaseType-$sha"
}

object EquellaVersion {
  val RegEx = """(\d+).(\d+).(\d+)-(.+)-(.+)""".r
  def apply(s: String): EquellaVersion = s match {
    case RegEx(major, minor, patch, rt, sha) =>
      EquellaVersion(major.toInt, minor.toInt, patch.toInt, rt, sha)
    case _ => EquellaVersion(6, 5, 0, "Unknown", "00000")
  }
}
