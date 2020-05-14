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
  templateDefaults,
  templateError,
  TemplateUpdateProps,
} from "../../../mainui/Template";
import SettingPageTemplate from "../../../components/SettingPageTemplate";
import { ReactElement, useState } from "react";
import {
  Card,
  CardActions,
  IconButton,
  List,
  ListItem,
  ListItemSecondaryAction,
  ListItemText,
  ListSubheader,
  makeStyles,
} from "@material-ui/core";
import { languageStrings } from "../../../util/langstrings";
import {
  batchDelete,
  batchUpdateOrAdd,
  facetComparator,
  FacetWithFlags,
  getFacetsFromServer,
  getHighestOrderIndex,
} from "./FacetedSearchSettingsModule";
import EditIcon from "@material-ui/icons/Edit";
import DeleteIcon from "@material-ui/icons/Delete";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import FacetDialog from "./FacetDialog";
import { useEffect } from "react";
import { routes } from "../../../mainui/routes";
import {
  addElement,
  deleteElement,
  replaceElement,
} from "../../../util/ImmutableArrayUtil";
import { generateFromError } from "../../../api/errors";
import MessageDialog from "../../../components/MessageDialog";
import { commonString } from "../../../util/commonstrings";

const useStyles = makeStyles({
  spacedCards: {
    margin: "16px",
    width: "75%",
    padding: "16px",
    float: "left",
  },
  cardAction: {
    display: "flex",
    justifyContent: "flex-end",
  },
});

/**
 * A page for setting Faceted search facets.
 */
const FacetedSearchSettingsPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const facetedsearchsettingStrings =
    languageStrings.settings.searching.facetedsearchsetting;
  const classes = useStyles();

  const [showSnackBar, setShowSnackBar] = useState<boolean>(false);
  const [showResultDialog, setShowResultDialog] = useState<boolean>(false);
  const [resultMessages, setResultMessagesMessages] = useState<string[]>([]);
  const [showEditingDialog, setShowEditingDialog] = useState<boolean>(false);
  const [facets, setFacets] = useState<FacetWithFlags[]>([]);
  const [currentFacet, setCurrentFacet] = useState<
    FacetWithFlags | undefined
  >();

  const listOfUpdates: FacetWithFlags[] = facets.filter(
    (facet) => facet.updated && !facet.deleted
  );
  const listOfDeleted: FacetWithFlags[] = facets.filter(
    (facet) => facet.deleted
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
    getFacets();
  }, []);

  /**
   * Get facets from the server and add flags to them.
   */
  const getFacets = () => {
    getFacetsFromServer()
      .then((facets) =>
        setFacets(
          facets.map((facet) => {
            return { ...facet, updated: false, deleted: false };
          })
        )
      )
      .catch((error: Error) => handleError(error));
  };

  /**
   * Save updated/deleted facets to the server.
   * Show the message dialog if any error message is received. Otherwise, show snackbar.
   */
  const save = () => {
    const updatePromise: Promise<string[]> = listOfUpdates.length
      ? batchUpdateOrAdd(listOfUpdates)
      : Promise.resolve([]);

    const deletePromise: Promise<string[]> = listOfDeleted.length
      ? batchDelete(listOfDeleted.map((facet) => facet.id!.toString()))
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
      .catch((error) => handleError(error))
      .finally(() => getFacets());
  };

  /**
   * Visually add/update a facet.
   */
  const addOrEdit = (
    name: string,
    schemaNode: string,
    maxResults: number | undefined
  ) => {
    let newFacet: FacetWithFlags;
    if (currentFacet) {
      newFacet = {
        ...currentFacet,
        name,
        schemaNode,
        maxResults,
        updated: true,
      };
      setFacets(
        replaceElement(facets, facetComparator(currentFacet), newFacet)
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
   * If ID is available, then update the delete flag; otherwise simply remove this facet from state.
   */
  const deleteFacet = (facet: FacetWithFlags) => {
    if (facet.id) {
      setFacets(
        replaceElement(facets, facetComparator(facet), {
          ...facet,
          deleted: true,
        })
      );
      return;
    }
    setFacets(deleteElement(facets, facetComparator(facet), 1));
  };

  /**
   * Error handling which throws a new error in order to break chained 'then'.
   */
  const handleError = (error: Error) => {
    updateTemplate(templateError(generateFromError(error)));
  };

  /**
   * Only renders a ListItem for each non-deleted facet.
   */
  const facetListItems: ReactElement[] = facets
    .filter((facet) => !facet.deleted)
    .map((facet, index) => {
      return (
        <ListItem divider key={facet.id ?? facet.name + index}>
          <ListItemText primary={facet.name} />
          <ListItemSecondaryAction>
            <IconButton
              color={"secondary"}
              onClick={() => {
                setShowEditingDialog(true);
                setCurrentFacet(facet);
              }}
            >
              <EditIcon />
            </IconButton>
            |
            <IconButton color="secondary" onClick={() => deleteFacet(facet)}>
              <DeleteIcon />
            </IconButton>
          </ListItemSecondaryAction>
        </ListItem>
      );
    });

  /**
   * A list display configured facets.
   */
  const facetList: ReactElement = (
    <List
      subheader={
        <ListSubheader disableGutters>
          {facetedsearchsettingStrings.name}
        </ListSubheader>
      }
    >
      {facetListItems}
    </List>
  );

  return (
    <SettingPageTemplate
      onSave={save}
      saveButtonDisabled={!changesUnsaved}
      snackbarOpen={showSnackBar}
      snackBarOnClose={() => setShowSnackBar(false)}
      preventNavigation={changesUnsaved}
    >
      <Card className={classes.spacedCards}>
        {facetList}
        <CardActions className={classes.cardAction}>
          <IconButton
            onClick={() => {
              setCurrentFacet(undefined);
              setShowEditingDialog(true);
            }}
            aria-label={facetedsearchsettingStrings.add}
            color={"primary"}
          >
            <AddCircleIcon fontSize={"large"} />
          </IconButton>
        </CardActions>
      </Card>

      <FacetDialog
        addOrEdit={addOrEdit}
        open={showEditingDialog}
        onClose={() => setShowEditingDialog(false)}
        handleError={handleError}
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
