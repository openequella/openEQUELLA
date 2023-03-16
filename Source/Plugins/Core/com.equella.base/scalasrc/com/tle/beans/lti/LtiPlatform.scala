package com.tle.beans.lti

import com.thoughtworks.xstream.annotations.XStreamOmitField
import com.tle.beans.Institution
import org.hibernate.annotations.NamedQuery

import javax.persistence.{
  CascadeType,
  CollectionTable,
  Column,
  ElementCollection,
  Entity,
  FetchType,
  GeneratedValue,
  GenerationType,
  Id,
  Index,
  JoinColumn,
  ManyToOne,
  OneToMany,
  Table
}

/**
  * This entity is used to store the configuration of LTI 1.3 platform.
  */
@Entity
@Table(indexes = Array {
  new Index(name = "lti_platform_id", columnList = "platformId")
})
@NamedQuery(
  name = "getByPlatformID",
  query = "from LtiPlatform WHERE platformId = :platformId AND institution = :institution")
class LtiPlatform {

  /**
    * Database automatically generated ID used as the primary key.
    */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  /**
    * ID of the learning platform.
    */
  @Column(nullable = false, unique = true)
  var platformId: String = _

  /**
    * Client ID provided by the platform.
    */
  @Column(nullable = false)
  var clientId: String = _

  /**
    * The platform's authentication request URL
    */
  @Column(nullable = false)
  var authUrl: String = _

  /**
    * JWKS keyset URL where to get the keys.
    */
  @Column(nullable = false)
  var keysetUrl: String = _

  /**
    * Prefix added to the user ID from the LTI request
    */
  var usernamePrefix: String = _

  /**
    * Suffix added to the user ID from the LTI request
    */
  var usernameSuffix: String = _

  /**
    * How to handle unknown users by one of the three options - ERROR, GUEST OR CREATE.
    */
  @Column(nullable = false, length = 10)
  var unknownUserHandling: String = _

  /**
    * The list of groups to be added to the user object If the unknown user handling is CREATE.
    */
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "lti_unknown_user_groups")
  var unknownUserDefaultGroups: java.util.Set[String] = _

  /**
    * A list of roles to be assigned to a LIT instructor role.
    */
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "lti_instructor_roles")
  var instructorRoles: java.util.Set[String] = _

  /**
    * A list of roles to be assigned to a LTI role that is neither the instructor or in the list of custom roles.
    */
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "lti_unknown_roles")
  var unknownRoles: java.util.Set[String] = _

  /**
    *
    * Mappings from LTI roles to OEQ roles.
    */
  @OneToMany(cascade = Array(CascadeType.ALL), fetch = FetchType.LAZY)
  @JoinColumn(name = "lti_platform_id", nullable = false)
  var customRoles: java.util.Set[LtiCustomRole] = _

  /**
    * The ACL Expression to control access from this platform.
    */
  var allowExpression: String = _

  /**
    * Institution which the key set belongs to.
    */
  @JoinColumn(nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  @Index(name = "lti_platform_institution_id", columnList = "institution_id")
  @XStreamOmitField
  var institution: Institution = _
}
