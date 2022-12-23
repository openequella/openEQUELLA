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
} from "@mui/material";
import AccountCircle from "@mui/icons-material/AccountCircle";
import ErrorOutline from "@mui/icons-material/ErrorOutline";
import InfoIcon from "@mui/icons-material/Info";
import SearchIcon from "@mui/icons-material/Search";
import * as OEQ from "@openequella/rest-api-client";
import { flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as ORD from "fp-ts/Ord";
import { not } from "fp-ts/Predicate";
import * as RA from "fp-ts/ReadonlyArray";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { KeyboardEvent, useEffect, useState } from "react";
import { sprintf } from "sprintf-js";
import * as GroupModule from "../modules/GroupModule";
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
  /**
   * Function which will resolve group IDs to full group details so that the group names can be
   * used for display.
   */
  resolveGroupsProvider?: (
    ids: ReadonlyArray<string>
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
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
  resolveGroupsProvider = GroupModule.resolveGroups,
}: UserSearchProps) => {
  const [query, setQuery] = useState<string>("");
  const [users, setUsers] = useState<OEQ.UserQuery.UserDetails[]>([]);
  const [groupDetails, setGroupDetails] = useState<
    OEQ.UserQuery.GroupDetails[]
  >([]);
  const [selectedUser, setSelectedUser] = useState<
    OEQ.Common.UuidString | undefined
  >(undefined);
  const [hasSearched, setHasSearched] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<String>();
  const [showSpinner, setShowSpinner] = useState<boolean>(false);

  useEffect(() => {
    if (!groupFilter) {
      setGroupDetails([]);
      return;
    }

    const retrieveGroupDetails: T.Task<void> = pipe(
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
      O.getOrElse(() => T.fromIO(() => setGroupDetails([])))
    );

    (async () => await retrieveGroupDetails())();
  }, [groupFilter, resolveGroupsProvider]);

  // Simple helper function to assist with providing useful id's for testing and theming.
  const genId = (suffix?: string) =>
    (id ? `${id}-` : "") + "UserSearch" + (suffix ? `-${suffix}` : "");

  const handleOnSearch = () => {
    setShowSpinner(true);
    setErrorMessage(undefined);
    userListProvider(query, groupFilter)
      .then((userDetails: OEQ.UserQuery.UserDetails[]) => {
        setUsers(
          userDetails.sort((a, b) =>
            a.username.toLowerCase().localeCompare(b.username.toLowerCase())
          )
        );
      })
      .catch((error: OEQ.Errors.ApiError) => {
        setUsers([]);
        if (error.status !== 404) {
          setErrorMessage(error.message);
        }
      })
      .finally(() => {
        setShowSpinner(false);
        setHasSearched(true);
      });
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
        <IconButton onClick={handleOnSearch} size="large">
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
          variant="standard"
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
            groupDetails,
            RA.sort(
              ORD.contramap((g: OEQ.UserQuery.GroupDetails) => g.name)(S.Ord)
            ),
            RA.map(({ id, name }) => <li key={id}>{name}</li>)
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
              <ErrorOutline color={errorMessage ? "secondary" : "inherit"} />
            </ListItemIcon>
            <ListItemText
              secondary={
                errorMessage ?? sprintf(failedToFindUsersMessage, query)
              }
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
