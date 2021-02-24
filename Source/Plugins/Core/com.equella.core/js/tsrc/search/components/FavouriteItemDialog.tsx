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
  Radio,
  RadioGroup,
  TextField,
} from "@material-ui/core";
import { useState } from "react";
import * as React from "react";
import ConfirmDialog from "../../components/ConfirmDialog";
import { languageStrings } from "../../util/langstrings";

export interface FavouriteItem {
  /**
   * Item's unique key which consists of UUID and version
   */
  itemKey: string;
  /**
   * ID of a Bookmark which links to the Item
   */
  bookmarkId?: number;
  /**
   * Whether this version is the latest version
   */
  isLatestVersion: boolean;
}

const defaultFavouriteItem: FavouriteItem = {
  itemKey: "",
  bookmarkId: 0,
  isLatestVersion: false,
};

export interface FavouriteItemDialogProps {
  /**
   * `true` to open the dialog
   */
  open: boolean;
  /**
   * Fired when the dialog is closed
   */
  onCancel: () => void;
  /**
   * An Item to be added to or removed from user's favourites
   */
  favouriteItem?: FavouriteItem;
}

interface AddFavouriteItemContentProps {
  value: string;
  setValue: (value: string) => void;
  isLatestVersion: boolean;
}

/**
 * Build a Grid as the dialog's content when the dialog is used for adding a favourite Item.
 */
const AddFavouriteItemContent = ({
  value,
  setValue,
  isLatestVersion,
}: AddFavouriteItemContentProps) => (
  <Grid container direction="column" spacing={2}>
    <Grid item>
      <TextField
        key="tags"
        value={value}
        label="Provide tags to help when searching (optional)."
        helperText="Each tag can be separated by a whitespace, a comma or a semi-colon."
        onChange={(event) => setValue(event.target.value)}
        fullWidth
      />
    </Grid>
    <Grid item>
      {isLatestVersion ? (
        <FormControl>
          <FormLabel>Select version to add:</FormLabel>
          <RadioGroup row>
            <FormControlLabel
              value="true"
              control={<Radio />}
              label="Always use latest version"
            />
            <FormControlLabel
              value="false"
              control={<Radio />}
              label="This version"
            />
          </RadioGroup>
        </FormControl>
      ) : (
        "NOTE: Adding this favourite will point to this version forever."
      )}
    </Grid>
  </Grid>
);

const favouriteItemStrings = languageStrings.searchpage.favouriteItem;

/**
 * Provide a Dialog where an Item can be added to or removed from user's favourites.
 */
export const FavouriteItemDialog = ({
  open,
  onCancel,
  favouriteItem = defaultFavouriteItem,
}: FavouriteItemDialogProps) => {
  const { itemKey, bookmarkId, isLatestVersion } = favouriteItem;
  const title = bookmarkId
    ? favouriteItemStrings.remove
    : favouriteItemStrings.add;

  const [tags, setTags] = useState<string>("");
  const onConfirm = bookmarkId
    ? () => console.log("remove " + bookmarkId)
    : () => console.log("add " + itemKey);

  return (
    <ConfirmDialog
      open={open}
      title={title}
      onConfirm={onConfirm}
      onCancel={onCancel}
      confirmButtonText="ok"
    >
      {bookmarkId ? (
        "Are you sure you want to remove from your favourites?"
      ) : (
        <AddFavouriteItemContent
          value={tags}
          setValue={setTags}
          isLatestVersion={isLatestVersion}
        />
      )}
    </ConfirmDialog>
  );
};
