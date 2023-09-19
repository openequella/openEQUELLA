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
} from "@mui/material";
import { commonString } from "../../../util/commonstrings";
import { languageStrings } from "../../../util/langstrings";
import {
  FacetedSearchClassificationWithFlags,
  validateFacetFields,
} from "../../../modules/FacetedSearchSettingsModule";
import SchemaSelector from "../../SchemaSelector";

export interface FacetDialogProps {
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
   * Define how to handle error
   */
  handleError: (error: string | Error) => void;
  /**
   * The facet to be edited; undefined if the action is to add a new one.
   */
  facet?: FacetedSearchClassificationWithFlags;
}

/**
 * A dialog for adding/editing a facet
 */
const FacetDialog = ({
  open,
  handleError,
  onClose,
  addOrEdit,
  facet
}: FacetDialogProps) => {
  const { facetedsearchsetting: facetedSearchSettingStrings } =
    languageStrings.settings.searching;
  const { facetfields: facetFieldStrings } = facetedSearchSettingStrings;

  const [name, setName] = useState<string>("");
  const [schemaNode, setSchemaNode] = useState<string>("");
  const [maxResults, setMaxResults] = useState<number | undefined>();

  const isNameInvalid = validateFacetFields(name);
  const isSchemaNodeInvalid = validateFacetFields(schemaNode);

  /**
   * Initialise `textfields` values when the dialog is opened.
   */
  useEffect(() => {
    if (open) {
      setName(facet?.name ?? "");
      setSchemaNode(facet?.schemaNode ?? "");
      setMaxResults(facet?.maxResults);
    }
  }, [open, facet]);

  const onAddOrEdit = () => {
    addOrEdit(name, schemaNode, maxResults);
    onClose();
  };

  return (
    <Dialog open={open}>
      <DialogTitle>
        {facet
          ? facetedSearchSettingStrings.edit
          : facetedSearchSettingStrings.add}
      </DialogTitle>
      <DialogContent>
        <TextField
          margin="dense"
          label={facetFieldStrings.name}
          value={name}
          helperText={facetFieldStrings.nameHelper}
          required
          fullWidth
          onChange={(event) => setName(event.target.value)}
          error={!!name && isNameInvalid}
          variant="standard"
        />
        <TextField
          type="number"
          margin="dense"
          label={facetFieldStrings.categoryNumber}
          value={maxResults}
          fullWidth
          onChange={(event) =>
            setMaxResults(
              event.target.value ? parseInt(event.target.value) : undefined
            )
          }
          helperText={facetFieldStrings.categoryNumberHelper}
          variant="standard"
        />
        <TextField
          margin="dense"
          label={facetFieldStrings.schemaNode}
          value={schemaNode}
          helperText={facetFieldStrings.schemaNodeHelper}
          onChange={(event) => {
            setSchemaNode(event.target.value);
          }}
          required
          fullWidth
          disabled
          error={!!schemaNode && validateFacetFields(schemaNode)}
          variant="standard"
        />
        <SchemaSelector
          handleError={handleError}
          setSchemaNode={(node) => {
            setSchemaNode(node.replace("/xml", ""));
          }}
        />
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
