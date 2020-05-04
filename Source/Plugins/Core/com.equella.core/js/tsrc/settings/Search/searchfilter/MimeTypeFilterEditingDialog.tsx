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
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
} from "@material-ui/core";
import {
  getMIMETypesFromServer,
  MimeTypeFilter,
  MimeTypeEntry,
  vaidateMimeTypeName,
} from "./SearchFilterSettingsModule";
import { useEffect, useState } from "react";
import { commonString } from "../../../util/commonstrings";
import MimeTypeList from "./MimeTypeList";
import { languageStrings } from "../../../util/langstrings";
import { addElement, deleteElement } from "../../../util/ImmutableArrayUtil";

interface MimeTypeFilterEditingDialogProps {
  /**
   * If true, the dialog will be shown.
   */
  open: boolean;
  /**
   * Fired when the dialog is closed.
   */
  onClose: () => void;
  /**
   * Fired when clicking the ADD or OK button.
   * @param filter The filter that has been added or edited
   */
  addOrUpdate: (filter: MimeTypeFilter) => void;
  /**
   * The filter to be edited, or undefined if the action is to add a new filter.
   */
  mimeTypeFilter?: MimeTypeFilter;
  /**
   * Error handling.
   */
  handleError: (error: Error) => void;
}

/**
 * Component that shows a dialog where users can add/edit a MIME type filter.
 */
const MimeTypeFilterEditingDialog = ({
  open,
  onClose,
  mimeTypeFilter,
  addOrUpdate,
  handleError,
}: MimeTypeFilterEditingDialogProps) => {
  const searchFilterStrings =
    languageStrings.settings.searching.searchfiltersettings;

  const [mimeTypeEntries, setMimeTypeEntries] = useState<MimeTypeEntry[]>([]);
  // Used to store the name of a MIME type filter.
  const [filterName, setFilterName] = useState<string>("");
  // Used to store the MIME types of a MIME type filter.
  const [selectedMimeTypes, setSelectedMimeTypes] = useState<string[]>([]);

  const isNameValid = vaidateMimeTypeName(filterName);

  useEffect(() => {
    getMIMETypesFromServer()
      .then((mimeTypes) => setMimeTypeEntries(mimeTypes))
      .catch((error) => handleError(error));
  }, []);

  /**
   * Clean up previously edited filter, depending on 'onClose'.
   */
  useEffect(() => {
    setFilterName(mimeTypeFilter ? mimeTypeFilter.name : "");
    setSelectedMimeTypes(mimeTypeFilter ? mimeTypeFilter.mimeTypes : []);
  }, [onClose]);

  /**
   * If a MIME type is selected and it doesn't exist in the collection of selected MIME types,
   * then add it to the collection.
   * if a MIME type is unselected and it exists in the collection of selected MIME types,
   * then remove it from the collection.
   */
  const updateMimeTypeSelections = React.useCallback(
    (checked: boolean, mimeType: string) => {
      if (checked && selectedMimeTypes.indexOf(mimeType) < 0) {
        setSelectedMimeTypes(addElement(selectedMimeTypes, mimeType));
      } else if (!checked && selectedMimeTypes.indexOf(mimeType) > -1) {
        setSelectedMimeTypes(
          deleteElement(
            selectedMimeTypes,
            (type: string) => type === mimeType,
            1
          )
        );
      }
    },
    [selectedMimeTypes]
  );

  const onAddOrUpdate = () => {
    addOrUpdate({
      id: mimeTypeFilter?.id,
      name: filterName,
      mimeTypes: selectedMimeTypes,
    });
    onClose();
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      disableBackdropClick
      disableEscapeKeyDown
      fullWidth
    >
      <DialogTitle>
        {mimeTypeFilter ? searchFilterStrings.edit : searchFilterStrings.add}
      </DialogTitle>
      <DialogContent>
        <TextField
          margin="dense"
          label={searchFilterStrings.filternamelabel}
          value={filterName}
          required
          fullWidth
          onChange={(event) => setFilterName(event.target.value)}
          error={!!filterName && !isNameValid}
        />
        <MimeTypeList
          entries={mimeTypeEntries}
          onChange={updateMimeTypeSelections}
          selected={selectedMimeTypes}
        />
      </DialogContent>
      <DialogActions>
        <Button
          id={"MimeTypeFilterEditingDialog_cancel"}
          onClick={onClose}
          color="primary"
        >
          {commonString.action.cancel}
        </Button>
        <Button
          id={"MimeTypeFilterEditingDialog_save"}
          onClick={onAddOrUpdate}
          color="primary"
          disabled={
            !filterName || !isNameValid || selectedMimeTypes.length === 0
          }
        >
          {mimeTypeFilter ? commonString.action.ok : commonString.action.add}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default MimeTypeFilterEditingDialog;
