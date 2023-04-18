package com.tle.integration.lti13

import com.tle.core.guice.Bind
import com.tle.legacy.LegacyGuice
import com.tle.web.integration.service.IntegrationService
import com.tle.web.integration.{
  AbstractIntegrationService,
  IntegrationSessionData,
  SingleSignonForm
}
import com.tle.web.sections.equella.AbstractScalaSection
import com.tle.web.sections.{SectionInfo, SectionNode}
import com.tle.web.sections.generic.DefaultSectionTree
import com.tle.web.selection.SelectionSession
import scala.jdk.CollectionConverters._
import javax.inject.{Inject, Singleton}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

class Lti13IntegrationSessionData extends IntegrationSessionData {
  var deepLinkingSettings: DeepLinkingSettings = _
  override def isForSelection: Boolean         = true
  override def getIntegrationType: String      = "lti13"
}

object Lti13IntegrationSessionData {
  def apply(deepLinkingSettings: DeepLinkingSettings): Lti13IntegrationSessionData = {
    val data = new Lti13IntegrationSessionData
    data.deepLinkingSettings = deepLinkingSettings
    data
  }
}

@Bind
@Singleton
class Lti13IntegrationService extends AbstractIntegrationService[Lti13IntegrationSessionData] {
  @Inject private var integrationService: IntegrationService = _

  override protected def canSelect(data: Lti13IntegrationSessionData): Boolean = data.isForSelection

  override protected def getIntegrationType = "lti13"

  override def createDataForViewing(info: SectionInfo): Lti13IntegrationSessionData =
    new Lti13IntegrationSessionData

  override def getClose(data: Lti13IntegrationSessionData): String = ???

  override def getCourseInfoCode(data: Lti13IntegrationSessionData): String = ???

  override def select(info: SectionInfo,
                      data: Lti13IntegrationSessionData,
                      session: SelectionSession): Boolean = ???

  // todo: Update implementation to configure Selection Session based on deep linking settings
  override def setupSelectionSession(info: SectionInfo,
                                     data: Lti13IntegrationSessionData,
                                     session: SelectionSession,
                                     model: SingleSignonForm): SelectionSession =
    super.setupSelectionSession(info, data, session, model)

  /**
    * Launch Selection Session for LTI 1.3. This is achieved by
    * 1. Build a temporary SectionInfo;
    * 2. Use LTI deep linking request details to build an IntegrationData and IntegrationActionInfo.
    * 3. Use `IntegrationService#standardForward` to navigate the page to Selection Session.
    *
    * @param deepLinkingRequest Deep linking request details
    * @param req HTTP Servlet request to be used to build a SectionInfo.
    * @param resp HTTP Servlet response to be used to build a SectionInfo.
    */
  def launchSelectionSession(deepLinkingRequest: LtiDeepLinkingRequest,
                             req: HttpServletRequest,
                             resp: HttpServletResponse): Unit = {

    def buildSectionInfo = {
      val blankTree =
        new DefaultSectionTree(
          LegacyGuice.treeRegistry,
          new SectionNode(
            "LTI13SelectionSession",
            new AbstractScalaSection {
              // We are not really using any Section so it's fine to use Unit as the model type.
              override type M = Unit
              override def newModel: SectionInfo => Unit = _ => ()
            }
          )
        )

      val info = LegacyGuice.sectionsController.createInfo(blankTree,
                                                           "/",
                                                           req,
                                                           resp,
                                                           null,
                                                           Map.empty[String, Array[String]].asJava,
                                                           null)

      info.fireBeforeEvents()
      info
    }

    // Use `selectOrAdd` as the default Selection Session display mode.
    val action =
      deepLinkingRequest.customParams.flatMap(_.get("selectionMode")).getOrElse("selectOrAdd")
    val integrationData = Lti13IntegrationSessionData(deepLinkingRequest.deepLinkingSettings)

    integrationService.standardForward(buildSectionInfo,
                                       "",
                                       integrationData,
                                       integrationService.getActionInfo(action, null),
                                       new SingleSignonForm)
  }
}
