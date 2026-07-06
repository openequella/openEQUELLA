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
import { Alert, Box, Stack, Typography } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { constVoid, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useContext } from "react";
import { AppContext } from "../../mainui/App";
import {
  batchUpdatePortletPreferences,
  updateDashboardLayout,
} from "../../modules/DashboardModule";
import { languageStrings } from "../../util/langstrings";
import { DashboardPageContext } from "../DashboardPageContext";
import {
  isSecondColumnPortlet,
  isTwoColumnLayout,
} from "../portlet/PortletHelper";
import { DashboardLayoutSelector } from "./DashboardLayoutSelector";

const { dashboardLayout: strings } = languageStrings.dashboard.editor;

/**
 * This component provides the UI for a user to select a new layout for their dashboard.
 */
export const DashboardLayout = () => {
  const { dashboardDetails, refreshDashboard } =
    useContext(DashboardPageContext);
  const { appErrorHandler } = useContext(AppContext);

  const [activeLayout, setActiveLayout] = React.useState<
    OEQ.Dashboard.DashboardLayout | undefined
  >(dashboardDetails?.layout);

  const updateDashboardLayoutTE = React.useCallback(
    (newLayout: OEQ.Dashboard.DashboardLayout) =>
      pipe(
        TE.tryCatch(() => updateDashboardLayout(newLayout), String),
        TE.map(() => setActiveLayout(newLayout)),
      ),
    [],
  );

  const getPortletsWithUpdatedPref = React.useCallback(
    (newLayout: OEQ.Dashboard.DashboardLayout) => {
      const isChangeToSingleColumnLayout = (
        newLayout: OEQ.Dashboard.DashboardLayout,
        prevLayout?: OEQ.Dashboard.DashboardLayout,
      ) => isTwoColumnLayout(prevLayout) && !isTwoColumnLayout(newLayout);

      const setPortletColumnToFirstColumn = (
        portlet: OEQ.Dashboard.BasicPortlet,
      ): OEQ.Dashboard.BasicPortlet => ({
        ...portlet,
        commonDetails: { ...portlet.commonDetails, column: 0 },
      });

      return pipe(
        O.fromNullable(dashboardDetails),
        O.filter(({ layout }) =>
          isChangeToSingleColumnLayout(newLayout, layout),
        ),
        O.filter(({ portlets }) => A.isNonEmpty(portlets)),
        O.map(({ portlets }) =>
          pipe(
            portlets,
            A.filter(isSecondColumnPortlet),
            A.map(setPortletColumnToFirstColumn),
          ),
        ),
        O.getOrElse<OEQ.Dashboard.BasicPortlet[]>(() => []),
      );
    },
    [dashboardDetails],
  );

  const portletsUpdateTask = React.useCallback(
    (layout: OEQ.Dashboard.DashboardLayout) =>
      pipe(
        layout,
        getPortletsWithUpdatedPref,
        batchUpdatePortletPreferences,
        TE.match(appErrorHandler, constVoid),
      ),
    [appErrorHandler, getPortletsWithUpdatedPref],
  );

  const handleChange = React.useCallback(
    (layout: OEQ.Dashboard.DashboardLayout) => {
      if (layout === activeLayout) return;

      pipe(
        layout,
        updateDashboardLayoutTE,
        // only update portlet preference if updateDashboardLayout API resolves
        TE.chainTaskK(() => portletsUpdateTask(layout)),
        TE.match(appErrorHandler, constVoid),
        T.flatMap(() => refreshDashboard()),
      )();
    },
    [
      activeLayout,
      refreshDashboard,
      updateDashboardLayoutTE,
      portletsUpdateTask,
      appErrorHandler,
    ],
  );

  return dashboardDetails ? (
    <Stack spacing={2}>
      <Typography variant="body1">{strings.chooseLayout}</Typography>
      <Box sx={{ display: "flex", justifyContent: "center" }}>
        <DashboardLayoutSelector value={activeLayout} onChange={handleChange} />
      </Box>
    </Stack>
  ) : (
    <Alert severity="error">{strings.alertNoDashboardDetails}</Alert>
  );
};
