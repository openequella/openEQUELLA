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

package com.tle.integration.lti13

/** Provide the support for parsing a custom username claim.
  *
  * Since an LTI claim can be hierarchical and have multiple paths, the custom username claim
  * configured in a LTI Platform must be provided in the format of bracket notation. That is, each
  * path of the claim must be wrapped by a pair of square brackets.
  */
object Lti13UsernameClaimParser {

  /** Regex to extract a list of chars that does not contain a '[' nor ']' from a pair of square
    * brackets.
    *
    * Examples:
    *   - `[https://lti13.org][moodle][ID]` -> "https://lti13.org", "moodle", "ID"
    *   - `[https://lti13.org][moodle[ID]` -> "https://lti13.org", "ID"
    *   - `https://lti13.org][moodle][ID]` -> "moodle", "ID
    *   - `[][https://lti13.org][moodle][ID][]` -> "https://lti13.org", "moodle", "ID"
    */
  private val USERNAME_CLAIM_REGEX = """\[([^\[\]]+?)\]""".r

  /** Function to verify whether the custom username claim configured in a LTI 1.3 Platform is valid
    * or not, based on the format of bracket notation.
    *
    * A regular expression is used to extract all the paths from the brackets into an array. The
    * paths are then verified by summing the length of each (including their brackets) and comparing
    * with the length of the original claim. While an equal result means the verification succeeds
    * and returns the array, an unequal result indicates that the claim must not be constructed
    * properly in the format of bracket notation and results in a [[PlatformDetailsError]].
    *
    * Examples:
    *   - `[https://lti13.org][moodle][ID]` -> `Array("https://lti13.org", "moodle", "ID")`
    *   - `[https://lti13.org][moodle[ID]` -> `PlatformDetailsError`
    *   - `][https://lti13.org][moodle][ID]` -> `PlatformDetailsError`
    *
    * @param claim
    *   The LTI custom user name claim to be verified.
    * @return
    *   Either an array of verified paths or an error
    */
  def parse(claim: String): Either[String, List[String]] = {
    val paths = USERNAME_CLAIM_REGEX.findAllMatchIn(claim).toList

    Either.cond(
      claim.nonEmpty && paths.map(_.matched.length).sum == claim.length, // including brackets
      paths.map(_.group(1)), // Only need the content within the brackets
      s"Syntax error in claim $claim"
    )
  }
}
