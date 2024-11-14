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
import { API_BASE_URL } from "../AppConfig";
/**
 * Get the OIDC settings from the server.
 */
export const getOidcSettings = (): Promise<OEQ.Oidc.IdentityProvider> =>
  OEQ.Oidc.getIdentityProvider(API_BASE_URL);

/**
 * Update the OIDC settings on the server.
 */
export const updateOidcSettings = (
  idp: OEQ.Oidc.IdentityProvider,
): Promise<void> => OEQ.Oidc.updateIdentityProvider(API_BASE_URL, idp);
