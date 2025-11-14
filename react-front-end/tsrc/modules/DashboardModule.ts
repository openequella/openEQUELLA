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
import { AxiosError } from "axios";
import { API_BASE_URL } from "../AppConfig";
import { OLD_DASHBOARD_PATH } from "../mainui/routes";
import { ChangeRoute, submitRequest } from "./LegacyContentModule";
import * as t from "io-ts";

/**
 * Codec for representing the position of a portlet in the dashboard.
 */
export const PortletPositionCodec = t.type({
  /**
   * Index of the column where the portlet is located.
   */
  column: OEQ.Codec.Dashboard.PortletColumnCodec,
  /**
   * Index of the order in the column where the portlet is located.
   */
  order: t.number,
});

/**
 * Unlike the `column` and 'order' value in `OEQ.Dashboard.PortletCommonDetails`,
 * it represents the real position(index) of a portlet in the dashboard page.
 */
export type PortletPosition = t.TypeOf<typeof PortletPositionCodec>;

/**
 * Retrieve Dashboard details, including the list of viewable Portlets and the layout.
 */
export const getDashboardDetails =
  (): Promise<OEQ.Dashboard.DashboardDetails> =>
    OEQ.Dashboard.getDashboardDetails(API_BASE_URL);

/**
 * Update the Dashboard layout.
 *
 * @param layout New layout selected by the user
 */
export const updateDashboardLayout = (
  layout: OEQ.Dashboard.DashboardLayout,
): Promise<void> => OEQ.Dashboard.updateDashboardLayout(API_BASE_URL, layout);

/**
 * Retrieve a list of portlet types that can be created by the user.
 */
export const getCreatablePortlets = (): Promise<
  OEQ.Dashboard.PortletCreatable[]
> => OEQ.Dashboard.getCreatablePortlets(API_BASE_URL);

/**
 * Retrieve a list of portlets that have been closed by the user.
 */
export const getClosedPortlets = (): Promise<OEQ.Dashboard.PortletClosed[]> =>
  OEQ.Dashboard.getClosedPortlets(API_BASE_URL);

/**
 * Update the user's UI preferences of a specific portlet.
 *
 * @param uuid UUID of the target portlet
 * @param preference The new preferences to be applied to the portlet
 */
export const updatePortletPreference = (
  uuid: string,
  preference: OEQ.Dashboard.PortletPreference,
): Promise<void> =>
  OEQ.Dashboard.updatePortletPreferences(API_BASE_URL, uuid, preference);

/**
 * Delete a portlet by UUID.
 *
 * @param uuid UUID of the target portlet
 */
export const deletePortlet = (uuid: string): Promise<void> =>
  OEQ.Dashboard.deletePortlet(API_BASE_URL, uuid);

/**
 * Retrieves the route to the legacy portlet creation page by submitting a Legacy content API request
 * to `home.do`. The endpoint is `psh.createPortletFromNewDashboard` where `psh` is the name of Section
 * `ShowPortletsSection` and `createPortletFromNewDashboard` is the event handler for this request.
 *
 * @param portletType Type of the portlet to be created, which must be supplied to the event handler as the first
 * parameter.
 */
export const getLegacyPortletCreationPageRoute = (
  portletType: OEQ.Dashboard.PortletType,
): Promise<string> =>
  submitRequest<ChangeRoute>(OLD_DASHBOARD_PATH, {
    event__: ["psh.createPortletFromNewDashboard"],
    eventp__0: [portletType],
  }).then(({ route }) => `/${route}`);

/**
 * Initiate an editing session for a portlet.
 *
 * This is achieved by submitting a form to the legacy UI's `/home.do` endpoint,
 * which triggers the `psh.editPortletFromNewDashboard` event handler. `psh` is the ID
 * for the `ShowPortletsSection` which then initiates the editing session and
 * returns the URL of the specific portlet editing page.
 *
 * @param uuid UUID of the portlet to be edited.
 * @returns A promise which resolves to the URL path for the legacy editing page.
 */
export const editPortlet = (uuid: string): Promise<string> =>
  submitRequest<ChangeRoute>(OLD_DASHBOARD_PATH, {
    event__: ["psh.editPortletFromNewDashboard"],
    eventp__0: [uuid],
  })
    .then(({ route }) => `/${route}`)
    .catch((error: AxiosError | Error) => {
      throw OEQ.Errors.repackageError(error);
    });
