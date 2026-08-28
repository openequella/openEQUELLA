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

package com.tle.core.settings.loginnotice.impl

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class PreLoginNoticeSanitiserTest extends AnyFunSpec with Matchers with TableDrivenPropertyChecks {

  private val INSTITUTION_BASE_URL = "https://myinst.example.com/institution/"

  private def sanitise(html: String): SanitisationResult =
    PreLoginNoticeSanitiser.sanitise(html, INSTITUTION_BASE_URL)

  describe("PreLoginNoticeSanitiser.sanitise") {

    describe("general behaviour") {
      it("returns an empty, unmodified result for null input") {
        val SanitisationResult(content, hadDisallowedContent) = sanitise(null)
        content shouldEqual ""
        hadDisallowedContent shouldBe false
      }

      it("removes script tags and reports the content as modified") {
        val input = "<div>test</div><script>alert(1)</script>"

        val SanitisationResult(content, hadDisallowedContent) = sanitise(input)
        content shouldEqual "<div>test</div>"
        hadDisallowedContent shouldBe true
      }

      it("removes event handler attributes but keeps the element, and reports modification") {
        val input    = """<div onclick="alert(1)">test</div>"""
        val expected = "<div>test</div>"

        val SanitisationResult(content, hadDisallowedContent) = sanitise(input)
        content shouldEqual expected
        hadDisallowedContent shouldBe true
      }

      it("strips id/name but keeps class, the element and content, and reports modification") {
        val input    = """<div id="foo" name="bar" class="baz">hello</div>"""
        val expected = """<div class="baz">hello</div>"""

        val SanitisationResult(content, hadDisallowedContent) = sanitise(input)
        content shouldEqual expected
        hadDisallowedContent shouldBe true
      }

      it("does not report modification for content that only has a class attribute") {
        val input = """<div class="baz">hello</div>"""

        val SanitisationResult(content, hadDisallowedContent) = sanitise(input)
        content shouldEqual input
        hadDisallowedContent shouldBe false
      }

      it(
        "does not report modification for already-clean content, despite cosmetic re-serialisation"
      ) {
        // Each of these is legitimately rewritten by the sanitiser without anything being
        // removed - rgb() spacing, mailto entity-encoding, implicit <tbody> insertion - so
        // hadDisallowedContent must stay false for all of them.
        val cleanInputs = Table(
          "html",
          "<p>clean</p>",
          """<span style="color: rgb(224, 62, 45);">rgb colour</span>""",
          """<a href="mailto:help@example.com">mail</a>""",
          "<table><tr><td>D</td></tr></table>"
        )

        forAll(cleanInputs) { html =>
          sanitise(html).hadDisallowedContent shouldBe false
        }
      }
    }

    describe("applyStandardRules") {
      it("allows common block and inline formatting elements but strips unknown tags") {
        // A bare <span> with no attributes is unwrapped to just its text content - this is an
        // OWASP HtmlPolicyBuilder default (a span with nothing on it is content-neutral), not
        // something our allowlist controls, so the span here carries a style attribute.
        val input =
          "<h1>H1</h1><h2>H2</h2><h3>H3</h3><h4>H4</h4><h5>H5</h5><h6>H6</h6>" +
            """<p>P</p><span style="color:red">Span</span><div>Div</div>""" +
            "<b>B</b><i>I</i><u>U</u><strong>Strong</strong><em>Em</em>" +
            "<strike>Strike</strike><sub>Sub</sub><sup>Sup</sup><code>x</code>"
        sanitise(input).content shouldEqual input
        sanitise("<blink>blink</blink>").content shouldEqual "blink"
      }

      it("allows lists but strips definition lists") {
        val input = "<ul><li>one</li></ul><ol><li>two</li></ol>"
        sanitise(input).content shouldEqual input

        val definitionList = "<dl><dt>term</dt><dd>desc</dd></dl>"
        sanitise(definitionList).content shouldEqual "termdesc"
      }

      it("removes disallowed CSS properties but keeps allowed ones") {
        // position is not in CssSchema.DEFAULT's whitelist; width is.
        val input = """<p style="position:fixed;width:100%;color:red">x</p>"""
        sanitise(input).content shouldEqual """<p style="width:100%;color:red">x</p>"""
      }

      it("does not drop rgb()/rgba() function colours") {
        // Regression guard: CssSchema.DEFAULT includes the rgb()/rgba()/hsl()/hsla() pseudo-keys,
        // so colours expressed this way survive - some OWASP schemas drop the whole element (not
        // just the style attribute) when a colour function's key isn't allowlisted.
        val input =
          """<span style="color: rgb(224, 62, 45); background-color: rgb(255,255,0);">rgb colour</span>"""
        val expected =
          """<span style="color:rgb( 224 , 62 , 45 );background-color:rgb( 255 , 255 , 0 )">rgb colour</span>"""
        sanitise(input).content shouldEqual expected
      }

      it("allows the direction style property but strips the dir attribute") {
        val input    = """<p dir="rtl" style="direction:rtl">Arabic text</p>"""
        val expected = """<p style="direction:rtl">Arabic text</p>"""
        sanitise(input).content shouldEqual expected
      }
    }

    describe("allowAdditionalElements") {
      it("allows hr and pre, which aren't covered by the common element policies") {
        val input = "<p>a</p><hr /><pre>b</pre>"
        sanitise(input).content shouldEqual input
      }
    }

    describe("allowLinks") {
      it("strips a javascript: scheme href but keeps the link text, and reports modification") {
        val input = """<a href="javascript:alert(1)">click</a>"""

        val SanitisationResult(content, hadDisallowedContent) = sanitise(input)
        content shouldEqual "click"
        hadDisallowedContent shouldBe true
      }

      it("allows a mailto: href without reporting modification") {
        val input    = """<a href="mailto:help@example.com">mail</a>"""
        val expected = """<a href="mailto:help&#64;example.com">mail</a>"""

        val SanitisationResult(content, hadDisallowedContent) = sanitise(input)
        content shouldEqual expected
        hadDisallowedContent shouldBe false
      }
    }

    describe("allowImages") {
      it(
        "keeps an img whose src matches the trusted prefix, with its alt/width/height, unmodified"
      ) {
        val imageUrl = INSTITUTION_BASE_URL + "api/preloginnotice/image/a.png"
        val input    = s"""<img src="$imageUrl" alt="banner" width="300" height="150" />"""

        val SanitisationResult(content, hadDisallowedContent) = sanitise(input)
        content shouldEqual input
        hadDisallowedContent shouldBe false
      }

      it("removes an img whose src is from an untrusted origin, and reports modification") {
        val input = """<div><img src="https://evil.example.com/institution/a.png">image</div>"""

        val SanitisationResult(content, hadDisallowedContent) = sanitise(input)
        content shouldEqual "<div>image</div>"
        hadDisallowedContent shouldBe true
      }

      it("keeps a root-relative img src that resolves under the institution, unmodified") {
        val input =
          """<img src="/institution/file/99234d7f-bb5b-4343-81ec-6524153234cc/1/cat.png" />"""

        val SanitisationResult(content, hadDisallowedContent) = sanitise(input)
        content shouldEqual input
        hadDisallowedContent shouldBe false
      }

      it("removes a relative img src that resolves outside the institution via path traversal") {
        val input = """<img src="../../evil/leak.png">"""

        val SanitisationResult(content, hadDisallowedContent) = sanitise(input)
        content shouldEqual ""
        hadDisallowedContent shouldBe true
      }
    }

    describe("allowTables") {
      it("allows table structure but strips the caption") {
        val input =
          "<table><thead><tr><th>H</th></tr></thead>" +
            "<tbody><tr><td>D</td></tr></tbody>" +
            "<tfoot><tr><td>F</td></tr></tfoot></table>"
        sanitise(input).content shouldEqual input

        // <caption> is dropped (its text survives, unwrapped) and a bare <tr> with no explicit
        // <tbody> gets one added implicitly by the sanitiser's output serialiser.
        val withCaption = "<table><caption>Cap</caption><tr><td>D</td></tr></table>"
        val expected    = "<table>Cap<tbody><tr><td>D</td></tr></tbody></table>"

        val SanitisationResult(content, hadDisallowedContent) = sanitise(withCaption)
        content shouldEqual expected
        hadDisallowedContent shouldBe true
      }
    }

    describe("hadDisallowedContent") {
      it(
        "does not detect a disallowed CSS property/value stripped alongside an allowed one in the same style attribute"
      ) {
        // Documented blind spot (see the sanitise scaladoc): OWASP's HtmlChangeListener only
        // fires once a style attribute is emptied entirely, not when part of it is rewritten.
        // The output is still safe either way - `position` and the malicious `url()` are both
        // gone from `content` - but hadDisallowedContent can't see it happened.
        sanitise(
          """<p style="position:fixed;color:red">x</p>"""
        ).hadDisallowedContent shouldBe false
        sanitise(
          """<p style="background-image:url(https://evil.example.com/leak.png);color:blue">x</p>"""
        ).hadDisallowedContent shouldBe false

        // When a style attribute is disallowed *entirely*, that IS detected.
        sanitise("""<p style="position:fixed">x</p>""").hadDisallowedContent shouldBe true
      }
    }
  }
}
