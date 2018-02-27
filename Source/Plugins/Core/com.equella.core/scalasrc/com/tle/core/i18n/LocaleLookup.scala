package com.tle.core.i18n

import com.dytech.edge.web.WebConstants
import com.tle.common.i18n.LocaleData
import com.tle.legacy.LegacyGuice

object LocaleLookup {

  def selectLocale : LocaleData = {
    val ss = LegacyGuice.userSessionService
    val sessionAvailable = ss.isSessionAvailable
    val sessionLocale = if (sessionAvailable) Option(ss.getAttribute[LocaleData](WebConstants.KEY_LOCALE)) else None
    sessionLocale.getOrElse {
      val request = ss.getAssociatedRequest
      val preferredLocale = LegacyGuice.userPreferenceService.getPreferredLocale(request)
      val locale = new LocaleData(preferredLocale, LegacyGuice.languageService.isRightToLeft(preferredLocale))
      if (sessionAvailable) ss.setAttribute(WebConstants.KEY_LOCALE, locale)
      locale
    }
  }
}
