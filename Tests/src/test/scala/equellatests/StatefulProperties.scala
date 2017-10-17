package equellatests

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.UUID

import com.tle.webtests.framework.{PageContext, ScreenshotTaker, TestConfig}
import equellatests.domain._
import equellatests.pages.{BrowserPage, HomePage, LoginPage}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import org.openqa.selenium.WebDriver
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Prop, Properties}

import scala.util.Try

trait SeleniumBrowser {
  def page: BrowserPage
}

object SimpleSeleniumBrowser {
  val wrongPageProp = Prop.falsified.label("Expected to be on a different page")
  val wrongState: BrowserPage => (BrowserPage, Prop) =
    _ -> wrongPageProp
}

case class SimpleSeleniumBrowser(var page: BrowserPage) extends SeleniumBrowser {

  val unique: String = UUID.randomUUID().toString

  def uniquePrefix(s: String) = s"$unique $s"

  def run(action: => BrowserPage): Prop = withTry(Try(action).map((_, Prop.proved)))

  def runOnPage(action: PartialFunction[BrowserPage, BrowserPage]): Prop = verifyOnPage(action.andThen((_, Prop.proved)))

  def withTry(tried: Try[(BrowserPage, Prop)]): Prop = tried.fold(t => Prop.exception(t), { p => page = p._1; p._2 })

  def verify(action: => (BrowserPage, Prop)): Prop = withTry(Try(action))

  def verifyOnPage(action: PartialFunction[BrowserPage, (BrowserPage, Prop)]): Prop =
    withTry(Try(action.applyOrElse(page, SimpleSeleniumBrowser.wrongState)))

  def verifyOnPageAndState[S](s: S)(action: PartialFunction[(S, BrowserPage), (BrowserPage, Prop)]): Prop =
    withTry(Try(action.applyOrElse(s -> page, (p: (S, BrowserPage)) => p._2 -> SimpleSeleniumBrowser.wrongPageProp)))

}

trait LogonTestCase {
  def logon: TestLogon

  type Browser = SimpleSeleniumBrowser

  def createInital = SimpleSeleniumBrowser

  def createBrowser: Browser = {
    val testConfig = new TestConfig(GlobalConfig.baseFolderForInst(logon.inst), false)
    val driver = TestChecker.withBrowserDriver[WebDriver](testConfig)(identity)
    val context = new PageContext(driver, testConfig, testConfig.getInstitutionUrl)
    Try(createInital(new LoginPage(context).load().login(logon.username, logon.password))).fold({ t =>
      driver.quit()
      throw t
    }, identity)
  }

  def destroyBrowser(sut: Browser): Unit = {
    sut.page.driver.quit()
  }

}

case class FailedTestCase(shortName: String, propertiesClass: String, testCase: Json)

object FailedTestCase {
  implicit val ftcEnc: Encoder[FailedTestCase] = deriveEncoder
  implicit val ftcDec: Decoder[FailedTestCase] = deriveDecoder
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

  def executeProp(shortName: String, allCommands: Seq[Command], replaying: Boolean): Prop = {
    val b = createBrowser

    def nextCommand(s: State, commands: List[Command]): Prop = commands match {
      case Nil => Prop.proved
      case c :: tail => {
        if (replaying) System.err.println(c.toString)
        runCommandInBrowser(c, s, b).flatMap { r =>
          if (!r.success) {
            val tc = b.page.ctx.getTestConfig
            val filename = name + "_" + shortName.replace(' ', '_')
            Try(ScreenshotTaker.takeScreenshot(b.page.driver, tc.getScreenshotFolder, filename, tc.isChromeDriverSet))
            if (!replaying) {
              val testRunFile = tc.getResultsFolder.toPath.resolve(filename + "_test.json")
              val failure = FailedTestCase(shortName, getClass.getName, allCommands.asJson).asJson
              System.err.println(s"Wrote failed test to ${testRunFile.toAbsolutePath.toString}")
              Files.write(testRunFile, failure.spaces2.getBytes(StandardCharsets.UTF_8))
            }
            Prop(prms => r)
          }
          else Prop(r) && nextCommand(runCommand(c, s), tail)
        }
      }
    }

    nextCommand(initialState, allCommands.toList).map { r => destroyBrowser(b); r }
  }

  def statefulProp(shortName: String)(testCaseGen: Gen[Seq[Command]]) =
    property(shortName) = forAllNoShrink(testCaseGen)(tc => executeProp(shortName, tc, false))

  def applyCommands(s: State, commands: List[Command]): State = {
    commands.foldLeft(s)((s, c) => runCommand(c, s))
  }

  def generateCommands(f: State => Gen[List[Command]]): Gen[List[Command]] = {
    def gen(s: State) : Gen[List[Command]] = {
      f(s).flatMap {
        cl => if (cl.isEmpty) Gen.const(Nil) else gen(applyCommands(s, cl)).map(nl => cl ++ nl)
      }
    }
    gen(initialState)
  }
}