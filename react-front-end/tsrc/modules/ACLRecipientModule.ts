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
import * as E from "fp-ts/Either";
import * as EQ from "fp-ts/Eq";
import { constant, flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as ORD from "fp-ts/Ord";
import * as RNEA from "fp-ts/ReadonlyNonEmptyArray";
import * as S from "fp-ts/string";
import * as TE from "fp-ts/TaskEither";
import { Literal, Static, Union } from "runtypes";
import { ACLEntityResolvers } from "./ACLEntityModule";

/**
 * ACL Recipient types:
 * * user: U:xxx-xxx-xxx
 * * group: G:xxx-xxx-xxx
 * * role: R:xxx-xxx-xxx
 * * owner: $OWNER
 * * everyone: *
 * * refer: F:xxx
 * * IP: I:255.255.0.0%2F24
 * * Sso: T:xxx
 */
export const ACLRecipientTypes = {
  User: "U",
  Group: "G",
  Role: "R",
  Everyone: "*",
  Owner: "$OWNER",
  Refer: "F",
  Ip: "I",
  Sso: "T",
};

const ACLRecipientTypesUnion = Union(
  Literal(ACLRecipientTypes.User),
  Literal(ACLRecipientTypes.Group),
  Literal(ACLRecipientTypes.Role),
  Literal(ACLRecipientTypes.Everyone),
  Literal(ACLRecipientTypes.Owner),
  Literal(ACLRecipientTypes.Refer),
  Literal(ACLRecipientTypes.Ip),
  Literal(ACLRecipientTypes.Sso)
);

export type ACLRecipientType = Static<typeof ACLRecipientTypesUnion>;

/**
 * Represents a recipient in the ACL expression.
 *
 * Examples:
 * - [types]: [expressions]
 * - user: U:f4c788d1-6e97-5247-e06c-3d4982b9469a
 * - user: U:trish.ahsam
 * - group: G:d72eb802-0ea6-4384-907a-341ee60628c0
 * - role: R:366ac350-4eaa-4377-8f1c-e1b12da96d64
 * - role: R:ROLE_CONTENT_ADMINISTRATOR
 * - owner: $OWNER
 * - everyone: *
 * - refer: F:https://edalex.com
 * - refer: F:*edalex*
 * - IP: I:255.255.0.0%2F24
 * - Sso: T:moodle
 */
export interface ACLRecipient {
  /** Represents the type of ACL recipient. */
  type: ACLRecipientType;
  /** A human-readable name for the recipient. */
  name?: string;
  /** The raw recipient expression text without type. */
  expression: string;
}

/**
 * Eq for `ACLRecipient` with equality based on the recipient's type and expression.
 */
export const recipientEq: EQ.Eq<ACLRecipient> = EQ.contramap(
  (r: ACLRecipient) => r.type + r.expression
)(S.Eq);

/**
 * Ord for `ACLRecipient` with ordering rule based on the recipient's name.
 */
export const recipientOrd: ORD.Ord<ACLRecipient> = ORD.contramap(
  (r: ACLRecipient) => r.name ?? r.type + r.expression
)(S.Ord);

/**
 * Convert user details to ACL recipient.
 */
export const userToRecipient = ({
  firstName,
  lastName,
  username,
  id,
}: OEQ.UserQuery.UserDetails): ACLRecipient => ({
  name: `${firstName} ${lastName} [${username}]`,
  expression: id,
  type: ACLRecipientTypes.User,
});

/**
 * Convert group details to ACL recipient.
 */
export const groupToRecipient = ({
  name,
  id,
}: OEQ.UserQuery.GroupDetails): ACLRecipient => ({
  name: name,
  expression: id,
  type: ACLRecipientTypes.Group,
});

/**
 * Convert role details to ACL recipient.
 */
export const roleToRecipient = ({
  name,
  id,
}: OEQ.UserQuery.RoleDetails): ACLRecipient => ({
  name: name,
  expression: id,
  type: ACLRecipientTypes.Role,
});

/**
 * Parse a given ACL recipient and return corresponding ACL Recipient name.
 *
 * Recipient Type : Name example
 * User: "Firstname Lastname [username]",
 * Group: "Group name",
 * Role: "Role name",
 * Everyone: "Everyone",
 * Owner: "Owner",
 * Refer: "From https://edalex.com",
 * Ip: "From 195.168.0.1",
 * Sso: "Token ID is moodle",
 */
const generateACLRecipientName =
  ({
    resolveUserProvider,
    resolveGroupProvider,
    resolveRoleProvider,
  }: ACLEntityResolvers) =>
  (recipient: ACLRecipient): TE.TaskEither<string, string> => {
    const { type, expression } = recipient;

    return pipe(
      type,
      ACLRecipientTypesUnion.match(
        (User) =>
          pipe(
            TE.tryCatch<string, OEQ.UserQuery.UserDetails | undefined>(
              () => resolveUserProvider(expression),
              (err) => `Failed to fetch user details: ${err}`
            ),
            TE.chainOptionK<string>(constant(`Can't find user: ${expression}`))(
              flow(
                O.fromNullable,
                O.chainNullableK(
                  (u: OEQ.UserQuery.UserDetails) => userToRecipient(u).name
                )
              )
            )
          ),
        (Group) =>
          pipe(
            TE.tryCatch(
              () => resolveGroupProvider(expression),
              (err) => `Failed to fetch group details: ${err}`
            ),
            TE.chainNullableK<string>(`Can't find group: ${expression}`)(
              (g: OEQ.UserQuery.GroupDetails | undefined) => g?.name
            )
          ),
        (Role) =>
          pipe(
            TE.tryCatch(
              () => resolveRoleProvider(expression),
              (err) => `Failed to fetch role details: ${err}`
            ),
            TE.chainNullableK<string>(`Can't find role: ${expression}`)(
              (r: OEQ.UserQuery.RoleDetails | undefined) => r?.name
            )
          ),
        (Everyone) => TE.right("Everyone"),
        ($OWNER) => TE.right("Owner"),
        (Ip) => TE.right("From " + expression),
        (Refer) => TE.right("From " + expression),
        (Sso) => TE.right("Token ID is " + expression)
      )
    );
  };

/**
 * Show the full raw expression string for an ACL Recipient.
 *
 * For `Everyone` and `Owner`, directly return the expression.
 * For other types return the expression with type prefix.
 */
export const showRecipient = ({ type, expression }: ACLRecipient): string =>
  type === ACLRecipientTypes.Owner || type === ACLRecipientTypes.Everyone
    ? expression
    : `${type}:${expression}`;

/**
 * Show a human-readable string for an ACL Recipient. If `name` is already defined then that will be returned
 * otherwise requests will be made to the server (if required) to determine the human-readable name.
 */
export const showRecipientHumanReadable =
  (providers: ACLEntityResolvers) =>
  (recipient: ACLRecipient): TE.TaskEither<string, string> =>
    recipient.name
      ? TE.right(recipient.name)
      : pipe(recipient, generateACLRecipientName(providers));

/**
 * Parse a given string and return corresponding ACL Recipient type.
 */
const parseACLRecipientType = (
  text: string
): E.Either<string, ACLRecipientType> =>
  pipe(
    text,
    S.split(":"),
    RNEA.head,
    E.fromPredicate(
      ACLRecipientTypesUnion.guard,
      (invalid) => `Failed to parse recipient: ${invalid}`
    )
  );

/**
 * Given an expected ACL Recipient expression, will attempt the parse the string and return a new
 * `ACLRecipient`. However, if there are issues then an error string will be captured in `left`.
 */
export const createACLRecipient = (
  expression: string
): E.Either<string, ACLRecipient> =>
  pipe(
    expression,
    parseACLRecipientType,
    E.map((recipientType) => ({
      expression: pipe(expression, S.split(":"), RNEA.last),
      type: recipientType,
    }))
  );
