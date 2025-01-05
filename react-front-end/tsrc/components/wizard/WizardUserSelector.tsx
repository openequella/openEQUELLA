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
} from "@mui/material";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import ListIcon from "@mui/icons-material/List";
import * as OEQ from "@openequella/rest-api-client";
import * as E from "fp-ts/Either";
import { flow, identity, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as ORD from "fp-ts/Ord";
import * as RA from "fp-ts/ReadonlyArray";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import {
  eqUserById,
  resolveUsers,
  resolveUsersCached,
  UserCache,
  userIds,
} from "../../modules/UserModule";
import { languageStrings } from "../../util/langstrings";
import { SelectUserDialog } from "../securityentitydialog/SelectUserDialog";
import { TooltipIconButton } from "../TooltipIconButton";
import { WizardControlBasicProps, WizardErrorContext } from "./WizardHelper";
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
  groupFilter: ReadonlySet<string>;
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
  userListProvider?: (
    query?: string,
    groupFilter?: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.UserDetails[]>;
  /**
   * Function which will resolve group IDs to full group details so that the group names can be
   * used for display.
   */
  resolveGroupsProvider?: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
  /**
   * Function which can provide the full details of specified users based on id.
   */
  resolveUsersProvider?: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.UserDetails[]>;
}

export const WizardUserSelector = ({
  id,
  label,
  mandatory,
  description,
  groupFilter,
  multiple,
  onChange,
  users,
  userListProvider,
  resolveGroupsProvider,
  resolveUsersProvider = resolveUsers,
}: WizardUserSelectorProps): React.JSX.Element => {
  const [showSelectUserDialog, setShowSelectUserDialog] =
    React.useState<boolean>(false);
  const [fullUsers, setFullUsers] = React.useState<
    ReadonlySet<OEQ.UserQuery.UserDetails>
  >(new Set());

  const { handleError } = React.useContext(WizardErrorContext);

  // Update `fullUsers` when `users` changes
  React.useEffect(() => {
    if (RSET.getEq(S.Eq).equals(users, pipe(fullUsers, userIds))) {
      // Already in sync, no action required.
      return;
    }

    console.debug("useEffect(users)", users);

    // Update `fullUsers` but only if there's a change
    const updateFullUsers: (updatedList: typeof fullUsers) => void = flow(
      O.fromPredicate(
        (updatedList) => !RSET.getEq(eqUserById).equals(updatedList, fullUsers),
      ),
      O.map(setFullUsers),
    );

    const processUsersUpdate: T.Task<void> = pipe(
      users,
      // We use the cached version to avoid excessive server calls in the two way binding
      // of `users` and `onChange`
      resolveUsersCached(userCache, updateUserCache, resolveUsersProvider),
      // Report any errors
      TE.mapLeft(flow(E.toError, handleError)),
      // But either way ensure we've got something to show
      TE.match(
        () =>
          pipe(
            users,
            RSET.map(eqUserById)((id: string) => ({
              id,
              username: id,
              firstName: S.empty,
              lastName: S.empty,
            })),
          ),
        identity,
      ),
      T.map(updateFullUsers),
    );

    (async () => await processUsersUpdate())();
  }, [fullUsers, handleError, resolveUsersProvider, users]);

  const handleCloseSelectUserDialog = (
    selection?: OEQ.UserQuery.UserDetails,
  ) => {
    setShowSelectUserDialog(false);

    const callOnChange = (
      updatedFullUsers: ReadonlySet<OEQ.UserQuery.UserDetails>,
    ) => pipe(updatedFullUsers, userIds, onChange);

    pipe(
      selection,
      O.fromNullable,
      O.map((u) =>
        multiple
          ? pipe(fullUsers, RSET.insert(eqUserById)(u))
          : RSET.singleton(u),
      ),
      O.map(callOnChange),
    );
  };

  const fullUsersArray: ReadonlyArray<OEQ.UserQuery.UserDetails> = pipe(
    fullUsers,
    RSET.toReadonlyArray<OEQ.UserQuery.UserDetails>(
      ORD.contramap((ud: OEQ.UserQuery.UserDetails) => ud.username)(S.Ord),
    ),
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
                ),
              ),
            )}
          </List>
        </Grid>
      </Grid>
      <SelectUserDialog
        open={showSelectUserDialog}
        onClose={handleCloseSelectUserDialog}
        groupFilter={groupFilter}
        resolveGroupsProvider={resolveGroupsProvider}
        userListProvider={userListProvider}
      />
    </>
  );
};
