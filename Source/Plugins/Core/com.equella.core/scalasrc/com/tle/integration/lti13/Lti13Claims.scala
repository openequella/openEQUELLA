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

/** OAuth 2.0 claims specific to LTI 1.3.
  */
object Lti13Claims {

  /** Contains a (possibly empty) array of URI values for roles that the user has within the
    * message's associated context.
    *
    * If this list is not empty, it MUST contain at least one role from the role vocabularies
    * described in role vocabularies. <https://www.imsglobal.org/spec/lti/v1p3/#role-vocabularies>
    *
    * If the sender of the message wants to include a role from another vocabulary namespace, by
    * best practice it should use a fully-qualified URI to identify the role. By best practice,
    * systems should not use roles from another role vocabulary, as this may limit interoperability.
    */
  val ROLES = "https://purl.imsglobal.org/spec/lti/claim/roles"

  /** Contains a string value to provide a fully qualified URL which a tool provider must redirect
    * the workflow to once the resource selection is completed.
    */
  val TARGET_LINK_URI = "https://purl.imsglobal.org/spec/lti/claim/target_link_uri"

  /** Contains a string value to specify which kind of the message type is.
    */
  val MESSAGE_TYPE = "https://purl.imsglobal.org/spec/lti/claim/message_type"

  /** Contains a key-value map which provides a list of custom properties configured for a platform.
    * Map values must be strings, and "empty-string" is a valid value. However, null is not valid.
    */
  val CUSTOM_PARAMETERS = "https://purl.imsglobal.org/spec/lti/claim/custom"

  /** Contains a list of deep linking settings for a platform as a JSON string.
    */
  val DEEP_LINKING_SETTINGS = "https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings"

  /** Contains a case-sensitive string that identifies the platform-tool integration governing the
    * message. It MUST NOT exceed 255 ASCII characters in length.
    */
  val DEPLOYMENT_ID = "https://purl.imsglobal.org/spec/lti/claim/deployment_id"

  /** Contains an opaque value which must be returned by the tool in its response if it was passed
    * in on the request.
    */
  val DATA = "https://purl.imsglobal.org/spec/lti-dl/claim/data"

  /** Contains a JSON array of selected content items. An empty array or the absence of this claim
    * indicates there should be no item added as a result of this interaction. This claim is
    * optional.
    */
  val CONTENT_ITEMS = "https://purl.imsglobal.org/spec/lti-dl/claim/content_items"

  /** Contains a string that indicates the version of LTI to which the message conforms. For
    * conformance with LTI 1.3 specification, the claim must have the value 1.3.0.
    */
  val VERSION = "https://purl.imsglobal.org/spec/lti/claim/version"

  /** Contains a JSON object to provide properties for the context from within which the resource
    * link launch occurs.
    */
  val CONTEXT = "https://purl.imsglobal.org/spec/lti/claim/context"

  /** Checks the provided role for the standard LIS (v2) context role identifying a role claim for
    * an 'Instructor'. Does _not_ support 'simple names' as this method is considered deprecated and
    * so "by best practice, vendors should use the full URIs for all roles (context roles
    * included)".
    *
    * @param role
    *   a role claim to be inspected
    * @return
    *   `true` if the claim is for an Instructor
    */
  def instructorRolePredicate(role: String): Boolean =
    role.contains("http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor") ||
      // support instructor context sub roles too - https://www.imsglobal.org/spec/lti/v1p3/#context-sub-roles
      "http://purl.imsglobal.org/vocab/lis/v2/membership/Instructor#.+".r.matches(role)
}
