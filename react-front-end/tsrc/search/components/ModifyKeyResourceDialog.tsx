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
import AddIcon from "@mui/icons-material/Add";
import RemoveIcon from "@mui/icons-material/Remove";
import LoadingButton from "@mui/lab/LoadingButton";
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { constFalse, constTrue, pipe } from "fp-ts/function";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useContext, useEffect, useState } from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import HierarchyTree from "../../hierarchy/components/HierarchyTree";
import HierarchyTreeSkeleton from "../../hierarchy/components/HierarchyTreeSkeleton";
import {
  addKeyResource,
  deleteKeyResource,
  getRootHierarchies,
  getHierarchyIdsWithKeyResource,
} from "../../modules/HierarchyModule";
import { languageStrings } from "../../util/langstrings";
import { pfTernary } from "../../util/pointfree";
import { SearchContext } from "../SearchPageHelper";

const { dialogTitle, dialogDesc, add, remove } =
  languageStrings.searchpage.addToHierarchy;
const { close: closeText } = languageStrings.common.action;

const LABELLED_BY = "modify-key-resource-dialog-title";
const DESCRIBE_BY = "modify-key-resource-dialog-description";

export interface ModifyKeyResourceDialogProps {
  /**
   * Open the dialog when true.
   */
  open: boolean;
  /**
   * Fired when click the Close button.
   */
  onClose: () => void;
  /**
   * The Item to be updated as a key resource for hierarchies.
   */
  item: OEQ.Search.SearchResultItem;
  /**
   * Function to retrieve a list of all the hierarchies.
   */
  getHierarchiesProvider?: () => Promise<
    OEQ.BrowseHierarchy.HierarchyTopicSummary[]
  >;
  /**
   * Function to retrieve a list of hierarchies that already have the provided key resource.
   */
  getHierarchyIdsWithKeyResourceProvider?: (
    uuid: string,
    version: number,
  ) => Promise<string[]>;
}

/**
 * A dialog which contains a tree view of the hierarchy topics and supports updating an
 * Item as key resource for each topic.
 */
const ModifyKeyResourceDialog = ({
  open,
  onClose,
  item,
  getHierarchiesProvider = getRootHierarchies,
  getHierarchyIdsWithKeyResourceProvider = getHierarchyIdsWithKeyResource,
}: ModifyKeyResourceDialogProps) => {
  const { searchPageErrorHandler } = useContext(SearchContext);
  const [isLoading, setIsLoading] = useState(true);
  // The UUIDs of the hierarchy that is currently being updated.
  const [updatingList, setUpdatingList] = useState<string[]>([]);

  const [hierarchies, setHierarchies] = useState<
    OEQ.BrowseHierarchy.HierarchyTopicSummary[]
  >([]);
  // List of hierarchies that already have the provided key resource.
  const [hierarchyIdsWithKeyResource, setHierarchyIdsWithKeyResource] =
    useState<string[]>([]);

  useEffect(() => {
    const getHierarchiesTask = pipe(
      TE.tryCatch(
        () => getHierarchiesProvider(),
        (e) => new Error(`Failed to get hierarchies: ${e}`),
      ),
      TE.map(setHierarchies),
    );

    const updateHierarchyIdsTask = pipe(
      TE.tryCatch(
        () => getHierarchyIdsWithKeyResourceProvider(item.uuid, item.version),
        (e) => new Error(`Failed to get hierarchy IDs with key resource: ${e}`),
      ),
      TE.map(setHierarchyIdsWithKeyResource),
    );

    pipe(
      [getHierarchiesTask, updateHierarchyIdsTask],
      TE.sequenceArray,
      TE.match(searchPageErrorHandler, () => setIsLoading(false)),
    )();
  }, [
    searchPageErrorHandler,
    getHierarchiesProvider,
    getHierarchyIdsWithKeyResourceProvider,
    item,
  ]);

  /**
   * Add or delete a key resource for a hierarchy in four steps:
   * 1. Add the hierarchy to the updating list.
   * 2. Do the update;
   * 3. Update the list of hierarchies ids for the key resource;
   * 4. Remove the hierarchy from the updating list.
   */
  const updateKeyResource = async (
    hierarchyUuid: string,
    item: OEQ.Search.SearchResultItem,
    update: (
      compoundUuid: string,
      itemUuid: string,
      itemVersion: number,
    ) => Promise<void>,
  ): Promise<void> => {
    // add UUID to the updating list
    setUpdatingList(A.append(hierarchyUuid));

    const updateKeyResourceTask = TE.tryCatch(
      () => update(hierarchyUuid, item.uuid, item.version),
      (e) => new Error(`Failed to update key resource: ${e}`),
    );

    const updateHierarchyKeyResourceTask = pipe(
      TE.tryCatch(
        () => getHierarchyIdsWithKeyResourceProvider(item.uuid, item.version),
        (e) => new Error(`Failed to get hierarchy IDs with key resource: ${e}`),
      ),
      TE.map(setHierarchyIdsWithKeyResource),
    );

    await pipe(
      [updateKeyResourceTask, updateHierarchyKeyResourceTask],
      TE.sequenceSeqArray,
      TE.mapLeft(searchPageErrorHandler),
    )();

    // remove UUID from the updating list
    setUpdatingList(A.filter((uuid) => uuid !== hierarchyUuid));
  };

  /**
   * Add/Remove button displayed in the hierarchy tree for each hierarchy.
   * It will display a loading icon when the hierarchy is being updated.
   */
  const updateKeyResourceButton = (
    hierarchyUuid: string,
    item: OEQ.Search.SearchResultItem,
    isAdded: boolean,
  ) => {
    const label = isAdded ? remove : add;
    const action = isAdded ? deleteKeyResource : addKeyResource;

    return A.exists((hierarchyId) => hierarchyId === hierarchyUuid)(
      updatingList,
    ) ? (
      // overwrite minWidth to align the progress icon with the add/remove icon.
      <LoadingButton loading variant="text" sx={{ minWidth: "40px" }} />
    ) : (
      <TooltipIconButton
        title={label}
        aria-label={label}
        onClick={async (event) => {
          // Prevent to collapse the tree node when click the button.
          event.stopPropagation();
          await updateKeyResource(hierarchyUuid, item, action);
        }}
      >
        {isAdded ? <RemoveIcon /> : <AddIcon />}
      </TooltipIconButton>
    );
  };

  /**
   * Build the add/remove button for the hierarchy.
   */
  const buildUpdateKeyResourceButton =
    (item: OEQ.Search.SearchResultItem) => (hierarchyUuid: string) =>
      pipe(
        hierarchyIdsWithKeyResource,
        pfTernary(
          A.exists((uuid) => uuid === hierarchyUuid),
          constTrue,
          constFalse,
        ),
        (isAdded) => updateKeyResourceButton(hierarchyUuid, item, isAdded),
      );

  return (
    <Dialog
      open={open}
      onClose={onClose}
      aria-labelledby={LABELLED_BY}
      aria-describedby={DESCRIBE_BY}
      fullWidth
      maxWidth="md"
    >
      <DialogTitle id={LABELLED_BY}>
        <Box sx={(theme) => ({ marginBottom: theme.spacing(1) })}>
          {dialogTitle}
        </Box>
        <DialogContentText id={DESCRIBE_BY}>{dialogDesc}</DialogContentText>
      </DialogTitle>

      <DialogContent
        sx={{
          // Handle overflow case for hierarchy tree.
          overflow: "auto",
        }}
      >
        {isLoading ? (
          <HierarchyTreeSkeleton />
        ) : (
          <HierarchyTree
            hierarchies={hierarchies}
            onlyShowTitle
            disableTitleLink
            customActionBuilder={buildUpdateKeyResourceButton(item)}
          />
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="secondary">
          {closeText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ModifyKeyResourceDialog;
