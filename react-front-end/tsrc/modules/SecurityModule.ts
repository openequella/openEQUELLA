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
import { getRenderData } from "../AppConfig";

/**
 * Checks if the current user has the specified ACL granted.
 */
export const hasAcl = async (acl: string): Promise<boolean> => {
  // TODO: CalL REST Module to check if user has a specific permission
  return Promise.resolve(true);
};

/**
 * True if the user has authenticated before the initial rendering of New UI.
 */
export const hasAuthenticated = getRenderData()?.hasAuthenticated ?? false;
