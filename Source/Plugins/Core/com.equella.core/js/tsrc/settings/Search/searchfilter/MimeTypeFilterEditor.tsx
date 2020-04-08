import * as React from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle
} from "@material-ui/core";
import {
  getMIMETypesFromServer,
  MimeTypeFilter,
  MimeTypeEntry,
  vaidateMimeTypeName
} from "./SearchFilterSettingsModule";
import { useEffect, useState } from "react";
import TextField from "@material-ui/core/TextField";
import { commonString } from "../../../util/commonstrings";
import Typography from "@material-ui/core/Typography";
import { makeStyles } from "@material-ui/styles";
import { MimeTypeList } from "./MimeTypeList";

const useStyles = makeStyles({
  listTitle: {
    marginTop: "20px"
  }
});

interface MimeTypeFilterEditorProps {
  open: boolean;
  onClose: () => void;
  onAddOrUpdate: (filter: MimeTypeFilter, add: boolean) => void;
  mimeTypeFilter?: MimeTypeFilter;
}

const MimeTypeFilterEditor = ({
  open,
  onClose,
  mimeTypeFilter,
  onAddOrUpdate
}: MimeTypeFilterEditorProps) => {
  const classes = useStyles();
  const [filterName, setFilterName] = useState<string | undefined>("");
  const [selectedMimeTypes, setSelectedMimeTypes] = useState<string[]>([]);
  const [mimeTypeEntries, setMimeTypeEntries] = useState<MimeTypeEntry[]>([]);

  useEffect(() => {
    getMIMETypesFromServer()
      .then(mimeTypes => setMimeTypeEntries(mimeTypes))
      .catch(error => console.log(error));
  }, []);

  useEffect(() => {
    setFilterName(mimeTypeFilter ? mimeTypeFilter.name : "");
    setSelectedMimeTypes(mimeTypeFilter ? mimeTypeFilter.mimeTypes : []);
  }, [mimeTypeFilter]);

  const updateMimeTypeSelections = React.useCallback(
    (checked: boolean, mimeType: string) => {
      // If selected and the value is not in mimeTypeSelections then add it to mimeTypeSelections
      if (checked && selectedMimeTypes.indexOf(mimeType) < 0) {
        setSelectedMimeTypes(selectedMimeTypes.concat(mimeType));
      }
      // If unselected and the value is already in mimeTypeSelections then remove it
      else if (!checked && selectedMimeTypes.indexOf(mimeType) >= 0) {
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

  const isNameValid = vaidateMimeTypeName(filterName);
  const DIALOG_TITLE = mimeTypeFilter
    ? "Edit MIME type filter"
    : "Create new MIME type filter";

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
          label={"Name"}
          value={filterName}
          required
          fullWidth
          helperText={"This field is mandatory"}
          onChange={event => updateFilterName(event.target.value)}
          error={!!filterName && !isNameValid}
        />
        <Typography
          variant={"subtitle2"}
          color={"textSecondary"}
          className={classes.listTitle}
        >
          MIME types *
        </Typography>
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
          onClick={() =>
            onAddOrUpdate(
              {
                id: mimeTypeFilter?.id,
                name: filterName!,
                mimeTypes: selectedMimeTypes
              },
              !mimeTypeFilter
            )
          }
          color="primary"
          disabled={!filterName || !isNameValid}
        >
          {mimeTypeFilter ? commonString.action.edit : commonString.action.add}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default MimeTypeFilterEditor;
