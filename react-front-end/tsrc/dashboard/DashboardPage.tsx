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
import { Skeleton, Fab, useTheme, alpha } from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import { pipe, constVoid } from "fp-ts/function";
import { monitorForElements } from "@atlaskit/pragmatic-drag-and-drop/element/adapter";
import * as React from "react";
import { sprintf } from "sprintf-js";
import { useCallback, useContext, useEffect, useState } from "react";
import { AppContext } from "../mainui/App";
import {
  HEADER_OFFSET,
  templateDefaults,
  TemplateUpdateProps,
} from "../mainui/Template";
import {
  batchUpdatePortletPreferences,
  deletePortlet as deletePortletApi,
  getCreatablePortlets,
  getDashboardDetails,
} from "../modules/DashboardModule";
import { hasCreatePortletACL } from "../modules/SecurityModule";
import { languageStrings } from "../util/langstrings";
import WelcomeBoard from "./components/WelcomeBoard";
import * as TE from "fp-ts/TaskEither";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import * as E from "fp-ts/Either";
import * as O from "fp-ts/Option";
import * as T from "fp-ts/Task";
import { DashboardEditor } from "./DashboardEditor";
import { DashboardPageContext } from "./DashboardPageContext";
import {
  computeDndPortletNewPosition,
  decodeDndData,
  getMovedPortlets,
  movePortlet,
  updateDashboardDetails,
  updatePortletPreferenceTE,
} from "./DashboardPageHelper";
import { PortletContainer } from "./portlet/PortletContainer";
import { TooltipCustomComponent } from "../components/TooltipCustomComponent";

const { title } = languageStrings.dashboard;
const { editDashboard: editDashboardLabel } = languageStrings.dashboard.editor;
const { errors: dashboardErrors } = languageStrings.dashboard;

const DashboardPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const { appErrorHandler, currentUser } = useContext(AppContext);
  const theme = useTheme();

  const [isLoading, setIsLoading] = useState(true);
  const [dashboardDetails, setDashboardDetails] =
    useState<OEQ.Dashboard.DashboardDetails>();
  const [hasCreatePortletAcl, setHasCreatePortletAcl] = useState<boolean>();
  const [openDashboardEditor, setOpenDashboardEditor] =
    useState<boolean>(false);
  const [restoredPortletId, setRestoredPortletId] = useState<
    undefined | string
  >(undefined);

  const [creatablePortletTypes, setCreatablePortletTypes] = useState<
    OEQ.Dashboard.PortletCreatable[]
  >([]);

  const checkCreatePortletAcl = useCallback(
    (): T.Task<void> =>
      pipe(
        hasCreatePortletACL,
        TE.match(appErrorHandler, setHasCreatePortletAcl),
      ),
    [appErrorHandler],
  );

  const loadDashboard = useCallback(
    (restoredPortletId?: string): T.Task<void> =>
      pipe(
        TE.tryCatch(
          () => getDashboardDetails(),
          (e) => sprintf(dashboardErrors.failedToGetDashboardDetails, `${e}`),
        ),
        TE.match(appErrorHandler, (details) => {
          setDashboardDetails(details);
          restoredPortletId && setRestoredPortletId(restoredPortletId);
        }),
      ),
    [appErrorHandler],
  );

  const getCreatablePortletTypes = useCallback(
    (): T.Task<void> =>
      pipe(
        TE.tryCatch(getCreatablePortlets, (e) =>
          sprintf(dashboardErrors.failedToGetCreatablePortlets, `${e}`),
        ),
        TE.match(appErrorHandler, setCreatablePortletTypes),
      ),
    [appErrorHandler],
  );

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(title)(tp),
    }));
  }, [updateTemplate]);

  useEffect(() => {
    setIsLoading(true);
    pipe(
      T.sequenceArray([
        loadDashboard(),
        checkCreatePortletAcl(),
        getCreatablePortletTypes(),
      ]),
      T.tapIO(() => () => setIsLoading(false)),
    )();
  }, [loadDashboard, checkCreatePortletAcl, getCreatablePortletTypes]);

  // Register drag-and-drop monitoring for dashboard portlets.
  useEffect(
    () =>
      monitorForElements({
        onDrop(args) {
          if (!dashboardDetails) {
            return;
          }

          const portletSourceData = args.source.data;
          const targets = args.location.current.dropTargets;

          pipe(
            decodeDndData(portletSourceData, targets),
            E.bindTo("dndData"),
            E.bind("newPosition", ({ dndData }) =>
              computeDndPortletNewPosition(dndData),
            ),
            E.chain(({ dndData, newPosition }) =>
              pipe(
                dashboardDetails,
                movePortlet(dndData.sourceDndData.portlet, newPosition),
              ),
            ),
            E.map((updatedPortlets) => {
              const movedPortlets = getMovedPortlets(
                dashboardDetails.portlets,
                updatedPortlets,
              );

              // Update local state.
              setDashboardDetails((oldDashboard) => ({
                ...oldDashboard,
                portlets: updatedPortlets,
              }));

              // Save new position to server and reload dashboard.
              pipe(
                batchUpdatePortletPreferences(movedPortlets),
                TE.mapLeft((e) => {
                  appErrorHandler(`Failed to update portlet positions: ${e}`);
                  // Reload dashboard to reset state.
                  loadDashboard();
                }),
              )();
            }),
            E.mapLeft(appErrorHandler),
          );
        },
      }),
    [appErrorHandler, dashboardDetails, loadDashboard],
  );

  useEffect(() => {
    if (dashboardDetails && restoredPortletId) {
      const targetElement = document.getElementById(
        `portlet-${restoredPortletId}`,
      );

      if (targetElement) {
        const rect = targetElement.getBoundingClientRect();
        // If rect.top is less than HEADER_OFFSET, it's behind the app header
        const isHiddenAbove = rect.top < HEADER_OFFSET;

        // If rect.bottom is greater than the window height, it's off-screen
        const isHiddenBelow = rect.bottom > window.innerHeight;

        if (isHiddenAbove || isHiddenBelow) {
          targetElement.scrollIntoView({
            behavior: "smooth",
          });
        }

        const targetCardElement = targetElement.querySelector(".MuiCard-root");
        const highlightColor = theme.palette.secondary.main;

        if (targetCardElement) {
          targetCardElement.animate(
            [
              { backgroundColor: alpha(highlightColor, 0.25) },
              { backgroundColor: alpha(highlightColor, 0) },
            ],
            {
              duration: 3000,
              easing: theme.transitions.easing.easeInOut,
            },
          );
        }
      }
      setRestoredPortletId(undefined);
    }
  }, [dashboardDetails, restoredPortletId, theme]);

  const updatePortletPreferenceAndRefresh = useCallback(
    (uuid: string, pref: OEQ.Dashboard.PortletPreference) =>
      pipe(
        updatePortletPreferenceTE(uuid, pref),
        TE.match(appErrorHandler, constVoid),
        T.tapIO(() => loadDashboard()),
      )(),
    [appErrorHandler, loadDashboard],
  );

  const deletePortletAndRefresh = useCallback(
    (uuid: string) =>
      pipe(
        TE.tryCatch(
          () => deletePortletApi(uuid),
          (e) => sprintf(dashboardErrors.failedToDeletePortlet, `${e}`),
        ),
        TE.match(appErrorHandler, constVoid),
        T.tapIO(() => loadDashboard()),
      )(),
    [appErrorHandler, loadDashboard],
  );

  const closePortlet = useCallback(
    (uuid: string, portletPref: OEQ.Dashboard.PortletPreference) => {
      setDashboardDetails(updateDashboardDetails(uuid));

      return updatePortletPreferenceAndRefresh(uuid, portletPref);
    },
    [updatePortletPreferenceAndRefresh],
  );

  const deletePortlet = useCallback(
    (uuid: string) => {
      setDashboardDetails(updateDashboardDetails(uuid));

      return deletePortletAndRefresh(uuid);
    },
    [deletePortletAndRefresh],
  );

  const minimisePortlet = useCallback(
    (uuid: string, portletPref: OEQ.Dashboard.PortletPreference) => {
      setDashboardDetails(updateDashboardDetails(uuid, portletPref));

      return updatePortletPreferenceAndRefresh(uuid, portletPref);
    },
    [updatePortletPreferenceAndRefresh],
  );

  const editDashboardButton = (
    <TooltipCustomComponent
      title={editDashboardLabel}
      sx={{ position: "fixed", bottom: 24, right: 24 }}
    >
      <Fab
        color="secondary"
        aria-label={editDashboardLabel}
        onClick={() => setOpenDashboardEditor(true)}
      >
        <EditIcon />
      </Fab>
    </TooltipCustomComponent>
  );

  const renderDashboardForNonSystemUser = () =>
    pipe(
      O.Do,
      O.apS("details", O.fromNullable(dashboardDetails)),
      O.bind("portlets", ({ details: { portlets } }) =>
        pipe(portlets, O.fromPredicate(A.isNonEmpty)),
      ),
      O.bind("layout", ({ details: { layout } }) => O.some(layout)),
      O.fold(
        () => <WelcomeBoard hasCreatePortletAcl={hasCreatePortletAcl} />,
        ({ layout, portlets }) => (
          <PortletContainer portlets={portlets} layout={layout} />
        ),
      ),
      (mainContent) => (
        <>
          {mainContent}
          {editDashboardButton}
        </>
      ),
    );

  const renderDashboard = () =>
    currentUser?.isSystem ? (
      <WelcomeBoard isSystemUser />
    ) : (
      <>
        {renderDashboardForNonSystemUser()}
        {openDashboardEditor && (
          <DashboardEditor
            onClose={() => setOpenDashboardEditor(false)}
            creatablePortletTypes={creatablePortletTypes}
          />
        )}
      </>
    );

  return isLoading ? (
    <Skeleton variant="rounded" height="100%" />
  ) : (
    <DashboardPageContext.Provider
      value={{
        closePortlet,
        deletePortlet,
        minimisePortlet,
        refreshDashboard: loadDashboard,
        dashboardDetails,
      }}
    >
      {renderDashboard()}
    </DashboardPageContext.Provider>
  );
};

export default DashboardPage;
