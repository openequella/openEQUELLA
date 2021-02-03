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
import {
  Card,
  CardActions,
  CardContent,
  IconButton,
  List,
  ListItem,
  ListItemSecondaryAction,
  ListItemText,
  makeStyles,
} from "@material-ui/core";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import DeleteIcon from "@material-ui/icons/Delete";
import EditIcon from "@material-ui/icons/Edit";
import * as React from "react";
import { useEffect, useState } from "react";
import { generateFromError } from "../../../api/errors";
import MessageDialog from "../../../components/MessageDialog";
import SettingPageTemplate from "../../../components/SettingPageTemplate";
import SettingsList from "../../../components/SettingsList";
import SettingsListControl from "../../../components/SettingsListControl";
import SettingsListHeading from "../../../components/SettingsListHeading";
import SettingsToggleSwitch from "../../../components/SettingsToggleSwitch";
import { routes } from "../../../mainui/routes";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps,
} from "../../../mainui/Template";
import {
  batchDelete,
  batchUpdateOrAdd,
  filterComparator,
  getMimeTypeFiltersFromServer,
  MimeTypeFilter,
} from "../../../modules/SearchFilterSettingsModule";
import {
  defaultSearchSettings,
  getSearchSettingsFromServer,
  saveSearchSettingsToServer,
} from "../../../modules/SearchSettingsModule";
import { commonString } from "../../../util/commonstrings";
import { idExtractor } from "../../../util/idExtractor";
import {
  addElement,
  deleteElement,
  replaceElement,
} from "../../../util/ImmutableArrayUtil";
import { languageStrings } from "../../../util/langstrings";
import useError from "../../../util/useError";
import MimeTypeFilterEditingDialog from "./MimeTypeFilterEditingDialog";
import * as OEQ from "@openequella/rest-api-client";

const useStyles = makeStyles({
  cardAction: {
    display: "flex",
    justifyContent: "flex-end",
  },
});

const searchFilterStrings =
  languageStrings.settings.searching.searchfiltersettings;

const SearchFilterPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const classes = useStyles();

  // The general Search settings. Here only configure searchingDisableOwnerFilter and searchingDisableDateModifiedFilter.
  const [
    searchSettings,
    setSearchSettings,
  ] = useState<OEQ.SearchSettings.Settings>(defaultSearchSettings);

  // Used to record the initial Search settings and compare if values are changed or not when saving.
  const [
    initialSearchSettings,
    setInitialSearchSettings,
  ] = useState<OEQ.SearchSettings.Settings>(defaultSearchSettings);

  // mimeTypeFilters contains all filters displayed in the list, including those saved in the Server and visually added/deleted.
  const [mimeTypeFilters, setMimeTypeFilters] = useState<MimeTypeFilter[]>([]);

  // changedMimeTypeFilters contains filters to be added or edited on the Server.
  const [changedMimeTypeFilters, setChangedMimeTypeFilters] = useState<
    MimeTypeFilter[]
  >([]);

  // deletedMimeTypeFilters contains filters to be deleted from the Server.
  const [deletedMimeTypeFilters, setDeletedMimeTypeFilters] = useState<
    MimeTypeFilter[]
  >([]);

  // selectedMimeTypeFilter refers to the filter to be edited in the dialog.
  const [selectedMimeTypeFilter, setSelectedMimeTypeFilter] = useState<
    MimeTypeFilter | undefined
  >();

  const [showSnackBar, setShowSnackBar] = useState<boolean>(false);
  const [
    openMimeTypeFilterEditor,
    setOpenMimeTypeFilterEditor,
  ] = useState<boolean>(false);
  const [openMessageDialog, setOpenMessageDialog] = useState<boolean>(false);
  const [messageDialogMessages, setMessageDialogMessages] = useState<string[]>(
    []
  );
  const [reset, setReset] = useState<boolean>(true);

  const setError = useError((error: Error) => {
    updateTemplate(templateError(generateFromError(error)));
  });

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchFilterStrings.name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  useEffect(() => {
    /**
     * Fetch the general Search Settings from the Server.
     * Set the initial values of the Owner filter and Date modified filter to state.
     */
    const getSearchSettings = () => {
      getSearchSettingsFromServer()
        .then((settings) => {
          setSearchSettings(settings);
          setInitialSearchSettings(settings);
        })
        .catch(setError);
    };

    /**
     * Fetch MIME type filters from the Server and set them to state.
     * Clear the two collections of changed and deleted MIME type filters.
     */
    const getMimeTypeFilters = () => {
      getMimeTypeFiltersFromServer()
        .then((filters: MimeTypeFilter[]) => {
          setMimeTypeFilters(filters);
          setDeletedMimeTypeFilters([]);
          setChangedMimeTypeFilters([]);
        })
        .catch(setError);
    };

    getSearchSettings();
    getMimeTypeFilters();
  }, [reset, setError]);

  const mimeTypeFilterChanged =
    deletedMimeTypeFilters.length || changedMimeTypeFilters.length;

  const generalSearchSettingChanged =
    initialSearchSettings.searchingDisableOwnerFilter !==
      searchSettings.searchingDisableOwnerFilter ||
    initialSearchSettings.searchingDisableDateModifiedFilter !==
      searchSettings.searchingDisableDateModifiedFilter;

  const changesUnsaved = !!mimeTypeFilterChanged || generalSearchSettingChanged;

  /**
   * Visually add or update a filter.
   * If 'selectedMimeTypeFilter' is undefined, then the action is adding a new filter, so add
   * 'filter' to the 'mimeTypeFilters' and 'changedMimeTypeFilters'; otherwise replace 'selectedMimeTypeFilter'
   * with 'filter'.
   */
  const addOrUpdateMimeTypeFilter = (filter: MimeTypeFilter) => {
    if (!selectedMimeTypeFilter) {
      setMimeTypeFilters(addElement(mimeTypeFilters, filter));
      setChangedMimeTypeFilters(addElement(changedMimeTypeFilters, filter));
    } else {
      setMimeTypeFilters(
        replaceElement(
          mimeTypeFilters,
          filterComparator(selectedMimeTypeFilter),
          filter
        )
      );
      setChangedMimeTypeFilters(
        replaceElement(
          changedMimeTypeFilters,
          filterComparator(selectedMimeTypeFilter),
          filter
        )
      );
    }
  };

  /**
   * Visually delete a filter.
   * Remove 'filter' from 'mimeTypeFilters' and 'changedMimeTypeFilters'.
   * If the filter has an ID then add it to 'deletedMimeTypeFilters'.
   */
  const deleteMimeTypeFilter = (filter: MimeTypeFilter) => {
    setMimeTypeFilters(
      deleteElement(mimeTypeFilters, filterComparator(filter), 1)
    );
    setChangedMimeTypeFilters(
      deleteElement(changedMimeTypeFilters, filterComparator(filter), 1)
    );

    // Only put filters that already have an id into deletedMimeTypeFilters
    if (filter.id) {
      setDeletedMimeTypeFilters(addElement(deletedMimeTypeFilters, filter));
    }
  };

  const openMimeTypeFilterDialog = (filter?: MimeTypeFilter) => {
    setOpenMimeTypeFilterEditor(true);
    setSelectedMimeTypeFilter(filter);
  };

  const closeMimeTypeFilterDialog = () => {
    setOpenMimeTypeFilterEditor(false);
  };

  /**
   * Save general Search setting only when the configuration of Owner filter or Date modified filter has been changed.
   * Save MIME type filters only when they have been updated, delete or just created.
   */
  const save = () => {
    const errorMessages: string[] = [];
    if (!mimeTypeFilterChanged && !generalSearchSettingChanged) {
      return;
    }
    (generalSearchSettingChanged
      ? saveSearchSettingsToServer(searchSettings).catch((error) =>
          handleError(error, true)
        )
      : Promise.resolve()
    )
      .then(
        (): Promise<number | void> =>
          changedMimeTypeFilters.length
            ? batchUpdateOrAdd(changedMimeTypeFilters)
                .then((messages) => errorMessages.push(...messages))
                .catch((error) => handleError(error, true))
            : Promise.resolve()
      )
      .then(
        (): Promise<number | void> =>
          // Filters stored in 'deletedMimeTypeFilters' always have an ID.
          deletedMimeTypeFilters.length
            ? batchDelete(deletedMimeTypeFilters.map(idExtractor))
                .then((messages) => errorMessages.push(...messages))
                .catch((error) => handleError(error, true))
            : Promise.resolve()
      )
      .then(() => {
        // If the 207 response includes any non-2xx responses then display a dialog to show error messages.
        if (errorMessages.length > 0) {
          setMessageDialogMessages(errorMessages);
          setOpenMessageDialog(true);
        } else {
          setShowSnackBar(true);
        }
      })
      .catch(() => {}) // Errors have been handled and subsequent promises have skipped.
      .finally(() => {
        // Toggle `reset` to trigger effect
        setReset(!reset);
      });
  };

  const handleError = (error: Error, withRethrow = false) => {
    updateTemplate(templateError(generateFromError(error)));
    // The reason for throwing an error again is to prevent subsequent REST calls.
    // Is this a sign of a more fundamental issue? Leaving for now, as now's not
    // the time to drastically change such things
    if (withRethrow) {
      throw new Error(error.message);
    }
  };

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
          <SettingsList subHeading={searchFilterStrings.visibilityconfigtitle}>
            <SettingsListControl
              divider
              primaryText={searchFilterStrings.disableownerfilter}
              control={
                <SettingsToggleSwitch
                  value={searchSettings.searchingDisableOwnerFilter}
                  setValue={(disabled) => {
                    setSearchSettings({
                      ...searchSettings,
                      searchingDisableOwnerFilter: disabled,
                    });
                  }}
                  id="disable_owner_filter_toggle"
                />
              }
            />
            <SettingsListControl
              primaryText={searchFilterStrings.disabledatemodifiedfilter}
              control={
                <SettingsToggleSwitch
                  value={searchSettings.searchingDisableDateModifiedFilter}
                  setValue={(disabled) => {
                    setSearchSettings({
                      ...searchSettings,
                      searchingDisableDateModifiedFilter: disabled,
                    });
                  }}
                  id="disable_date_modified_filter_toggle"
                />
              }
            />
          </SettingsList>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <SettingsListHeading
            heading={searchFilterStrings.mimetypefiltertitle}
          />
          <List>
            {mimeTypeFilters.map((filter, index) => {
              return (
                <ListItem divider key={index}>
                  <ListItemText primary={filter.name} />
                  <ListItemSecondaryAction>
                    <IconButton
                      onClick={() => {
                        openMimeTypeFilterDialog(filter);
                      }}
                      aria-label={`${searchFilterStrings.edit} ${filter.name}`}
                      color="secondary"
                    >
                      <EditIcon />
                    </IconButton>
                    |
                    <IconButton
                      onClick={() => deleteMimeTypeFilter(filter)}
                      aria-label={`${searchFilterStrings.delete} ${filter.name}`}
                      color="secondary"
                    >
                      <DeleteIcon />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              );
            })}
          </List>
        </CardContent>

        <CardActions className={classes.cardAction}>
          <IconButton
            onClick={() => openMimeTypeFilterDialog()}
            aria-label={searchFilterStrings.add}
            color="primary"
          >
            <AddCircleIcon fontSize="large" />
          </IconButton>
        </CardActions>
      </Card>

      {
        // Delay creation of the MimeTypeFilterEditingDialog so that related REST calls are only
        // made when needed.
        openMimeTypeFilterEditor && (
          <MimeTypeFilterEditingDialog
            open={openMimeTypeFilterEditor}
            onClose={closeMimeTypeFilterDialog}
            addOrUpdate={addOrUpdateMimeTypeFilter}
            mimeTypeFilter={selectedMimeTypeFilter}
            handleError={handleError}
          />
        )
      }

      <MessageDialog
        open={openMessageDialog}
        messages={messageDialogMessages}
        title={commonString.result.errors}
        close={() => setOpenMessageDialog(false)}
      />
    </SettingPageTemplate>
  );
};

export default SearchFilterPage;
