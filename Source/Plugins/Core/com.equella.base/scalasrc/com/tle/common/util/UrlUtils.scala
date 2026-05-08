package com.tle.common.util

import io.lemonlabs.uri.Url
import com.dytech.edge.web.WebConstants.{HTTP, HTTPS}

object UrlUtils {

  /** Safely checks if a given string is a valid, absolute HTTP or HTTPS URL.
    *
    * To return true, the URL string must:
    *   1. Be successfully parsed without errors.
    *   2. Have a scheme of either "http" or "https".
    *   3. Contain a defined host (e.g., "example.com").
    *
    * @param url
    *   The URL string to validate.
    * @return
    *   True if the string meets all absolute HTTP/HTTPS criteria, false otherwise.
    */
  def isAbsoluteHttpUrl(url: String): Boolean =
    Url
      .parseTry(url)
      .toOption
      .exists(u =>
        u.schemeOption.exists(Set(HTTP, HTTPS)) &&
          u.hostOption.isDefined
      )
}
