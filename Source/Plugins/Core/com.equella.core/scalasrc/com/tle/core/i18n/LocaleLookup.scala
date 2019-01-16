/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.i18n

import com.dytech.edge.web.WebConstants
import com.tle.common.i18n.LocaleData
import com.tle.legacy.LegacyGuice

object LocaleLookup {

  def selectLocale: LocaleData = {
    val ss = LegacyGuice.userSessionService
    val sessionAvailable = ss.isSessionAvailable
    val sessionLocale =
      if (sessionAvailable)
        Option(ss.getAttribute[LocaleData](WebConstants.KEY_LOCALE))
      else None
    sessionLocale.getOrElse {
      val request = ss.getAssociatedRequest
      val preferredLocale =
        LegacyGuice.userPreferenceService.getPreferredLocale(request)
      val locale = new LocaleData(
        preferredLocale,
        LegacyGuice.languageService.isRightToLeft(preferredLocale))
      if (sessionAvailable) ss.setAttribute(WebConstants.KEY_LOCALE, locale)
      locale
    }
  }
}
