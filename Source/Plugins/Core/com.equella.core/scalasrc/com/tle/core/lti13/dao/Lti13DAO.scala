package com.tle.core.lti13.dao

import com.tle.beans.lti.LtiPlatform
import com.tle.core.hibernate.dao.GenericInstitutionalDao

trait Lti13DAO extends GenericInstitutionalDao[LtiPlatform, java.lang.Long] {

  /**
    * Retrieve a LTI Platform configuration by Platform ID.
    *
    * @param platformID Unique ID of a LTI Platform.
    * @return Option of the LTI Platform configuration, or None if it does not exist.
    */
  def getByPlatformID(platformID: String): Option[LtiPlatform]
}
