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
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
} from "@material-ui/core";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useState } from "react";
import { languageStrings } from "../util/langstrings";
import UserSearch from "./UserSearch";

interface SelectUserDialogProps {
  /** Controls displaying of dialog. */
  open: boolean;
  /** Handler for when dialog closes. */
  onClose: (selection?: OEQ.UserQuery.UserDetails) => void;
  /** Function which will provide the list of users for UserSearch. */
  userListProvider?: (query?: string) => Promise<OEQ.UserQuery.UserDetails[]>;
}

/**
 * Simple dialog to prompt user to search and select a user to use in the owner filter.
 */
export const SelectUserDialog = ({
  open,
  onClose,
  userListProvider,
}: SelectUserDialogProps) => {
  const [selectedUser, setSelectedUser] = useState<
    OEQ.UserQuery.UserDetails | undefined
  >(undefined);

  const handleClose = () => {
    onClose(selectedUser);
    setSelectedUser(undefined);
  };

  return (
    <Dialog open={open} onClose={handleClose} fullWidth>
      <DialogTitle>
        {languageStrings.searchpage.filterOwner.selectTitle}
      </DialogTitle>
      <DialogContent>
        <UserSearch
          onSelect={setSelectedUser}
          userListProvider={userListProvider}
          listHeight={300}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={() => onClose()} color="primary">
          {languageStrings.common.action.cancel}
        </Button>
        <Button
          onClick={handleClose}
          color="primary"
          autoFocus
          disabled={!selectedUser}
        >
          {languageStrings.common.action.select}
        </Button>
      </DialogActions>
    </Dialog>
  );
};