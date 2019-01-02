/*
 * Copyright 2019 Apereo
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

package com.tle.web.api.item

import com.tle.legacy.LegacyGuice.urlService
import io.lemonlabs.uri.{AbsoluteUrl, RelativeUrl, Url}

object ItemUrlDisplay {

  def addBaseUri(path: String): String = {
    Url.parse(path) match {
      case rurl: RelativeUrl => AbsoluteUrl.parse(urlService.getBaseInstitutionURI.toString)
        .withPath(rurl.path)
        .withQueryString(rurl.query).toString()
      case url => url.toString()
    }
  }
}
