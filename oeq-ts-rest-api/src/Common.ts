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
import { Literal, Static, Union } from 'runtypes';
import { is } from 'typescript-is';
import * as Security from './Security';

export type i18nString = string;

export type I18nStrings = Record<string, string>;

export type UuidString = string;

export interface User {
  id: string;
  username?: string;
  firstName?: string;
  lastName?: string;
  emailAddress?: string;
}

export interface EntityLock {
  uuid: string;
  owner: User;
  links: Record<string, string>;
}

export interface BaseEntityExport {
  exportVersion: string;
  lock: EntityLock;
}

export interface BaseEntityReadOnly {
  granted: string[];
}

export interface BaseEntity {
  uuid: string;
  // Attempted to use Date here, but it broke checks with typescript-is. But Date could be used
  // post processing
  modifiedDate?: string;
  createdDate?: string;
  owner?: User;
  name: i18nString;
  nameStrings: I18nStrings;
  description?: i18nString;
  descriptionStrings?: I18nStrings;
  security?: Security.BaseEntitySecurity;
  exportDetails?: BaseEntityExport;
  readonly?: BaseEntityReadOnly;
  links: Record<string, string>;
}

export interface BaseEntityReference {
  uuid: string;
  name?: i18nString;

  // BEWARE: The server model (com.tle.common.interfaces.BaseEntityReference) has 'extras'
  // which means there's potential for additional fields added dynamically at runtime.
}

/**
 * Summary information for a BaseEntity, which should be enough for display purposes and pulling
 * any further information as required (due to the UUID).
 *
 * Although shape wise very similar to `BaseEntityReference`, on the server side this has a
 * concrete implementation.
 */
export interface BaseEntitySummary {
  /**
   * The unique ID of the underlying BaseEntity
   */
  uuid: UuidString;
  /**
   * The default locale human readable name for a BaseEntity
   */
  name: string;
}

/**
 * Helper validator function for checking for an array of `BaseEntitySummary`s implemented via
 * typescript-is.
 *
 * @param instance A potential array of `BaseEntitySummary`s
 */
export const isBaseEntitySummaryArray = (
  instance: unknown
): instance is BaseEntitySummary[] => is<BaseEntitySummary[]>(instance);

export const ItemStatuses = Union(
  Literal('ARCHIVED'),
  Literal('DELETED'),
  Literal('DRAFT'),
  Literal('LIVE'),
  Literal('MODERATING'),
  Literal('PERSONAL'),
  Literal('REJECTED'),
  Literal('REVIEW'),
  Literal('SUSPENDED')
);

export type ItemStatus = Static<typeof ItemStatuses>;

export interface PagedResult<T> {
  start: number;
  length: number;
  available: number;
  results: T[];
  resumptionToken?: string;
}

/**
 * Helper function for a standard validator for BaseEntity  instances wrapped in a PagedResult
 * via typescript-is.
 *
 * @param instance An instance to validate.
 */
export const isPagedBaseEntity = (
  instance: unknown
): instance is PagedResult<BaseEntity> => is<PagedResult<BaseEntity>>(instance);

/**
 * Query params for common to listing endpoints. All are optional!
 */
export interface ListCommonParams {
  /**
   * Search name and description
   */
  q?: string;
  /**
   * Privilege(s) to filter by
   */
  privilege?: string[];
  /**
   * Resumption token for paging
   */
  resumption?: string;
  /**
   * Number of results
   */
  length?: number;
  /**
   * Return full entity (needs VIEW or EDIT privilege)
   */
  full?: boolean;
}
