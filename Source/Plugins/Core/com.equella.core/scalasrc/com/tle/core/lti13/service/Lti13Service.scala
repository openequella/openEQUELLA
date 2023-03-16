package com.tle.core.lti13.service

import com.tle.beans.lti.LtiPlatform

trait Lti13Service {

  /**
    * Retrieve a LTI Platform Configuration from the current Institution by Platform ID.
    *
    * @param platformID The ID identifying a LTI Platform.
    * @return Option of the LTI platform configuration, or None if no configuration matches the ID.
    */
  def getByPlatformID(platformID: String): Option[LtiPlatform]

  /**
    * Retrieve all the LTI Platform configurations for the current Institution.
    *
    * @return A list of LTI Platform configurations.
    */
  def getAll: List[LtiPlatform]

  /**
    * Save the provided LTI Platform configuration for the current Institution.
    *
    * @param ltiPlatform The configuration to be saved.
    * @return ID of the new saved entity.
    */
  def create(ltiPlatform: LtiPlatform): Long

  /**
    * Update an existing LTI Platform.
    *
    * @param ltiPlatform The configuration that has updates.
    */
  def update(ltiPlatform: LtiPlatform): Unit

  /**
    * Delete LTI Platform configuration from the current Institution.
    *
    * @param platformID The ID identifying a LTI Platform.
    */
  def delete(ltiPlatform: LtiPlatform): Unit
}
