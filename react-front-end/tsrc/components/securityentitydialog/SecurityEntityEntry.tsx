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
import AccountCircle from "@mui/icons-material/AccountCircle";
import DeleteIcon from "@mui/icons-material/Delete";
import {
  ListItem,
  ListItemIcon,
  ListItemSecondaryAction,
  ListItemText,
} from "@mui/material";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";
import { TooltipIconButton } from "../TooltipIconButton";

const { delete: deleteLabel } = languageStrings.common.action;

export interface SecurityEntityEntryProps {
  /** The name display in the entry. */
  name: string;
  /** Handler for when bin icon is clicked. */
  onDelete: () => void;
  /** Icon to be displayed on the left side of the entry if provided. */
  icon?: React.JSX.Element;
}

/**
 * Used to represent a security entity (e.g. GroupDetails & RoleDetails),
 * and shows its name in the entity list (such as in GroupSelector and RoleSelectors).
 */
const SecurityEntityEntry = ({
  name,
  onDelete,
  icon,
}: SecurityEntityEntryProps) => (
  <ListItem>
    <ListItemIcon>{icon ?? <AccountCircle color="secondary" />}</ListItemIcon>
    <ListItemText style={{ overflowWrap: "anywhere" }} secondary={name} />
    <ListItemSecondaryAction>
      <TooltipIconButton
        title={deleteLabel}
        aria-label={deleteLabel}
        onClick={onDelete}
        size="large"
      >
        <DeleteIcon />
      </TooltipIconButton>
    </ListItemSecondaryAction>
  </ListItem>
);

export default SecurityEntityEntry;
