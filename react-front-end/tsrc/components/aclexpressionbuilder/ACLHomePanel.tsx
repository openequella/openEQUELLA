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
import * as t from "io-ts";
import {
  FormControl,
  FormControlLabel,
  FormLabel,
  Grid,
  RadioGroup,
  Radio,
} from "@mui/material";
import * as E from "../../util/Either.extended";
import * as React from "react";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as RSET from "fp-ts/ReadonlySet";
import { ChangeEvent, useState } from "react";
import {
  ACLRecipient,
  groupToRecipient,
  recipientEq,
  roleToRecipient,
  userToRecipient,
} from "../../modules/ACLRecipientModule";
import { languageStrings } from "../../util/langstrings";
import { simpleUnionMatch } from "../../util/match";
import GroupSearch from "../securityentitysearch/GroupSearch";
import RoleSearch from "../securityentitysearch/RoleSearch";
import UserSearch from "../securityentitysearch/UserSearch";

/**
 * Runtypes definition for home panel search filter type.
 */
const SearchFilterTypesUnion = t.union([
  t.literal("Users"),
  t.literal("Groups"),
  t.literal("Roles"),
]);

type SearchFilterType = t.TypeOf<typeof SearchFilterTypesUnion>;

const {
  aclExpressionBuilder: { type: typeLabel },
} = languageStrings;

export interface ACLHomePanelProps {
  /**
   * Handler for `add` button in each EntitySearch.
   * Triggered if user select a/some entities which can be formed a/some valid recipients.
   */
  onAdd: (recipients: ReadonlySet<ACLRecipient>) => void;
  /**
   * Function used to replace the default `search` prop for `UserSearch` component.
   */
  searchUserProvider?: (
    query?: string,
    filter?: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.UserDetails[]>;
  /**
   * Function used to replace the default `search` prop for `GroupSearch` component.
   */
  searchGroupProvider?: (
    query?: string,
    filter?: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
  /**
   * Function used to replace the default `search` prop for `RoleSearch` component.
   */
  searchRoleProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
  /**
   * Function used to replace the default `resolveGroupsProvider` prop for `UserSearch` and `GroupSearch` component.
   */
  resolveGroupsProvider?: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
}

const ACLHomePanel = ({
  onAdd,
  searchUserProvider,
  searchGroupProvider,
  searchRoleProvider,
  resolveGroupsProvider,
}: ACLHomePanelProps) => {
  const [activeSearchFilterType, setActiveSearchFilterType] =
    useState<SearchFilterType>("Users");

  const handleSearchFilterChange = (event: ChangeEvent<HTMLInputElement>) =>
    pipe(
      event.target.value,
      SearchFilterTypesUnion.decode,
      E.getOrThrow,
      setActiveSearchFilterType,
    );

  const handleOnAdded = <T,>(
    selections: ReadonlySet<T>,
    entityToRecipient: (entity: T) => ACLRecipient,
  ) => pipe(selections, RSET.map(recipientEq)(entityToRecipient), onAdd);

  const sharedProps = {
    listHeight: 300,
    groupFilterEditable: true,
    groupSearch: searchGroupProvider,
    resolveGroupsProvider: resolveGroupsProvider,
    enableMultiSelection: true,
  };

  return (
    <FormControl fullWidth component="fieldset">
      <Grid spacing={4} container direction="row" alignItems="center">
        <Grid>
          <FormLabel>{typeLabel}</FormLabel>
        </Grid>
        <Grid>
          <RadioGroup
            row
            name="searchFilterType"
            value={activeSearchFilterType}
            onChange={handleSearchFilterChange}
          >
            {SearchFilterTypesUnion.types.map((searchType) => (
              <FormControlLabel
                key={searchType.value}
                value={searchType.value}
                control={<Radio />}
                label={searchType.value}
              />
            ))}
          </RadioGroup>
        </Grid>
      </Grid>
      {pipe(
        activeSearchFilterType,
        simpleUnionMatch<SearchFilterType, React.JSX.Element>({
          Users: () => (
            <UserSearch
              key={activeSearchFilterType}
              mode={{
                type: "one_click",
                onAdd: (user) =>
                  handleOnAdded(RSET.singleton(user), userToRecipient),
              }}
              {...sharedProps}
              search={searchUserProvider}
              onSelectAll={(users) => handleOnAdded(users, userToRecipient)}
              showHelpText
            />
          ),
          Groups: () => (
            <GroupSearch
              key={activeSearchFilterType}
              mode={{
                type: "one_click",
                onAdd: (group) =>
                  handleOnAdded(RSET.singleton(group), groupToRecipient),
              }}
              {...sharedProps}
              search={searchGroupProvider}
              onSelectAll={(groups) => handleOnAdded(groups, groupToRecipient)}
              showHelpText
            />
          ),
          Roles: () => (
            <RoleSearch
              key={activeSearchFilterType}
              mode={{
                type: "one_click",
                onAdd: (role) =>
                  handleOnAdded(RSET.singleton(role), roleToRecipient),
              }}
              {...sharedProps}
              search={searchRoleProvider}
              onSelectAll={(roles) => handleOnAdded(roles, roleToRecipient)}
              listHeight={367}
              groupFilterEditable={false}
              showHelpText
            />
          ),
        }),
      )}
    </FormControl>
  );
};

export default ACLHomePanel;
