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
/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Alert, Skeleton } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { AppContext } from "../../mainui/App";
import { getClosedPortlets } from "../../modules/DashboardModule";
import { languageStrings } from "../../util/langstrings";
import { PortletSearchResultNoneFound } from "../components/PortletSearchResultNoneFound";
import { ClosedPortletsProvider } from "../DashboardEditor";
import { DashboardPageContext } from "../DashboardPageContext";
import {
  getOrderForRestoredPortlet,
  updatePortletPreferenceTE,
} from "../DashboardPageHelper";
import { ClosedPortletsList } from "./ClosedPortletsList";

const { noClosedPortlets: noClosedPortletsLabel } =
  languageStrings.dashboard.editor.restorePortlet;

interface RestorePortletsTabProps {
  /** Optional provider for closed portlets - primarily used for testing/storybook. */
  closedPortletsProvider?: ClosedPortletsProvider;
}

/**
 * Represents the current state of the request for 'closed portlets' from the server.
 */
type ClosedPortletsRequestState =
  | { state: "loading" }
  | { state: "success"; portlets: OEQ.Dashboard.PortletClosed[] }
  | { state: "failed"; reason: string };

const updateStateByRemovingPortlet = (
  prevState: ClosedPortletsRequestState,
  uuid: string,
): ClosedPortletsRequestState =>
  prevState.state === "success"
    ? {
        state: "success",
        portlets: pipe(
          prevState.portlets,
          A.filter((p) => p.uuid !== uuid),
        ),
      }
    : prevState;

/**
 * This component provides the UI for a user to restore closed portlets.
 */
export const RestorePortletsTab = ({
  closedPortletsProvider = getClosedPortlets,
}: RestorePortletsTabProps) => {
  const { appErrorHandler } = React.useContext(AppContext);
  const { restorePortlet, dashboardDetails } =
    React.useContext(DashboardPageContext);
  const [closedPortlets, setClosedPortlets] =
    React.useState<ClosedPortletsRequestState>({ state: "loading" });

  // This ref is used to avoid race condition in case of rapid-clicks to restore portlets
  const nextOrderRef = React.useRef<number>(0);

  const fetchClosedPortlets = React.useCallback(
    () =>
      pipe(
        TE.tryCatch(() => closedPortletsProvider(), String),
        TE.match(
          (e) => {
            appErrorHandler(e);
            setClosedPortlets({ state: "failed", reason: e });
          },
          (portlets) => setClosedPortlets({ state: "success", portlets }),
        ),
      ),
    [appErrorHandler, closedPortletsProvider],
  );

  React.useEffect(() => {
    const calculatedOrder = getOrderForRestoredPortlet(dashboardDetails);
    if (calculatedOrder >= nextOrderRef.current) {
      nextOrderRef.current = calculatedOrder;
    }
  }, [dashboardDetails]);

  React.useEffect(() => {
    fetchClosedPortlets()();
  }, [fetchClosedPortlets]);

  const onPortletRestore = React.useCallback(
    (uuid: string) => {
      setClosedPortlets((prev) => updateStateByRemovingPortlet(prev, uuid));

      const orderToUse = nextOrderRef.current;
      // This ensures the next click gets expected order number, even if dashboardDetails hasn't updated yet.
      nextOrderRef.current = nextOrderRef.current + 1;

      pipe(
        updatePortletPreferenceTE(uuid, {
          isClosed: false,
          isMinimised: false,
          column: 0,
          order: orderToUse,
        }),
        TE.match(
          (e) => {
            appErrorHandler(e);
            fetchClosedPortlets()();
          },
          () => restorePortlet(uuid),
        ),
      )();
    },
    [restorePortlet, fetchClosedPortlets, appErrorHandler],
  );

  const renderContent = pipe(closedPortlets, (cp) => {
    switch (cp.state) {
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
        if (A.isNonEmpty(cp.portlets)) {
          return (
            <ClosedPortletsList
              closedPortlets={cp.portlets}
              onPortletRestore={onPortletRestore}
            />
          );
        } else {
          return (
            <PortletSearchResultNoneFound
              noneFoundMessage={noClosedPortletsLabel}
            />
          );
        }
      case "failed":
        return <Alert severity="error">{cp.reason}</Alert>;
    }
  });

  return renderContent;
};
