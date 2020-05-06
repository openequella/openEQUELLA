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
import { FacetedSearchClassification } from "./FacetedSearchSettingsModule";
import EditIcon from "@material-ui/icons/Edit";
import DeleteIcon from "@material-ui/icons/Delete";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import ClassificationDialog from "./ClassificationDialog";
import { useEffect } from "react";
import { routes } from "../../../mainui/routes";
import { addElement } from "../../../util/ImmutableArrayUtil";
import { generateFromError } from "../../../api/errors";

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

  const [showSnackBar, setShowSnackBar] = useState<boolean>(false);
  const [openDialog, setOpenDialog] = useState<boolean>(false);

  // A list of Classifications displayed in this page, including unsaved changes.
  const [classifications, setClassifications] = useState<
    FacetedSearchClassification[]
  >([]);

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(facetedsearchsettingStrings.name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, []);

  const save = () => {
    // No implementation until required REST endpoints are completed.
  };

  /**
   * Visually add/update a classification
   */
  const addOrUpdate = (classification: FacetedSearchClassification) => {
    setClassifications(addElement(classifications, classification));
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
      saveButtonDisabled={false}
      snackbarOpen={showSnackBar}
      snackBarOnClose={() => setShowSnackBar(false)}
      preventNavigation={false}
    >
      <Card className={classes.spacedCards}>
        <List
          subheader={
            <ListSubheader disableGutters>
              {facetedsearchsettingStrings.name}
            </ListSubheader>
          }
        >
          {classifications.map((classification, index) => {
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
            onClick={() => setOpenDialog(true)}
            aria-label={facetedsearchsettingStrings.add}
            color={"primary"}
          >
            <AddCircleIcon fontSize={"large"} />
          </IconButton>
        </CardActions>
      </Card>

      <ClassificationDialog
        addOrEdit={addOrUpdate}
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        handleError={handleError}
        classification={undefined}
      />
    </SettingPageTemplate>
  );
};

export default FacetedSearchSettingsPage;
