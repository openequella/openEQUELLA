package com.tle.core.securitykey.dao

import com.tle.beans.securitykey.SecurityKey
import com.tle.core.hibernate.dao.GenericDao

trait SecurityKeyDAO extends GenericDao[SecurityKey, java.lang.Long] {

  /**
    * Retrieve a SecurityKey by key ID.
    *
    * @param keyId Unique ID of the key pair.
    * @return Option of the retrieved SecurityKey, or None if not found.
    */
  def getByKeyID(keyId: String): Option[SecurityKey]
}
