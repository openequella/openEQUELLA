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
import ErrorOutline from "@mui/icons-material/ErrorOutline";
import {
  Button,
  Divider,
  Grid,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Typography,
} from "@mui/material";
import * as EQ from "fp-ts/Eq";
import { pipe } from "fp-ts/function";
import * as ORD from "fp-ts/Ord";
import * as RA from "fp-ts/ReadonlyArray";
import * as RS from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useContext, useEffect, useState } from "react";
import { AppContext } from "../../mainui/App";
import { BaseSecurityEntity, entityIds } from "../../modules/ACLEntityModule";
import { languageStrings } from "../../util/langstrings";
import ConfirmDialog from "../ConfirmDialog";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import SecurityEntityEntrySkeleton from "./SecurityEntityEntrySkeleton";

const { removeAll: removeAllLabel } = languageStrings.common.action;

export interface SelectEntityDialogProps<T extends BaseSecurityEntity> {
  /** Open the dialog when true. */
  open: boolean;
  /** The title of the dialog for selector. */
  title: string;
  /** The currently selected ids of security entities. */
  value: ReadonlySet<OEQ.Common.UuidString>;
  /** Order used to sort items.*/
  itemOrd: ORD.Ord<T>;
  /**
   * Function to build a display component for the provided security entity.
   * Ideally the element will be `SecurityEntityEntry`.
   *
   * @param entity Entity to display.
   * @param onDelete function to handle delete event.
   */
  entityDetailsToEntry: (entity: T, onDelete: () => void) => React.JSX.Element;
  /** Handler for when user confirms (press ok button) the selections. */
  onConfirm: (selections: ReadonlySet<OEQ.Common.UuidString>) => void;
  /** Handler for when user cancel (press cancel button or click the area where outside dialog) the selections. */
  onCancel: () => void;
  /**
   * Function use to build the search component specific to the type of security entity to be searched.
   * Receives the various handlers for that component, and returns the completed search component.
   *
   * @param onAdd Triggered when user clicks the `add` icon in search component.
   * @param onSelectAll Triggered when user clicks the `Select All` button in search component.
   */
  searchComponent: (
    onAdd: (entity: T) => void,
    onSelectAll: (entities: ReadonlySet<T>) => void,
  ) => React.JSX.Element;
  /** The message is displayed in the right-hand result list when there is no selected entity. */
  addEntityMessage: string;
  /**
   * Function to get all entity details by their ids.
   */
  findEntitiesByIds: (ids: ReadonlySet<string>) => Promise<ReadonlyArray<T>>;
}

const { ok: okLabel } = languageStrings.common.action;
const { currentSelections: currentSelectionsLabel } =
  languageStrings.selectEntityDialog;
/**
 * This component provides a template for a standard dialog used for searching for security entities,
 * and making selections of those entities. The search component is displayed on the left hand side,
 * from which a user can make selections which will then be added to a list on the right hand side.
 * When the user confirms the dialog (click's OK),
 * that list of users on the right hand side is returned to the calling component.
 */
const SelectEntityDialog = <T extends BaseSecurityEntity>({
  open,
  title,
  value,
  itemOrd,
  entityDetailsToEntry,
  onConfirm,
  onCancel,
  searchComponent,
  addEntityMessage,
  findEntitiesByIds,
}: SelectEntityDialogProps<T>) => {
  const { appErrorHandler } = useContext(AppContext);

  // State for currently selected entities.
  const [selectedEntities, setSelectedEntities] = useState<ReadonlySet<T>>(
    new Set(),
  );
  const [initialEntities, setInitialEntities] = useState<ReadonlySet<T>>();

  // Indicates if the component has been initialized with provided role ids.
  const hasInitialized = initialEntities !== undefined;

  useEffect(() => {
    pipe(
      TE.tryCatch(
        () => pipe(value, findEntitiesByIds),
        (e) => `Failed to get entities by IDs: ${e}`,
      ),
      TE.match(appErrorHandler, (entities) => {
        const entitySet = RS.fromReadonlyArray(itemOrd)(entities);
        setInitialEntities(entitySet);
        setSelectedEntities(entitySet);
      }),
    )();
  }, [appErrorHandler, findEntitiesByIds, itemOrd, value]);

  const eqByID = EQ.contramap<string, T>((entry: T) => entry.id)(S.Eq);

  const handleConfirm = () => pipe(selectedEntities, entityIds, onConfirm);

  const handleCancel = () => {
    onCancel();

    // Reset selected entities to initial entities when cancel button is clicked.
    if (hasInitialized) {
      setSelectedEntities(initialEntities);
    }
  };

  const handleDeleteForEachEntry = (entity: T) =>
    pipe(
      selectedEntities,
      RS.remove(EQ.contramap<string, T>((entry: T) => entry.id)(S.Eq))(entity),
      setSelectedEntities,
    );

  const hasSelectedEntities = !RS.isEmpty(selectedEntities);

  const handleOnAdd = (entity: T) =>
    pipe(selectedEntities, RS.insert(eqByID)(entity), setSelectedEntities);

  const handleOnSelectAll = (entities: ReadonlySet<T>) =>
    pipe(selectedEntities, RS.union(eqByID)(entities), setSelectedEntities);

  const renderSelectedEntities = () =>
    hasSelectedEntities ? (
      pipe(
        selectedEntities,
        RS.toReadonlyArray(itemOrd),
        RA.map((entity) =>
          entityDetailsToEntry(entity, () => handleDeleteForEachEntry(entity)),
        ),
      )
    ) : (
      <ListItem>
        <ListItemIcon>
          <ErrorOutline />
        </ListItemIcon>
        <ListItemText secondary={addEntityMessage} />
      </ListItem>
    );

  return (
    <ConfirmDialog
      title={title}
      open={open}
      confirmButtonText={okLabel}
      disableConfirmButton={!hasInitialized}
      onConfirm={handleConfirm}
      onCancel={handleCancel}
      maxWidth="md"
    >
      <Grid container>
        <Grid item xs>
          {searchComponent(handleOnAdd, handleOnSelectAll)}
        </Grid>

        <Divider
          orientation="vertical"
          flexItem
          sx={{ opacity: 0.6, margin: "5px" }}
        />

        <Grid container item xs direction="column" rowSpacing={2}>
          <Grid item>
            {/*paddingLeft is used to align title with the list item below it*/}
            <Typography variant="h6" sx={{ paddingLeft: 2 }} gutterBottom>
              {currentSelectionsLabel}
            </Typography>

            <List disablePadding>
              {hasInitialized
                ? renderSelectedEntities()
                : pipe(
                    A.makeBy(3, (i) => i + 1),
                    A.map((i) => <SecurityEntityEntrySkeleton key={i} />),
                  )}
            </List>
          </Grid>
          {!RS.isEmpty(selectedEntities) && (
            <Grid item>
              <Button
                color="secondary"
                onClick={() => setSelectedEntities(RS.empty)}
                sx={{ float: "right" }}
              >
                {removeAllLabel}
              </Button>
            </Grid>
          )}
        </Grid>
      </Grid>
    </ConfirmDialog>
  );
};

export default SelectEntityDialog;
