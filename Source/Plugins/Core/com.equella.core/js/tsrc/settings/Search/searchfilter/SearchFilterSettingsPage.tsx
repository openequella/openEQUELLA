import * as React from "react";
import {
  templateDefaults,
  templateError,
  TemplateUpdate,
  TemplateUpdateProps
} from "../../../mainui/Template";
import { routes } from "../../../mainui/routes";
import {
  Button,
  Card,
  ListItem,
  ListItemSecondaryAction,
  ListItemText,
  ListSubheader
} from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import { languageStrings } from "../../../util/langstrings";
import SettingsList from "../../../components/SettingsList";
import SettingsListControl from "../../../components/SettingsListControl";
import { useEffect, useState } from "react";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import SettingsToggleSwitch from "../../../components/SettingsToggleSwitch";
import List from "@material-ui/core/List";
import IconButton from "@material-ui/core/IconButton";
import EditIcon from "@material-ui/icons/Edit";
import DeleteIcon from "@material-ui/icons/Delete";
import {
  defaultSearchSettings,
  getSearchSettingsFromServer,
  saveSearchSettingsToServer,
  SearchSettings
} from "../SearchSettingsModule";
import { Save } from "@material-ui/icons";
import MessageInfo from "../../../components/MessageInfo";
import {
  batchUpdateOrAdd,
  getMimeTypeFiltersFromServer,
  MimeTypeFilter
} from "./SearchFilterSettingsModule";
import MimeTypeFilterEditor from "./MimeTypeFilterEditor";
import { commonString } from "../../../util/commonstrings";
import CardActions from "@material-ui/core/CardActions";
import { fromAxiosError } from "../../../api/errors";

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
  const [mimeTypeFilters, setMimeTypeFilters] = useState<MimeTypeFilter[]>([]);
  const [showSuccess, setShowSuccess] = useState<boolean>(false);

  const [openMimeTypeFilterEditor, setOpenMimeTypeFilterEditor] = useState<
    boolean
  >(false);
  const [selectedMimeTypeFilter, setSelectedMimeTypeFilter] = useState<
    MimeTypeFilter | undefined
  >();

  useEffect(() => {
    updateTemplate(tp => ({
      ...templateDefaults(searchingStrings.searchfiltersettings.name)(tp),
      backRoute: routes.Settings.to
    }));
  }, []);

  useEffect(() => {
    getSearchSettingsFromServer()
      .then(settings => setSearchSettings(settings))
      .catch(error => console.log(error));
  }, []);

  useEffect(() => {
    getMimeTypeFiltersFromServer()
      .then((filters: MimeTypeFilter[]) => setMimeTypeFilters(filters))
      .catch(error => console.log(error));
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

  const deleteMimeTypeFilter = (filter: MimeTypeFilter) => {
    setMimeTypeFilters(
      mimeTypeFilters.filter(mimeTypeFilter => mimeTypeFilter !== filter)
    );
  };

  const addOrUpdateMimeTypeFilter = (filter: MimeTypeFilter, add: boolean) => {
    if (add) {
      setMimeTypeFilters(mimeTypeFilters.concat(filter));
    } else {
      const index = mimeTypeFilters.indexOf(selectedMimeTypeFilter!);
      const filters = [...mimeTypeFilters];
      filters[index] = filter;
      setMimeTypeFilters(filters);
    }
    setOpenMimeTypeFilterEditor(false);
    setSelectedMimeTypeFilter(undefined);
  };

  const openMimeTypeFilterDialog = (filter?: MimeTypeFilter) => {
    setOpenMimeTypeFilterEditor(true);
    setSelectedMimeTypeFilter(filter);
  };

  const closeMimeTypeFilterDialog = () => {
    setOpenMimeTypeFilterEditor(false);
    setSelectedMimeTypeFilter(undefined);
  };

  const save = () => {
    saveSearchSettingsToServer(searchSettings)
      .then(() => batchUpdateOrAdd(mimeTypeFilters))
      .then(data => {
        setShowSuccess(true);
      })
      .catch(error => {
        handleError(templateError(fromAxiosError(error)));
      });
  };

  const handleError = (error: TemplateUpdate) => {
    updateTemplate(error);
  };

  return (
    <>
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

      <MessageInfo
        title={searchingStrings.searchPageSettings.success}
        open={showSuccess}
        onClose={() => setShowSuccess(false)}
        variant={"success"}
      />

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
