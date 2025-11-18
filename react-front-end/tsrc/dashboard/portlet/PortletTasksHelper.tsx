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
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import * as A from "fp-ts/Array";
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { languageStrings } from "../../util/langstrings";
import { TasksList } from "./TasksList";

const strings = {
  ...languageStrings.dashboard.portlets.tasks,
};

/**
 * Represents a partitioned group of task filter counts with a parent and its children.
 */
export interface TaskPartition {
  parent: OEQ.Task.TaskFilterCount;
  children: OEQ.Task.TaskFilterCount[];
}

/**
 * Partitions task filter counts by parent category.
 */
export const partitionByParent = (
  counts: OEQ.Task.TaskFilterCount[],
  parentFilter: OEQ.Task.TaskFilter,
): E.Either<string, TaskPartition> => {
  const parent = pipe(
    counts,
    A.findFirst((item) => item.id === parentFilter),
  );

  const children = pipe(
    counts,
    A.filter((item) => item.parent === parentFilter),
  );

  return pipe(
    parent,
    E.fromOption(() => `${strings.unableToFindItemsOfType} ${parentFilter}`),
    E.map((parentItem) => ({
      parent: parentItem,
      children,
    })),
  );
};

/**
 * Helper function to create TasksList component from Optional partition.
 * Maps Option to TasksList component or null.
 */
export const createTasksListMaybe =
  (childIcon: React.ReactElement) => (partition: O.Option<TaskPartition>) =>
    pipe(
      partition,
      O.map(({ parent, children }) => (
        <TasksList group={parent} items={children} itemIcon={childIcon} />
      )),
      O.toNullable,
    );
