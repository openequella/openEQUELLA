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
import { Folder } from "@mui/icons-material";
import {
  Box,
  Chip,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { useHistory } from "react-router";
import { OLD_DASHBOARD_PATH } from "../../mainui/routes";
import { ChangeRoute, submitRequest } from "../../modules/LegacyContentModule";

export interface TasksListProps {
  /** The top level count item */
  group: OEQ.Task.TaskFilterCount;
  /** Individual count items */
  items: OEQ.Task.TaskFilterCount[];
  /** Icon to display for items */
  itemIcon: React.ReactElement;
}

interface TaskItemDisplayProps {
  /** The task item to display */
  item: OEQ.Task.TaskFilterCount;
}

/**
 * Component that displays the name and count for a task/notification item.
 * Used for both parent groups and child items to maintain consistent styling.
 */
const TaskItemDisplay: React.FC<TaskItemDisplayProps> = ({ item }) => (
  <Box display="flex" alignItems="center" gap={1}>
    <span>{item.name || item.id}</span>
    {item.count > 0 && <Chip label={item.count} color="primary" size="small" />}
  </Box>
);

/**
 * Component that renders a nested list of task/notification items with a parent category
 * and its children, similar to how FavouriteItemsTab handles favourite items.
 */
export const TasksList: React.FC<TasksListProps> = ({
  group,
  items,
  itemIcon,
}) => {
  const history = useHistory();

  // This is a little bit specialised - clicking on an item submits a legacy request to
  // navigate to the relevant task/notification page. Because before the user goes to that page,
  // a request is submitted to set up the relevant search/filter in the server session to be
  // rendered by the legacy UI.
  const onClick = (taskid: string) => () =>
    submitRequest<ChangeRoute>(OLD_DASHBOARD_PATH, {
      event__: ["pptl.execSearch"],
      eventp__0: [taskid],
    }).then(({ route }) => history.push(`/${route}`));

  const parentListItem = (
    <ListItemButton key={group.id} onClick={onClick(group.id)}>
      <ListItemIcon>
        <Folder />
      </ListItemIcon>
      <ListItemText primary={<TaskItemDisplay item={group} />} />
    </ListItemButton>
  );

  const childItems = pipe(
    items,
    A.map((item) => (
      <ListItemButton key={item.id} sx={{ pl: 4 }} onClick={onClick(item.id)}>
        <ListItemIcon>{itemIcon}</ListItemIcon>
        <ListItemText primary={<TaskItemDisplay item={item} />} />
      </ListItemButton>
    )),
  );

  return (
    <List dense>
      {parentListItem}
      {childItems}
    </List>
  );
};
