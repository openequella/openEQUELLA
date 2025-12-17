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

export const getTaskAndNotificationCountsResp: OEQ.Task.TaskFilterCount[] = [
  {
    id: "taskall",
    name: "All Tasks",
    count: 9,
  },
  {
    id: "taskme",
    name: "Tasks assigned to me",
    parent: "taskall",
    count: 1,
  },
  {
    id: "taskothers",
    name: "Tasks assigned to others",
    parent: "taskall",
    count: 1,
  },
  {
    id: "tasknoone",
    name: "Unassigned tasks",
    parent: "taskall",
    count: 7,
  },
  {
    id: "taskmust",
    name: "Must moderate",
    parent: "taskall",
    count: 7,
  },
  {
    id: "noteall",
    name: "All notifications",
    count: 4,
  },
  {
    id: "notewentlive",
    name: "Resources gone Live (notified by owner)",
    parent: "noteall",
    count: 0,
  },
  {
    id: "notewentlive2",
    name: "Resources gone Live (watching)",
    parent: "noteall",
    count: 0,
  },
  {
    id: "notemylive",
    name: "Resources gone Live (owned by me)",
    parent: "noteall",
    count: 0,
  },
  {
    id: "noterejected",
    name: "Resources that were rejected",
    parent: "noteall",
    count: 0,
  },
  {
    id: "notebadurl",
    name: "Resources that contain bad URLs",
    parent: "noteall",
    count: 0,
  },
  {
    id: "noteoverdue",
    name: "Resources that are overdue to be moderated",
    parent: "noteall",
    count: 4,
  },
  {
    id: "noteerror",
    name: "Resources that contain script errors",
    parent: "noteall",
    count: 0,
  },
  {
    id: "noteexecuted",
    name: "Resources that have scripts executed",
    parent: "noteall",
    count: 0,
  },
];
