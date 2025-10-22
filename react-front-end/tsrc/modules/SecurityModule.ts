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
import { flow, pipe } from "fp-ts/function";
import * as TE from "fp-ts/TaskEither";
import * as O from "fp-ts/Option";
import * as IO from "fp-ts/IO";
import { API_BASE_URL, getRenderData } from "../AppConfig";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { getTopicIDFromLocation } from "./HierarchyModule";

/**
 * Represents a TaskEither for permission check. The right side is a boolean indicating whether the
 * permission is granted, and the left side is an error message if the check operation fails.
 */
export type PermissionCheck = TE.TaskEither<string, boolean>;
/**
 * Represents a TaskEither for permission check where the right side is `true` to indicate the permission
 * is granted and the left side is the string representation of the targeted ACL. However, the left side
 * can be an error message if that is more suitable.
 */
export type RequiredPermissionCheck = TE.TaskEither<string, true>;

const buildAclCheckTask = (
  check: () => Promise<boolean>,
  acl: string,
): PermissionCheck =>
  TE.tryCatch(
    check,
    (err) =>
      `Failed to check if ACL ${acl} has been granted to the current user: ${String(err)}`,
  );

const buildRequiredAclCheckTask = (
  check: () => Promise<boolean>,
  acl: string,
): RequiredPermissionCheck =>
  pipe(
    buildAclCheckTask(check, acl),
    TE.chain((has) =>
      has ? TE.right(true) : TE.left(`Current user does not have ACL: ${acl}`),
    ),
  );

/**
 * Checks if the current user has the specified non-entity ACL granted.
 */
const hasAcl = (acl: string): PermissionCheck =>
  buildAclCheckTask(
    () => OEQ.Acl.checkPrivilege(API_BASE_URL, [acl]).then(A.isNonEmpty),
    acl,
  );

/**
 * Checks if the current user has the specified non-entity ACL granted. Unlike `hasAcl`, it only returns TE.right(true)
 * when the ACL is granted, otherwise TE.left(acl)/TE.left(error).
 */
const hasRequiredAcl = (acl: string): RequiredPermissionCheck =>
  buildRequiredAclCheckTask(
    () => OEQ.Acl.checkPrivilege(API_BASE_URL, [acl]).then(A.isNonEmpty),
    acl,
  );

/**
 * Check if the current user has the specified ACL granted for a setting.
 */
const hasRequiredSettingAcl = (
  setting: OEQ.Acl.SETTING,
  acl: string,
): RequiredPermissionCheck =>
  buildRequiredAclCheckTask(
    () => OEQ.Acl.checkSettingPrivilege(API_BASE_URL, setting, acl),
    acl,
  );

/**
 * Check if the current user has the specified ACL granted for a Hierarchy topic.
 */
const hasRequiredHierarchyAcl = (acl: string): RequiredPermissionCheck => {
  const check: (topicID: string | null) => TE.TaskEither<string, boolean> =
    flow(
      O.fromNullable,
      O.match(
        () => TE.left("Topic ID not found"),
        (id) =>
          TE.tryCatch(
            () => OEQ.Acl.checkHierarchyPrivilege(API_BASE_URL, id, acl),
            (_) =>
              `Failed to check if ACL ${acl} has been granted to the current user for topic ${id}`,
          ),
      ),
    );

  return pipe(
    IO.of(getTopicIDFromLocation), // Must use IO to ensure the execution of getTopicIDFromURL happens when the Task is executed
    TE.fromIO,
    TE.map((f) => f()),
    TE.chain(check),
    TE.chain((has) => (has ? TE.right(true) : TE.left(acl))),
  );
};

/**
 * Return a TaskEither to check whether ACL HIERARCHY_PAGE is granted to the current user.
 */
export const isHierarchyPageACLGranted: RequiredPermissionCheck =
  hasRequiredAcl(OEQ.Acl.ACL_HIERARCHY_PAGE);

/**
 * Return a TaskEither to check whether ACL SEARCH_PAGE is granted to the current user.
 */
export const isSearchPageACLGranted: RequiredPermissionCheck = hasRequiredAcl(
  OEQ.Acl.ACL_SEARCH_PAGE,
);

/**
 * Return a TaskEither to check whether ACL MANAGE_CLOUD_PROVIDER is granted to the current user.
 */
export const isManageCloudProviderACLGranted: RequiredPermissionCheck =
  hasRequiredAcl(OEQ.Acl.ACL_MANAGE_CLOUD_PROVIDER);

/**
 * Return a TaskEither to check whether ACL VIEW_HIERARCHY_TOPIC is granted to the current user for
 * the target topic.
 */
export const isViewHierarchyTopicACLGranted: RequiredPermissionCheck =
  hasRequiredHierarchyAcl(OEQ.Acl.ACL_VIEW_HIERARCHY_TOPIC);

/**
 * Return a TaskEither to check whether ACL EDIT_SYSTEM_SETTINGS is granted to the current user
 * for the specified setting.
 */
export const isEditSystemSettingsGranted: (
  setting: OEQ.Acl.SETTING,
) => RequiredPermissionCheck = (setting: OEQ.Acl.SETTING) =>
  hasRequiredSettingAcl(setting, OEQ.Acl.ACL_EDIT_SYSTEM_SETTINGS);

/**
 * True if the user has authenticated before the initial rendering of New UI.
 */
export const hasAuthenticated = getRenderData()?.hasAuthenticated ?? false;

/**
 * Return a TaskEither to check whether ACL CREATE_PORTLET is granted to the current user.
 */
export const hasCreatePortletACL: PermissionCheck = hasAcl(
  OEQ.Acl.ACL_CREATE_PORTLET,
);
