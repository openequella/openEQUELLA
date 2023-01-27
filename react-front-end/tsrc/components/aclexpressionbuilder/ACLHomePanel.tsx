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
import {
  FormControl,
  FormControlLabel,
  FormLabel,
  Grid,
  RadioGroup,
  Radio,
} from "@mui/material";
import * as React from "react";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as RSET from "fp-ts/ReadonlySet";
import { ChangeEvent, useState } from "react";
import { Literal, Static, Union } from "runtypes";
import {
  ACLRecipient,
  groupToRecipient,
  recipientEq,
  roleToRecipient,
  userToRecipient,
} from "../../modules/ACLRecipientModule";
import { languageStrings } from "../../util/langstrings";
import GroupSearch from "../securityentitysearch/GroupSearch";
import RoleSearch from "../securityentitysearch/RoleSearch";
import UserSearch from "../securityentitysearch/UserSearch";

/**
 * Runtypes definition for home panel search filter type.
 */
const SearchFilterTypesUnion = Union(
  Literal("Users"),
  Literal("Groups"),
  Literal("Roles")
);

type SearchFilterType = Static<typeof SearchFilterTypesUnion>;

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
    filter?: ReadonlySet<string>
  ) => Promise<OEQ.UserQuery.UserDetails[]>;
  /**
   * Function used to replace the default `search` prop for `GroupSearch` component.
   */
  searchGroupProvider?: (
    query?: string,
    filter?: ReadonlySet<string>
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
  /**
   * Function used to replace the default `search` prop for `RoleSearch` component.
   */
  searchRoleProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
  /**
   * Function used to replace the default `resolveGroupsProvider` prop for `UserSearch` and `GroupSearch` component.
   */
  resolveGroupsProvider?: (
    ids: ReadonlyArray<string>
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

  const [userSelections, setUserSelections] = useState<
    ReadonlySet<OEQ.UserQuery.UserDetails>
  >(RSET.empty);
  const [groupSelections, setGroupSelections] = useState<
    ReadonlySet<OEQ.UserQuery.GroupDetails>
  >(RSET.empty);
  const [roleSelections, setRoleSelections] = useState<
    ReadonlySet<OEQ.UserQuery.RoleDetails>
  >(RSET.empty);

  const handleSearchFilterChange = (event: ChangeEvent<HTMLInputElement>) =>
    setActiveSearchFilterType(SearchFilterTypesUnion.check(event.target.value));

  const handleOnAdded = <T,>(
    selections: ReadonlySet<T>,
    entityToRecipient: (entity: T) => ACLRecipient
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
        <Grid item>
          <FormLabel>{typeLabel}</FormLabel>
        </Grid>
        <Grid item>
          <RadioGroup
            row
            name="searchFilterType"
            value={activeSearchFilterType}
            onChange={handleSearchFilterChange}
          >
            {SearchFilterTypesUnion.alternatives.map((searchType) => (
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
        SearchFilterTypesUnion.match(
          (Users) => (
            <UserSearch
              key={Users}
              {...sharedProps}
              search={searchUserProvider}
              selections={userSelections}
              onChange={setUserSelections}
              onClearAll={setUserSelections}
              onSelectAll={setUserSelections}
              onAdd={(user) =>
                handleOnAdded(RSET.singleton(user), userToRecipient)
              }
              selectButton={{
                onClick: () => handleOnAdded(userSelections, userToRecipient),
              }}
            />
          ),
          (Groups) => (
            <GroupSearch
              key={Groups}
              {...sharedProps}
              search={searchGroupProvider}
              selections={groupSelections}
              onChange={setGroupSelections}
              onClearAll={setGroupSelections}
              onSelectAll={setGroupSelections}
              onAdd={(group) =>
                handleOnAdded(RSET.singleton(group), groupToRecipient)
              }
              selectButton={{
                onClick: () => handleOnAdded(groupSelections, groupToRecipient),
              }}
            />
          ),
          (Roles) => (
            <RoleSearch
              key={Roles}
              {...sharedProps}
              search={searchRoleProvider}
              selections={roleSelections}
              onChange={setRoleSelections}
              onClearAll={setRoleSelections}
              onSelectAll={setRoleSelections}
              onAdd={(role) =>
                handleOnAdded(RSET.singleton(role), roleToRecipient)
              }
              listHeight={367}
              groupFilterEditable={false}
              selectButton={{
                onClick: () => handleOnAdded(roleSelections, roleToRecipient),
              }}
            />
          )
        )
      )}
    </FormControl>
  );
};

export default ACLHomePanel;
