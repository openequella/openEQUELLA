package com.tle.core.notification

import java.util.UUID

import com.tle.beans.Institution
import com.tle.common.usermanagement.user.valuebean.UserBean

case class EmailKey(id: UUID, user: UserBean, institution: Institution, cb: () => Unit) {
  def successCallback(): Unit = cb()
}
