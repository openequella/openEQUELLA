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
import { List, ListItemButton } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { useHistory } from "react-router";
import { ListItemContent } from "../components/ListItemContent";
import { buildTaskOnClickHandler } from "./PortletTasksHelper";

export interface TasksListProps {
  /** The top level count item */
  group: OEQ.Task.TaskFilterCount;
  /** Individual count items */
  items: OEQ.Task.TaskFilterCount[];
  /** Icon to display for items */
  itemIcon: React.ReactElement;
}

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

  const onClick = buildTaskOnClickHandler(history, "pptl.execSearch");

  const parentListItem = (
    <ListItemButton key={group.id} onClick={onClick(group.id)}>
      <ListItemContent text={group.name || group.id} count={group.count} />
    </ListItemButton>
  );

  const childItems = pipe(
    items,
    A.map((item) => (
      <ListItemButton key={item.id} sx={{ pl: 4 }} onClick={onClick(item.id)}>
        <ListItemContent
          text={item.name || item.id}
          count={item.count}
          icon={itemIcon}
        />
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
