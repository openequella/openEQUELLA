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

package com.tle.core.dashboard.model

import com.tle.common.i18n.CurrentLocale
import com.tle.common.portal.PortletTypeDescriptor

import scala.util.{Failure, Success, Try}

/** Basic information about a portlet type that can be created by the user.
  *
  * @param portletType
  *   One of portlet types defined in [[PortletType]]
  * @param name
  *   Display name of the portlet type
  * @param desc
  *   Short description explaining the portlet's purpose
  */
final case class PortletCreatable(portletType: PortletType.Value, name: String, desc: String)

object PortletCreatable {

  /** Attempts to transform the legacy [[PortletTypeDescriptor]] to a [[PortletCreatable]].
    */
  def fromDescriptor(portletDescriptor: PortletTypeDescriptor): Either[String, PortletCreatable] = {
    val portletType = portletDescriptor.getType
    Try(PortletType.withName(portletType)) match {
      case Success(pt) =>
        Right(
          PortletCreatable(
            portletType = pt,
            name = CurrentLocale.get(portletDescriptor.getNameKey),
            desc = CurrentLocale.get(portletDescriptor.getDescriptionKey)
          )
        )
      case Failure(_) =>
        Left(s"Invalid portlet type '$portletType'")
    }
  }
}
