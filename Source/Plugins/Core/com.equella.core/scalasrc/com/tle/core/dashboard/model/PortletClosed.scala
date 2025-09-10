package com.tle.core.dashboard.model

import com.tle.common.i18n.LangUtils
import com.tle.common.portal.entity.Portlet

/** Basic information about a portlet that has been closed by the user.
  *
  * @param uuid
  *   UUID of the portlet
  * @param name
  *   Display name of the portlet
  */
final case class PortletClosed(uuid: String, name: String)

object PortletClosed {
  def apply(portlet: Portlet): PortletClosed =
    PortletClosed(uuid = portlet.getUuid, name = LangUtils.getString(portlet.getName))
}
