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

package com.tle.core.dashboard.service

import com.tle.core.dashboard.model.{
  PortletClosed,
  PortletCreatable,
  PortletDetails,
  PortletPreferenceUpdate
}

object DashboardLayout extends Enumeration {
  val SingleColumn, TwoEqualColumns, TwoColumnsRatio2to1, TwoColumnsRatio1to2 = Value
}

/** Service for managing the dashboard of the current user.
  */
trait DashboardService {

  /** Returns all portlets that the current user can see on their dashboard.
    *
    * A portlet is considered "viewable" even if it is closed since it is still partially displayed
    * on the dashboard.
    */
  def getViewablePortlets: List[PortletDetails]

  /** Retrieves the dashboard layout as configured by the user, if available.
    *
    * @return
    *   Some(layout) if a layout has been configured, or None otherwise.
    */
  def getDashboardLayout: Option[DashboardLayout.Value]

  /** Updates the dashboard layout configuration with the provided layout name.
    *
    * @param layout
    *   One of the supported layouts defined in [[DashboardLayout]].
    * @return
    *   Either an error message describing why the update failed, or Unit on success.
    */
  def updateDashboardLayout(layout: DashboardLayout.Value): Either[String, Unit]

  /** Returns a list of portlet types that the current user can add to their dashboard.
    */
  def getCreatablePortlets: List[PortletCreatable]

  /** Returns the basic information of portlets that the current user has closed on their dashboard.
    */
  def getClosedPortlets: List[PortletClosed]

  /** Updates the UI preferences for a portlet identified by its UUID.
    *
    * @param uuid
    *   The unique identifier of the portlet to update.
    * @param updates
    *   Preference updates to be applied to the target portlet.
    * @return
    *   Either the exception captured during the update, or Unit on Success.
    */
  def updatePortletPreference(
      uuid: String,
      updates: PortletPreferenceUpdate
  ): Either[Throwable, Unit]
}

object DashboardService {

  /** Name of the configuration property that stores the dashboard layout.
    */
  val DASHBOARD_LAYOUT: String = "dashboard.layout"
}
