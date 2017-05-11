import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import de.heikoseeberger.sbtheader._
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object CommonSettings extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = HeaderPlugin && JvmPlugin


  override def projectSettings = Seq(
    organization := "org.apereo.equella",
    javacOptions ++= Seq("-source", "1.8"),
    compileOrder := CompileOrder.JavaThenScala,
    headerLicense := Some(HeaderLicense.ALv2("2015", "Apereo")),
    resolvers += "Local EQUELLA deps" at "file://" + Path.userHome.absolutePath + "/equella-deps",
    libraryDependencies += "junit" % "junit" % "4.12" % Test
  )
}