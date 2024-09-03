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
import * as React from "react";
import ConfirmDialog from "../../components/ConfirmDialog";
import SelectItemVersionDialog from "../../components/SelectItemVersionDialog";
import { languageStrings } from "../../util/langstrings";
import type { SelectItemVersionDialogProps } from "../../components/SelectItemVersionDialog";

const { add, remove, removeAlert, tagDescription } =
  languageStrings.searchpage.favouriteItem;

export interface FavouriteItemDialogProps
  extends Omit<
    SelectItemVersionDialogProps,
    "title" | "tagDescription" | "onConfirm"
  > {
  /**
   * `true` if the Item is already added.
   */
  isAdded: boolean;
  /**
   * The function to add/delete favourite.
   */
  updateFavouriteItem: (
    isAdded: boolean,
    isAlwaysLatest: boolean,
    tags?: string[],
  ) => Promise<void>;
}

/**
 * Dialog for selecting the version of an item to add to the hierarchy key resource.
 * And its also display a confirmation dialog for removing a key resource.
 */
const FavouriteItemDialog = (props: FavouriteItemDialogProps) => {
  const { isAdded, isLatestVersion, open, updateFavouriteItem, closeDialog } =
    props;

  return isAdded ? (
    <ConfirmDialog
      open={open}
      title={remove}
      onConfirm={async () => {
        await updateFavouriteItem(true, isLatestVersion);
        closeDialog();
      }}
      onCancel={closeDialog}
      confirmButtonText={languageStrings.common.action.ok}
    >
      {removeAlert}
    </ConfirmDialog>
  ) : (
    <SelectItemVersionDialog
      {...props}
      title={add}
      tagDescription={tagDescription}
      onConfirm={(isLatest, tags) => updateFavouriteItem(false, isLatest, tags)}
    />
  );
};

export default FavouriteItemDialog;
