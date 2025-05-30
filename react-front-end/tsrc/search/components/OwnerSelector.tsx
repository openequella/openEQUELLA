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
  Grid,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemSecondaryAction,
  ListItemText,
} from "@mui/material";
import AccountCircle from "@mui/icons-material/AccountCircle";
import DeleteIcon from "@mui/icons-material/Delete";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useState } from "react";
import { SelectUserDialog } from "../../components/securityentitydialog/SelectUserDialog";
import { languageStrings } from "../../util/langstrings";

export interface OwnerSelectorProps {
  /** The currently selected user or undefined if none. */
  value?: OEQ.UserQuery.UserDetails;
  /** Handler for when current selection is cleared. */
  onClearSelect: () => void;
  /** Handler for when a user is selected. */
  onSelect: (selection: OEQ.UserQuery.UserDetails) => void;
  /** Function which will provide the list of users for UserSearch. */
  userListProvider?: (query?: string) => Promise<OEQ.UserQuery.UserDetails[]>;
}

const OwnerSelector = ({
  value,
  onClearSelect,
  onSelect,
  userListProvider,
}: OwnerSelectorProps) => {
  const [showFindUserDialog, setShowFindUserDialog] = useState<boolean>(false);

  const handleCloseFindUserDialog = (selection?: OEQ.UserQuery.UserDetails) => {
    setShowFindUserDialog(false);
    if (selection) {
      onSelect(selection);
    }
  };

  return (
    <>
      <Grid container alignItems="center" spacing={1}>
        {value ? (
          <Grid size={12}>
            <List disablePadding>
              <ListItem dense>
                <ListItemIcon>
                  <AccountCircle color="secondary" />
                </ListItemIcon>
                <ListItemText
                  style={{ overflowWrap: "anywhere" }}
                  secondary={value.username}
                />
                <ListItemSecondaryAction>
                  <IconButton
                    aria-label={languageStrings.searchpage.filterOwner.clear}
                    onClick={onClearSelect}
                    size="large"
                  >
                    <DeleteIcon />
                  </IconButton>
                </ListItemSecondaryAction>
              </ListItem>
            </List>
          </Grid>
        ) : (
          <Grid>
            <Button
              variant="outlined"
              onClick={() => setShowFindUserDialog(true)}
            >
              {languageStrings.common.action.select}
            </Button>
          </Grid>
        )}
      </Grid>
      <SelectUserDialog
        open={showFindUserDialog}
        onClose={handleCloseFindUserDialog}
        userListProvider={userListProvider}
      />
    </>
  );
};

export default OwnerSelector;
