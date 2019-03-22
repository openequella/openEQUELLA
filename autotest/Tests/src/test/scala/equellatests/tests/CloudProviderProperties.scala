package equellatests.tests

import equellatests.domain.{Fairness, RandomWord, RandomWords, TestLogon}
import equellatests.instgen.workflow._
import equellatests.pages.cloudprovider.{
  CloudProviderListPage,
  TestCloudProviderDetails,
  TestCloudProviderPage
}
import equellatests.restapi.ERest
import equellatests.restapi.cloudprovider.{RCloudProviderForward, RCloudProviders}
import equellatests.{SimpleSeleniumBrowser, SimpleTestCase, StatefulProperties}
import integtester.IntegTester
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.scalacheck.{Gen, Prop}
import Prop._
import equellatests.tests.SanityTestProperties.Pages

object CloudProviderProperties extends StatefulProperties("Cloud Providers") with SimpleTestCase {

  object Scenarios extends Enumeration {
    val Add, Delete = Value
  }
  sealed trait CloudProviderTestCommand

  case class RegisterProvider(provider: TestCloudProviderDetails) extends CloudProviderTestCommand
  case class DeleteProvider(providerName: String)                 extends CloudProviderTestCommand

  case class ProviderTestState(registered: Set[TestCloudProviderDetails],
                               scenarios: Scenarios.ValueSet)

  type State   = ProviderTestState
  type Command = CloudProviderTestCommand

  implicit val testCaseEncoder: Encoder[CloudProviderTestCommand] = deriveEncoder
  implicit val testCaseDecoder: Decoder[CloudProviderTestCommand] = deriveDecoder

  val genCloudProvider = for {
    name     <- RandomWords.someWords
    descSize <- Gen.choose(0, 10)
    desc     <- Gen.listOfN(descSize, RandomWord.word)
  } yield
    TestCloudProviderDetails(name.asString,
                             Some(desc.map(_.word).mkString(" ")).filter(_.nonEmpty),
                             None)

  override def logon = tleAdminLogon

  def doAdd: Gen[List[CloudProviderTestCommand]] =
    for {
      nProviders <- Gen.choose(1, 5)
      providers <- Gen.listOfN(nProviders, for {
        sz <- Gen.choose(0, 10)
        p  <- Gen.resize(sz, genCloudProvider)
      } yield p)
    } yield providers.map(RegisterProvider)

  def genTestCommands(s: ProviderTestState): Gen[List[CloudProviderTestCommand]] = s match {
    case s if s.scenarios == Scenarios.values => List()
    case s =>
      for {
        command <- Fairness.favourIncomplete(1, 0)(Scenarios.values.toSeq, s.scenarios.contains)
        res <- command match {
          case Scenarios.Delete if s.registered.nonEmpty =>
            Gen.oneOf(s.registered.toSeq).map(p => List(DeleteProvider(p.name)))
          case _ => doAdd
        }
      } yield res

  }

  statefulProp("register a cloud provider") {
    generateCommands(genTestCommands)
  }

  override def initialState: ProviderTestState =
    ProviderTestState(registered = Set.empty, scenarios = Scenarios.ValueSet.empty)

  override def runCommand(c: CloudProviderTestCommand, s: ProviderTestState): ProviderTestState =
    c match {
      case RegisterProvider(provider) =>
        s.copy(scenarios = s.scenarios + Scenarios.Add, registered = s.registered + provider)
      case DeleteProvider(providerName) =>
        s.copy(scenarios = s.scenarios + Scenarios.Delete,
               registered = s.registered.filter(_.name != providerName))
    }

  override def runCommandInBrowser(c: CloudProviderTestCommand,
                                   s: ProviderTestState,
                                   b: SimpleSeleniumBrowser): Prop = c match {
    case RegisterProvider(provider) =>
      val actualProvider = provider.copy(name = s"${b.unique} ${provider.name}")
      val listPage       = CloudProviderListPage(b.page.ctx).load()
      val addPage        = listPage.add()
      val cpp            = TestCloudProviderPage(b.page.ctx, actualProvider)
      addPage.registerProvider(cpp.createRegistrationUrl())
      cpp.get()
      cpp.registerProvider()
      val cplp = cpp.returnToEQUELLA()
      cplp.waitForResults()
      val result      = cplp.resultForName(actualProvider.name)
      val description = Some(result.description()).filter(_.nonEmpty)
      Prop.?=(description, actualProvider.description).label("Description should match")
    case DeleteProvider(name) =>
      val listPage = CloudProviderListPage(b.page.ctx).load()
      val realName = s"${b.unique} ${name}"
      listPage.delete(realName)
      val existing = listPage.checkCloudProviderExisting(realName)
      Prop(!existing).label(name + " is deleted")

  }
}
