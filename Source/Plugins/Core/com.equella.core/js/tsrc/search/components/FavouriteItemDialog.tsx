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
  onCancel: () => void;
  /**
   * An Item to be added to or removed from user's favourites
   */
  item: FavouriteItemInfo;
}

const {
  add: addFavouriteItemString,
  remove: removeFavouriteItemString,
  removeAlert: removeAlertString,
  tags: tagsString,
} = languageStrings.searchpage.favouriteItem;

/**
 * Build a Grid as the dialog's content when the dialog is used for adding a favourite Item.
 */
const AddFavouriteItemContent = ({
  setTags,
  isLatestVersion,
}: {
  setTags: (tags: string[]) => void;
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
        onChange={(_, value: string[]) => {
          setTags(value);
        }}
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
              label={tagsString.versionOptions.useLatestVersion}
            />
            <FormControlLabel
              value="false"
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
  onCancel,
  item: { uuid, version, bookmarkId, isLatestVersion },
}: FavouriteItemDialogProps) => {
  const [tags, setTags] = useState<string[]>([]);
  const onConfirm = bookmarkId
    ? () => deleteFavouriteItem(bookmarkId)
    : () => addFavouriteItem(`${uuid}/${version}`, tags, isLatestVersion);

  return (
    <ConfirmDialog
      open={open}
      title={bookmarkId ? removeFavouriteItemString : addFavouriteItemString}
      onConfirm={onConfirm}
      onCancel={onCancel}
      confirmButtonText={languageStrings.common.action.ok}
    >
      {bookmarkId ? (
        removeAlertString
      ) : (
        <AddFavouriteItemContent
          setTags={setTags}
          isLatestVersion={isLatestVersion}
        />
      )}
    </ConfirmDialog>
  );
};
