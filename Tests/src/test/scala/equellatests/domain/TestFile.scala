package equellatests.domain

import java.io.File
import java.net.URL
import java.nio.file.{Files, Paths}

import com.tle.common.PathUtils
import org.scalacheck.{Arbitrary, Gen}
import Arbitrary._
import TestFile._
import io.circe.generic.semiauto._

case class TestFile(realFilename: String) {
  def extension: String = PathUtils.extension(realFilename)

  lazy val packageDetails: Option[(String, String)] = packageMap.get(realFilename)
  lazy val extraDetails: Iterable[(String, String)] = additionalMeta(realFilename)

  def packageName: Option[String] = packageDetails.map(_._1).filterNot(_.isEmpty)
  def ispackage : Boolean = packageDetails.isDefined
  def packageType : Option[String] = packageDetails.map(_._2)
  lazy val file = new File(baseDir, realFilename)
  lazy val fileSize: Long = file.length()
}

object TestFile {

  implicit val tfEncoder = deriveEncoder[TestFile]
  implicit val tfDecoder = deriveDecoder[TestFile]

  val bannedExt = Set("exe")
  val baseDir = Paths.get(getClass.getResource("/com/tle/webtests/test/files/").toURI).toFile
  lazy val tmpDir = Files.createTempDirectory("fupload")

  lazy val testFiles = baseDir.listFiles().toSeq.filter(_.isFile).map(f => TestFile(f.getName))

  val imsPackageType = "IMS Package"
  val qtiTestType = "QTI Test"
  val scormPackageType = "SCORM Package"

  val viewersFor = Map(
    imsPackageType -> Set("downloadIms"),
    scormPackageType -> Set("downloadIms")
  ).withDefaultValue(Set.empty)

  val packageMap = Map(
    "repaired bbq.zip" -> ("BBQs test package", qtiTestType),
    "package.zip" -> ("Zou ba! Visiting China: Is this your first visit?", imsPackageType),
    "qti.zip" -> ("RespondusIntroduction (Respondus IMS QTI export)", imsPackageType),
    "package2.zip" -> ("Arrays: word problems with products from 10 to 30", imsPackageType),
    "realqti.zip" -> ("BBQs test package", qtiTestType),
    "scorm.zip" -> ("", scormPackageType)
  )
  val qtiMeta = Iterable(
    "Allow navigation:" -> "Yes",
    "Maximum time limit:" -> "No time limit",
    "Tool version:" -> "0.1",
    "Tool name:" -> "Spectatus",
    "Number of sections:" -> "2",
    "Number of questions:" -> "22")
  val additionalMeta = Map(
    "docxforindexing.docx" -> Iterable("Author:" -> "Aaron"),
    "repaired bbq.zip" -> qtiMeta,
    "realqti.zip" -> qtiMeta
  ).withDefaultValue(Iterable.empty)



  def realFile(tf: TestFile, actualFilename: String) : File = {
    val targetPath = tmpDir.resolve(actualFilename)
    Files.copy(Paths.get(tf.file.toURI), targetPath)
    targetPath.toFile
  }

}
