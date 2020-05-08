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
import { commonString } from "../../../util/commonstrings";
import { languageStrings } from "../../../util/langstrings";
import { useEffect, useState } from "react";
import {
  FacetedSearchClassification,
  validateClassificationFields,
} from "./FacetedSearchSettingsModule";

interface ClassificationDialogProps {
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
  addOrEdit: (classification: FacetedSearchClassification) => void;
  /**
   * The classification to be edited; undefined if the action is to add a new one.
   */
  classification?: FacetedSearchClassification;
  /**
   * Error handling.
   */
  handleError: (error: Error) => void;
  /**
   * Current highest order index.
   */
  highestOrderIndex: number;
}

/**
 * A dialog for adding/editing a Faceted search classification
 */
const ClassificationDialog = ({
  open,
  onClose,
  addOrEdit,
  handleError,
  classification,
  highestOrderIndex,
}: ClassificationDialogProps) => {
  const {
    facetedsearchsetting: facetedSearchSettingStrings,
  } = languageStrings.settings.searching;
  const { classification: classificationStrings } = facetedSearchSettingStrings;

  const [name, setName] = useState<string>("");
  const [schemaNode, setSchemaNode] = useState<string>("");
  const [maxResults, setMaxResults] = useState<number | undefined>();

  const isNameInvalid = validateClassificationFields(name);
  const isSchemaNodeInvalid = validateClassificationFields(schemaNode);

  /**
   * Initialise textfields' values, depending on 'onClose'.
   */
  useEffect(() => {
    setName(classification ? classification.name : "");
    setSchemaNode(classification ? classification.schemaNode : "");
    setMaxResults(classification ? classification.maxResults : undefined);
  }, [onClose]);

  const onAddOrEdit = () => {
    addOrEdit({
      name: name,
      schemaNode: schemaNode,
      maxResults: maxResults,
      orderIndex: highestOrderIndex + 1,
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
      <DialogTitle>{facetedSearchSettingStrings.add}</DialogTitle>
      <DialogContent>
        <TextField
          margin="dense"
          label={classificationStrings.name}
          value={name}
          required
          fullWidth
          onChange={(event) => setName(event.target.value)}
          error={!!name && isNameInvalid}
        />
        <TextField
          margin="dense"
          label={classificationStrings.schemanode}
          value={schemaNode}
          required
          fullWidth
          onChange={(event) => setSchemaNode(event.target.value)}
          error={!!schemaNode && isSchemaNodeInvalid}
        />
        <TextField
          type={"number"}
          margin="dense"
          label={classificationStrings.categorynumber}
          value={maxResults}
          fullWidth
          onChange={(event) => {
            const value = event.target.value;
            setMaxResults(value ? parseInt(value) : undefined);
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
          {commonString.action.add}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ClassificationDialog;
