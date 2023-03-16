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

/**
  * This entity is used to store the mappings between LTI roles and OEQ roles for LTI 1.3 platforms.
  */
@Entity
class LtiCustomRole {

  /**
    * Database automatically generated ID used as the primary key.
    */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  /**
    * The LTI role which targets to one or multiple OEQ roles.
    */
  @Column(nullable = false, unique = true)
  var ltiRole: String = _

  /**
    * A list of OEQ roles which is targeted by a LTI role.
    */
  @ElementCollection
  @CollectionTable(name = "lti_custom_role_target")
  @Column(name = "oeq_role")
  var oeqRoles: java.util.Set[String] = _
}
