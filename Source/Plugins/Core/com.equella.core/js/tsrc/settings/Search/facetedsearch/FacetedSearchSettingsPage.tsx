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
import { useState } from "react";
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
  addFlags,
  batchUpdateOrAdd,
  ModifiedClassification,
  FacetedSearchClassification,
  getClassificationsFromServer,
  removeFlags,
  getHighestOrderIndex,
} from "./FacetedSearchSettingsModule";
import EditIcon from "@material-ui/icons/Edit";
import DeleteIcon from "@material-ui/icons/Delete";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import ClassificationDialog from "./ClassificationDialog";
import { useEffect } from "react";
import { routes } from "../../../mainui/routes";
import { addElement } from "../../../util/ImmutableArrayUtil";
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
 * A page for setting Faceted search classifications.
 */
const FacetedSearchSettingsPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const facetedsearchsettingStrings =
    languageStrings.settings.searching.facetedsearchsetting;
  const classes = useStyles();

  // Show the snackbar when all the status of a 207 response is 2xx.
  const [showSnackBar, setShowSnackBar] = useState<boolean>(false);
  // Show a message dialog when a 207 response includes any status that's 4xx.
  const [showResultDialog, setShowResultDialog] = useState<boolean>(false);
  const [resultMessages, setResultMessagesMessages] = useState<string[]>([]);

  const [showEditingDialog, setShowEditingDialog] = useState<boolean>(false);
  const [classifications, setClassifications] = useState<
    ModifiedClassification[]
  >([]);

  const addOrEditQueue = classifications
    .filter((classification) => classification.changed)
    .map((classification) => removeFlags(classification));
  const changesUnsaved = addOrEditQueue.length > 0;

  /**
   * Update the page title and back route, and get a list of classifications.
   */
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(facetedsearchsettingStrings.name)(tp),
      backRoute: routes.Settings.to,
    }));
    getClassifications();
  }, []);

  const getClassifications = () => {
    getClassificationsFromServer().then((classifications) => {
      setClassifications(
        classifications.map((classification) => {
          return addFlags(classification, false, false, false);
        })
      );
    });
  };

  /**
   * Save classifications in the queue to the Server.
   * Show the message dialog if any error message is received otherwise show snackbar.
   */
  const save = () => {
    const errorMessages: string[] = [];
    batchUpdateOrAdd(addOrEditQueue)
      .then((messages) => {
        errorMessages.push(...messages);
        if (errorMessages.length > 0) {
          setResultMessagesMessages(errorMessages);
          setShowResultDialog(true);
        } else {
          setShowSnackBar(true);
        }
      })
      .catch((error) => handleError(error))
      .finally(() => getClassifications());
  };

  /**
   * Visually add/update a classification
   */
  const addOrEdit = (classification: FacetedSearchClassification) => {
    setClassifications(
      addElement(classifications, addFlags(classification, true, false, true))
    );
  };

  /**
   * Error handling which throws a new error in order to break chained 'then'.
   */
  const handleError = (error: Error) => {
    updateTemplate(templateError(generateFromError(error)));
    throw new Error(error.message);
  };

  return (
    <SettingPageTemplate
      onSave={save}
      saveButtonDisabled={!changesUnsaved}
      snackbarOpen={showSnackBar}
      snackBarOnClose={() => setShowSnackBar(false)}
      preventNavigation={changesUnsaved}
    >
      <Card className={classes.spacedCards}>
        <List
          subheader={
            <ListSubheader disableGutters>
              {facetedsearchsettingStrings.name}
            </ListSubheader>
          }
        >
          {classifications
            .filter((classification) => !classification.deleted)
            .map((classification, index) => {
              return (
                <ListItem divider={true} key={index}>
                  <ListItemText primary={classification.name} />
                  <ListItemSecondaryAction>
                    <IconButton color={"secondary"}>
                      <EditIcon />
                    </IconButton>
                    |
                    <IconButton color="secondary">
                      <DeleteIcon />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              );
            })}
        </List>
        <CardActions className={classes.cardAction}>
          <IconButton
            onClick={() => setShowEditingDialog(true)}
            aria-label={facetedsearchsettingStrings.add}
            color={"primary"}
          >
            <AddCircleIcon fontSize={"large"} />
          </IconButton>
        </CardActions>
      </Card>

      <ClassificationDialog
        addOrEdit={addOrEdit}
        open={showEditingDialog}
        onClose={() => setShowEditingDialog(false)}
        handleError={handleError}
        classification={undefined}
        highestOrderIndex={getHighestOrderIndex(classifications)}
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
