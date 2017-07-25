import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object JPFPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = noTrigger

  override def requires: Plugins = JvmPlugin

  object autoImport {
    lazy val jpfCodeDirs = settingKey[Seq[File]]("JPF runtime code")
    lazy val jpfResourceDirs = settingKey[Seq[File]]("JPF runtime resources")
    lazy val jpfLibraryJars = taskKey[Classpath]("JPF runtime jars")
    lazy val jpfRuntime = taskKey[JPFRuntime]("JPF runtime")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    autoScalaLibrary := false,
    managedScalaInstance := false,
    javacOptions ++= Seq("-source", "1.8"),
    jpfCodeDirs := Seq((classDirectory in Compile).value),
    resourceDirectory in Compile := baseDirectory.value / "resources",
    jpfResourceDirs := (resourceDirectories in Compile).value,
    jpfRuntime := JPFRuntime(baseDirectory.value / "plugin-jpf.xml", jpfCodeDirs.value, jpfResourceDirs.value,
      jpfLibraryJars.value.files, baseDirectory.value.getParentFile.getName),
    jpfLibraryJars := Seq(),
    managedClasspath in Compile ++= jpfLibraryJars.value,
    compileOrder := CompileOrder.JavaThenScala,
    javaSource in Compile := baseDirectory.value / "src",
    javaSource in Test := baseDirectory.value / "test",

    unmanagedSourceDirectories in Compile := (javaSource in Compile).value :: Nil,
    unmanagedSourceDirectories in Test := (javaSource in Test).value :: Nil,
    updateOptions := updateOptions.value.withCachedResolution(true)
  )
}