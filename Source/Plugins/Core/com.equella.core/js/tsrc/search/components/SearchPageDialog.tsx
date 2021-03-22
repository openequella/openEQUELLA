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
import {
  FavouriteItemDialog,
  FavouriteItemDialogSpecificProps,
} from "./FavouriteItemDialog";
import { FavouriteSearchDialog } from "./FavouriteSearchDialog";
import type { FavouriteSearchDialogSpecificProps } from "./FavouriteSearchDialog";

export interface SearchPageDialogCommonProps {
  /**
   * `true` to open the dialog
   */
  open: boolean;
  /**
   * Fired when the dialog is closed
   */
  closeDialog: () => void;
}

export interface SearchPageDialogProps extends SearchPageDialogCommonProps {
  /**
   * Additional props required by a certain Dialog to be rendered in Search page.
   */
  additionalDialogProps?:
    | {
        type: "item";
        props: FavouriteItemDialogSpecificProps;
      }
    | {
        type: "search";
        props: FavouriteSearchDialogSpecificProps;
      };
}

/**
 * Provide an entry to different Dialogs rendered in Search page.
 */
export const SearchPageDialog = ({
  open,
  closeDialog,
  additionalDialogProps,
}: SearchPageDialogProps) => {
  switch (additionalDialogProps?.type) {
    case "item":
      return (
        <FavouriteItemDialog
          open={open}
          closeDialog={closeDialog}
          {...additionalDialogProps.props}
        />
      );
    case "search":
      return (
        <FavouriteSearchDialog
          open={open}
          closeDialog={closeDialog}
          {...additionalDialogProps.props}
        />
      );
    default:
      throw new Error("Unexpected Dialog type");
  }
};
