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
  Chip,
  FormControl,
  FormControlLabel,
  FormLabel,
  Grid,
  Radio,
  RadioGroup,
  TextField,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { useState } from "react";
import * as React from "react";
import ConfirmDialog from "../../components/ConfirmDialog";
import type { FavouriteItemVersionOption } from "../../modules/FavouriteModule";
import { languageStrings } from "../../util/langstrings";

/**
 * Type for the two actions performed in this dialog.
 */
export type FavDialogConfirmAction = "add" | "delete";

/**
 * Type for the function fired to add a favourite Item.
 */
type FavDialogConfirmToAdd = (
  tags: string[],
  isAlwaysLatest: boolean
) => Promise<void>;

/**
 * Type for the function fired to delete a favourite Item.
 */
type FavDialogConfirmToDelete = () => Promise<void>;

/**
 * A union type for the function fired when clicking the Confirm button.
 */
type FavDialogConfirmFunction =
  | FavDialogConfirmToAdd
  | FavDialogConfirmToDelete;

/**
 * Type guard to help determine the type of the onConfirm handler.
 */
const isConfirmToDelete = (
  action: FavDialogConfirmAction,
  func: FavDialogConfirmFunction
): func is FavDialogConfirmToDelete => action === "delete";

export interface FavouriteItemDialogProps {
  /**
   * `true` to open the dialog
   */
  open: boolean;
  /**
   * Fired when the dialog is closed
   */
  closeDialog: () => void;
  /**
   * `true` if the Item is already in favourites
   */
  isAddedToFavourite: boolean;
  /**
   * `true` if the Item is on its latest version
   */
  isOnLatestVersion: boolean;
  /**
   * Literal string indicating what action will be performed when the Confirm button is clicked
   */
  action: FavDialogConfirmAction;
  /**
   * The handler for clicking the Confirm button. Type of this handler depends on the value of 'action'.
   * Must call type guard 'isConfirmToDelete' first to narrow down its type.
   */
  onConfirm: FavDialogConfirmFunction;
}

// Type which has partial FavouriteItemDialogProps for the ease of collecting some Item information from other components.
export type FavouriteItemInfo = Pick<
  FavouriteItemDialogProps,
  "isAddedToFavourite" | "isOnLatestVersion" | "action" | "onConfirm"
>;

const {
  title: { add: addString, remove: removeString },
  removeAlert: removeAlertString,
  tags: tagsString,
} = languageStrings.searchpage.favouriteItem;

/**
 * Build a Grid as the dialog's content when the dialog is used for adding a favourite Item.
 */
const AddFavouriteItemContent = ({
  setTags,
  setVersionOption,
  isOnLatestVersion,
}: {
  setTags: (tags: string[]) => void;
  setVersionOption: (version: FavouriteItemVersionOption) => void;
  isOnLatestVersion: boolean;
}) => (
  <Grid container direction="column" spacing={2}>
    <Grid item>
      <Autocomplete
        multiple
        freeSolo
        renderTags={(value: string[], getTagProps) =>
          value.map((option: string, index: number) => (
            <Chip label={option} {...getTagProps({ index })} />
          ))
        }
        renderInput={(params) => (
          <TextField {...params} label={tagsString.description} />
        )}
        options={[]}
        onChange={(_, value: string[]) => setTags(value)}
      />
    </Grid>
    <Grid item>
      {isOnLatestVersion ? (
        <FormControl>
          <FormLabel>{tagsString.selectVersion}</FormLabel>
          <RadioGroup
            row
            onChange={(event) =>
              setVersionOption(event.target.value as FavouriteItemVersionOption)
            }
            defaultValue="latest"
          >
            <FormControlLabel
              value="latest"
              control={<Radio />}
              label={tagsString.versionOptions.useLatestVersion}
            />
            <FormControlLabel
              value="this"
              control={<Radio />}
              label={tagsString.versionOptions.useThisVersion}
            />
          </RadioGroup>
        </FormControl>
      ) : (
        tagsString.toThisVersion
      )}
    </Grid>
  </Grid>
);

/**
 * Provide a Dialog where an Item can be added to or removed from user's favourites.
 */
export const FavouriteItemDialog = ({
  open,
  closeDialog,
  isAddedToFavourite,
  isOnLatestVersion,
  action,
  onConfirm,
}: FavouriteItemDialogProps) => {
  const [tags, setTags] = useState<string[]>([]);
  const [
    versionOption,
    setVersionOption,
  ] = useState<FavouriteItemVersionOption>("latest");

  const confirmHandler = () => {
    const doConfirm = isConfirmToDelete(action, onConfirm)
      ? () => onConfirm()
      : () => onConfirm(tags, versionOption === "latest");

    doConfirm().finally(() => {
      // Need to reset versionOption to match the RadioGroup's default selected value .
      setVersionOption("latest");
      closeDialog();
    });
  };

  return (
    <ConfirmDialog
      open={open}
      title={isAddedToFavourite ? removeString : addString}
      onConfirm={confirmHandler}
      onCancel={closeDialog}
      confirmButtonText={languageStrings.common.action.ok}
    >
      {isAddedToFavourite ? (
        removeAlertString
      ) : (
        <AddFavouriteItemContent
          setTags={setTags}
          setVersionOption={setVersionOption}
          isOnLatestVersion={isOnLatestVersion}
        />
      )}
    </ConfirmDialog>
  );
};
