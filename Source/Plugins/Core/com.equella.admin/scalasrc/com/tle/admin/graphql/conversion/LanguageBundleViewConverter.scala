/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.admin.graphql.conversion

import com.tle.beans.entity.{LanguageBundle, LanguageString}
import io.github.openequella.graphql.api.views.{LanguageBundleView, LanguageStringView}

object LanguageBundleViewConverter {
  def toLanguageBundle(view: LanguageBundleView): LanguageBundle = {
    val bundle = new LanguageBundle()
    bundle.setId(view.id)
    bundle.setStrings(buildStringMap(view.strings, bundle))
    bundle
  }

  private def buildStringMap(
      stringViews: List[LanguageStringView],
      bundle: LanguageBundle
  ): java.util.Map[String, LanguageString] =
    stringViews
      .map { stringView =>
        val languageString = new LanguageString()
        languageString.setId(stringView.id)
        languageString.setBundle(bundle)
        languageString.setLocale(stringView.locale)
        languageString.setPriority(stringView.priority)
        languageString.setText(stringView.text)

        stringView.locale -> languageString
      }
      .toMap
      .asHashMap

  def fromLanguageBundle(bundle: LanguageBundle): LanguageBundleView = {
    val strings = NullSafeMapValues(bundle.getStrings) convert fromLanguageString

    LanguageBundleView(
      id = bundle.getId,
      strings = strings
    )
  }

  private def fromLanguageString(languageString: LanguageString): LanguageStringView =
    LanguageStringView(
      id = languageString.getId,
      priority = languageString.getPriority,
      locale = languageString.getLocale,
      text = languageString.getText
    )
}
