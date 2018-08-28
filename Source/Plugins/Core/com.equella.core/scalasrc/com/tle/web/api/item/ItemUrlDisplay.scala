package com.tle.web.api.item

import com.tle.legacy.LegacyGuice.urlService
import io.lemonlabs.uri.{AbsoluteUrl, RelativeUrl}

object ItemUrlDisplay {

  def addBaseUri(path: String): String = {
    val rurl = RelativeUrl.parse(path)
    AbsoluteUrl.parse(urlService.getBaseInstitutionURI.toString)
      .withPath(rurl.path)
      .withQueryString(rurl.query).toString()
  }
}
