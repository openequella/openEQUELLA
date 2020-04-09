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
  const searchingStrings = languageStrings.settings.searching;

  const [searchSettings, setSearchSettings] = useState<SearchSettings>(
    defaultSearchSettings
  );

  const [initialOwnerFilter, setInitialOwnerFilter] = useState<boolean>(false);
  const [initialDateModifiedFilter, setInitialDateModifiedFilter] = useState<
    boolean
  >(false);

  // mimeTypeFilters contains all filters displayed in the list, including those saved in the Server and visually added
  const [mimeTypeFilters, setMimeTypeFilters] = useState<MimeTypeFilter[]>([]);

  // changedMimeTypeFilters contains filters that are added or edited
  const [changedMimeTypeFilters, setChangedMimeTypeFilters] = useState<
    MimeTypeFilter[]
  >([]);

  // deletedMimeTypeFilters contains filters to be deleted from the Server
  const [deletedMimeTypeFilters, setDeletedMimeTypeFilters] = useState<
    MimeTypeFilter[]
  >([]);

  // selectedMimeTypeFilter refers to the filter to be edited
  const [selectedMimeTypeFilter, setSelectedMimeTypeFilter] = useState<
    MimeTypeFilter | undefined
  >();

  const [showSnackBar, setShowSnackBar] = useState<boolean>(false);
  const [openMimeTypeFilterEditor, setOpenMimeTypeFilterEditor] = useState<
    boolean
  >(false);

  const [changesDetected, setChangesDetected] = useState<boolean>(false);

  useEffect(() => {
    updateTemplate(tp => ({
      ...templateDefaults(searchingStrings.searchfiltersettings.name)(tp),
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
  const getMimeTypeFilters = () => {
    getMimeTypeFiltersFromServer()
      .then((filters: MimeTypeFilter[]) => {
        setMimeTypeFilters(filters);
        setDeletedMimeTypeFilters([]);
        setChangedMimeTypeFilters([]);
      })
      .catch((error: AxiosError) => handleError(error));
  };

  // Visually add or update a filter, and put this filter into changedMimeTypeFilters
  const addOrUpdateMimeTypeFilter = (filter: MimeTypeFilter, add: boolean) => {
    if (add) {
      setMimeTypeFilters([...mimeTypeFilters, filter]);
    } else {
      const index = mimeTypeFilters.indexOf(selectedMimeTypeFilter!);
      const filters = [...mimeTypeFilters];
      filters[index] = filter;
      setMimeTypeFilters(filters);
    }
    setChangedMimeTypeFilters([...changedMimeTypeFilters, filter]);
    setOpenMimeTypeFilterEditor(false);
    setSelectedMimeTypeFilter(undefined);
  };

  // Visually delete a filter and put this filter in deletedMimeTypeFilters
  const deleteMimeTypeFilter = (filter: MimeTypeFilter) => {
    setMimeTypeFilters(
      mimeTypeFilters.filter(mimeTypeFilter => mimeTypeFilter !== filter)
    );
    // Only put filters that already have an id into this array
    if (filter.id) {
      setDeletedMimeTypeFilters([...deletedMimeTypeFilters, filter]);
    }
  };

  const openMimeTypeFilterDialog = (filter?: MimeTypeFilter) => {
    setOpenMimeTypeFilterEditor(true);
    setSelectedMimeTypeFilter(filter);
  };

  const closeMimeTypeFilterDialog = () => {
    setOpenMimeTypeFilterEditor(false);
    setSelectedMimeTypeFilter(undefined);
  };

  // Save all the changes to the Server
  const save = () => {
    if (mimeTypeFilterchanged || filterVisibilityChanged) {
      saveSearchSettingsToServer(searchSettings)
        .catch(error => handleError(error))
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
        .catch() // Errors have been handled and subsequent promises have skipped
        .finally(() => {
          getMimeTypeFilters();
          getSearchSettings();
          setChangesDetected(true);
        });
    } else {
      setChangesDetected(false);
    }

    setShowSnackBar(true);
  };

  const mimeTypeFilterchanged =
    deletedMimeTypeFilters.length || changedMimeTypeFilters.length;

  const filterVisibilityChanged =
    initialOwnerFilter !== searchSettings.searchingDisableOwnerFilter ||
    initialDateModifiedFilter !==
      searchSettings.searchingDisableDateModifiedFilter;

  const handleError = (error: AxiosError) => {
    updateTemplate(templateError(fromAxiosError(error)));
    throw error;
  };

  return (
    <>
      {/* Owner filter and Date modified filter*/}
      <Card className={classes.spacedCards}>
        <SettingsList subHeading={"Filter visibility"}>
          <SettingsListControl
            divider
            primaryText={"Disable Owner filter"}
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
            primaryText={"Disable Date modified filter"}
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
              {"Attachment MIME type filters"}
            </ListSubheader>
          }
        >
          {mimeTypeFilters.map(filter => {
            return (
              <ListItem
                alignItems={"flex-start"}
                divider={true}
                key={filter.name}
              >
                <ListItemText primary={filter.name} />
                <ListItemSecondaryAction>
                  <IconButton
                    onClick={() => {
                      openMimeTypeFilterDialog(filter);
                    }}
                    aria-label={`edit MIME type filter ${filter.name}`}
                    color="secondary"
                  >
                    <EditIcon />
                  </IconButton>
                  |
                  <IconButton
                    onClick={() => deleteMimeTypeFilter(filter)}
                    aria-label={`delete MIME type filter ${filter.name}`}
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
            aria-label={"add MIME type filter"}
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
        aria-label={"save Search filter configurations"}
      >
        <Save />
        {commonString.action.save}
      </Button>

      {/* Snackbar */}
      <MessageInfo
        title={
          changesDetected
            ? searchingStrings.searchPageSettings.success
            : "You have no changes to save."
        }
        open={showSnackBar}
        onClose={() => setShowSnackBar(false)}
        variant={changesDetected ? "success" : "info"}
      />

      {/* MIME type filter dialog */}
      <MimeTypeFilterEditor
        open={openMimeTypeFilterEditor}
        onClose={closeMimeTypeFilterDialog}
        onAddOrUpdate={addOrUpdateMimeTypeFilter}
        mimeTypeFilter={selectedMimeTypeFilter}
      />
    </>
  );
};

export default SearchFilterPage;
