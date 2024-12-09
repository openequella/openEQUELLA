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

  val CloudProviderFirstLast = ("Cloud", "Provider")

  object Scenarios extends Enumeration {
    val Add, Delete = Value
  }
  sealed trait CloudProviderTestCommand

  case class RegisterProvider(provider: TestCloudProviderDetails) extends CloudProviderTestCommand
  case class DeleteProvider(providerName: String)                 extends CloudProviderTestCommand

  case class ProviderTestState(
      registered: Set[TestCloudProviderDetails],
      scenarios: Scenarios.ValueSet
  )

  type State   = ProviderTestState
  type Command = CloudProviderTestCommand

  implicit val testCaseEncoder: Encoder[CloudProviderTestCommand] = deriveEncoder
  implicit val testCaseDecoder: Decoder[CloudProviderTestCommand] = deriveDecoder

  val genCloudProvider = for {
    name     <- RandomWords.someWords
    descSize <- Gen.choose(0, 10)
    desc     <- Gen.listOfN(descSize, RandomWord.word)
  } yield TestCloudProviderDetails(
    name.asString,
    Some(desc.map(_.word).mkString(" ")).filter(_.nonEmpty),
    None
  )

  override def logon = tleAdminLogon

  def doAdd: Gen[List[CloudProviderTestCommand]] =
    for {
      nProviders <- Gen.choose(1, 3)
      providers <- Gen.listOfN(
        nProviders,
        for {
          sz <- Gen.choose(0, 10)
          p  <- Gen.resize(sz, genCloudProvider)
        } yield p
      )
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
        s.copy(
          scenarios = s.scenarios + Scenarios.Delete,
          registered = s.registered.filter(_.name != providerName)
        )
    }

  def loadProviderPage(b: SimpleSeleniumBrowser): CloudProviderListPage = {
    val listPage = b.page match {
      case lp: CloudProviderListPage => lp
      case _                         => CloudProviderListPage(b.page.ctx).load()
    }
    b.page = listPage
    listPage
  }

  override def runCommandInBrowser(
      c: CloudProviderTestCommand,
      s: ProviderTestState,
      b: SimpleSeleniumBrowser
  ): Prop = c match {
    case RegisterProvider(provider) =>
      val actualProvider   = provider.copy(name = s"${b.unique} ${provider.name}")
      var listPage         = loadProviderPage(b)
      val testProviderPage = TestCloudProviderPage(b.page.ctx, actualProvider)
      listPage.add(testProviderPage.createRegistrationUrl())
      testProviderPage.get()
      testProviderPage.registerProvider()
      testProviderPage.authenticateAsProvider()
      val firstName = testProviderPage.getFirstName
      val lastName  = testProviderPage.getLastName
      listPage = testProviderPage.returnToEQUELLA()
      listPage.waitForResults()
      val result      = listPage.resultForName(actualProvider.name)
      val description = result.description()
      Prop.all(
        ((
          firstName,
          lastName
        ) ?= CloudProviderFirstLast) :| "Should be able to authenticate as the cloud provider",
        (description ?= actualProvider.description) :| "Description should match"
      )

    case DeleteProvider(providerName) =>
      val listPage = loadProviderPage(b)
      val realName = s"${b.unique} $providerName"
      listPage.delete(realName)
      val existing = listPage.checkCloudProviderExisting(realName)
      Prop(!existing).label(providerName + " is deleted")

  }
}
