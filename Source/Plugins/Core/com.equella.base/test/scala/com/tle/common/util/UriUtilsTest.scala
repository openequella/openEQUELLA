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

package com.tle.common.util

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

import java.net.URI

class UriUtilsTest extends AnyFunSpec with Matchers {

  private def uri(s: String): URI = URI.create(s)

  private val base = uri("https://oeq.example.com/inst1/")

  describe("isAbsoluteHttpUrl") {
    it("accepts only absolute http/https URLs with a host") {
      val cases = Table(
        ("name", "url", "expected"),
        ("https with host", "https://oeq.example.com/page", true),
        ("http with host", "http://oeq.example.com/page", true),
        ("relative path", "/page", false),
        ("scheme-relative, no host component parsed as such", "//oeq.example.com/page", false),
        ("non-http scheme", "ftp://oeq.example.com/page", false),
        ("javascript scheme", "javascript:alert(1)", false),
        ("unparseable", "http://%zz", false)
      )

      forAll(cases) { (_, url, expected) =>
        UriUtils.isAbsoluteHttpUrl(url) shouldBe expected
      }
    }
  }

  describe("sameOrigin") {
    it("compares scheme, host and port, falling back to default ports when unspecified") {
      val cases = Table(
        ("name", "target", "expected"),
        ("exact match", "https://oeq.example.com/inst1/", true),
        ("explicit default port == unspecified port", "https://oeq.example.com:443/other", true),
        ("different host", "https://attacker.example/inst1/", false),
        ("different explicit port", "https://oeq.example.com:8443/inst1/", false),
        // :443 is https's default port, so host/port alone would look like a match - sameOrigin
        // still rejects it because the scheme itself differs.
        (
          "scheme downgrade even when port explicitly matches default",
          "http://oeq.example.com:443/inst1/",
          false
        )
      )

      forAll(cases) { (_, target, expected) =>
        UriUtils.sameOrigin(base, uri(target)) shouldBe expected
      }
    }
  }

  describe("underPath") {
    it("checks the target path is at or under a slash-terminated base path") {
      // Base path ends with a slash ("/inst1/").
      val trailingSlashCases = Table(
        ("name", "target", "expected"),
        ("exact base path", "https://oeq.example.com/inst1/", true),
        ("sub path", "https://oeq.example.com/inst1/somepage?x=1", true),
        ("path outside prefix", "https://oeq.example.com/otherinst/somepage", false),
        // Sibling directory whose name merely starts with "inst1" must not be treated as nested.
        ("prefix-sibling directory", "https://oeq.example.com/inst1extra/page", false),
        ("base path without trailing slash", "https://oeq.example.com/inst1", false),
        // Dot segments are normalized before comparison: ".." escaping the institution is rejected,
        // while ".." that stays inside is accepted.
        ("dot-segments escaping the institution", "https://oeq.example.com/inst1/../app", false),
        ("dot-segments staying inside", "https://oeq.example.com/inst1/sub/../page", true),
        // Percent-encoded dot segments are not literal ".."/"." at the point normalize() runs, so
        // they survive it unchanged and only decode into ".." once getPath() is read afterwards -
        // this must still be rejected, not accepted via a bare startsWith on the decoded string.
        (
          "percent-encoded dot-segments escaping the institution",
          "https://oeq.example.com/inst1/%2e%2e/app",
          false
        ),
        (
          "fully percent-encoded traversal escaping the institution",
          "https://oeq.example.com/inst1/%2e%2e%2fapp",
          false
        )
      )

      forAll(trailingSlashCases) { (_, target, expected) =>
        UriUtils.underPath(base, uri(target)) shouldBe expected
      }
    }

    it("does not confuse a sibling that merely shares a string prefix (app vs application)") {
      // Base path does NOT end with a slash ("/app") - the interesting false-positive scenario.
      val noTrailingSlashBase  = uri("https://oeq.example.com/app")
      val noTrailingSlashCases = Table(
        ("name", "target", "expected"),
        ("exact base path", "https://oeq.example.com/app", true),
        ("nested under base", "https://oeq.example.com/app/page", true),
        // "/application" shares the string prefix "/app" but is a different path - must be rejected.
        ("sibling sharing string prefix", "https://oeq.example.com/application", false),
        ("sibling sharing prefix with sub path", "https://oeq.example.com/application/page", false)
      )

      forAll(noTrailingSlashCases) { (_, target, expected) =>
        UriUtils.underPath(noTrailingSlashBase, uri(target)) shouldBe expected
      }
    }
  }

  describe("isHttpUri") {
    it("accepts only http and https schemes") {
      val cases = Table(
        ("name", "target", "expected"),
        ("https", "https://oeq.example.com/inst1/", true),
        ("http", "http://oeq.example.com/inst1/", true),
        ("ftp", "ftp://oeq.example.com/inst1/", false),
        ("file", "file:///etc/passwd", false),
        // Opaque URIs whose scheme is not http(s) - the schemes an open redirect must never follow.
        ("javascript", "javascript:alert(1)", false),
        ("data", "data:text/html,x", false)
      )

      forAll(cases) { (_, target, expected) =>
        UriUtils.isHttpUri(uri(target)) shouldBe expected
      }
    }
  }

  describe("isSafeRedirectUri") {
    it("requires an http(s) URI that is sameOrigin and underPath") {
      val cases = Table(
        ("name", "target", "expected"),
        // --- accepted: http(s), same origin, under base path ---
        ("exact base match", "https://oeq.example.com/inst1/", true),
        ("sub path with query", "https://oeq.example.com/inst1/somepage?x=1", true),
        (
          "deep nested path with query and fragment",
          "https://oeq.example.com/inst1/a/b/c?x=1#f",
          true
        ),
        (
          "explicit default port equivalent to unspecified",
          "https://oeq.example.com:443/inst1/page",
          true
        ),
        // --- rejected on origin (host / port / scheme) ---
        ("different host", "https://attacker.example/inst1/somepage", false),
        ("different explicit port", "https://oeq.example.com:8443/inst1/somepage", false),
        (
          "scheme downgrade with matching default port",
          "http://oeq.example.com:443/inst1/somepage",
          false
        ),
        // Host comparison is case-sensitive.
        ("host differing only in case", "https://OEQ.EXAMPLE.COM/inst1/somepage", false),
        // --- rejected on path prefix ---
        ("path outside prefix", "https://oeq.example.com/otherinst/somepage", false),
        // Trailing slash on the base path stops a sibling whose name merely starts with "inst1".
        ("prefix-sibling directory", "https://oeq.example.com/inst1extra/page", false),
        // The bare institution path without its trailing slash is not "under" the base path.
        ("base path without trailing slash", "https://oeq.example.com/inst1", false),
        // Percent-encoded traversal must be rejected here too, not just at the underPath level.
        (
          "percent-encoded dot-segments escaping the institution",
          "https://oeq.example.com/inst1/%2e%2e/app",
          false
        ),
        // --- rejected on scheme even when origin and path would otherwise match ---
        ("non-http scheme (ftp)", "ftp://oeq.example.com/inst1/somepage", false),
        ("non-http scheme (file)", "file://oeq.example.com/inst1/somepage", false),
        // Opaque dangerous schemes.
        ("javascript scheme", "javascript:alert(1)", false),
        ("data scheme", "data:text/html,x", false)
      )

      forAll(cases) { (_, target, expected) =>
        UriUtils.isSafeRedirectUri(base, uri(target)) shouldBe expected
      }
    }
  }
}
