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
import AddCircleIcon from "@mui/icons-material/AddCircle";
import { Grid, List } from "@mui/material";
import { styled } from "@mui/material/styles";
import * as RS from "fp-ts/ReadonlySet";
import * as React from "react";
import { useState } from "react";
import type { BaseSecurityEntity } from "../../modules/ACLEntityModule";
import ConfirmDialog from "../ConfirmDialog";
import { languageStrings } from "../../util/langstrings";
import { TooltipIconButton } from "../TooltipIconButton";

export interface BaseSelectorProps<T extends BaseSecurityEntity> {
  /** The title of the dialog for selector. */
  title: string;
  /** The currently selected security entities. */
  value: T[];
  /**
   * Function to build a display component for the provided security entity.
   * Ideally the element will be `SecurityEntityEntry`.
   */
  entityDetailsToEntry: (entity: T) => JSX.Element;
  /** Handler for closing the entity selection dialog. */
  onClose: (selections?: ReadonlySet<T>) => void;
  /**
   * Function to determine which security entity search component to be displayed in the dialog.
   * @param selection Initial selected entity
   * @param setSelection SetState function to update selected entity
   */
  searchComponent: (
    selection: ReadonlySet<T>,
    setSelection: (selection: ReadonlySet<T>) => void
  ) => JSX.Element;
}

const StyledGridForAddButton = styled(Grid)({
  [`&`]: {
    // the best choice to align with the above delete icon (bin icon) based on the central axis.
    paddingRight: "15px",
  },
});

const { select, add } = languageStrings.common.action;

/**
 * This component displays a list of selected security entities and an icon button
 * which is used to display a dialog for entity selection.
 */
const BaseSelector = <T extends BaseSecurityEntity>({
  title,
  value,
  entityDetailsToEntry,
  onClose,
  searchComponent,
}: BaseSelectorProps<T>) => {
  const [showSelectRoleDialog, setShowSelectRoleDialog] =
    useState<boolean>(false);

  const [selectedEntities, setSelectedEntities] = useState<ReadonlySet<T>>(
    RS.empty
  );

  const handleConfirm = () => {
    onClose(selectedEntities);
    setShowSelectRoleDialog(false);
  };

  const handleCancel = () => {
    onClose();
    setShowSelectRoleDialog(false);
  };

  return (
    <>
      <Grid container spacing={1}>
        <Grid item xs={12}>
          <List disablePadding>{value.map(entityDetailsToEntry)}</List>
        </Grid>

        <StyledGridForAddButton
          item
          container
          justifyContent="flex-end"
          xs={12}
        >
          <TooltipIconButton
            color="primary"
            title={add}
            aria-label={add}
            onClick={() => setShowSelectRoleDialog(true)}
          >
            <AddCircleIcon fontSize="large"></AddCircleIcon>
          </TooltipIconButton>
        </StyledGridForAddButton>
      </Grid>

      <ConfirmDialog
        title={title}
        open={showSelectRoleDialog}
        confirmButtonText={select}
        onConfirm={handleConfirm}
        onCancel={handleCancel}
      >
        {searchComponent(selectedEntities, setSelectedEntities)}
      </ConfirmDialog>
    </>
  );
};

export default BaseSelector;
