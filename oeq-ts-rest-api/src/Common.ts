import * as Security from './Security';
import { is } from 'typescript-is';

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

export interface PagedResult<T> {
  start: number;
  length: number;
  available: number;
  results: T[];
  resumptionToken?: string;
}

/**
 * Helper function for a standard validator for BaseEntity instances via typescript-is.
 *
 * @param instance An instance to validate.
 */
export const isBaseEntity = (instance: unknown): boolean =>
  is<BaseEntity>(instance);

/**
 * Helper function for a standard validator for BaseEntity  instances wrapped in a PagedResult
 * via typescript-is.
 *
 * @param instance An instance to validate.
 */
export const isPagedBaseEntity = (instance: unknown): boolean =>
  is<PagedResult<BaseEntity>>(instance);
