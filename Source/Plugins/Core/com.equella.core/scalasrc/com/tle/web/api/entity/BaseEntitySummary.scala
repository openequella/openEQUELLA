package com.tle.web.api.entity

import com.tle.beans.entity.BaseEntity
import com.tle.web.api.ApiHelper

/**
  * Summary information for a BaseEntity, which should be enough for display purposes and pulling
  * any further information as required (due to the UUID).
  *
  * @param uuid The unique ID of the underlying BaseEntity
  * @param name The default locale human readable name for a BaseEntity
  */
case class BaseEntitySummary(uuid: String, name: String)

object BaseEntitySummary {
  def apply(be: BaseEntity): BaseEntitySummary =
    BaseEntitySummary(uuid = be.getUuid, name = ApiHelper.getEntityName(be))
}
