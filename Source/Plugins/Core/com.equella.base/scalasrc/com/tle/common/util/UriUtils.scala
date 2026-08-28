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

import sttp.model.Uri

import java.net.URI

/** Helpers for validating that a candidate URI is safe to follow, whether as a redirect target or
  * as the source of embedded content, relative to a trusted base.
  */
object UriUtils {

  val HTTP = "http"

  val HTTPS = "https"

  private val defaultPorts = Map(HTTP -> 80, HTTPS -> 443)

  /** Scheme lower-cased (schemes are case-insensitive per RFC 3986); None for a schemeless URI. */
  private def scheme(uri: URI): Option[String] = Option(uri.getScheme).map(_.toLowerCase)

  /** The uri's explicit port, or the scheme's default port (http 80, https 443) when unspecified,
    * or -1 when neither is known.
    */
  private def portOrDefault(uri: URI): Int =
    if (uri.getPort != -1) uri.getPort else scheme(uri).flatMap(defaultPorts.get).getOrElse(-1)

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
    Uri
      .parse(url)
      .toOption
      .exists(u =>
        u.scheme.exists(Set(HTTP, HTTPS)) &&
          u.host.isDefined
      )

  /** True if the uri's scheme is http or https. Guards against schemes such as javascript: and
    * data: that must never be used as a redirect target.
    */
  def isHttpUri(uri: URI): Boolean = scheme(uri).exists(s => s == HTTP || s == HTTPS)

  /** True if base and target share scheme, host, and port - falling back to the scheme's default
    * port (http 80, https 443) when the port is unspecified, so a default port and an omitted port
    * are treated as equivalent.
    *
    * Host comparison is case-sensitive. Note that two schemeless or two hostless URIs compare as
    * "equal" on those components; callers guarding redirects should gate on [[isHttpUri]] first (as
    * [[isSafeRedirectUri]] does) so opaque URIs never reach here.
    */
  def sameOrigin(base: URI, target: URI): Boolean =
    scheme(base) == scheme(target) &&
      base.getHost == target.getHost &&
      portOrDefault(base) == portOrDefault(target)

  /** True if target's path is at, or nested under, base's path.
    *
    * Both paths are normalized first (dot segments removed) so a target such as "/inst1/../app"
    * cannot escape the base path - URI.resolve leaves ".." in an absolute-path reference intact, so
    * an unnormalized target would otherwise slip through. Normalizing alone still isn't enough:
    * percent-encoded dot segments (e.g. "%2e%2e") are not literal "." / ".." at the point
    * normalize() runs, so they survive it unchanged and only decode into ".." afterwards, when
    * getPath() reads the (now normalized) URI - so any dot segment still present in the decoded
    * path is rejected outright as well, closing that gap.
    *
    * The base path is then treated as a directory boundary so that a plain string prefix can't
    * produce a false match between sibling paths - e.g. base "/app" must not match target
    * "/application". A target equal to the base path, or under it once the base is given a trailing
    * slash, counts; anything else (including a target with no path, e.g. an opaque URI) does not.
    */
  def underPath(base: URI, target: URI): Boolean = {
    val basePath   = Option(base.normalize().getPath).getOrElse("")
    val targetPath = Option(target.normalize().getPath).getOrElse("")
    val baseDir    = if (basePath.endsWith("/")) basePath else basePath + "/"

    val noDotSegments =
      !targetPath.split("/").exists(segment => segment == ".." || segment == ".")

    noDotSegments && (targetPath == basePath || targetPath.startsWith(baseDir))
  }

  /** True if target is safe to use as a redirect target relative to base: an http(s) URI of the
    * same origin, and under base's path.
    */
  def isSafeRedirectUri(base: URI, target: URI): Boolean =
    isHttpUri(target) && sameOrigin(base, target) && underPath(base, target)
}
