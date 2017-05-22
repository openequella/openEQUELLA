import org.apache.commons.configuration.{FileConfiguration, PropertiesConfiguration}

prepareDevConfig := {
  val bc = buildConfig.value.getConfig("devconfig")
  val javaHome = file(System.getProperty("java.home"))
  val baseDir = baseDirectory.value
  val installerConfig = (baseDirectory in LocalProject("Installer")).value / "data/server/learningedge-config"
  val defaultsDir = baseDirectory.value / "defaults"
  val fromInstaller = Seq(
    installerConfig / "hikari.properties",
    installerConfig / "plugins/com.tle.web.viewitem/mandatory.properties"
  ).pair(rebase(installerConfig, baseDir))
  val fromDefaults = Seq(
    "learningedge-log4j.properties",
    "optional-config.properties",
    "hibernate.properties"
  ).map(f => (defaultsDir / s"$f.default", baseDir / f))

  IO.copy(fromInstaller ++ fromDefaults)

  val port = bc.getInt("port")
  val hostname = bc.getString("hostname")
  val imPath = bc.getString("imagemagick")
  val adminurl = if (bc.hasPath("adminurl")) bc.getString("adminurl") else s"http://$hostname:$port/"
  val mc = new PropertiesConfiguration()
  mc.load(installerConfig / "mandatory-config.properties")
  mc.setProperty("freetext.index.location", baseDir / "data/freetext")
  mc.setProperty("filestore.root", baseDir / "data/filestore")
  mc.setProperty("freetext.stopwords.file", installerConfig / "en-stopWords.txt")
  mc.setProperty("plugins.location", (target in LocalProject("equellaserver")).value / "manifests")
  mc.setProperty("admin.url", adminurl)
  mc.setProperty("java.home", javaHome)
  mc.setProperty("http.port", port)
  mc.save(baseDir / "mandatory-config.properties")

  val imc = new PropertiesConfiguration()
  imc.load(installerConfig / "plugins/com.tle.core.imagemagick/config.properties.unresolved")
  imc.setProperty("imageMagick.path", imPath)
  imc.save(baseDir / "plugins/com.tle.core.imagemagick/config.properties")

  val log = sLog.value
  log.info(s"Dev configuration with admin url of '$adminurl'")
  log.info(s"ImageMagick binary dir set to '$imPath'")
  log.info(s"Please edit database configuration file at '${(baseDir / "hibernate.properties").absolutePath}'")
}