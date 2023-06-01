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
import * as EQ from "fp-ts/Eq";
import * as ORD from "fp-ts/Ord";
import * as S from "fp-ts/string";
import { shallowEqual } from "shallow-equal-object";
import { API_BASE_URL } from "../AppConfig";

/**
 * Ord for `OEQ.LtiPlatform.LtiPlatform` with order based on the platform's name.
 */
export const platformOrd: ORD.Ord<OEQ.LtiPlatform.LtiPlatform> = ORD.contramap(
  (p: OEQ.LtiPlatform.LtiPlatform) => p.name
)(S.Ord);

/**
 * Eq for `OEQ.LtiPlatform.LtiPlatform` with equality based on the `shallowEqual`.
 */
export const platformEq = EQ.fromEquals(
  (a: OEQ.LtiPlatform.LtiPlatform, b: OEQ.LtiPlatform.LtiPlatform) =>
    shallowEqual(a, b)
);

/**
 * Provide all platforms in a list.
 */
export const getPlatforms = (): Promise<OEQ.LtiPlatform.LtiPlatform[]> =>
  OEQ.LtiPlatform.getAllPlatforms(API_BASE_URL);

/**
 * Update enabled status for platforms.
 *
 * @params enabledStatus An array of platform id with the new value of enabled status.
 */
export const updateEnabledPlatforms = (
  enabledStatus: OEQ.LtiPlatform.LtiPlatformEnabledStatus[]
): Promise<OEQ.BatchOperationResponse.BatchOperationResponse[]> =>
  OEQ.LtiPlatform.updateEnabledPlatforms(API_BASE_URL, enabledStatus);
