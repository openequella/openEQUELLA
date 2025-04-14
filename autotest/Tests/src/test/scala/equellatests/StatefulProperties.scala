package equellatests

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.UUID

import com.tle.webtests.framework.{PageContext, ScreenshotTaker, TestConfig}
import equellatests.browserpage.BrowserPage
import equellatests.domain._
import equellatests.pages.{HomePage, LoginPage}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import org.openqa.selenium.WebDriver
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Prop, Properties}

import scala.collection.mutable
import scala.util.Try

trait SeleniumBrowser {
  var page: BrowserPage

  def createUnique: String = UUID.randomUUID().toString.filterNot(_ == '-')

  var unique: String = createUnique

  def resetUnique(): Unit = unique = createUnique

  def uniquePrefix(s: String) = s"$unique $s"

  def allUniqueQuery = s"+$unique"

  def uniqueQuery(n: String) = s"""+$unique "$n""""

  def run(action: => BrowserPage): Prop = withTry(Try(action).map((_, Prop.proved)))

  def runOnPage(action: PartialFunction[BrowserPage, BrowserPage]): Prop =
    verifyOnPage(action.andThen((_, Prop.proved)))

  def withTry(tried: Try[(BrowserPage, Prop)]): Prop =
    tried.fold(
      t => Prop.exception(t),
      { p =>
        page = p._1; p._2
      }
    )

  def verify(action: => (BrowserPage, Prop)): Prop = withTry(Try(action))

  def verifyOnPage(action: PartialFunction[BrowserPage, (BrowserPage, Prop)]): Prop =
    withTry(Try(action.applyOrElse(page, SimpleSeleniumBrowser.wrongState)))

  def verifyOnPageAndState[S](
      s: S
  )(action: PartialFunction[(S, BrowserPage), (BrowserPage, Prop)]): Prop =
    withTry(
      Try(
        action.applyOrElse(
          s -> page,
          (p: (S, BrowserPage)) => p._2 -> SimpleSeleniumBrowser.wrongPageProp
        )
      )
    )
}

object SimpleSeleniumBrowser {
  val wrongPageProp = Prop.falsified.label("Expected to be on a different page")
  val wrongState: BrowserPage => (BrowserPage, Prop) =
    _ -> wrongPageProp
}

case class SimpleSeleniumBrowser(var page: BrowserPage) extends SeleniumBrowser

trait LogonTestCase {
  def logon: TestLogon

  type Browser <: SeleniumBrowser

  def createInital: BrowserPage => Browser

  def createBrowser: Browser = {
    val testConfig = new TestConfig(GlobalConfig.baseFolderForInst(logon.inst), false)
    TestChecker.withBrowserDriver("opening", testConfig) { driver =>
      val context = new PageContext(driver, testConfig, testConfig.getInstitutionUrl)
      createInital(new LoginPage(context).load().login(logon.username, logon.password))
    }
  }

  def destroyBrowser(sut: Browser): Unit = {
    sut.page.driver.quit()
  }

}

trait SimpleTestCase extends LogonTestCase {
  type Browser = SimpleSeleniumBrowser
  override def createInital: BrowserPage => Browser = SimpleSeleniumBrowser.apply
}

case class FailedTestCase(
    shortName: String,
    propertiesClass: String,
    failedAfter: Int,
    testCase: Json
)

object FailedTestCase {
  implicit val ftcEnc: Encoder[FailedTestCase] = deriveEncoder[FailedTestCase]
  implicit val ftcDec: Decoder[FailedTestCase] = deriveDecoder[FailedTestCase]
}

abstract class StatefulProperties(name: String) extends Properties(name: String) {
  type Command
  type Browser <: SeleniumBrowser
  type State

  implicit val testCaseDecoder: Decoder[Command]
  implicit val testCaseEncoder: Encoder[Command]

  def initialState: State

  def createBrowser: Browser

  def destroyBrowser(b: Browser): Unit

  def runCommand(c: Command, s: State): State

  def runCommandInBrowser(c: Command, s: State, b: Browser): Prop

  def executeProp(shortName: String, allCommands: Seq[Command], failedAfter: Option[Int]): Prop = {
    val b = createBrowser

    def nextCommand(s: State, commands: List[Command], previousCommands: Int): Prop =
      commands match {
        case Nil => Prop.proved
        case c :: tail => {
          failedAfter.foreach { failedAt =>
            if (failedAt == previousCommands) System.err.println("*** Failed on next command ***")
            System.err.println(c.toString)
          }
          Try(runCommandInBrowser(c, s, b)).fold(Prop.exception(_), identity).flatMap { r =>
            if (!r.success) {
              val tc       = b.page.ctx.getTestConfig
              val filename = (name + " " + shortName).replace(' ', '_')
              Try(
                ScreenshotTaker.takeScreenshot(
                  b.page.driver,
                  tc.getScreenshotFolder,
                  filename,
                  tc.isChromeDriverSet
                )
              )
              if (failedAfter.isEmpty) {
                val testRunFile =
                  Uniqueify.uniqueFile(tc.getResultsFolder.toPath).apply(filename + "_test.json")
                val failure = FailedTestCase(
                  shortName,
                  getClass.getName,
                  previousCommands,
                  allCommands.asJson
                ).asJson
                System.err.println(s"Wrote failed test to ${testRunFile.toAbsolutePath.toString}")
                Files.write(testRunFile, failure.spaces2.getBytes(StandardCharsets.UTF_8))
              }
              Prop(prms => r)
            } else Prop(r) && nextCommand(runCommand(c, s), tail, previousCommands + 1)
          }
        }
      }

    nextCommand(initialState, allCommands.toList, 0).map { r =>
      destroyBrowser(b); r
    }
  }

  def statefulProp(shortName: String)(testCaseGen: Gen[Seq[Command]]) =
    property(shortName) = forAllNoShrink(testCaseGen)(tc => executeProp(shortName, tc, None))

  def applyCommands(s: State, commands: List[Command]): State = {
    commands.foldLeft(s)((s, c) => runCommand(c, s))
  }

  def generateCommands(f: State => Gen[List[Command]]): Gen[List[Command]] = {
    def gen(s: State, previous: List[Command]): Gen[List[Command]] = {
      if (previous.size > 500) sys.error("Generating too many commands: " + previous)
      f(s).flatMap { cl =>
        if (cl.isEmpty) Gen.const(previous) else gen(applyCommands(s, cl), previous ++ cl)
      }
    }
    gen(initialState, List.empty)
  }

  // for type inference
  def commandsWith[A](f: A)(pf: PartialFunction[A, Gen[List[Command]]]): Gen[List[Command]] = {
    pf.apply(f)
  }
}
