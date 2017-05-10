import de.heikoseeberger.sbtheader._
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._

import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.{AutoPlugin, CompileOrder, Def, PluginTrigger, Plugins}

object CommonSettings extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = HeaderPlugin && JvmPlugin


  override def projectSettings = Seq(
    organization := "org.apereo.equella",
    javacOptions ++= Seq("-source", "1.8"),
    compileOrder := CompileOrder.JavaThenScala,
    headerLicense := Some(HeaderLicense.ALv2("2015", "Apereo"))
  )
}