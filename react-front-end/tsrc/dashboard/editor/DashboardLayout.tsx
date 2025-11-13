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
import { DashboardLayoutSelector } from "../components/DashboardLayoutSelector";
import { languageStrings } from "../../util/langstrings";
import { AppContext } from "../../mainui/App";
import { constVoid, pipe } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as TE from "fp-ts/TaskEither";
import * as T from "fp-ts/Task";
import * as O from "fp-ts/Option";
import {
  updateDashboardLayout,
  updatePortletPreference,
} from "../../modules/DashboardModule";
import { isTwoColumnLayout } from "../portlet/PortletHelper";

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
        TE.match(appErrorHandler, constVoid),
      ),
    [appErrorHandler],
  );

  const createUpdatePortletPrefTask = React.useCallback(
    (
      portlet: OEQ.Dashboard.BasicPortlet,
      newColumn: OEQ.Dashboard.PortletColumn,
    ) => {
      const {
        commonDetails: { uuid, isClosed, isMinimised, order },
      } = portlet;
      return pipe(
        TE.tryCatch(
          () =>
            updatePortletPreference(uuid, {
              isMinimised,
              isClosed,
              order,
              column: newColumn,
            }),
          String,
        ),
        TE.match(appErrorHandler, constVoid),
      );
    },
    [appErrorHandler],
  );

  const updatePortletPrefTask = React.useCallback(
    (newLayout: OEQ.Dashboard.DashboardLayout): T.Task<void>[] =>
      pipe(
        O.fromNullable(dashboardDetails),
        O.chain((d) =>
          A.isNonEmpty(d.portlets)
            ? O.some({ prevLayout: d.layout, portlets: d.portlets })
            : O.none,
        ),
        // Only update preferences when going from second column to first column
        O.filter(
          ({ prevLayout }) =>
            isTwoColumnLayout(prevLayout) && !isTwoColumnLayout(newLayout),
        ),
        // Build updatePreference tasks for all second column portlets to update column value to 0
        O.map(({ portlets }) =>
          pipe(
            portlets,
            A.filter((p) => p.commonDetails.column === 1),
            A.map((p) => createUpdatePortletPrefTask(p, 0)),
          ),
        ),
        O.getOrElseW(() => []),
      ),
    [dashboardDetails, createUpdatePortletPrefTask],
  );

  const handleChange = React.useCallback(
    (layout: OEQ.Dashboard.DashboardLayout) => {
      if (layout === activeLayout) return;

      pipe(
        [updateDashboardLayoutTask(layout), ...updatePortletPrefTask(layout)],
        T.sequenceArray,
        T.map(() => {
          setActiveLayout(layout);
          refreshDashboard();
        }),
      )();
    },
    [
      activeLayout,
      updatePortletPrefTask,
      refreshDashboard,
      updateDashboardLayoutTask,
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
