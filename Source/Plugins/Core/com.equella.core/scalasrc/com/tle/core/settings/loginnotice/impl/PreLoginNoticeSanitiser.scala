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

import com.tle.common.util.HttpUtils.{isSameOrigin, isSamePath}
import org.owasp.html.{AttributePolicy, HtmlPolicyBuilder}

import java.net.URI
import scala.util.{Failure, Success, Try}

/** Utility class providing allowlist-based HTML sanitisation for the pre-login notice. */
object PreLoginNoticeSanitiser {

  /** Sanitise the supplied raw HTML by applying a list of rules to the HTML content. This is not an
    * exhaustive list, just the highlights:
    *
    * Kept:
    *   - Common formatting: headings, paragraphs, lists, `hr`/`pre`, tables, bold/italic/etc.
    *   - Links (`a`/`href`), restricted to the `http`/`https`/`mailto` schemes.
    *   - Images (`img`/`src`), only when same-origin and same-path as `trustedImageSrcPrefix` (see
    *     below) - both fully-qualified and root-relative URLs are accepted.
    *   - Inline `style`, limited to the properties in `CssSchema.DEFAULT` (colour, alignment,
    *     sizing, etc.) - arbitrary CSS is not allowed through wholesale.
    *
    * Dropped:
    *   - `<script>`, event handler attributes (`onclick` etc.), and `javascript:`/other disallowed
    *     URL schemes.
    *   - `id`/`class` on any element - `id` in particular is a DOM-clobbering vector.
    *   - Images whose `src` isn't same-origin/same-path with the institution (see
    *     `trustedImageSrcPrefix`), including path-traversal attempts (`../`, percent-encoded or
    *     not) to escape that containment.
    *   - Any element or attribute not explicitly allowlisted above.
    *
    * @param html
    *   the raw pre-login notice HTML.
    * @param trustedImageSrcPrefix
    *   the institution's base URL. An `img` `src` is allowed if, once resolved against this URL, it
    *   shares the same origin (scheme/host/port) and its path starts with the institution's own
    *   path - this covers both fully-qualified URLs and root-relative ones (e.g.
    *   `/fiveo/file/<uuid>/<version>/<name>`, which is what this app's own attachment links
    *   normally look like).
    * @return
    *   The sanitised HTML content.
    */
  def sanitise(html: String, trustedImageSrcPrefix: String): String = {
    val builder = applyStandardRules
      .andThen(allowAdditionalElements)
      .andThen(allowImages(trustedImageSrcPrefix))
      .andThen(allowLinks)
      .andThen(allowTables)(new HtmlPolicyBuilder())

    builder.toFactory.sanitize(html)
  }

  private val applyStandardRules: HtmlPolicyBuilder => HtmlPolicyBuilder = builder =>
    builder
      .allowCommonBlockElements()
      .allowCommonInlineFormattingElements()
      .allowStyling()
      .allowStandardUrlProtocols()

  private val allowTables: HtmlPolicyBuilder => HtmlPolicyBuilder = builder =>
    builder.allowElements("table", "thead", "tbody", "tr", "th", "td")

  // Elements that are safe and common to use but not in the standard list.
  private val allowAdditionalElements: HtmlPolicyBuilder => HtmlPolicyBuilder = builder =>
    builder.allowElements("hr", "pre")

  private val allowLinks: HtmlPolicyBuilder => HtmlPolicyBuilder = builder =>
    builder.allowElements("a").allowAttributes("href").onElements("a")

  private val allowImages: String => HtmlPolicyBuilder => HtmlPolicyBuilder =
    trustedImageSrcPrefix =>
      builder =>
        builder
          .allowElements("img")
          .allowAttributes("src")
          .matching(imageSrcPolicy(trustedImageSrcPrefix))
          .onElements("img")
          .allowAttributes("alt", "width", "height")
          .onElements("img")

  // `AttributePolicy` requires returning `null` to disallow the attribute.

  private def imageSrcPolicy(trustedImageSrcPrefix: String): AttributePolicy = {
    val institutionUri = new URI(trustedImageSrcPrefix)

    (_, _, value) =>
      Try {
        institutionUri.resolve(value)
      } match {
        case Success(target) =>
          val sameOrigin = isSameOrigin(institutionUri, target)
          val samePath   = isSamePath(institutionUri, target)
          Option.when(sameOrigin && samePath)(value).orNull
        case Failure(exception) => null
      }
  }
}
