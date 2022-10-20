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
import { ListItemText, Typography } from "@material-ui/core";
import * as OEQ from "@openequella/rest-api-client";
import { flow, pipe } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as O from "fp-ts/Option";
import * as ORD from "fp-ts/Ord";
import { not } from "fp-ts/Predicate";
import * as RA from "fp-ts/ReadonlyArray";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import * as TA from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import { useEffect, useState } from "react";
import * as React from "react";
import { resolveGroups } from "../../modules/GroupModule";
import { listUsers } from "../../modules/UserModule";
import { languageStrings } from "../../util/langstrings";
import { OrdAsIs } from "../../util/Ord";
import BaseSearch, { CommonEntitySearchProps } from "./BaseSearch";

const { filteredByPrelude } = languageStrings.userSearchComponent;

export interface UserSearchProps
  extends CommonEntitySearchProps<OEQ.UserQuery.UserDetails> {
  userListProvider?: (
    query?: string,
    filter?: ReadonlySet<string>
  ) => Promise<OEQ.UserQuery.UserDetails[]>;
  /** A list of groups UUIDs to filter the user by. */
  groupFilter?: ReadonlySet<string>;
  /**
   * Function which will resolve group IDs to full group details so that the group names can be
   * used for display.
   */
  resolveGroupsProvider?: (
    ids: ReadonlyArray<string>
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
}

/**
 * Provides a control to list users via an input field text query filter.
 * Users can then be selected (support single/multiple select).
 */
const UserSearch = ({
  listHeight,
  userListProvider = (query?: string, groupFilter?: ReadonlySet<string>) =>
    listUsers(query ? `${query}*` : undefined, groupFilter),
  onChange,
  selections,
  enableMultiSelection,
  groupFilter,
  resolveGroupsProvider = resolveGroups,
}: UserSearchProps) => {
  // Group details used for show group's names in the tooltip content
  const [groupDetails, setGroupDetails] = useState<
    OEQ.UserQuery.GroupDetails[]
  >([]);

  useEffect(() => {
    if (!groupFilter) {
      setGroupDetails([]);
      return;
    }

    const retrieveGroupDetails: TA.Task<void> = pipe(
      groupFilter,
      O.fromPredicate(not(RSET.isEmpty)),
      O.map(
        flow(
          RSET.toReadonlyArray<string>(OrdAsIs),
          TE.tryCatchK(
            resolveGroupsProvider,
            (reason) => `Failed to retrieve full group details: ${reason}`
          ),
          TE.match(console.error, setGroupDetails)
        )
      ),
      O.getOrElse(() => TA.fromIO(() => setGroupDetails([])))
    );

    (async () => await retrieveGroupDetails())();
  }, [groupFilter, resolveGroupsProvider]);

  const filterDetails = A.isNonEmpty(groupDetails) ? (
    <>
      <Typography variant="caption">{filteredByPrelude}</Typography>
      <ul>
        {pipe(
          groupDetails,
          RA.sort(
            ORD.contramap((g: OEQ.UserQuery.GroupDetails) => g.name)(S.Ord)
          ),
          RA.map(({ id, name }) => <li key={id}>{name}</li>)
        )}
      </ul>
    </>
  ) : undefined;

  /**
   * A template used to display a user entry in BaseSearch (in CheckboxList).
   */
  const userEntry = ({
    username,
    firstName,
    lastName,
  }: OEQ.UserQuery.UserDetails) => (
    <ListItemText primary={username} secondary={`${firstName} ${lastName}`} />
  );

  return (
    <BaseSearch<OEQ.UserQuery.UserDetails>
      listHeight={listHeight}
      strings={languageStrings.userSearchComponent}
      selections={selections}
      onChange={onChange}
      itemOrd={ORD.contramap((u: OEQ.UserQuery.UserDetails) => u.username)(
        S.Ord
      )}
      itemDetailsToEntry={userEntry}
      itemListProvider={userListProvider}
      enableMultiSelection={enableMultiSelection}
      filterDetails={filterDetails}
    />
  );
};

export default UserSearch;
