import * as Security from './Security';
import {is} from 'typescript-is';

export type i18nString = string;

export type I18nStrings = Record<string, string>;

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

export enum ItemStatus {
  DRAFT = 'DRAFT',
  LIVE = 'LIVE',
  REJECTED = 'REJECTED',
  MODERATING = 'MODERATING',
  ARCHIVED = 'ARCHIVED',
  SUSPENDED = 'SUSPENDED',
  DELETED = 'DELETED',
  REVIEW = 'REVIEW',
  PERSONAL = 'PERSONAL',
}

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
    resumptionToken?: string;
    /**
     * Number of results
     */
    length?: number;
    /**
     * Return full entity (needs VIEW or EDIT privilege)
     */
    full?: boolean;
}
