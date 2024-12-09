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

package com.tle.beans.lti

import javax.persistence.{
  CollectionTable,
  Column,
  ElementCollection,
  Entity,
  GeneratedValue,
  GenerationType,
  Id
}

/** This entity is used to store the mappings between LTI roles and OEQ roles for LTI 1.3 platforms.
  */
@Entity
class LtiPlatformCustomRole {

  /** Database automatically generated ID used as the primary key.
    */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  /** The LTI role which targets to one or multiple OEQ roles.
    */
  @Column(nullable = false)
  var ltiRole: String = _

  /** A list of OEQ roles which is targeted by a LTI role.
    */
  @ElementCollection
  @CollectionTable(name = "lti_platform_custom_target")
  @Column(name = "oeq_role")
  var oeqRoles: java.util.Set[String] = _
}

object LtiPlatformCustomRole {
  def apply(ltiRole: String, oeqRoles: java.util.Set[String]): LtiPlatformCustomRole = {
    val mapping = new LtiPlatformCustomRole
    mapping.ltiRole = ltiRole
    mapping.oeqRoles = oeqRoles
    mapping
  }
}
