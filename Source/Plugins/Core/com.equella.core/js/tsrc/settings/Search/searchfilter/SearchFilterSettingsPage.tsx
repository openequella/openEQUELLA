import * as React from "react";
import {
  templateDefaults,
  TemplateUpdateProps
} from "../../../mainui/Template";
import { routes } from "../../../mainui/routes";
import {
  Button,
  Card,
  ListItem,
  ListItemSecondaryAction,
  ListItemText
} from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import { languageStrings } from "../../../util/langstrings";
import SettingsList from "../../../components/SettingsList";
import SettingsListControl from "../../../components/SettingsListControl";
import { useEffect, useState } from "react";
import ArrowUpwardIcon from "@material-ui/icons/ArrowUpward";
import ArrowDownwardIcon from "@material-ui/icons/ArrowDownward";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import SettingsToggleSwitch from "../../../components/SettingsToggleSwitch";
import Typography from "@material-ui/core/Typography";
import List from "@material-ui/core/List";
import IconButton from "@material-ui/core/IconButton";
import {
  defaultSearchSettings,
  getSearchSettingsFromServer,
  saveSearchSettingsToServer,
  SearchSettings
} from "../SearchSettingsModule";
import { Save } from "@material-ui/icons";
import CardActions from "@material-ui/core/CardActions";
import MessageInfo from "../../../components/MessageInfo";
import {
  deleteMIMETypeFilterFromServer,
  getMIMETypeFiltersFromServer,
  MIMETypeFilter
} from "./SearchFilterSettingsModule";

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
  }
});

const SearchFilterPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const classes = useStyles();
  const searchingStrings = languageStrings.settings.searching;

  const [searchSettings, setSearchSettings] = useState<SearchSettings>(
    defaultSearchSettings
  );
  const [mimeTypeFilters, setMimeTypeFilters] = useState<MIMETypeFilter[]>([]);
  const [showSuccess, setShowSuccess] = useState<boolean>(false);
  const [ascendingSorting, setAscendingSorting] = useState<boolean>(true);

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
    getMIMETypeFilters();
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

  const saveFilterVisibility = () => {
    saveSearchSettingsToServer(searchSettings).then(() => setShowSuccess(true));
  };

  const sortMIMETypeFilters = () => {
    mimeTypeFilters.sort((s1, s2) => {
      if (!ascendingSorting) {
        return s1.name > s2.name ? 1 : -1;
      }
      return s1.name < s2.name ? 1 : -1;
    });
    setAscendingSorting(!ascendingSorting);
  };

  const getMIMETypeFilters = () => {
    getMIMETypeFiltersFromServer()
      .then((filters: MIMETypeFilter[]) => setMimeTypeFilters(filters))
      .catch(error => console.log(error));
  };

  const deleteMIMETypeFilter = (uuid: string) => {
    deleteMIMETypeFilterFromServer(uuid)
      .then(() => {
        setShowSuccess(true);
        getMIMETypeFilters();
      })
      .catch(error => console.log(error));
  };

  return (
    <>
      <Card className={classes.spacedCards}>
        <Typography
          variant={"subheading"}
          color={"textSecondary"}
          paragraph={true}
        >
          Configure filter visibility
        </Typography>

        <SettingsList>
          <SettingsListControl
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

        <CardActions className={classes.cardAction}>
          <Button
            color={"primary"}
            variant="contained"
            size="large"
            onClick={saveFilterVisibility}
          >
            <Save />
            Save
          </Button>
        </CardActions>
      </Card>

      <Card className={classes.spacedCards}>
        <Typography variant={"subheading"} color={"textSecondary"}>
          Configure filters based on attachment file MIME types
          <IconButton onClick={sortMIMETypeFilters}>
            {ascendingSorting ? <ArrowUpwardIcon /> : <ArrowDownwardIcon />}
          </IconButton>
        </Typography>
        <List>
          {mimeTypeFilters.map(item => {
            return (
              <ListItem alignItems={"flex-start"} divider={true} key={item.id}>
                <ListItemText primary={item.name} />
                <ListItemSecondaryAction>
                  <Button>edit</Button>|
                  <Button
                    onClick={() => {
                      deleteMIMETypeFilter(item.id);
                    }}
                  >
                    delete
                  </Button>
                </ListItemSecondaryAction>
              </ListItem>
            );
          })}
        </List>
        <Button variant={"text"}>
          Add a new MIME type filter
          <IconButton onClick={() => alert(123)}>
            <AddCircleIcon />
          </IconButton>
        </Button>
      </Card>

      <MessageInfo
        title={searchingStrings.searchPageSettings.success}
        open={showSuccess}
        onClose={() => setShowSuccess(false)}
        variant={"success"}
      />
    </>
  );
};

export default SearchFilterPage;
