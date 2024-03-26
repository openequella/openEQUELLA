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
import { pipe } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as O from "fp-ts/Option";
import { groups } from "./GroupModule.mock";
import { roles } from "./RoleModule.mock";
import { users } from "./UserModule.mock";

const teacherRoleId = roles[2].id;

export const canvas: OEQ.LtiPlatform.LtiPlatform = {
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
  enabled: true,
};

export const moodle: OEQ.LtiPlatform.LtiPlatform = {
  platformId: "http://localhost:8100",
  name: "moodle",
  clientId: "test client moodle",
  authUrl: "http://testmoodle",
  keysetUrl: "http://testmoodle",
  usernamePrefix: "hello",
  usernameSuffix: "world",
  unknownUserHandling: "CREATE",
  unknownUserDefaultGroups: new Set(["deletedGroup1", "deletedGroup2"]),
  instructorRoles: new Set(["deletedRole1"]),
  unknownRoles: new Set(["deletedRole2"]),
  customRoles: new Map([
    [
      "http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator",
      new Set(["deletedRole3"]),
    ],
  ]),
  allowExpression: "",
  enabled: true,
};

export const blackboard: OEQ.LtiPlatform.LtiPlatform = {
  platformId: "http://blackboard:8200",
  name: "other",
  clientId: "test client blackboard",
  authUrl: "http://blackboard/auth",
  keysetUrl: "http://blackboard/jwks",
  usernameClaim: "[https://edalex.com][id]",
  usernamePrefix: "hello",
  usernameSuffix: "blackboard",
  unknownUserHandling: "CREATE",
  unknownUserDefaultGroups: new Set([groups[0].id]),
  instructorRoles: new Set([roles[1].id]),
  unknownRoles: new Set([roles[2].id]),
  customRoles: new Map([["", new Set([roles[1].id])]]),
  allowExpression: `U:${users[0].id}`,
  enabled: false,
};

export const platforms = [canvas, moodle, blackboard];

/**
 * Helper function to inject into component for platforms retrieval.
 */
export const getPlatforms = () => Promise.resolve(platforms);

/**
 * Helper function to inject into component for platform retrieval.
 */
export const getPlatform = async (platformId: string) => {
  // A sleep to emulate latency
  await new Promise((resolve) => setTimeout(resolve, 500));
  return Promise.resolve(
    pipe(
      platforms,
      A.findFirst(
        (p: OEQ.LtiPlatform.LtiPlatform) => p.platformId === platformId,
      ),
      O.getOrElseW(() => {
        throw new Error(`Can't find platform with ID: ${platformId}`);
      }),
    ),
  );
};
