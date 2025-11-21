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
import * as A from "fp-ts/Array";
import { flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";
import { simpleMatch } from "../../util/match";
import { PortletSearchResultNoneFound } from "../components/PortletSearchResultNoneFound";
import { ClosedPortletsState } from "./RestorePortletsTab";

const { noClosedPortlets: noClosedPortletsLabel } =
  languageStrings.dashboard.editor.restorePortlet;
const { restore: restoreLabel } = languageStrings.common.action;

export interface ClosedPortletsListProps {
  /**
   * Current state of closed portlets, including loading, success and error.
   */
  closedPortlets: ClosedPortletsState;
  /**
   * Handler invoked when the user chooses to restore a closed portlet.
   *
   * @param uuid UUID of the portlet to restore.
   */
  onPortletRestore: (uuid: string) => void;
}

type ClosedPortletsSuccessState = Extract<
  ClosedPortletsState,
  { state: "success" }
>;

type ClosedPortletsFailedState = Extract<
  ClosedPortletsState,
  { state: "failed" }
>;

/**
 * Renders the list for closed portlets, including loading, error and empty states.
 */
export const ClosedPortletsList = ({
  closedPortlets,
  onPortletRestore,
}: ClosedPortletsListProps) => {
  const renderClosedPortlets = () =>
    pipe(
      (closedPortlets as ClosedPortletsSuccessState).results,
      O.fromPredicate(A.isNonEmpty),
      O.map(
        flow(
          A.map(({ name, uuid }) => (
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
          )),
          (items) => <List data-testid="closed-portlets-list">{items}</List>,
        ),
      ),
      O.getOrElse(() => (
        <PortletSearchResultNoneFound
          noneFoundMessage={noClosedPortletsLabel}
        />
      )),
    );

  const content = pipe(
    closedPortlets.state,
    simpleMatch({
      loading: () => (
        <Skeleton
          variant="rectangular"
          width="100%"
          height={400}
          data-testid="tab-content-skeleton"
        />
      ),
      success: () => renderClosedPortlets(),
      failed: () => (
        <Alert severity="error">
          {(closedPortlets as ClosedPortletsFailedState).reason}
        </Alert>
      ),
      _: () => (
        <Alert severity="error">
          Unknown state: {String(closedPortlets.state)}
        </Alert>
      ),
    }),
  );

  return content;
};
