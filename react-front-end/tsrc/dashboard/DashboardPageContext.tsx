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

import { createContext } from "react";
import * as OEQ from "@openequella/rest-api-client";

interface DashboardPageContextProps {
  /**
   * Function to close a portlet.
   *
   * @param uuid UUID of the portlet to be closed.
   * @param portletPref The portlet's preference where `isClosed` is updated to the new value and other properties remain unchanged.
   */
  closePortlet: (
    uuid: string,
    portletPref: OEQ.Dashboard.PortletPreference,
  ) => void;
  /**
   * Function to delete a portlet.
   *
   * @param uuid UUID of the portlet to be deleted.
   */
  deletePortlet: (uuid: string) => void;
  /**
   * Function to minimise/expand a portlet.
   *
   * @param uuid UUID of the portlet to be minimised/expanded.
   * @param portletPref The portlet's preference where `isMinimised` is updated to the new value and other properties remain unchanged.
   */
  minimisePortlet: (
    uuid: string,
    portletPref: OEQ.Dashboard.PortletPreference,
  ) => void;
}

export const DashboardPageContext = createContext<DashboardPageContextProps>({
  closePortlet: () => {},
  deletePortlet: () => {},
  minimisePortlet: () => {},
});
