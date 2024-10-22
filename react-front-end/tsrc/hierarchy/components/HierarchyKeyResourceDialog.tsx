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
import * as OEQ from "@openequella/rest-api-client";
import { languageStrings } from "../../util/langstrings";

const { add, remove, removeAlert } =
  languageStrings.searchpage.hierarchyKeyResourceDialog;

export interface HierarchyKeyResourceDialogProps {
  /**
   * `true` to open the dialog
   */
  open: boolean;
  /**
   * Fired when the dialog is closed
   */
  closeDialog: () => void;
  /**
   * The UUID of the item to be selected.
   */
  itemUuid: OEQ.Common.UuidString;
  /**
   * The version of the item to be selected.
   */
  itemVersion: number;
  /**
   * `true` if the item is a key resource.
   */
  isKeyResource: boolean;
  /**
   * `true` if the item is on its latest version.
   * It's used to control the visibility of the version selection for dialog.
   * And it's only used when adding a key resource.
   */
  isLatestVersion?: boolean;
  /** Function to add/remove a key resource. */
  updateKeyResource: (
    itemUuid: OEQ.Common.UuidString,
    itemVersion: number,
    isDelete: boolean,
    isAlwaysLatest?: boolean,
  ) => Promise<void>;
}

/**
 * Dialog for either adding the selected Item to a Hierarchy as a key resource or
 * removing the Item from the key resource list.
 * When adding, user can choose either to use the latest version or a fixed version of the Item.
 */
const HierarchyKeyResourceDialog = ({
  open,
  closeDialog,
  itemUuid,
  itemVersion,
  isLatestVersion,
  isKeyResource,
  updateKeyResource,
}: HierarchyKeyResourceDialogProps) =>
  isKeyResource ? (
    <ConfirmDialog
      open={open}
      title={remove}
      onConfirm={async () => {
        await updateKeyResource(itemUuid, itemVersion, isKeyResource);
        closeDialog();
      }}
      onCancel={closeDialog}
      confirmButtonText={languageStrings.common.action.ok}
    >
      {removeAlert}
    </ConfirmDialog>
  ) : (
    <SelectItemVersionDialog
      title={add}
      isLatestVersion={isLatestVersion ?? true}
      onConfirm={(isLatest: boolean) =>
        updateKeyResource(itemUuid, itemVersion, isKeyResource, isLatest)
      }
      open={open}
      closeDialog={closeDialog}
    />
  );

export default HierarchyKeyResourceDialog;
