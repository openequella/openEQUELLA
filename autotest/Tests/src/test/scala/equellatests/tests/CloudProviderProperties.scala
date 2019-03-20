package equellatests.tests

import equellatests.domain.{RandomWord, RandomWords, TestLogon}
import equellatests.instgen.workflow.workflowInst
import equellatests.pages.cloudprovider.{TestCloudProviderDetails, TestCloudProviderPage}
import equellatests.restapi.ERest
import equellatests.restapi.cloudprovider.{RCloudProviderForward, RCloudProviders}
import equellatests.{SimpleSeleniumBrowser, SimpleTestCase, StatefulProperties}
import integtester.IntegTester
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.scalacheck.{Gen, Prop}

object CloudProviderProperties extends StatefulProperties("Cloud Providers") with SimpleTestCase {

  case class RegisterProvider(provider: TestCloudProviderDetails)

  case class ProviderTestState(registered: Set[TestCloudProviderDetails])

  type State   = ProviderTestState
  type Command = RegisterProvider

  implicit val testCaseEncoder: Encoder[RegisterProvider] = deriveEncoder
  implicit val testCaseDecoder: Decoder[RegisterProvider] = deriveDecoder

  val genCloudProvider = for {
    name <- RandomWords.someWords
    desc <- Gen.listOf(RandomWord.arbWord)
  } yield TestCloudProviderDetails(name.asString, Some(desc.mkString(" ")).filter(_.nonEmpty), None)

  override def logon = TestLogon("TLE_ADMINISTRATOR", workflowInst.systemPassword, workflowInst)

  def genTestCommands(s: ProviderTestState): Gen[List[RegisterProvider]] = {
    if (s.registered.isEmpty) for {
      provider <- genCloudProvider
    } yield List(RegisterProvider(provider))
    else Gen.const(List())
  }

  statefulProp("register a cloud provider") {
    generateCommands(genTestCommands)
  }

  override def initialState: ProviderTestState = ProviderTestState(registered = Set.empty)

  override def runCommand(c: RegisterProvider, s: ProviderTestState): ProviderTestState = c match {
    case RegisterProvider(provider) => s.copy(registered = s.registered + provider)
  }

  override def runCommandInBrowser(c: RegisterProvider,
                                   s: ProviderTestState,
                                   b: SimpleSeleniumBrowser): Prop = c match {
    case RegisterProvider(provider) =>
      ERest.run(b.page.ctx) {
        RCloudProviders.initCallback(IntegTester.providerRegistrationUrl).map {
          case RCloudProviderForward(url) =>
            val cpp = TestCloudProviderPage(b.page.ctx, url, provider)
            cpp.load()
            cpp.registerProvider()
            cpp.returnToEQUELLA()
            Prop(true)
        }
      }
  }
}
