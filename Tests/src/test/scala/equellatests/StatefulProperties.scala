package equellatests

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import com.tle.webtests.framework.{PageContext, ScreenshotTaker, TestConfig}
import equellatests.TestCase.{CommandT, TestCaseT}
import equellatests.domain._
import equellatests.pages.{BrowserPage, HomePage, LoginPage}
import equellatests.tests.StateAndCommands
import io.circe.{Decoder, Encoder, Json}
import io.circe.parser.parse
import io.circe.syntax._
import io.circe.generic.semiauto._
import org.openqa.selenium.WebDriver
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Prop, Properties}

import scala.util.Try

trait SeleniumBrowser
{
  def page: BrowserPage
}

case class SimpleSeleniumBrowser(var page: BrowserPage) extends SeleniumBrowser

trait TestCase
{
  type State
  type Browser <: SeleniumBrowser
  def initialBrowser: Browser
  def destroyBrowser(b: Browser)
  def initialState: State
  def commands: List[CommandT[State, Browser]]
}

object TestCase {
  type TestCaseT[S2, B2] = TestCase {
    type State = S2
    type Browser = B2
  }
  type CommandT[S2, B2] = Command {
    type State = S2
    type Browser = B2
  }
}

trait LogonTestCase extends TestCase {
  def logon: TestLogon
  type Browser <: SeleniumBrowser
  def createInital: HomePage => Browser

  def initialBrowser: Browser = {
    val testConfig = new TestConfig(GlobalConfig.baseFolderForInst(logon.inst), false)
    val driver = TestChecker.withBrowserDriver[WebDriver](testConfig)(identity)
    val context = new PageContext(driver, testConfig, testConfig.getInstitutionUrl)
    createInital(new LoginPage(context).load().login(logon.username, logon.password))
  }

  def destroyBrowser(sut: Browser): Unit = {
    sut.page.driver.quit()
  }

}

trait Command {
  type State
  type Browser
  def dumpFilename: String = getClass.toString
  def runCommand(b: Browser, state: State): Prop
  def nextState(state: State): State
}

trait UnitCommand extends Command
{
  def run(b: Browser, state: State): Unit
  def runCommand(b: Browser, state: State): Prop = {
    Try(run(b, state)).fold(t => Prop.exception(t), _ => true)
  }
}

trait VerifyCommand extends Command {
  type BrowserResult

  def postCondition(state: State, result: BrowserResult): Prop
  def run(sut: Browser, state: State): BrowserResult
  def runCommand(b: Browser, state: State): Prop = {
    Try(run(b, state)).fold(t => Prop.exception(t), br => postCondition(state, br))
  }
}

case class FailedTestCase(propertiesClass: String, testCase: Json)
object FailedTestCase {
  implicit val ftcEnc : Encoder[FailedTestCase] = deriveEncoder
  implicit val ftcDec : Decoder[FailedTestCase] = deriveDecoder
}

abstract class StatefulProperties(name: String) extends Properties(name: String) {
  type TC <: TestCase

  implicit val testCaseDecoder : Decoder[TC]
  implicit val testCaseEncoder : Encoder[TC]


  def executeProp(testCase: TC, writeCase: Boolean): Prop = {
    val b = testCase.initialBrowser

    def nextCommand(s: testCase.State, commands: List[CommandT[testCase.State, testCase.Browser]]): Prop = commands match {
      case Nil => Prop.proved
      case c :: tail => {
        c.runCommand(b, s).flatMap { r =>
          if (!r.success) {
            val tc = b.page.ctx.getTestConfig
            val filename = name+"_"+c.dumpFilename
            Try(ScreenshotTaker.takeScreenshot(b.page.driver, tc.getScreenshotFolder, filename , tc.isChromeDriverSet))
            if (writeCase) {
              val testRunFile = tc.getResultsFolder.toPath.resolve(filename + "_test.json")
              val failure = FailedTestCase(getClass.getName, testCase.asJson).asJson
              System.err.println(s"Wrote failed test to ${testRunFile.toAbsolutePath.toString}")
              Files.write(testRunFile, failure.spaces2.getBytes(StandardCharsets.UTF_8))
            }
            Prop(prms => r)
          }
          else nextCommand(c.nextState(s), tail)
        }
      }
    }
    nextCommand(testCase.initialState, testCase.commands).map { r => testCase.destroyBrowser(b); r }
  }

  def statefulProp(testCaseGen: Gen[TC]): Prop = forAllNoShrink(testCaseGen)(tc => executeProp(tc, true))

}