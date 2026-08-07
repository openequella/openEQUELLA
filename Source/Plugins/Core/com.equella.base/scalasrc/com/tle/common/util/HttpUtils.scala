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

object HttpUtils {

  val HTTP = "http"

  val HTTPS = "https"

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

  /** Checks that `target` shares the same origin as `origin` - i.e. the same scheme, host and port.
    *
    * @param origin
    *   The trusted URI to compare against.
    * @param target
    *   The untrusted URI being checked.
    * @return
    *   True if `target` has the same scheme, host and port as `origin`.
    */
  def isSameOrigin(origin: URI, target: URI): Boolean =
    target.getScheme == origin.getScheme &&
      target.getHost == origin.getHost &&
      target.getPort == origin.getPort

  /** Safely checks that `target`'s path is contained within `origin`'s path - i.e. `target` is
    * `origin` itself or something underneath it, not a path that merely starts with the same
    * characters.
    *
    * `target` is treated as untrusted and may try to escape containment with `..`/`.` segments, so
    * a plain `startsWith` on the path is not enough. Two defences are needed together:
    *   1. `target.normalize()` collapses literal dot-segments, e.g. `/origin/../evil` becomes
    *      `/evil`.
    *   2. Rejecting any remaining `.`/`..` segment after normalizing, decoded. This catches
    *      percent-encoded traversal (`%2e%2e`) that step 1 misses: `normalize()` only recognises
    *      dot-segments that are literally `.`/`..` in the URI's raw, still-encoded path, so an
    *      encoded segment survives normalisation unchanged and only becomes `..` once decoded when
    *      `getPath()` is read afterwards.
    *
    * @param origin
    *   The trusted base URI whose path `target` must be contained within.
    * @param target
    *   The untrusted URI being checked.
    * @return
    *   True if `target`'s (normalized, decoded) path starts with `origin`'s path and contains no
    *   leftover `.`/`..` segment.
    */
  def isSamePath(origin: URI, target: URI): Boolean =
    Option(target.normalize().getPath).exists { path =>
      path.startsWith(origin.getPath) && !path
        .split("/")
        .exists(segment => segment == ".." || segment == ".")
    }
}
