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

import { TextField } from "@mui/material";
import { useContext, useState } from "react";
import * as React from "react";
import ConfirmDialog from "../../components/ConfirmDialog";
import { languageStrings } from "../../util/langstrings";
import { SearchContext } from "../SearchPageHelper";

export interface FavouriteSearchDialogProps {
  /**
   * `true` to open the dialog
   */
  open: boolean;
  /**
   * Fired when the dialog is closed
   */
  closeDialog: () => void;
  /**
   * Fired to add a search definition to user's favourites.
   * @param name Name of a search definition
   */
  onConfirm: (name: string) => Promise<void>;
}

const favouriteSearchStrings = languageStrings.searchpage.favouriteSearch;

/**
 * Provide a Dialog where a search definition can be added to user's favourites.
 */
export const FavouriteSearchDialog = ({
  open,
  onConfirm,
  closeDialog,
}: FavouriteSearchDialogProps) => {
  const [searchName, setSearchName] = useState<string>("");
  const { searchPageErrorHandler } = useContext(SearchContext);
  const confirmHandler = () =>
    onConfirm(searchName).catch(searchPageErrorHandler).finally(closeDialog);

  return (
    <ConfirmDialog
      open={open}
      title={favouriteSearchStrings.title}
      onConfirm={confirmHandler}
      onCancel={closeDialog}
      confirmButtonText={languageStrings.common.action.ok}
      disableConfirmButton={!searchName.trim()}
    >
      <TextField
        label={favouriteSearchStrings.text}
        value={searchName}
        variant="standard"
        autoFocus
        required
        fullWidth
        onChange={(event) => setSearchName(event.target.value)}
      />
    </ConfirmDialog>
  );
};
