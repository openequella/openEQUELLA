package com.tle.core.securitykey.dao

import com.tle.beans.securitykey.SecurityKey
import com.tle.core.hibernate.dao.GenericDao

trait SecurityKeyDAO extends GenericDao[SecurityKey, java.lang.Long] {
  def getByKeyID(keyId: String): SecurityKey
}
