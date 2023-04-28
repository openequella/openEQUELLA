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
import { roles } from "./RoleModule.mock";

const teacherRoleId = roles[2].id;

const canvas: OEQ.LtiPlatform.LtiPlatform = {
  platformId: "http://localhost:8200",
  name: "canvas",
  clientId: "test client canvas",
  authUrl: "http://test",
  keysetUrl: "http://test",
  usernamePrefix: "hello",
  usernameSuffix: "world",
  unknownUserHandling: "CREATE",
  unknownUserDefaultGroups: new Set("group A"),
  instructorRoles: new Set(teacherRoleId),
  unknownRoles: new Set("builder"),
  customRoles: new Map().set("tutor", new Set("role A")),
  allowExpression: "",
  enabled: false,
};

const moodle: OEQ.LtiPlatform.LtiPlatform = {
  platformId: "http://localhost:8200",
  name: "moodle",
  clientId: "test client moodle",
  authUrl: "http://testmoodle",
  keysetUrl: "http://testmoodle",
  usernamePrefix: "hello",
  usernameSuffix: "world",
  unknownUserHandling: "CREATE",
  unknownUserDefaultGroups: new Set(),
  instructorRoles: new Set(),
  unknownRoles: new Set(),
  customRoles: new Map(),
  allowExpression: "",
  enabled: false,
};

export const platforms = [canvas, moodle];

/**
 * Helper function to inject into component for platforms retrieval.
 */
export const getPlatforms = () => Promise.resolve(platforms);
