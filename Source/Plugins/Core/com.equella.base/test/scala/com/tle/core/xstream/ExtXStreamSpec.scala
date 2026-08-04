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

package com.tle.core.xstream

import com.dytech.edge.wizard.beans.DefaultWizardPage
import com.dytech.edge.wizard.beans.control.Html
import com.tle.beans.entity.itemdef.SummarySectionsConfig
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.sql.Timestamp
import java.util.Collections
import scala.io.Source
import scala.jdk.CollectionConverters._
import scala.util.Using

/** Locks down the wire format of oEQ's persisted XML.
  *
  * The fixtures under `test/resources/xstream` were captured from a running instance, so the
  * byte-for-byte assertions below are the compatibility contract with every wizard blob, summary
  * display template and institution export already out in the wild.
  *
  * These tests run in the plain sbt JVM, which passes no `--add-opens`, so they also demonstrate
  * that the legacy singleton-map format needs no privileged access to `java.base` to read or write.
  *
  * That is a claim about the formats covered here, not about every blob oEQ can hold. DRM pages are
  * the known exception: `DRMPage.NetworkSet` extends `TreeSet` and so serialises through Java
  * custom serialization, whose XStream emulation reflects into `java.util.TreeSet` and does require
  * `--add-opens=java.base/java.util=ALL-UNNAMED`. Every JVM that runs this code in anger supplies
  * it — the server config, the Admin Console launcher and `adminTool/run` — but this test JVM does
  * not, which is why there is no DRM fixture here. The anonymous-comparator fixture below covers
  * only the type-permission half of that case.
  */
class ExtXStreamSpec extends AnyFunSpec with Matchers {

  private val Locale                   = "en"
  private val HtmlText                 = "<hr>"
  private val PageTitleText            = "Page 1"
  private val AnonymousComparatorClass =
    "com.dytech.edge.wizard.beans.DRMPage$NetworkSet$1"

  private val singletonMapNullValueXml =
    """<singleton-map>
      |  <k class="string">en</k>
      |</singleton-map>""".stripMargin

  private val singletonMapNullKeyXml =
    """<singleton-map>
      |  <v class="string">text</v>
      |</singleton-map>""".stripMargin

  private def xstream = new ExtXStream(null)

  private def fixture(name: String): String =
    Using.resource(Source.fromInputStream(getClass.getResourceAsStream(s"/xstream/$name")))(
      _.mkString.trim
    )

  private def firstPage(pagesXml: String): DefaultWizardPage =
    xstream
      .fromXML(pagesXml)
      .asInstanceOf[java.util.List[DefaultWizardPage]]
      .asScala
      .head

  private def firstControl(pagesXml: String): Html =
    firstPage(pagesXml).getControls.asScala.head.asInstanceOf[Html]

  describe("ExtXStream and the legacy singleton-map format") {

    it("reads language bundles stored as an immutable singleton map") {
      val control = firstControl(fixture("wizard-pages-singleton-map.xml"))

      control.getDescription.getStrings.get(Locale).getText shouldBe HtmlText
    }

    it("reads a page title stored as an immutable singleton map") {
      val page = firstPage(fixture("wizard-pages-singleton-map.xml"))

      page.getTitle.getStrings.get(Locale).getText shouldBe PageTitleText
    }

    it("resolves the back reference from a language string to its bundle") {
      val bundle = firstControl(fixture("wizard-pages-singleton-map.xml")).getDescription

      bundle.getStrings.get(Locale).getBundle should be theSameInstanceAs bundle
    }

    it("writes the format back byte for byte") {
      val pagesXml = fixture("wizard-pages-singleton-map.xml")

      xstream.toXML(xstream.fromXML(pagesXml)) shouldBe pagesXml
    }

    it("omits a null value rather than writing an empty element") {
      val map = Collections.singletonMap(Locale, null)

      xstream.toXML(map) shouldBe singletonMapNullValueXml
    }

    it("reads a singleton map whose value was omitted") {
      xstream.fromXML(singletonMapNullValueXml) shouldBe Collections.singletonMap(Locale, null)
    }

    it("reads a singleton map whose key was omitted") {
      xstream.fromXML(singletonMapNullKeyXml) shouldBe Collections.singletonMap(null, "text")
    }
  }

  describe("ExtXStream permissions") {

    it("allows the anonymous comparator that DRM pages serialise") {
      val comparator = xstream.fromXML(fixture("anonymous-comparator.xml"))

      comparator.getClass.getName shouldBe AnonymousComparatorClass
    }
  }

  describe("ExtXStream sql timestamps") {

    it("round trips a timestamp without shifting it between time zones") {
      val timestamp = Timestamp.valueOf("2020-03-04 05:06:07.0")

      val xml = xstream.toXML(timestamp)

      xml shouldBe "<sql-timestamp>2020-03-04 05:06:07.0</sql-timestamp>"
      xstream.fromXML(xml) shouldBe timestamp
    }
  }

  describe("ExtXStream annotation handling") {

    it("omits fields annotated with XStreamOmitField") {
      val config = new SummarySectionsConfig("basic")
      config.setTitle("Basic Information")

      xstream.toXML(config) should not include "<title>"
    }
  }
}
