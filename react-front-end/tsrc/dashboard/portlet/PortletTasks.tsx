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
import { ChecklistRtl, Notifications } from "@mui/icons-material";
import { Alert } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import * as E from "fp-ts/Either";
import { flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { getTaskAndNotificationCounts } from "../../modules/TaskModule";
import { languageStrings } from "../../util/langstrings";
import { DraggablePortlet } from "../components/DraggablePortlet";
import { PortletBasicProps } from "./PortletHelper";
import {
  createTasksListMaybe,
  partitionByParent,
  TaskPartition,
} from "./PortletTasksHelper";

const strings = {
  ...languageStrings.dashboard.portlets.tasks,
};

export interface PortletTasksProps extends PortletBasicProps {
  /**
   * A provider function to fetch task and notification counts. Primarily for testing.
   */
  tasksProvider?: typeof getTaskAndNotificationCounts;
}

/**
 * Portlet that displays task and notification counts.
 */
export const PortletTasks: React.FC<PortletTasksProps> = ({
  cfg,
  tasksProvider = getTaskAndNotificationCounts,
  ...restProps
}) => {
  const [isLoading, setIsLoading] = React.useState(true);
  const [errorMessage, setErrorMessage] = React.useState<string | undefined>();
  const [tasksPartition, setTasksPartition] = React.useState<
    TaskPartition | undefined
  >(undefined);
  const [notificationsPartition, setNotificationsPartition] = React.useState<
    TaskPartition | undefined
  >(undefined);

  React.useEffect(() => {
    // Clear previous state
    setErrorMessage(undefined);
    setTasksPartition(undefined);
    setNotificationsPartition(undefined);

    const fetchTaskCounts = pipe(
      TE.tryCatch(
        () => tasksProvider(),
        (error) => `${strings.failedToInitialise} [${error}]`,
      ),
      TE.chain(
        flow(
          TE.fromPredicate(
            A.isNonEmpty,
            () => `${strings.failedToInitialise} [${strings.nothingReturned}]`,
          ),
        ),
      ),
    );

    const processTaskCounts = (counts: OEQ.Task.TaskFilterCount[]) => {
      const tasksResult = partitionByParent(counts, "taskall");
      const notificationsResult = partitionByParent(counts, "noteall");

      pipe(
        E.Do,
        E.apS("tasks", tasksResult),
        E.apS("notifications", notificationsResult),
        E.match(
          (error) => setErrorMessage(error),
          ({ tasks, notifications }) => {
            setTasksPartition(tasks);
            setNotificationsPartition(notifications);
          },
        ),
      );
    };

    const fetchAndProcessTask = pipe(
      fetchTaskCounts,
      TE.match((fetchError) => setErrorMessage(fetchError), processTaskCounts),
    );

    pipe(
      fetchAndProcessTask,
      T.tapIO(() => () => setIsLoading(false)),
    )();
  }, [tasksProvider]);

  const tasksList = pipe(
    tasksPartition,
    O.fromNullable,
    createTasksListMaybe(<ChecklistRtl />),
  );

  const notificationsList = pipe(
    notificationsPartition,
    O.fromNullable,
    createTasksListMaybe(<Notifications />),
  );

  return (
    <DraggablePortlet portlet={cfg} isLoading={isLoading} {...restProps}>
      {errorMessage ? (
        <Alert severity="error">{errorMessage}</Alert>
      ) : (
        <>
          {tasksList}
          {notificationsList}
        </>
      )}
    </DraggablePortlet>
  );
};
