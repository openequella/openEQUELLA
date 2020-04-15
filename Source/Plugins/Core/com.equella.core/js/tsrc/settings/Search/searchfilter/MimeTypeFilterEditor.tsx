import * as React from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField
} from "@material-ui/core";
import {
  getMIMETypesFromServer,
  MimeTypeFilter,
  MimeTypeEntry,
  vaidateMimeTypeName
} from "./SearchFilterSettingsModule";
import { useEffect, useState } from "react";
import { commonString } from "../../../util/commonstrings";
import { MimeTypeList } from "./MimeTypeList";
import { languageStrings } from "../../../util/langstrings";
import { templateError, TemplateUpdate } from "../../../mainui/Template";
import { fromAxiosError } from "../../../api/errors";

interface MimeTypeFilterEditorProps {
  open: boolean;
  onClose: () => void;
  addOrUpdate: (filter: MimeTypeFilter) => void;
  mimeTypeFilter?: MimeTypeFilter;
  updateTemplate: (update: TemplateUpdate) => void;
}

const MimeTypeFilterEditor = ({
  open,
  onClose,
  mimeTypeFilter,
  addOrUpdate,
  updateTemplate
}: MimeTypeFilterEditorProps) => {
  const searchFilterStrings =
    languageStrings.settings.searching.searchfiltersettings;

  const [mimeTypeEntries, setMimeTypeEntries] = useState<MimeTypeEntry[]>([]);
  // Used to store the name of a MIME type filter
  const [filterName, setFilterName] = useState<string>("");
  // Used to store the MIME types of a MIME type filter
  const [selectedMimeTypes, setSelectedMimeTypes] = useState<string[]>([]);

  useEffect(() => {
    getMIMETypesFromServer()
      .then(mimeTypes => setMimeTypeEntries(mimeTypes))
      .catch(error => updateTemplate(templateError(fromAxiosError(error))));
  }, []);

  useEffect(() => {
    setFilterName(mimeTypeFilter ? mimeTypeFilter.name : "");
    setSelectedMimeTypes(mimeTypeFilter ? mimeTypeFilter.mimeTypes : []);
  }, [onClose]);

  /**
   * If a MIME type is selected and it doesn't exist in the collection of selected MIME types,
   * then add it to the collection;
   * if a MIME type is unselected and it exists in the collection of selected MIME types,
   * then remove it from the collection.
   */
  const updateMimeTypeSelections = React.useCallback(
    (checked: boolean, mimeType: string) => {
      if (checked && selectedMimeTypes.indexOf(mimeType) < 0) {
        setSelectedMimeTypes([...selectedMimeTypes, mimeType]);
      } else if (!checked && selectedMimeTypes.indexOf(mimeType) > -1) {
        setSelectedMimeTypes(
          selectedMimeTypes.filter(selection => selection !== mimeType)
        );
      }
    },
    [selectedMimeTypes]
  );

  const updateFilterName = (name: string) => {
    setFilterName(name);
  };

  const onAddOrUpdate = () => {
    addOrUpdate({
      id: mimeTypeFilter?.id,
      name: filterName,
      mimeTypes: selectedMimeTypes
    });
    onClose();
  };

  const isNameValid = vaidateMimeTypeName(filterName);
  const DIALOG_TITLE = mimeTypeFilter
    ? searchFilterStrings.edit
    : searchFilterStrings.add;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      disableBackdropClick={true}
      disableEscapeKeyDown={true}
      fullWidth
    >
      <DialogTitle>{DIALOG_TITLE}</DialogTitle>
      <DialogContent>
        <TextField
          margin="dense"
          label={searchFilterStrings.filternamelabel}
          value={filterName}
          required
          fullWidth
          helperText={searchFilterStrings.helptext}
          onChange={event => updateFilterName(event.target.value)}
          error={!!filterName && !isNameValid}
        />
        <MimeTypeList
          entries={mimeTypeEntries}
          onChange={updateMimeTypeSelections}
          selected={selectedMimeTypes}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="primary">
          {commonString.action.cancel}
        </Button>
        <Button
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

export default MimeTypeFilterEditor;
