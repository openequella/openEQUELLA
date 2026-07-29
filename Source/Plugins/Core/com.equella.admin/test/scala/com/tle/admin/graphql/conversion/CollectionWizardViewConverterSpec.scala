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

import com.dytech.edge.wizard.beans.DefaultWizardPage
import com.dytech.edge.wizard.beans.control.Html
import io.github.openequella.graphql.api.views.CollectionWizardView
import org.scalatest.OptionValues
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._

/** Covers the wizard XML blobs exchanged with the server.
  *
  * The `pages` fixtures below are in the format the server's `ExtXStream` writes, which is what the
  * Admin Console actually receives. That format is not what a stock XStream instance produces, so
  * these tests fail unless both ends share the same XStream configuration.
  */
class CollectionWizardViewConverterSpec extends AnyFunSpec with Matchers with OptionValues {

  private val WizardName    = "Contribution wizard"
  private val HtmlText      = "<hr>"
  private val PageTitleText = "Page 1"
  private val Locale        = "en"

  /** A page holding a Raw HTML control whose description is a single-locale language bundle backed
    * by an immutable `Collections.singletonMap` — as produced by the predefined-element option of
    * the Raw HTML control editor, and as held by legacy collections such as `Books`.
    */
  private val singletonMapPagesXml =
    s"""<list>
       |  <com.dytech.edge.wizard.beans.DefaultWizardPage>
       |    <title>
       |      <id>0</id>
       |      <strings class="singleton-map">
       |        <k class="string">$Locale</k>
       |        <v class="com.tle.beans.entity.LanguageString">
       |          <id>0</id>
       |          <locale>$Locale</locale>
       |          <priority>1</priority>
       |          <text>$PageTitleText</text>
       |          <bundle reference="../../.."/>
       |        </v>
       |      </strings>
       |    </title>
       |    <controls>
       |      <com.dytech.edge.wizard.beans.control.Html>
       |        <include>true</include>
       |        <description>
       |          <id>0</id>
       |          <strings class="singleton-map">
       |            <k class="string">$Locale</k>
       |            <v class="com.tle.beans.entity.LanguageString">
       |              <id>0</id>
       |              <locale>$Locale</locale>
       |              <priority>1</priority>
       |              <text>&lt;hr&gt;</text>
       |              <bundle reference="../../.."/>
       |            </v>
       |          </strings>
       |        </description>
       |        <targetnodes/>
       |        <items/>
       |      </com.dytech.edge.wizard.beans.control.Html>
       |    </controls>
       |  </com.dytech.edge.wizard.beans.DefaultWizardPage>
       |</list>""".stripMargin

  private def viewWithPages(pagesXml: String): CollectionWizardView =
    CollectionWizardView(
      name = Some(WizardName),
      redraftScript = None,
      saveScript = None,
      allowNonSequentialNavigation = false,
      showPageTitlesNextPrev = false,
      additionalCssClass = None,
      pages = Some(pagesXml),
      fixedMetadata = None
    )

  private def firstHtmlControl(view: CollectionWizardView): Html =
    CollectionWizardViewConverter
      .toWizard(view)
      .getPages
      .asScala
      .head
      .asInstanceOf[DefaultWizardPage]
      .getControls
      .asScala
      .head
      .asInstanceOf[Html]

  private def textOf(bundle: com.tle.beans.entity.LanguageBundle): String =
    bundle.getStrings.get(Locale).getText

  describe("CollectionWizardViewConverter.toWizard") {

    it("reads a control description held in a legacy singleton-map language bundle") {
      val control = firstHtmlControl(viewWithPages(singletonMapPagesXml))

      textOf(control.getDescription) shouldBe HtmlText
    }

    it("reads a page title held in a legacy singleton-map language bundle") {
      val page = CollectionWizardViewConverter
        .toWizard(viewWithPages(singletonMapPagesXml))
        .getPages
        .asScala
        .head
        .asInstanceOf[DefaultWizardPage]

      textOf(page.getTitle) shouldBe PageTitleText
    }
  }

  describe("CollectionWizardViewConverter round trip") {

    it("writes pages the server can read back, preserving the singleton-map format") {
      val wizard = CollectionWizardViewConverter.toWizard(viewWithPages(singletonMapPagesXml))

      val roundTripped = CollectionWizardViewConverter.fromWizard(wizard)

      roundTripped.pages.value should include("""<strings class="singleton-map">""")
      textOf(firstHtmlControl(roundTripped).getDescription) shouldBe HtmlText
    }
  }
}
