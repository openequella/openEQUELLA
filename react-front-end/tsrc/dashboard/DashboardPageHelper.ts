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
import { pipe } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as OEQ from "@openequella/rest-api-client";

/**
 * A factory that produces a function to update a specific portlet's preference.
 *
 * @param uuid The UUID of the portlet to be updated.
 * @param updates An object containing the fields of the portlet to be updated.
 * @returns A function that takes an array of portlets and returns a new array with the specified portlet updated.
 */
export const createPortletPreferenceUpdater =
  (uuid: string, updates: Partial<OEQ.Dashboard.PortletBase>) =>
  (portlets: OEQ.Dashboard.BasicPortlet[]): OEQ.Dashboard.BasicPortlet[] =>
    pipe(
      portlets,
      A.map((portlet) =>
        portlet.commonDetails.uuid === uuid
          ? {
              ...portlet,
              commonDetails: { ...portlet.commonDetails, ...updates },
            }
          : portlet,
      ),
    );

/**
 * Update the `portlets` within a `DashboardDetails` object.
 *
 * @param dashboard The original dashboard details object.
 * @param portletsUpdateFn A function that transforms the array of portlets.
 * @returns A new dashboard details object with the updated portlets.
 */
export const updateDashboardDetails = (
  dashboard: OEQ.Dashboard.DashboardDetails,
  portletsUpdateFn: (
    _: OEQ.Dashboard.BasicPortlet[],
  ) => OEQ.Dashboard.BasicPortlet[],
): OEQ.Dashboard.DashboardDetails => ({
  ...dashboard,
  portlets: portletsUpdateFn(dashboard.portlets),
});
