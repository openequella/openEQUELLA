import org.apache.commons.configuration.{FileConfiguration, PropertiesConfiguration}
import Path.rebase

prepareDevConfig := {
  val bc          = buildConfig.value.getConfig("devconfig")
  val javaHome    = file(System.getProperty("java.home"))
  val baseDir     = baseDirectory.value
  val srcRoot     = baseDir.getParentFile.getParentFile
  val pluginRoots = Seq(srcRoot / "Source/Plugins", srcRoot / "Platform", srcRoot / "Interface")
  val installerConfig =
    (LocalProject("Installer") / baseDirectory).value / "data/server/learningedge-config"
  val defaultsDir = baseDirectory.value / "defaults"
  val fromInstaller = Seq(
    installerConfig / "hikari.properties"
  ).pair(rebase(installerConfig, baseDir))
  val fromDefaults = Seq(
    "learningedge-log4j.yaml",
    "optional-config.properties",
    "hibernate.properties"
  ).map(f => (defaultsDir / s"$f.default", baseDir / f))

  IO.copy(fromInstaller ++ fromDefaults, CopyOptions().withOverwrite(false))

  val port       = bc.getInt("port")
  val hostname   = bc.getString("hostname")
  val imPath     = bc.getString("imagemagick")
  var auditLevel = if (bc.hasPath("audit.level")) bc.getString("audit.level") else "NONE"
  val adminurl =
    if (bc.hasPath("adminurl")) bc.getString("adminurl") else s"http://$hostname:$port/"
  val mc = new PropertiesConfiguration()
  mc.load(installerConfig / "mandatory-config.properties")

  mc.setProperty("freetext.index.location", baseDir / "data/freetext")
  mc.setProperty("filestore.root", baseDir / "data/filestore")
  mc.setProperty("freetext.stopwords.file", installerConfig / "en-stopWords.txt")
  mc.setProperty("equella.devmode", "true")
  mc.setProperty("plugins.location", pluginRoots.mkString(","))
  mc.setProperty("admin.url", adminurl)
  mc.setProperty("java.home", javaHome)
  mc.setProperty("http.port", port)
  mc.save(baseDir / "mandatory-config.properties")

  val imc = new PropertiesConfiguration()
  imc.load(installerConfig / "plugins/com.tle.core.imagemagick/config.properties.unresolved")
  imc.setProperty("imageMagick.path", imPath)
  imc.save(baseDir / "plugins/com.tle.core.imagemagick/config.properties")

  val viewItemConfiguration = new PropertiesConfiguration()
  viewItemConfiguration.load(
    installerConfig / "plugins/com.tle.web.viewitem/mandatory.properties.unresolved"
  )
  viewItemConfiguration.setProperty("audit.level", auditLevel)
  viewItemConfiguration.save(baseDir / "plugins/com.tle.web.viewitem/mandatory.properties")

  val log = streams.value.log
  log.info(s"Dev configuration with admin url of '$adminurl'")
  log.info(s"ImageMagick binary dir set to '$imPath'")
  log.info(
    s"Please edit database configuration file at '${(baseDir / "hibernate.properties").absolutePath}'"
  )
  jpfWriteDevJars
    .all(ScopeFilter(inAggregates(LocalProject("allPlugins"), includeRoot = false)))
    .value
}
