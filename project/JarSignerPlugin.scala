import sbt.Keys._
import sbt._
import CommonSettings.autoImport._

object JarSignerPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = noTrigger

  override def requires: Plugins = CommonSettings

  object autoImport {
    lazy val keystore = settingKey[File]("The signing keystore")
    lazy val storePassword = settingKey[String]("The store password")
    lazy val keyAlias = settingKey[String]("The key alias")
    lazy val keyPassword = settingKey[Option[String]]("The key password")
    lazy val jarSigner = taskKey[(File, File) => Unit]("The jarsigner task")
  }

  import autoImport._

  override def projectSettings = Seq(
    (keystore in ThisBuild) := {
      val c = buildConfig.value
      if (c.hasPath("signer.keystore")) file(c.getString("signer.keystore")) else {
        (baseDirectory in ThisProject).value / "generated.keystore"
      }
    },
    (keyAlias in ThisBuild) := {
      val c = buildConfig.value
      if (c.hasPath("signer.alias")) c.getString("signer.alias") else "genalias"
    },
    (storePassword in ThisBuild) := {
      val c = buildConfig.value
      if (c.hasPath("signer.storePassword")) c.getString("signer.storePassword") else "genpassword"
    },
    (keyPassword in ThisBuild) := {
      val c = buildConfig.value
      if (c.hasPath("signer.keyPassword")) Some(c.getString("signer.keyPassword")) else None
    },
    (jarSigner in ThisBuild) := {
      (inJar, outJar) =>
        val log = sLog.value
        val keyFile = keystore.value
        val alias = keyAlias.value
        val spasswd = storePassword.value
        val kpasswd = keyPassword.value
        if (!keyFile.exists) {
          log.info(s"Keystore ${keyFile.absolutePath} does not exist, creating.")
          val cmd = List(
            "keytool", "-genkey",
            "-alias", alias,
            "-keystore", keyFile.absolutePath,
            "-storepass", spasswd,
            "-keypass", kpasswd.getOrElse(spasswd),
            "-dname", "CN=equella"
          )
          cmd !
        }
        outJar.getParentFile.mkdirs()
        val ops = Seq(
          SignJar.keyStore(keyFile.toURI.toURL),
          SignJar.storePassword(spasswd),
          SignJar.signedJar(outJar)
        ) ++ kpasswd.map(SignJar.keyPassword)
        SignJar.sign(inJar, alias, ops) {
          (cmd, ops) => (cmd +: ops) !
        }
    }
  )
}
