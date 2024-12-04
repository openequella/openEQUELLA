import sbt.File

case class JPFRuntime(
    manifest: File,
    code: Seq[File],
    resources: Seq[File],
    jars: Seq[File],
    group: String
)
