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

import { Skeleton } from "@mui/material";
import { pipe, constVoid } from "fp-ts/function";
import { useCallback, useContext, useEffect, useState } from "react";
import * as React from "react";
import { AppContext } from "../mainui/App";
import { templateDefaults, TemplateUpdateProps } from "../mainui/Template";
import {
  getDashboardDetails,
  updatePortletPreference,
} from "../modules/DashboardModule";
import { languageStrings } from "../util/langstrings";
import WelcomeBoard from "./components/WelcomeBoard";
import * as TE from "fp-ts/TaskEither";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import * as O from "fp-ts/Option";
import * as T from "fp-ts/Task";
import { DashboardPageContext } from "./DashboardPageContext";
import { buildNewDashboardDetails } from "./DashboardPageHelper";
import { PortletContainer } from "./portlet/PortletContainer";

const { title } = languageStrings.dashboard;

const DashboardPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const { appErrorHandler, currentUser } = useContext(AppContext);

  const [isLoading, setIsLoading] = useState(true);
  const [dashboardDetails, setDashboardDetails] =
    useState<OEQ.Dashboard.DashboardDetails>();

  const getPortlets = useCallback(() => {
    pipe(
      TE.tryCatch(
        () => getDashboardDetails(),
        (e) => `Failed to get dashboard details: ${e}`,
      ),
      TE.match(appErrorHandler, setDashboardDetails),
      T.tapIO(() => () => {
        setIsLoading(false);
      }),
    )();
  }, [appErrorHandler]);

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(title)(tp),
    }));
  }, [updateTemplate]);

  const updatePortletPreferenceAndRefresh = useCallback(
    (uuid: string, pref: OEQ.Dashboard.PortletPreference) =>
      pipe(
        TE.tryCatch(
          () => updatePortletPreference(uuid, pref),
          (e) => `Failed to update portlet preference: ${e}`,
        ),
        TE.match(appErrorHandler, constVoid),
        T.tapIO(() => getPortlets), // ensures it’s invoked
      )(),
    [appErrorHandler, getPortlets],
  );

  const closePortlet = (uuid: string) => {
    // TODO: REMOVE ME.
    console.debug(uuid);
    // TODO: update dashboardDetails to remove the closed one.
    // TODO: add API call to update preference and get portlets again.
  };

  const deletePortlet = (uuid: string) => {
    // TODO: REMOVE ME.
    console.debug(uuid);
    // TODO: update dashboardDetails to remove the deleted one.
    // TODO: add API call to update preference and get portlets again.
  };

  const minimisePortlet = useCallback(
    async (uuid: string, isMinimised: boolean) => {
      setDashboardDetails(buildNewDashboardDetails(uuid, { isMinimised }));

      await updatePortletPreferenceAndRefresh(uuid, { isMinimised });
    },
    [updatePortletPreferenceAndRefresh],
  );

  useEffect(() => {
    getPortlets();
  }, [getPortlets]);

  const renderDashboardForNonSystemUser = () =>
    pipe(
      O.Do,
      O.apS("details", O.fromNullable(dashboardDetails)),
      O.bind("portlets", ({ details: { portlets } }) =>
        pipe(portlets, O.fromPredicate(A.isNonEmpty)),
      ),
      O.bind("layout", ({ details: { layout } }) => O.some(layout)),
      O.fold(
        () => <WelcomeBoard />,
        ({ layout, portlets }) => (
          <PortletContainer portlets={portlets} layout={layout} />
        ),
      ),
    );

  const renderDashboard = () =>
    currentUser?.isSystem ? (
      <WelcomeBoard isSystemUser />
    ) : (
      renderDashboardForNonSystemUser()
    );

  return isLoading ? (
    <Skeleton variant="rounded" height="100%" />
  ) : (
    <DashboardPageContext.Provider
      value={{ closePortlet, deletePortlet, minimisePortlet }}
    >
      {renderDashboard()}
    </DashboardPageContext.Provider>
  );
};

export default DashboardPage;
