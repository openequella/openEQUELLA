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

import com.thoughtworks.xstream.annotations.XStreamOmitField
import com.tle.beans.Institution
import com.tle.beans.webkeyset.WebKeySet
import org.hibernate.annotations.NamedQuery

import java.time.Instant
import javax.persistence.{
  CascadeType,
  CollectionTable,
  Column,
  ElementCollection,
  Entity,
  GeneratedValue,
  GenerationType,
  Id,
  Index,
  JoinColumn,
  JoinTable,
  Lob,
  ManyToOne,
  OneToMany,
  OneToOne,
  Table,
  UniqueConstraint
}

/** This entity is used to store the configuration of LTI 1.3 platform.
  */
@Entity
@Table(
  uniqueConstraints =
    Array(new UniqueConstraint(columnNames = Array("platformId", "institution_id"))),
  indexes = Array {
    new Index(name = "lti_platform_id", columnList = "platformId, institution_id")
  }
)
@NamedQuery(
  name = "getByPlatformID",
  query = "from LtiPlatform WHERE platformId = :platformId AND institution = :institution"
)
@NamedQuery(
  name = "deleteByPlatformID",
  query = "Delete from LtiPlatform WHERE platformId = :platformId AND institution = :institution"
)
class LtiPlatform {

  /** Database automatically generated ID used as the primary key.
    */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  /** ID of the learning platform.
    */
  @Column(nullable = false)
  var platformId: String = _

  /** Name of the learning platform.
    */
  @Column(nullable = false)
  var name: String = _

  /** Client ID provided by the platform.
    */
  @Column(nullable = false)
  var clientId: String = _

  /** The platform's authentication request URL
    */
  @Column(nullable = false)
  var authUrl: String = _

  /** JWKS keyset URL where to get the keys.
    */
  @Column(nullable = false)
  var keysetUrl: String = _

  /** Prefix added to the user ID from the LTI request
    */
  var usernamePrefix: String = _

  /** Suffix added to the user ID from the LTI request
    */
  var usernameSuffix: String = _

  /** The claim which can be used to retrieve username from the LTI request.
    */
  var usernameClaim: String = _

  /** How to handle unknown users by one of the three options - ERROR, GUEST OR CREATE.
    */
  @Column(nullable = false, length = 10)
  var unknownUserHandling: String = _

  /** The list of groups to be added to the user object If the unknown user handling is CREATE.
    */
  @ElementCollection
  @CollectionTable(name = "lti_platform_unknown_groups")
  var unknownUserDefaultGroups: java.util.Set[String] = _

  /** A list of roles to be assigned to a LTI instructor role.
    */
  @ElementCollection
  @CollectionTable(name = "lti_platform_instructor_roles")
  var instructorRoles: java.util.Set[String] = _

  /** A list of roles to be assigned to a LTI role that is neither the instructor or in the list of
    * custom roles.
    */
  @ElementCollection
  @CollectionTable(name = "lti_platform_unknown_roles")
  var unknownRoles: java.util.Set[String] = _

  /** Mappings from LTI roles to OEQ roles.
    */
  @OneToMany(cascade = Array(CascadeType.ALL), orphanRemoval = true)
  @JoinColumn(name = "lti_platform_id", nullable = false)
  var customRoles: java.util.Set[LtiPlatformCustomRole] = _

  /** The ACL Expression to control access from this platform.
    */
  @Lob
  var allowExpression: String = _

  /** Institution which the key set belongs to.
    */
  @JoinColumn(nullable = false)
  @ManyToOne
  @Index(name = "lti_platform_institution_id", columnList = "institution_id")
  @XStreamOmitField
  var institution: Institution = _

  /** Whether the platform is enabled or not.
    */
  @Column(nullable = false)
  var enabled: Boolean = _

  /** Key pairs used to sign the JWT for LTI platform.
    *
    * NOTE: A join table has been explicitly used here to ensure that `WebKeySet` is not tightly
    * coupled to LTI Platform, as we can use that for other items in the future - e.g. improved
    * OAuth2 with JWT support.
    */
  @OneToMany(cascade = Array(CascadeType.ALL), orphanRemoval = true)
  @JoinTable(
    name = "lti_platform_key_pairs",
    joinColumns = Array(new JoinColumn(name = "lti_platform_id", referencedColumnName = "id")),
    inverseJoinColumns = Array(new JoinColumn(name = "web_key_set_id", referencedColumnName = "id"))
  )
  var keyPairs: java.util.Set[WebKeySet] = _

  /** When the platform was created.
    */
  @Column(nullable = false)
  var dateCreated: Instant = _

  /** ID of the user who created the platform.
    */
  @Column(nullable = false)
  var createdBy: String = _

  /** When the platform was last modified.
    */
  var dateLastModified: Instant = _

  /** ID of the user Who last modified the platform.
    */
  var lastModifiedBy: String = _
}
