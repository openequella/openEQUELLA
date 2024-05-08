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
import { API_BASE_URL, getRenderData } from "../AppConfig";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";

/**
 * Checks if the current user has the specified ACL granted.
 */
const hasAcl = async (acl: string): Promise<boolean> =>
  OEQ.Acl.checkPrivilege(API_BASE_URL, [acl]).then(A.isNonEmpty);

/**
 * Check if the current user has the specified ACL granted for a setting.
 */
const hasSettingAcl = async (
  setting: OEQ.Acl.SETTING,
  acl: string,
): Promise<boolean> =>
  OEQ.Acl.checkSettingPrivilege(API_BASE_URL, setting, [acl]).then(
    A.isNonEmpty,
  );

/**
 * Return a Promise of boolean to indicate whether ACL HIERARCHY_PAGE is granted to the current user.
 */
export const isHierarchyPageACLGranted = () => hasAcl(OEQ.Acl.HIERARCHY_PAGE);

/**
 * Return a Promise of boolean to indicate whether ACL SEARCH_PAGE is granted to the current user.
 */
export const isSearchPageACLGranted = () => hasAcl(OEQ.Acl.SEARCH_PAGE);

/**
 * Return a Promise of boolean to indicate whether ACL EDIT_SYSTEM_SETTINGS is granted to the current user
 * for the specified setting.
 */
export const isEditSystemSettingsGranted = (setting: OEQ.Acl.SETTING) => () =>
  hasSettingAcl(setting, OEQ.Acl.EDIT_SYSTEM_SETTINGS);

/**
 * Return a Promise of boolean to indicate whether ACL MANAGE_CLOUD_PROVIDER is granted to the current user.
 */
export const isManageCloudProviderACLGranted = () =>
  hasAcl(OEQ.Acl.MANAGE_CLOUD_PROVIDER);

/**
 * True if the user has authenticated before the initial rendering of New UI.
 */
export const hasAuthenticated = getRenderData()?.hasAuthenticated ?? false;
