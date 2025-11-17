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
import { useContext } from "react";
import * as React from "react";
import { Alert, Grid, Typography } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import { DashboardPageContext } from "../DashboardPageContext";
import { DashboardLayoutSelector } from "./DashboardLayoutSelector";
import { languageStrings } from "../../util/langstrings";
import { AppContext } from "../../mainui/App";
import { pipe, constVoid } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as TE from "fp-ts/TaskEither";
import * as T from "fp-ts/Task";
import * as O from "fp-ts/Option";
import {
  batchUpdatePortletPreferences,
  updateDashboardLayout,
} from "../../modules/DashboardModule";
import {
  isSecondColumnPortlet,
  isTwoColumnLayout,
} from "../portlet/PortletHelper";

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

  const updateDashboardLayoutTask = React.useCallback(
    (newLayout: OEQ.Dashboard.DashboardLayout) =>
      pipe(
        TE.tryCatch(() => updateDashboardLayout(newLayout), String),
        TE.match(appErrorHandler, () => setActiveLayout(newLayout)),
      ),
    [appErrorHandler],
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
        updateDashboardLayoutTask,
        T.flatMap(() => portletsUpdateTask(layout)),
        T.tapIO(() => () => refreshDashboard()),
      )();
    },
    [
      activeLayout,
      refreshDashboard,
      updateDashboardLayoutTask,
      portletsUpdateTask,
    ],
  );

  return dashboardDetails ? (
    <Grid container direction="column" spacing={2}>
      <Grid>
        <Typography variant="body1">{strings.chooseLayout}</Typography>
      </Grid>
      <Grid display="flex" justifyContent="center">
        <DashboardLayoutSelector value={activeLayout} onChange={handleChange} />
      </Grid>
    </Grid>
  ) : (
    <Alert severity="error">{strings.alertNoDashboardDetails}</Alert>
  );
};
