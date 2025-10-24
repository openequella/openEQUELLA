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
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";

/**
 * Updates dashboard’s portlets by applying a portlet update for the given UUID.
 * Safe to pass directly to a React state setter.
 *
 * @param uuid - UUID of the target portlet.
 * @param pref - Preferences to update in the targeted portlet.
 * @returns A function that takes dashboard details and returns the updated version.
 */
export const updateDashboardDetails =
  (uuid: string, pref?: OEQ.Dashboard.PortletPreference) =>
  (dashboard?: OEQ.Dashboard.DashboardDetails) => {
    if (!dashboard) {
      return dashboard;
    }

    const updateTargetPortlet = (portlet: OEQ.Dashboard.BasicPortlet) =>
      portlet.commonDetails.uuid === uuid
        ? {
            ...portlet,
            commonDetails: { ...portlet.commonDetails, ...pref },
          }
        : portlet;

    const filterOutTargetPortlet = (
      portlet: OEQ.Dashboard.BasicPortlet,
    ): boolean => portlet.commonDetails.uuid !== uuid;

    // Choose the operation based on whether pref is provided
    const updatedPortlets = pref
      ? pipe(dashboard.portlets, A.map(updateTargetPortlet)) // Update if pref exists
      : pipe(dashboard.portlets, A.filter(filterOutTargetPortlet)); // Filter if pref is missing

    return { ...dashboard, portlets: updatedPortlets };
  };
