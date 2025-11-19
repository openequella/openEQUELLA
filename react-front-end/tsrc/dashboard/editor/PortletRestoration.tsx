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
import AddIcon from "@mui/icons-material/Add";
import {
  Alert,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Skeleton,
  Tooltip,
} from "@mui/material";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { AppContext } from "../../mainui/App";
import { PortletSearchResultNoneFound } from "../components/PortletSearchResultNoneFound";
import { ClosedPortletsState } from "../DashboardEditor";
import { DashboardPageContext } from "../DashboardPageContext";
import {
  getOrderForRestoredPortlet,
  updatePortletPreferenceTE,
} from "../DashboardPageHelper";
import { constVoid, pipe } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as T from "fp-ts/Task";
import * as OEQ from "@openequella/rest-api-client";
import { languageStrings } from "../../util/langstrings";

const { noClosedPortlets: noClosedPortletsLabel } =
  languageStrings.dashboard.editor.restorePortlet;
const { restore: restoreLabel } = languageStrings.common.action;

export interface PortletRestorationProps {
  /** Current state of closed portlets (loading / success / failed). */
  closedPortlets: ClosedPortletsState;
  /** Task that fetches closed portlets from the API and updates component state. */
  getClosedPortletsTask: () => T.Task<void>;
}

/**
 * This component provides the UI for a user to restore closed portlets.
 */
export const PortletRestoration = ({
  closedPortlets,
  getClosedPortletsTask,
}: PortletRestorationProps) => {
  const { dashboardDetails, refreshDashboard } =
    React.useContext(DashboardPageContext);
  const { appErrorHandler } = React.useContext(AppContext);

  const updatePortletPreferenceTask = React.useCallback(
    (uuid: string, pref: OEQ.Dashboard.PortletPreference): T.Task<void> =>
      pipe(
        updatePortletPreferenceTE(uuid, pref),
        TE.match(appErrorHandler, constVoid),
      ),
    [appErrorHandler],
  );

  const refreshAfterRestoringPortletTask = React.useCallback(
    (uuid: string) =>
      pipe(
        [getClosedPortletsTask(), refreshDashboard(uuid)],
        A.sequence(T.ApplicativePar),
        T.map(constVoid),
      ),
    [getClosedPortletsTask, refreshDashboard],
  );

  const onPortletRestore = React.useCallback(
    (uuid: string): void => {
      pipe(
        updatePortletPreferenceTask(uuid, {
          isClosed: false,
          isMinimised: false,
          column: 0,
          order: getOrderForRestoredPortlet(dashboardDetails),
        }),
        T.chain(() => refreshAfterRestoringPortletTask(uuid)),
      )();
    },
    [
      updatePortletPreferenceTask,
      refreshAfterRestoringPortletTask,
      dashboardDetails,
    ],
  );

  const renderContent = () => {
    switch (closedPortlets.state) {
      case "loading":
        return (
          <Skeleton
            variant="rectangular"
            width="100%"
            height={400}
            data-testid="tab-content-skeleton"
          />
        );
      case "success":
        if (A.isNonEmpty(closedPortlets.results)) {
          return (
            <List data-testid="closed-portlets-list">
              {closedPortlets.results.map(({ name, uuid }) => (
                <ListItem
                  key={uuid}
                  secondaryAction={
                    <Tooltip title={restoreLabel}>
                      <IconButton
                        aria-label={restoreLabel}
                        onClick={() => onPortletRestore(uuid)}
                      >
                        <AddIcon />
                      </IconButton>
                    </Tooltip>
                  }
                >
                  <ListItemText primary={name} />
                </ListItem>
              ))}
            </List>
          );
        } else {
          return (
            <PortletSearchResultNoneFound
              noneFoundMessage={noClosedPortletsLabel}
            />
          );
        }
      case "failed":
        return <Alert severity="error">{closedPortlets.reason}</Alert>;
    }
  };

  return renderContent();
};
