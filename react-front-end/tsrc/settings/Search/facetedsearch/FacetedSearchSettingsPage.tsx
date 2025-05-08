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
import { monitorForElements } from "@atlaskit/pragmatic-drag-and-drop/element/adapter";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import { Card, CardContent, IconButton, List, Typography } from "@mui/material";
import * as A from "fp-ts/Array";
import { constVoid, constTrue, pipe, flow } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import { ReactElement, useContext, useEffect, useState } from "react";
import MessageDialog from "../../../components/MessageDialog";
import SettingPageTemplate from "../../../components/SettingPageTemplate";
import SettingsCardActions from "../../../components/SettingsCardActions";
import SettingsListHeading from "../../../components/SettingsListHeading";
import { AppContext } from "../../../mainui/App";
import { routes } from "../../../mainui/routes";
import {
  templateDefaults,
  TemplateUpdateProps,
} from "../../../mainui/Template";
import {
  batchDelete,
  batchUpdateOrAdd,
  facetComparator,
  FacetedSearchClassificationWithFlags,
  getFacetsFromServer,
  getHighestOrderIndex,
  removeFacetFromList,
  reorder,
} from "../../../modules/FacetedSearchSettingsModule";
import { commonString } from "../../../util/commonstrings";
import { idExtractor } from "../../../util/idExtractor";
import { addElement, replaceElement } from "../../../util/ImmutableArrayUtil";
import { languageStrings } from "../../../util/langstrings";
import { DraggableFacet, FacetDndPayloadCodec } from "./DraggableFacet";
import FacetDialog from "./FacetDialog";

const facetedsearchsettingStrings =
  languageStrings.settings.searching.facetedsearchsetting;

/**
 * A page for setting Faceted search facets.
 */
const FacetedSearchSettingsPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const [showSnackBar, setShowSnackBar] = useState<boolean>(false);
  const [showResultDialog, setShowResultDialog] = useState<boolean>(false);
  const [resultMessages, setResultMessagesMessages] = useState<string[]>([]);
  const [showEditingDialog, setShowEditingDialog] = useState<boolean>(false);
  const [facets, setFacets] = useState<FacetedSearchClassificationWithFlags[]>(
    [],
  );
  const [currentFacet, setCurrentFacet] = useState<
    FacetedSearchClassificationWithFlags | undefined
  >();
  const [reset, setReset] = useState<boolean>(true);
  const { appErrorHandler } = useContext(AppContext);

  const listOfUpdates: FacetedSearchClassificationWithFlags[] = facets.filter(
    (facet) => facet.updated && !facet.deleted,
  );
  const listOfDeleted: FacetedSearchClassificationWithFlags[] = facets.filter(
    (facet) => facet.deleted,
  );
  const changesUnsaved = listOfUpdates.length > 0 || listOfDeleted.length > 0;

  /**
   * Update the page title and back route, and get a list of facets.
   */
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(facetedsearchsettingStrings.name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  /**
   * Get facets from the server, sort them by order index, and add flags to them.
   */
  useEffect(() => {
    getFacetsFromServer()
      .then((facets) =>
        setFacets(
          facets.map((facet) => {
            return { ...facet, updated: false, deleted: false };
          }),
        ),
      )
      .catch(appErrorHandler);
  }, [reset, appErrorHandler]);

  useEffect(() => {
    const getFacetIndex: (data: Record<string, unknown>) => O.Option<number> =
      flow(
        O.fromPredicate(FacetDndPayloadCodec.is),
        O.map(({ index }) => index),
      );

    return monitorForElements({
      canMonitor: constTrue,
      onDrop: ({ location, source }) =>
        pipe(
          location.current.dropTargets,
          A.head,
          O.chain((target) =>
            pipe(
              O.Do,
              O.apS("startIndex", getFacetIndex(source.data)),
              O.apS("endIndex", getFacetIndex(target.data)),
              O.map(({ startIndex, endIndex }) =>
                reorder(facets, startIndex, endIndex),
              ),
            ),
          ),
          O.match(constVoid, setFacets),
        ),
    });
  }, [facets]);

  /**
   * Save updated/deleted facets to the server.
   * Show the message dialog if any error message is received. Otherwise, show snackbar.
   */
  const save = () => {
    const updatePromise: Promise<string[]> = listOfUpdates.length
      ? batchUpdateOrAdd(listOfUpdates)
      : Promise.resolve([]);

    const deletePromise: Promise<string[]> = listOfDeleted.length
      ? batchDelete(listOfDeleted.map(idExtractor))
      : Promise.resolve([]);

    Promise.all([updatePromise, deletePromise])
      .then((messages) => {
        const errorMessages = messages.flat();
        if (errorMessages.length > 0) {
          setResultMessagesMessages(errorMessages);
          setShowResultDialog(true);
        } else {
          setShowSnackBar(true);
        }
      })
      .catch(appErrorHandler)
      .finally(() => setReset(!reset));
  };

  /**
   * Visually add/update a facet.
   */
  const addOrEdit = (
    name: string,
    schemaNode: string,
    maxResults: number | undefined,
  ) => {
    let newFacet: FacetedSearchClassificationWithFlags;
    if (currentFacet) {
      newFacet = {
        ...currentFacet,
        name,
        schemaNode,
        maxResults,
        updated: true,
      };
      setFacets(
        replaceElement(facets, facetComparator(currentFacet), newFacet),
      );
    } else {
      newFacet = {
        name,
        schemaNode,
        maxResults,
        orderIndex: getHighestOrderIndex(facets) + 1,
        updated: true,
        deleted: false,
      };
      setFacets(addElement(facets, newFacet));
    }
  };

  /**
   * Visually delete a facet.
   */
  const deleteFacet = (deletedfacet: FacetedSearchClassificationWithFlags) => {
    setFacets(removeFacetFromList(facets, deletedfacet.orderIndex));
  };

  const editFacet = (facet: FacetedSearchClassificationWithFlags) => {
    setShowEditingDialog(true);
    setCurrentFacet(facet);
  };

  /**
   * Render a Draggable area which renders a ListItem for each non-deleted facet.
   */
  const facetListItems: ReactElement[] = facets
    .filter((facet) => !facet.deleted)
    .sort((prev, current) => prev.orderIndex - current.orderIndex)
    .map((facet, index) => {
      const key = facet.id ?? facet.name + index;
      return (
        <DraggableFacet
          key={key}
          facet={facet}
          index={index}
          onDelete={deleteFacet}
          onEdit={editFacet}
        />
      );
    });

  /**
   * Render a Droppable area which includes a list of configured facets.
   */
  const facetList: ReactElement = <List>{facetListItems}</List>;

  return (
    <SettingPageTemplate
      onSave={save}
      saveButtonDisabled={!changesUnsaved}
      snackbarOpen={showSnackBar}
      snackBarOnClose={() => setShowSnackBar(false)}
      preventNavigation={changesUnsaved}
    >
      <Card>
        <CardContent>
          <SettingsListHeading
            heading={facetedsearchsettingStrings.subHeading}
          />
          <Typography variant="caption">
            {facetedsearchsettingStrings.explanationText}
          </Typography>
          {facetList}
        </CardContent>
        <SettingsCardActions>
          <IconButton
            onClick={() => {
              setCurrentFacet(undefined);
              setShowEditingDialog(true);
            }}
            aria-label={facetedsearchsettingStrings.add}
            color="primary"
            size="large"
          >
            <AddCircleIcon fontSize="large" />
          </IconButton>
        </SettingsCardActions>
      </Card>

      <FacetDialog
        addOrEdit={addOrEdit}
        open={showEditingDialog}
        onClose={() => setShowEditingDialog(false)}
        facet={currentFacet}
      />

      <MessageDialog
        open={showResultDialog}
        messages={resultMessages}
        title={commonString.result.errors}
        close={() => setShowResultDialog(false)}
      />
    </SettingPageTemplate>
  );
};

export default FacetedSearchSettingsPage;
