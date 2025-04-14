import java.time.Instant
import java.time.temporal.{ChronoUnit, TemporalUnit}

import sbt.Keys._
import sbt._
import scala.sys.process._
import CommonSettings.autoImport._

object JarSignerPlugin extends AutoPlugin {

  var lastSignTime: Option[Instant] = None

  override def trigger: PluginTrigger = noTrigger

  override def requires: Plugins = CommonSettings

  object autoImport {
    lazy val keystore      = settingKey[File]("The signing keystore")
    lazy val storePassword = settingKey[String]("The store password")
    lazy val keyAlias      = settingKey[String]("The key alias")
    lazy val keyPassword   = settingKey[Option[String]]("The key password")
    lazy val jarSigner     = taskKey[(File, File) => Unit]("The jarsigner task")
    lazy val tsaUrl        = taskKey[Option[(String, Int)]]("The tsa url")
  }

  import autoImport._

  override def projectSettings = Seq(
    (ThisBuild / keystore) := {
      val c = buildConfig.value
      if (c.hasPath("signer.keystore")) file(c.getString("signer.keystore"))
      else {
        (ThisProject / baseDirectory).value / "generated.keystore"
      }
    },
    (ThisBuild / keyAlias) := {
      val c = buildConfig.value
      if (c.hasPath("signer.alias")) c.getString("signer.alias") else "genalias"
    },
    (ThisBuild / storePassword) := {
      val c = buildConfig.value
      if (c.hasPath("signer.storePassword")) c.getString("signer.storePassword") else "genpassword"
    },
    (ThisBuild / keyPassword) := {
      val c = buildConfig.value
      if (c.hasPath("signer.keyPassword")) Some(c.getString("signer.keyPassword")) else None
    },
    (ThisBuild / tsaUrl) := {
      val c       = buildConfig.value
      val seconds = c.getInt("signer.tsaDelay")
      if (c.hasPath("signer.tsaUrl")) Some((c.getString("signer.tsaUrl"), seconds)) else None
    },
    (ThisBuild / jarSigner) := { (inJar, outJar) =>
      val log     = sLog.value
      val keyFile = keystore.value
      val alias   = keyAlias.value
      val spasswd = storePassword.value
      val kpasswd = keyPassword.value
      if (!keyFile.exists) {
        log.info(s"Keystore ${keyFile.absolutePath} does not exist, creating.")
        val cmd = List(
          "keytool",
          "-genkey",
          "-alias",
          alias,
          "-keystore",
          keyFile.absolutePath,
          "-storepass",
          spasswd,
          "-keypass",
          kpasswd.getOrElse(spasswd),
          "-dname",
          "CN=equella"
        )
        cmd !
      }
      outJar.getParentFile.mkdirs()
      val ops = Seq(
        "jarsigner",
        "-keystore",
        keyFile.toURI.toString,
        "-storepass",
        spasswd,
        "-signedjar",
        outJar.absolutePath
      ) ++ kpasswd.map(kp => List("-keypass", kp)).getOrElse(Nil) ++
        tsaUrl.value.map(u => List("-tsa", u._1)).getOrElse(Nil) ++
        List(inJar.absolutePath, alias)
      tsaUrl.value.foreach { case (_, seconds) =>
        while (lastSignTime.exists(_.plus(seconds, ChronoUnit.SECONDS).isAfter(Instant.now()))) {
          log.info(s"Jar signer ran less than $seconds seconds ago - waiting")
          Thread.sleep(5000L)
        }
      }
      lastSignTime = Some(Instant.now())
      log.info(s"Signing jar ${inJar.absolutePath} to ${outJar.absolutePath}")
      val exResult = (ops !)
      if (exResult != 0) {
        sys.error(s"jarsigner exited with code $exResult")
      }
    }
  )
}
