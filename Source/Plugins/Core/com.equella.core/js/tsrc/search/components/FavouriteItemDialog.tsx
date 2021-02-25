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
import {
  addFavouriteItem,
  deleteFavouriteItem,
  FavouriteItemInfo,
  FavouriteItemVersionOption,
} from "../../modules/FavouriteModule";
import { languageStrings } from "../../util/langstrings";

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
   * An Item to be added to or removed from user's favourites
   */
  item: FavouriteItemInfo;
  /**
   * Fired when adding/removing a favourite Item is successful
   *
   * @param result Text describing an operation's result
   * @param newBookmarkID ID of the new Bookmark or undefined if the operation is deleting.
   */
  callback: (result: string, newBookmarkID?: number) => void;
  /**
   * Error handler for general errors
   */
  handleError: (error: Error) => void;
}

const {
  title: { add: addString, remove: removeString },
  removeAlert: removeAlertString,
  result: resultString,
  tags: tagsString,
} = languageStrings.searchpage.favouriteItem;

/**
 * Build a Grid as the dialog's content when the dialog is used for adding a favourite Item.
 */
const AddFavouriteItemContent = ({
  setTags,
  setVersionOption,
  isLatestVersion,
}: {
  setTags: (tags: string[]) => void;
  setVersionOption: (version: FavouriteItemVersionOption) => void;
  isLatestVersion: boolean;
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
      {isLatestVersion ? (
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
  item: { uuid, version, bookmarkId, isLatestVersion },
  callback,
  handleError,
}: FavouriteItemDialogProps) => {
  const [tags, setTags] = useState<string[]>([]);
  const [
    versionOption,
    setVersionOption,
  ] = useState<FavouriteItemVersionOption>("latest");

  const addFavourite = () =>
    addFavouriteItem(`${uuid}/${version}`, tags, versionOption === "latest")
      .then(({ bookmarkID }) => {
        callback(resultString.successfulAdd, bookmarkID);
        // Reset the version option to match the default selected value.
        setVersionOption("latest");
      })
      .catch(handleError)
      .finally(closeDialog);

  const deleteFavourite = () => {
    if (!bookmarkId) {
      throw new Error("Bookmark ID can't be falsy.");
    }
    deleteFavouriteItem(bookmarkId)
      .then(() => callback(resultString.successfulRemove, undefined))
      .catch(handleError)
      .finally(closeDialog);
  };

  return (
    <ConfirmDialog
      open={open}
      title={bookmarkId ? removeString : addString}
      onConfirm={bookmarkId ? deleteFavourite : addFavourite}
      onCancel={closeDialog}
      confirmButtonText={languageStrings.common.action.ok}
    >
      {bookmarkId ? (
        removeAlertString
      ) : (
        <AddFavouriteItemContent
          setTags={setTags}
          setVersionOption={setVersionOption}
          isLatestVersion={isLatestVersion}
        />
      )}
    </ConfirmDialog>
  );
};
