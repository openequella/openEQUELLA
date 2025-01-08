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
import * as A from "fp-ts/Array";
import * as E from "fp-ts/Either";
import * as EQ from "fp-ts/Eq";
import { constant, flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";

/**
 * The interface defines a basic security entity.
 * It includes a common property "id" that is used across other interfaces like User, Group, and Role.
 * The purpose of creating this base interface is to reuse these common properties in some generic component.
 */
export interface BaseSecurityEntity {
  id: string;
}

/**
 * Contains the functions used to lookup details for individual users, groups or roles.
 * @see {@link ACLEntityResolversMulti} if you need an interface for dealing with multiple instances.
 */
export interface ACLEntityResolvers {
  /**
   * Lookup user known in oEQ.
   */
  resolveUserProvider: (
    id: string,
  ) => Promise<OEQ.UserQuery.UserDetails | undefined>;
  /**
   * Lookup group known in oEQ.
   */
  resolveGroupProvider: (
    id: string,
  ) => Promise<OEQ.UserQuery.GroupDetails | undefined>;
  /**
   * Lookup role known in oEQ.
   */
  resolveRoleProvider: (
    id: string,
  ) => Promise<OEQ.UserQuery.RoleDetails | undefined>;
}

/**
 * Contains the functions used to lookup details for multiple users, groups or roles.
 * @see {@link ACLEntityResolvers} if you only need functions dealing with single instances.
 */
export interface ACLEntityResolversMulti {
  /**
   * Lookup users known in oEQ.
   */
  resolveUsersProvider: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.UserDetails[]>;
  /**
   * Lookup groups known in oEQ.
   */
  resolveGroupsProvider: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
  /**
   * Lookup roles known in oEQ.
   */
  resolveRolesProvider: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

/**
 * Generic function which can generate an `Eq` for entity with id attribute.
 */
export const eqEntityById = <T extends BaseSecurityEntity>() =>
  EQ.contramap<string, T>((entry: T) => entry.id)(S.Eq);

/**
 * Given a set of `Entities`, return a set of UUIDs for all the entities.
 */
export const entityIds: <T extends BaseSecurityEntity>(
  e: ReadonlySet<T>,
) => ReadonlySet<string> = flow(RSET.map(S.Eq)(({ id }) => id));

/**
 * Generic function for finding an entity's details by ID.
 *
 * @param entityId The unique ID of an entity
 * @param resolveEntities The function used to get the entity by id
 */
export const findEntityById = async <T>(
  entityId: string,
  resolveEntities: (ids: ReadonlySet<string>) => Promise<T[]>,
): Promise<T | undefined> =>
  pipe(
    await resolveEntities(RSET.singleton(entityId)),
    E.fromPredicate(
      (a) => a.length <= 1,
      constant(`More than one entity was resolved for id: ${entityId}`),
    ),
    E.map(flow(A.head, O.toUndefined)),
    E.getOrElseW((error) => {
      throw new Error(error);
    }),
  );
