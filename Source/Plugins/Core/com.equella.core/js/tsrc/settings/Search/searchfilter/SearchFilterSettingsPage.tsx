import * as React from "react";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps
} from "../../../mainui/Template";
import { routes } from "../../../mainui/routes";
import {
  Button,
  Card,
  CardActions,
  ListItem,
  ListItemSecondaryAction,
  ListItemText,
  ListSubheader,
  IconButton,
  List
} from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import EditIcon from "@material-ui/icons/Edit";
import DeleteIcon from "@material-ui/icons/Delete";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import { languageStrings } from "../../../util/langstrings";
import SettingsList from "../../../components/SettingsList";
import SettingsListControl from "../../../components/SettingsListControl";
import SettingsToggleSwitch from "../../../components/SettingsToggleSwitch";
import { useEffect, useState } from "react";
import {
  defaultSearchSettings,
  getSearchSettingsFromServer,
  saveSearchSettingsToServer,
  SearchSettings
} from "../SearchSettingsModule";
import { Save } from "@material-ui/icons";
import MessageInfo from "../../../components/MessageInfo";
import {
  batchDelete,
  batchUpdateOrAdd,
  getMimeTypeFiltersFromServer,
  MimeTypeFilter
} from "./SearchFilterSettingsModule";
import MimeTypeFilterEditor from "./MimeTypeFilterEditor";
import { commonString } from "../../../util/commonstrings";
import { fromAxiosError } from "../../../api/errors";
import { AxiosError } from "axios";

const useStyles = makeStyles({
  spacedCards: {
    margin: "16px",
    width: "75%",
    padding: "16px",
    float: "left"
  },
  cardAction: {
    display: "flex",
    justifyContent: "flex-end"
  },
  floatingButton: {
    position: "fixed",
    top: 0,
    right: 0,
    marginTop: "80px",
    marginRight: "16px",
    width: "calc(25% - 112px)"
  }
});

const SearchFilterPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const classes = useStyles();
  const searchFilterStrings =
    languageStrings.settings.searching.searchfiltersettings;

  // The general Search settings. Here only configure searchingDisableOwnerFilter and searchingDisableDateModifiedFilter
  const [searchSettings, setSearchSettings] = useState<SearchSettings>(
    defaultSearchSettings
  );

  // Used to record the initial values of the two filters and compare if values are changed or not when saving
  const [initialOwnerFilter, setInitialOwnerFilter] = useState<boolean>(false);
  const [initialDateModifiedFilter, setInitialDateModifiedFilter] = useState<
    boolean
  >(false);

  // mimeTypeFilters contains all filters displayed in the list, including those saved in the Server and visually added/deleted
  const [mimeTypeFilters, setMimeTypeFilters] = useState<MimeTypeFilter[]>([]);

  // changedMimeTypeFilters contains filters to be added or edited on the Server
  const [changedMimeTypeFilters, setChangedMimeTypeFilters] = useState<
    MimeTypeFilter[]
  >([]);

  // deletedMimeTypeFilters contains filters to be deleted from the Server
  const [deletedMimeTypeFilters, setDeletedMimeTypeFilters] = useState<
    MimeTypeFilter[]
  >([]);

  // selectedMimeTypeFilter refers to the filter to be edited in the dialog
  const [selectedMimeTypeFilter, setSelectedMimeTypeFilter] = useState<
    MimeTypeFilter | undefined
  >();

  const [showSnackBar, setShowSnackBar] = useState<boolean>(false);
  const [openMimeTypeFilterEditor, setOpenMimeTypeFilterEditor] = useState<
    boolean
  >(false);

  useEffect(() => {
    updateTemplate(tp => ({
      ...templateDefaults(searchFilterStrings.name)(tp),
      backRoute: routes.Settings.to
    }));
  }, []);

  useEffect(() => {
    getSearchSettings();
  }, []);

  useEffect(() => {
    getMimeTypeFilters();
  }, []);

  const setOwnerFilter = (disabled: boolean) => {
    setSearchSettings({
      ...searchSettings,
      searchingDisableOwnerFilter: disabled
    });
  };

  const setDateModifiedFilter = (disabled: boolean) => {
    setSearchSettings({
      ...searchSettings,
      searchingDisableDateModifiedFilter: disabled
    });
  };

  /**
   * Fetch the general Search Settings from the Server;
   * Set the initial values of the Owner filter and Date modified filter to state.
   */
  const getSearchSettings = () => {
    getSearchSettingsFromServer()
      .then(settings => {
        setSearchSettings(settings);
        setInitialOwnerFilter(settings.searchingDisableOwnerFilter);
        setInitialDateModifiedFilter(
          settings.searchingDisableDateModifiedFilter
        );
      })
      .catch((error: AxiosError) => handleError(error));
  };

  /**
   * Fetch MIME type filters from the Server and set them to state;
   * Clear the two collections of changed and deleted MIME type filters.
   */
  const getMimeTypeFilters = () => {
    getMimeTypeFiltersFromServer()
      .then((filters: MimeTypeFilter[]) => {
        setMimeTypeFilters(filters);
        setDeletedMimeTypeFilters([]);
        setChangedMimeTypeFilters([]);
      })
      .catch((error: AxiosError) => handleError(error));
  };

  /**
   * Visually add or update a filter;
   * Add this filter into the collection of changed MIME type filters
   */
  const addOrUpdateMimeTypeFilter = (filter: MimeTypeFilter) => {
    // Update the list first
    if (!selectedMimeTypeFilter) {
      setMimeTypeFilters([...mimeTypeFilters, filter]);
      setChangedMimeTypeFilters([...changedMimeTypeFilters, filter]);
    } else {
      const index = mimeTypeFilters.indexOf(selectedMimeTypeFilter);
      const filters = [...mimeTypeFilters];
      filters[index] = filter;
      setMimeTypeFilters(filters);
    }

    // Update the collection of changed MIME type filters
    // if 'filter' comes from the editing of 'selectedMimeTypeFilter' and 'selectedMimeTypeFilter'
    // exists in 'changedMimeTypeFilters', then replace 'selectedMimeTypeFilter' with 'filter';
    // otherwise add 'filter' to 'changedMimeTypeFilters'
    if (
      selectedMimeTypeFilter &&
      changedMimeTypeFilters.indexOf(selectedMimeTypeFilter) > -1
    ) {
      const index = changedMimeTypeFilters.indexOf(selectedMimeTypeFilter);
      const filters = [...changedMimeTypeFilters];
      filters[index] = filter;
      setChangedMimeTypeFilters(filters);
    } else {
      setChangedMimeTypeFilters([...changedMimeTypeFilters, filter]);
    }
  };

  /**
   * Visually delete a filter;
   * Add this filter into the collection of deleted MIME type filters.
   */
  const deleteMimeTypeFilter = (filter: MimeTypeFilter) => {
    // Update the list of filters
    setMimeTypeFilters(
      mimeTypeFilters.filter(mimeTypeFilter => mimeTypeFilter !== filter)
    );

    // Only put filters that already have an id into deletedMimeTypeFilters
    if (filter.id) {
      setDeletedMimeTypeFilters([...deletedMimeTypeFilters, filter]);
    }

    // If this filter is also in changedMimeTypeFilters then remove it from changedMimeTypeFilters
    const indexInChangedMimeTypeFilters = changedMimeTypeFilters.indexOf(
      filter
    );
    if (indexInChangedMimeTypeFilters > -1) {
      const filters = [...changedMimeTypeFilters];
      filters.splice(indexInChangedMimeTypeFilters, 1);
      setChangedMimeTypeFilters(filters);
    }
  };

  const openMimeTypeFilterDialog = (filter?: MimeTypeFilter) => {
    setOpenMimeTypeFilterEditor(true);
    setSelectedMimeTypeFilter(filter);
  };

  const closeMimeTypeFilterDialog = () => {
    setOpenMimeTypeFilterEditor(false);
    //setSelectedMimeTypeFilter(undefined);
  };

  const mimeTypeFilterchanged =
    deletedMimeTypeFilters.length || changedMimeTypeFilters.length;

  const generalSearchSettingChanged =
    initialOwnerFilter !== searchSettings.searchingDisableOwnerFilter ||
    initialDateModifiedFilter !==
      searchSettings.searchingDisableDateModifiedFilter;

  /**
   * Save general Search setting only when the configuration of Owner filter or Date modified filter has been changed;
   * Save MIME type filters only when they have been updated, delete or just created.
   */
  const save = () => {
    if (mimeTypeFilterchanged || generalSearchSettingChanged) {
      (generalSearchSettingChanged
        ? saveSearchSettingsToServer(searchSettings).catch(error =>
            handleError(error)
          )
        : Promise.resolve()
      )
        .then(
          (): Promise<any> =>
            changedMimeTypeFilters.length
              ? batchUpdateOrAdd(changedMimeTypeFilters).catch(error =>
                  handleError(error)
                )
              : Promise.resolve()
        )
        .then(
          (): Promise<any> =>
            deletedMimeTypeFilters.length
              ? batchDelete(deletedMimeTypeFilters).catch(error =>
                  handleError(error)
                )
              : Promise.resolve()
        )
        .then(() => setShowSnackBar(true))
        .catch(() => {}) // Errors have been handled and subsequent promises have skipped
        .finally(() => {
          getMimeTypeFilters();
          getSearchSettings();
        });
    }
  };

  const handleError = (error: AxiosError) => {
    updateTemplate(templateError(fromAxiosError(error)));
    // The reason for throwing an error again is to prevent subsequent REST calls
    throw new Error(error.message);
  };

  return (
    <>
      {/* Owner filter and Date modified filter*/}
      <Card className={classes.spacedCards}>
        <SettingsList subHeading={searchFilterStrings.visibilityconfigtitle}>
          <SettingsListControl
            divider
            primaryText={searchFilterStrings.disableownerfilter}
            secondaryText={""}
            control={
              <SettingsToggleSwitch
                value={searchSettings.searchingDisableOwnerFilter}
                setValue={value => {
                  setOwnerFilter(value);
                }}
                id={"disable_owner_filter_toggle"}
              />
            }
          />
          <SettingsListControl
            primaryText={searchFilterStrings.disabledatemodifiedfilter}
            secondaryText={""}
            control={
              <SettingsToggleSwitch
                value={searchSettings.searchingDisableDateModifiedFilter}
                setValue={value => {
                  setDateModifiedFilter(value);
                }}
                id={"disable_date_modified_filter_toggle"}
              />
            }
          />
        </SettingsList>
      </Card>

      {/* MIME type filters */}
      <Card className={classes.spacedCards}>
        <List
          subheader={
            <ListSubheader disableGutters>
              {searchFilterStrings.mimetypefiltertitle}
            </ListSubheader>
          }
        >
          {mimeTypeFilters.map(filter => {
            return (
              <ListItem divider={true} key={filter.name}>
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
        <CardActions className={classes.cardAction}>
          <IconButton
            onClick={() => openMimeTypeFilterDialog()}
            aria-label={searchFilterStrings.add}
            color={"primary"}
          >
            <AddCircleIcon fontSize={"large"} />
          </IconButton>
        </CardActions>
      </Card>

      {/* SAVE button*/}
      <Button
        color={"primary"}
        className={classes.floatingButton}
        variant="contained"
        size="large"
        onClick={save}
        aria-label={searchFilterStrings.save}
      >
        <Save />
        {commonString.action.save}
      </Button>

      {/* Snackbar */}
      <MessageInfo
        title={searchFilterStrings.changesaved}
        open={showSnackBar}
        onClose={() => setShowSnackBar(false)}
        variant={"success"}
      />

      {/* MIME type filter dialog */}
      <MimeTypeFilterEditor
        open={openMimeTypeFilterEditor}
        onClose={closeMimeTypeFilterDialog}
        addOrUpdate={addOrUpdateMimeTypeFilter}
        mimeTypeFilter={selectedMimeTypeFilter}
        updateTemplate={updateTemplate}
      />
    </>
  );
};

export default SearchFilterPage;
