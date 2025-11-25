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
  IconButton,
  List,
  ListItem,
  ListItemText,
  Tooltip,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as NEA from "fp-ts/NonEmptyArray";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";

const { restore: restoreLabel } = languageStrings.common.action;

export interface ClosedPortletsListProps {
  /**
   * Non-empty array of closed portlets.
   */
  closedPortlets: NEA.NonEmptyArray<OEQ.Dashboard.PortletClosed>;
  /**
   * Handler invoked when the user chooses to restore a closed portlet.
   *
   * @param uuid UUID of the portlet to restore.
   */
  onPortletRestore: (uuid: string) => void;
}

/**
 * Renders the list for closed portlets, including loading, error and empty states.
 */
export const ClosedPortletsList = ({
  closedPortlets,
  onPortletRestore,
}: ClosedPortletsListProps) => {
  const portletsList = pipe(
    closedPortlets,
    NEA.map(({ name, uuid }) => (
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
  );

  return <List data-testid="closed-portlets-list">{portletsList}</List>;
};
