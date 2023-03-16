package com.tle.web.settings

import com.tle.common.institution.CurrentInstitution
import com.tle.legacy.LegacyGuice

object AnalyticsSettings {
  private val AnalyticsPropName = "GOOGLE_ANALYTICS"

  def getAnalyticsId: String = {
    Option(CurrentInstitution.get()) match {
      case Some(_) =>
        LegacyGuice.configService.getProperty(AnalyticsPropName)
      case None => ""
    }
  }
}
