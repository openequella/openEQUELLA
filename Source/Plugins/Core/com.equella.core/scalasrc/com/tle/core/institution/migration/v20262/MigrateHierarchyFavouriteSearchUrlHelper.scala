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

package com.tle.core.institution.migration.v20262

/** Shared by `MigrateHierarchyFavouriteSearchUrl` and `MigrateHierarchyFavouriteSearchUrlXml` to
  * rewrite the hierarchy compound UUID embedded in the URL of a favourite search.
  */
object MigrateHierarchyFavouriteSearchUrlHelper {

  /** Matches a favourite search URL for a New UI hierarchy page, capturing the compound UUID
    * between the fixed page path and either the query string or the end of the URL.
    *
    *   - The first group excludes '?' to keep the match anchored in the path.
    *   - Only the New UI page carries a base64 encoded compound UUID. The Legacy UI page keeps the
    *     topic in a query parameter ("/hierarchy.do?topic=...") in the
    *     application/x-www-form-urlencoded format, which has never involved base64.
    *   - A compound UUID is a comma separated list of "uuid" or "uuid:base64 name", so the second
    *     group allows nothing beyond the standard base64 alphabet and the ':' and ',' separators,
    *     plus '-' for the hyphens in the UUID itself.
    *   - Those hyphens mean a compound UUID which is already URL-safe can match too, but that does
    *     no harm: translating an already translated value leaves it exactly as it was, so running
    *     the migration a second time changes nothing.
    *   - The last group is the query string, which carries the search options where '+', '/' and
    *     '=' are all meaningful, so it is captured whole and put back untouched.
    *
    * Matches, taking `uuid` to stand for a topic UUID:
    * {{{
    * /page/hierarchy/uuid:TXVzaWM/IFRoZW9yeSA+IEpheno=?searchOptions=%7B%7D  // rewritten
    * /page/hierarchy/uuid:RCwgRGF2aWQ,uuid:SG9iYXJ0                          // rewritten
    * /page/hierarchy/uuid                                                    // no name to rewrite
    * /page/hierarchy/uuid:5Lit5paH6K--56iL                                   // already URL-safe
    * }}}
    *
    * Does not match:
    * {{{
    * /hierarchy.do?topic=uuid                     // Legacy UI, never base64
    * /page/search?searchOptions=%7B%7D            // not a hierarchy page
    * /page/hierarchy/uuid%3ATXVzaWM%2F            // percent encoded, so not a shape we know
    * /page/hierarchy/uuid:TXVzaWM_IFRoZW9yeSA     // already URL-safe, and the '_' rules it out
    * }}}
    */
  private val HierarchyPageUrl = """([^?]*/page/hierarchy/)([A-Za-z0-9+/=:,-]+)(\?.*)?""".r

  /** Rewrites the hierarchy compound UUID embedded in the path of the given favourite search URL
    * from the standard base64 alphabet to the unpadded URL-safe one.
    *
    * Only the alphabet is translated - the encoded bytes are identical either way - so this is a
    * no-op for a URL which is already in the unpadded URL-safe format, and for a URL which is not
    * for a New UI hierarchy page at all.
    *
    * @param url
    *   The URL of a favourite search.
    * @return
    *   The URL with an unpadded URL-safe compound UUID.
    */
  def toUrlSafeFormat(url: String): String = url match {
    case HierarchyPageUrl(pagePath, compoundUuid, query) =>
      pagePath + toUrlSafeAlphabet(compoundUuid) + Option(query).getOrElse("")
    case other => other
  }

  /** Moves an already encoded value from the standard base64 alphabet (RFC 4648 section 4) to the
    * URL and filename safe one (RFC 4648 section 5), which differ only in the last two characters:
    * '+' becomes '-' and '/' becomes '_'. The padding is then dropped to match what
    * `HierarchyCompoundUuid` now produces, `Base64.getUrlEncoder.withoutPadding`.
    */
  private def toUrlSafeAlphabet(compoundUuid: String): String =
    compoundUuid.replace('+', '-').replace('/', '_').replace("=", "")
}
