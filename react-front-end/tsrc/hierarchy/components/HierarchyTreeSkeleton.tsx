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
import {
  ListItem,
  ListItemAvatar,
  ListItemText,
  Skeleton,
} from "@mui/material";
import { TreeView } from "@mui/x-tree-view/TreeView";
import { pipe } from "fp-ts/function";
import * as NEA from "fp-ts/NonEmptyArray";
import * as React from "react";

const HierarchyTreeSkeleton = () => {
  const topicSkeleton = (index: number) => (
    <ListItem key={index}>
      <ListItemAvatar>
        <Skeleton variant="rounded" width={30} height={30} />
      </ListItemAvatar>
      <ListItemText
        primary={<Skeleton variant="text" />}
        secondary={<Skeleton variant="text" />}
      />
    </ListItem>
  );

  return <TreeView>{pipe(NEA.range(1, 3), NEA.map(topicSkeleton))}</TreeView>;
};

export default HierarchyTreeSkeleton;
