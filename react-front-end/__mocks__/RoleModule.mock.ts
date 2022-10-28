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

/**
 * A list of roles to test with role search.
 */
export const roles: OEQ.UserQuery.RoleDetails[] = [
  {
    id: "62ed85d3-278a-46f5-8ee4-391a45f97899",
    name: "Teachers",
  },
  {
    id: "1ffbf760-2970-48d7-ab9f-62e95a64d07e",
    name: "Systems Administrators",
  },
  {
    id: "dc97436d-8e52-40db-abc6-ca198cbe6dae",
    name: "Content Administrators",
  },
  {
    id: "ffff7e1d-bf77-464f-bd41-de89d44a9cc6",
    name: "Student",
  },
];
