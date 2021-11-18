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
  Divider,
  Grid,
  List,
  ListItem,
  ListItemIcon,
  ListItemSecondaryAction,
  ListItemText,
} from "@material-ui/core";
import AccountCircleIcon from "@material-ui/icons/AccountCircle";
import AddIcon from "@material-ui/icons/Add";
import ClearIcon from "@material-ui/icons/Clear";
import DeleteIcon from "@material-ui/icons/Delete";
import ErrorIcon from "@material-ui/icons/Error";
import ListIcon from "@material-ui/icons/List";
import * as OEQ from "@openequella/rest-api-client";
import { flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as ORD from "fp-ts/Ord";
import * as RA from "fp-ts/ReadonlyArray";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import React, { useEffect, useState } from "react";
import {
  eqUserById,
  resolveUsers,
  resolveUsersCached,
  UserCache,
  userIds,
} from "../../modules/UserModule";
import { languageStrings } from "../../util/langstrings";
import { SelectUserDialog } from "../SelectUserDialog";
import { TooltipIconButton } from "../TooltipIconButton";
import { WizardControlBasicProps } from "./WizardHelper";
import { WizardLabel } from "./WizardLabel";

const {
  common: { action: commonActionStrings },
  wizard: {
    controls: { userSelector: userSelectorStrings },
  },
} = languageStrings;

/**
 * Repository for cached users with resolveUsersCached. Updated by `updateUserCache`.
 */
let userCache: UserCache = {};
/**
 * Centralised function for updating `userCache`.
 */
const updateUserCache = (newCache: UserCache): void => {
  userCache = newCache;
};

interface ListItemErrorProps {
  /**
   * The error to display.
   */
  error: string;
  /**
   * What do do when the click the clear button.
   */
  onClear: () => void;
}

/**
 * Display component for display any errors in the list of users.
 */
const ListItemError = ({ error, onClear }: ListItemErrorProps): JSX.Element => (
  <ListItem>
    <ListItemIcon>
      <ErrorIcon />
    </ListItemIcon>
    <ListItemText primary={error} />
    <TooltipIconButton title={commonActionStrings.clear} onClick={onClear}>
      <ClearIcon />
    </TooltipIconButton>
  </ListItem>
);

interface ListItemUserProps {
  /**
   * Callback for when the delete button is clicked
   */
  onDelete: () => void;
  /**
   * The details of the user to display
   */
  userDetails: {
    firstName: string;
    lastName: string;
    username: string;
  };
}

/**
 * Display component for showing a user's details in the list of user.
 */
const ListItemUser = ({
  onDelete,
  userDetails: { firstName, lastName, username },
}: ListItemUserProps): JSX.Element => (
  <ListItem>
    <ListItemIcon>
      <AccountCircleIcon />
    </ListItemIcon>
    <ListItemText primary={username} secondary={`${firstName} ${lastName}`} />
    <ListItemSecondaryAction>
      <TooltipIconButton
        aria-label={`${commonActionStrings.delete} ${username}`}
        title={commonActionStrings.delete}
        onClick={onDelete}
      >
        <DeleteIcon />
      </TooltipIconButton>
    </ListItemSecondaryAction>
  </ListItem>
);

export interface WizardUserSelectorProps extends WizardControlBasicProps {
  /**
   * Groups to filter the user selection to.
   */
  groupsFilter: ReadonlySet<string>;
  /**
   * Whether to support selection of multiple users.
   */
  multiple: boolean;
  /**
   * Called when a user adds or removes one of the selected user(s), with the passed parameter
   * being the current set of selections.
   */
  onChange: (_: ReadonlySet<string>) => void;
  /**
   * The currently selected user(s). Each user is represented by their UUID.
   */
  users: ReadonlySet<string>;
  /**
   * Function which will provide the list of users for UserSearch.
   */
  userListProvider?: (query?: string) => Promise<OEQ.UserQuery.UserDetails[]>;
  /**
   * Function which can provide the full details of specified users based on id.
   */
  resolveUsersProvider?: (
    ids: ReadonlyArray<string>
  ) => Promise<OEQ.UserQuery.UserDetails[]>;
}

export const WizardUserSelector = ({
  id,
  label,
  mandatory,
  description,
  multiple,
  onChange,
  users,
  userListProvider,
  resolveUsersProvider = resolveUsers,
}: WizardUserSelectorProps): JSX.Element => {
  const [showSelectUserDialog, setShowSelectUserDialog] =
    useState<boolean>(false);
  const [error, setError] = useState<string>();
  const [fullUsers, setFullUsers] = useState<
    ReadonlySet<OEQ.UserQuery.UserDetails>
  >(new Set());

  // Update `fullUsers` when `users` changes
  useEffect(() => {
    if (RSET.getEq(S.Eq).equals(users, pipe(fullUsers, userIds))) {
      // Already in sync, no action required.
      return;
    }

    console.debug("useEffect(users)", users);

    // Update `fullUsers` but only if there's a change
    const updateFullUsers: (updatedList: typeof fullUsers) => void = flow(
      O.fromPredicate(
        (updatedList) => !RSET.getEq(eqUserById).equals(updatedList, fullUsers)
      ),
      O.map(setFullUsers)
    );

    const processUsersUpdate: T.Task<void> = pipe(
      users,
      // We use the cached version to avoid excessive server calls in the two way binding
      // of `users` and `onChange`
      resolveUsersCached(userCache, updateUserCache, resolveUsersProvider),
      TE.match(setError, updateFullUsers)
    );

    (async () => await processUsersUpdate())();
  }, [users, fullUsers, resolveUsersProvider]);

  const handleCloseSelectUserDialog = (
    selection?: OEQ.UserQuery.UserDetails
  ) => {
    setError(undefined);
    setShowSelectUserDialog(false);

    const callOnChange = (
      updatedFullUsers: ReadonlySet<OEQ.UserQuery.UserDetails>
    ) => pipe(updatedFullUsers, userIds, onChange);

    pipe(
      selection,
      O.fromNullable,
      O.map((u) =>
        multiple
          ? pipe(fullUsers, RSET.insert(eqUserById)(u))
          : RSET.singleton(u)
      ),
      O.map(callOnChange)
    );
  };

  const fullUsersArray: ReadonlyArray<OEQ.UserQuery.UserDetails> = pipe(
    fullUsers,
    RSET.toReadonlyArray<OEQ.UserQuery.UserDetails>(
      ORD.contramap((ud: OEQ.UserQuery.UserDetails) => ud.username)(S.Ord)
    )
  );

  return (
    <>
      <WizardLabel
        mandatory={mandatory}
        label={label}
        description={description}
        labelFor={id}
      />
      <Grid id={id} container spacing={2}>
        <Grid item xs={12} sm={6}>
          <List aria-label={userSelectorStrings.userList}>
            <ListItem key="user selector">
              <ListItemIcon>
                <ListIcon />
              </ListItemIcon>
              <ListItemText primary={userSelectorStrings.selectUsers} />
              <ListItemSecondaryAction>
                <TooltipIconButton
                  title={commonActionStrings.add}
                  onClick={() => setShowSelectUserDialog(true)}
                >
                  <AddIcon />
                </TooltipIconButton>
              </ListItemSecondaryAction>
            </ListItem>
            {!RSET.isEmpty(users) && <Divider />}
            {error && (
              <ListItemError
                error={error}
                onClear={() => setError(undefined)}
              />
            )}
            {pipe(
              fullUsersArray,
              RA.map<OEQ.UserQuery.UserDetails, JSX.Element>(
                ({ id, firstName, lastName, username }) => (
                  <ListItemUser
                    key={id}
                    onDelete={() =>
                      pipe(users, RSET.remove(S.Eq)(id), onChange)
                    }
                    userDetails={{ firstName, lastName, username }}
                  />
                )
              )
            )}
          </List>
        </Grid>
      </Grid>
      <SelectUserDialog
        open={showSelectUserDialog}
        onClose={handleCloseSelectUserDialog}
        userListProvider={userListProvider}
      />
    </>
  );
};
