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
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { AppContext } from "../../mainui/App";
import { getClosedPortlets } from "../../modules/DashboardModule";
import { ClosedPortletsProvider } from "../DashboardEditor";
import { DashboardPageContext } from "../DashboardPageContext";
import {
  getOrderForRestoredPortlet,
  updatePortletPreferenceTE,
} from "../DashboardPageHelper";
import { ClosedPortletsList } from "./ClosedPortletsList";

interface RestorePortletsTabProps {
  /** Optional provider for closed portlets - primarily used for testing/storybook. */
  closedPortletsProvider?: ClosedPortletsProvider;
}

/**
 * Represents the different states of the closed portlets data loading process.
 */
export type ClosedPortletsState =
  | { state: "loading" }
  | { state: "success"; results: OEQ.Dashboard.PortletClosed[] }
  | { state: "failed"; reason: string };

const defaultClosedPortletsProvider: ClosedPortletsProvider = () =>
  getClosedPortlets();

const filterOutRestoredPortlet = (
  closedPortlets: ClosedPortletsState,
  uuid: string,
): ClosedPortletsState =>
  closedPortlets.state === "success"
    ? {
        state: "success",
        results: pipe(
          closedPortlets.results,
          A.filter((p) => p.uuid !== uuid),
        ),
      }
    : closedPortlets;

/**
 * This component provides the UI for a user to restore closed portlets.
 */
export const RestorePortletsTab = ({
  closedPortletsProvider = defaultClosedPortletsProvider,
}: RestorePortletsTabProps) => {
  const { restorePortlet, dashboardDetails } =
    React.useContext(DashboardPageContext);
  const [closedPortlets, setClosedPortlets] =
    React.useState<ClosedPortletsState>({ state: "loading" });
  const { appErrorHandler } = React.useContext(AppContext);

  // This ref is used to avoid race condition in case of rapid-clicks to restore portlets
  const nextOrderRef = React.useRef<number>(0);

  React.useEffect(() => {
    const calculatedOrder = getOrderForRestoredPortlet(dashboardDetails);
    if (calculatedOrder >= nextOrderRef.current) {
      nextOrderRef.current = calculatedOrder;
    }
  }, [dashboardDetails]);

  const fetchClosedPortlets = React.useCallback(
    () =>
      pipe(
        TE.tryCatch(() => closedPortletsProvider(), String),
        TE.match(
          (e) => {
            appErrorHandler(e);
            setClosedPortlets({ state: "failed", reason: e });
          },
          (results) => setClosedPortlets({ state: "success", results }),
        ),
      ),
    [appErrorHandler, closedPortletsProvider],
  );

  const onPortletRestore = React.useCallback(
    (uuid: string) => {
      setClosedPortlets((prev) => filterOutRestoredPortlet(prev, uuid));

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

  React.useEffect(() => {
    fetchClosedPortlets()();
  }, [fetchClosedPortlets]);

  return (
    <ClosedPortletsList
      closedPortlets={closedPortlets}
      onPortletRestore={onPortletRestore}
    />
  );
};
