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
import { useEffect, useState } from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
} from "@material-ui/core";
import { commonString } from "../../../util/commonstrings";
import { languageStrings } from "../../../util/langstrings";
import {
  FacetWithFlags,
  validateFacetFields,
} from "./FacetedSearchSettingsModule";
import SchemaSelector from "../../SchemaSelector";
import { makeStyles, Theme } from "@material-ui/core/styles";

const useStyles = makeStyles((theme: Theme) => {
  return {
    dialogPaper: {
      width: "45%",
      height: "90%",
      maxHeight: "none",
      maxWidth: "none",
      margin: theme.spacing(2),
      padding: theme.spacing(1),
      overflowY: "hidden",
    },
    dialog: {
      overflowY: "hidden",
    },
    root: {
      flexGrow: 1,
    },
  };
});
interface FacetDialogProps {
  /**
   * If true, the dialog will be shown.
   */
  open: boolean;
  /**
   * Fired when the dialog is closed.
   */
  onClose: () => void;
  /**
   * Fired when click the ADD button.
   */
  addOrEdit: (
    name: string,
    schemaNode: string,
    maxResults: number | undefined
  ) => void;
  /**
   * The facet to be edited; undefined if the action is to add a new one.
   */
  facet?: FacetWithFlags;
  /**
   * Error handling.
   */
  handleError: (error: Error) => void;
}

/**
 * A dialog for adding/editing a facet
 */
const FacetDialog = ({
  open,
  onClose,
  addOrEdit,
  handleError,
  facet,
}: FacetDialogProps) => {
  const {
    facetedsearchsetting: facetedSearchSettingStrings,
  } = languageStrings.settings.searching;
  const { facetfields: facetFieldStrings } = facetedSearchSettingStrings;

  const [name, setName] = useState<string>("");
  const [schemaNode, setSchemaNode] = useState<string>("");
  const [maxResults, setMaxResults] = useState<number | undefined>();

  const isNameInvalid = validateFacetFields(name);
  const isSchemaNodeInvalid = validateFacetFields(schemaNode);

  const classes = useStyles();
  /**
   * Initialise textfields' values, depending on 'onClose'.
   */
  useEffect(() => {
    setName(facet?.name ?? "");
    setSchemaNode(facet?.schemaNode ?? "");
    setMaxResults(facet?.maxResults);
  }, [onClose]);

  const onAddOrEdit = () => {
    addOrEdit(name, schemaNode, maxResults);
    onClose();
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      disableBackdropClick
      disableEscapeKeyDown
      className={classes.dialog}
      classes={{ paper: classes.dialogPaper }}
    >
      <DialogTitle>{facetedSearchSettingStrings.add}</DialogTitle>
      <DialogContent>
        <TextField
          margin="dense"
          label={facetFieldStrings.name}
          value={name}
          required
          fullWidth
          onChange={(event) => setName(event.target.value)}
          error={!!name && isNameInvalid}
        />
        <TextField
          type="number"
          margin="dense"
          label={facetFieldStrings.categorynumber}
          value={maxResults}
          fullWidth
          onChange={(event) =>
            setMaxResults(
              event.target.value ? parseInt(event.target.value) : undefined
            )
          }
          helperText={"Leave blank to display all categories"}
        />
        <TextField
          margin="dense"
          label={facetFieldStrings.schemanode}
          value={schemaNode}
          required
          fullWidth
          error={!!schemaNode && validateFacetFields(schemaNode)}
        />
        <SchemaSelector
          setSchemaNode={(node) => {
            setSchemaNode(node);
          }}
        />
        {/*</div>*/}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="primary">
          {commonString.action.cancel}
        </Button>
        <Button
          onClick={onAddOrEdit}
          color="primary"
          disabled={isNameInvalid || isSchemaNodeInvalid}
        >
          {facet ? commonString.action.ok : commonString.action.add}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default FacetDialog;
