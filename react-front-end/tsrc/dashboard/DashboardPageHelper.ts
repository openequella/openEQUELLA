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
import * as O from "fp-ts/Option";
import { pipe } from "fp-ts/function";

interface PortletPreference {
  isMinimised?: boolean;
  isClosed?: boolean;
}

/**
 * A factory that produces a function to update a single portlet if it matches the provided UUID.
 *
 * @param uuid The UUID of the portlet to be updated.
 * @param updates An object containing the fields of the portlet to be updated.
 * @returns A function that takes a portlet and returns an updated portlet if the UUID matches, otherwise the original portlet.
 */
export const updatePortlet =
  (uuid: string, updates: PortletPreference) =>
  (portlet: OEQ.Dashboard.BasicPortlet): OEQ.Dashboard.BasicPortlet =>
    portlet.commonDetails.uuid === uuid
      ? {
          ...portlet,
          commonDetails: { ...portlet.commonDetails, ...updates },
        }
      : portlet;

/**
 * Updates dashboard’s portlets by applying a portlet update for the given UUID.
 * Safe to pass directly to a React state setter.
 *
 * @param uuid - UUID of the target portlet.
 * @param pref - Preference field to update in the targeted portlet.
 * @returns A function that takes dashboard details and returns the updated version.
 */
export const buildNewDashboardDetails =
  (uuid: string, pref: OEQ.Dashboard.PortletPreference) =>
  (dashboard?: OEQ.Dashboard.DashboardDetails) =>
    pipe(
      O.fromNullable(dashboard),
      O.map((db) => ({
        ...db,
        portlets: pipe(db.portlets, A.map(updatePortlet(uuid, pref))),
      })),
      O.getOrElse(() => dashboard),
    );
