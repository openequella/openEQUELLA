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
  CircularProgress,
  Grid,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  TextField,
  Tooltip,
  Typography,
} from "@material-ui/core";
import AccountCircle from "@material-ui/icons/AccountCircle";
import ErrorOutline from "@material-ui/icons/ErrorOutline";
import InfoIcon from "@material-ui/icons/Info";
import SearchIcon from "@material-ui/icons/Search";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as RA from "fp-ts/ReadonlyArray";
import * as RSET from "fp-ts/ReadonlySet";
import * as React from "react";
import { KeyboardEvent, useState } from "react";
import { sprintf } from "sprintf-js";
import * as UserModule from "../modules/UserModule";
import { languageStrings } from "../util/langstrings";
import { OrdAsIs } from "../util/Ord";

const {
  failedToFindUsersMessage,
  filterActiveNotice,
  filteredByPrelude,
  queryFieldLabel,
} = languageStrings.userSearchComponent;

export interface UserSearchProps {
  /** An optional `id` attribute for the component. Will also be used to prefix core child elements. */
  id?: string;
  /** How high (in pixels) the list of users should be. */
  listHeight?: number;
  /** Callback triggered when a user entry is clicked on. */
  onSelect: (username: OEQ.UserQuery.UserDetails) => void;
  /** A list of group UUIDs to filter the users by. */
  groupFilter?: ReadonlySet<string>;
  /** Function which will provide the list of users. */
  userListProvider?: (
    query?: string,
    groupFilter?: ReadonlySet<string>
  ) => Promise<OEQ.UserQuery.UserDetails[]>;
}

/**
 * Provides a control to list users via an input field text query filter. Users can then
 * be selected (single select).
 */
const UserSearch = ({
  id,
  listHeight,
  onSelect,
  groupFilter,
  userListProvider = (query?: string, groupFilter?: ReadonlySet<string>) =>
    UserModule.listUsers(query ? `${query}*` : undefined, groupFilter),
}: UserSearchProps) => {
  const [query, setQuery] = useState<string>("");
  const [users, setUsers] = useState<OEQ.UserQuery.UserDetails[]>([]);
  const [selectedUser, setSelectedUser] = useState<
    OEQ.Common.UuidString | undefined
  >(undefined);
  const [hasSearched, setHasSearched] = useState<boolean>(false);
  const [showSpinner, setShowSpinner] = useState<boolean>(false);

  // Simple helper function to assist with providing useful id's for testing and theming.
  const genId = (suffix?: string) =>
    (id ? `${id}-` : "") + "UserSearch" + (suffix ? `-${suffix}` : "");

  const handleOnSearch = () => {
    setShowSpinner(true);
    userListProvider(query, groupFilter)
      .then((userDetails: OEQ.UserQuery.UserDetails[]) => {
        setHasSearched(true);
        setUsers(
          userDetails.sort((a, b) =>
            a.username.toLowerCase().localeCompare(b.username.toLowerCase())
          )
        );
      })
      .finally(() => setShowSpinner(false));
  };

  const handleQueryFieldKeypress = (event: KeyboardEvent<HTMLDivElement>) => {
    switch (event.key) {
      case "Escape":
        setQuery("");
        setUsers([]);
        setSelectedUser(undefined);
        setHasSearched(false);
        event.stopPropagation();
        break;
      case "Enter":
        handleOnSearch();
        break;
    }
  };

  const queryBar = (
    <Grid id={genId("QueryBar")} container spacing={1}>
      <Grid item>
        <IconButton onClick={handleOnSearch}>
          <SearchIcon />
        </IconButton>
      </Grid>
      <Grid item style={{ flexGrow: 1 }}>
        <TextField
          label={queryFieldLabel}
          value={query}
          onChange={(event) => {
            setHasSearched(false);
            setQuery(event.target.value);
          }}
          onKeyDown={handleQueryFieldKeypress}
          fullWidth
        />
      </Grid>
    </Grid>
  );

  const filterActive = groupFilter && !RSET.isEmpty(groupFilter);
  const filterDetails = (): JSX.Element | undefined => {
    if (!filterActive) {
      return undefined;
    }

    const toolTipContent: JSX.Element = (
      <>
        <Typography variant="caption">{filteredByPrelude}</Typography>
        <ul>
          {pipe(
            groupFilter,
            // TODO, come back and retrieve actual group names
            RSET.toReadonlyArray<string>(OrdAsIs),
            RA.map((g) => <li key={g}>{g}</li>)
          )}
        </ul>
      </>
    );

    return (
      <Grid container spacing={1}>
        <Grid item>
          <Tooltip title={toolTipContent}>
            <InfoIcon fontSize="small" />
          </Tooltip>
        </Grid>
        <Grid item>
          <Typography variant="caption">{filterActiveNotice}</Typography>
        </Grid>
      </Grid>
    );
  };

  const userList = () => {
    // If there's no users because a search has not been done,
    // then return with nothing
    if (users.length < 1 && !hasSearched) {
      return null;
    }

    return (
      <List
        id={genId("UserList")}
        style={listHeight ? { height: listHeight, overflow: "auto" } : {}}
      >
        {users.length ? (
          users.map((userDetails: OEQ.UserQuery.UserDetails) => (
            <ListItem
              button
              onClick={() => {
                setSelectedUser(userDetails.id);
                onSelect(userDetails);
              }}
              key={userDetails.id}
              selected={selectedUser === userDetails.id}
            >
              <ListItemIcon>
                <AccountCircle />
              </ListItemIcon>
              <ListItemText
                primary={userDetails.username}
                secondary={`${userDetails.firstName} ${userDetails.lastName}`}
              />
            </ListItem>
          ))
        ) : (
          <ListItem>
            <ListItemIcon>
              <ErrorOutline />
            </ListItemIcon>
            <ListItemText
              secondary={sprintf(failedToFindUsersMessage, query)}
            />
          </ListItem>
        )}
      </List>
    );
  };

  const spinner = (
    <Grid container justifyContent="center">
      <Grid item>
        <CircularProgress />
      </Grid>
    </Grid>
  );

  return (
    <Grid id={genId()} container direction="column" spacing={1}>
      <Grid item xs={12}>
        {queryBar}
      </Grid>
      {filterActive && <Grid item>{filterDetails()}</Grid>}
      <Grid item xs={12}>
        {showSpinner ? spinner : userList()}
      </Grid>
    </Grid>
  );
};

export default UserSearch;
