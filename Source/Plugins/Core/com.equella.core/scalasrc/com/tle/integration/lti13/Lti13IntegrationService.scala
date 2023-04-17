package com.tle.integration.lti13

import com.tle.core.guice.Bind
import com.tle.web.integration.{
  AbstractIntegrationService,
  IntegrationSessionData,
  SingleSignonForm
}
import com.tle.web.sections.SectionInfo
import com.tle.web.selection.SelectionSession
import javax.inject.Singleton

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
  override protected def canSelect(data: Lti13IntegrationSessionData): Boolean = data.isForSelection

  override protected def getIntegrationType = "lti13"

  override def createDataForViewing(info: SectionInfo): Lti13IntegrationSessionData =
    new Lti13IntegrationSessionData

  override def getClose(data: Lti13IntegrationSessionData): String = ???

  override def getCourseInfoCode(data: Lti13IntegrationSessionData): String = ???

  override def select(info: SectionInfo,
                      data: Lti13IntegrationSessionData,
                      session: SelectionSession): Boolean = ???

  override def setupSelectionSession(info: SectionInfo,
                                     data: Lti13IntegrationSessionData,
                                     session: SelectionSession,
                                     model: SingleSignonForm): SelectionSession =
    super.setupSelectionSession(info, data, session, model)
}
