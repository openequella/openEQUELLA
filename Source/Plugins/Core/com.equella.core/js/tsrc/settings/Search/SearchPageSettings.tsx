import * as React from "react";
import SettingsMenuContainer from "../../components/SettingsMenuContainer";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps
} from "../../mainui/Template";
import { routes } from "../../mainui/routes";
import {
  Button,
  Checkbox,
  FormControl,
  FormControlLabel,
  FormHelperText,
  FormLabel,
  MenuItem,
  Select
} from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import { languageStrings } from "../../util/langstrings";
import {
  CloudSettings,
  defaultSearchSettings,
  getCloudSettingsFromServer,
  getSearchSettingsFromServer,
  saveCloudSettingsToServer,
  saveSearchSettingsToServer,
  SearchSettings,
  SortOrder
} from "./SearchSettingsModule";
import { AxiosError, AxiosResponse } from "axios";
import { generateFromError, generateNewErrorID } from "../../api/errors";
import MessageInfo from "../../components/MessageInfo";

let useStyles = makeStyles({
  dropDown: {
    marginTop: "4px"
  },
  section: {
    marginLeft: "8px",
    marginTop: "4px",
    marginBottom: "24px"
  },
  floatingButton: {
    right: "16px",
    bottom: "16px",
    position: "fixed"
  },
  checkbox: {
    paddingTop: "4px",
    paddingBottom: "4px"
  }
});

export function SearchPageSettings(props: TemplateUpdateProps) {
  const [searchSettings, setSearchSettings] = React.useState<SearchSettings>(
    defaultSearchSettings
  );
  const [cloudSettings, setCloudSettings] = React.useState<CloudSettings>({
    disabled: false
  });
  const [errorMessage, setErrorMessage] = React.useState<boolean>(false);
  const [successMessage, setSuccessMessage] = React.useState<boolean>(false);
  const searchPageSettingsStrings =
    languageStrings.settings.searching.searchPageSettings;
  const classes = useStyles();

  React.useEffect(() => {
    props.updateTemplate(tp => ({
      ...templateDefaults(searchPageSettingsStrings.name)(tp),
      backRoute: routes.Settings.to
    }));
    getSearchSettingsFromServer()
      .then((settings: AxiosResponse<SearchSettings>) => {
        setSearchSettings(settings.data);
      })
      .then(() => {
        getCloudSettingsFromServer()
          .then((settings: AxiosResponse<CloudSettings>) => {
            setCloudSettings(settings.data);
          })
          .catch((error: AxiosError) => {
            handleError(error);
          });
      })
      .catch((error: AxiosError) => {
        handleError(error);
      });
  }, []);

  function DefaultSortOrderSetting() {
    return (
      <div className={classes.section}>
        <FormControl>
          <FormLabel>{searchPageSettingsStrings.defaultSortOrder}</FormLabel>
          <Select
            SelectDisplayProps={{ id: "_sortOrder" }}
            disabled={errorMessage}
            onChange={event =>
              setSearchSettings({
                ...searchSettings,
                defaultSearchSort: event.target.value as SortOrder
              })
            }
            className={classes.dropDown}
            variant={"standard"}
            value={searchSettings.defaultSearchSort}
          >
            <MenuItem value={SortOrder.RANK}>
              {searchPageSettingsStrings.relevance}
            </MenuItem>
            <MenuItem value={SortOrder.DATEMODIFIED}>
              {searchPageSettingsStrings.lastModified}
            </MenuItem>
            <MenuItem value={SortOrder.DATECREATED}>
              {searchPageSettingsStrings.dateCreated}
            </MenuItem>
            <MenuItem value={SortOrder.NAME}>
              {searchPageSettingsStrings.title}
            </MenuItem>
            <MenuItem value={SortOrder.RATING}>
              {searchPageSettingsStrings.userRating}
            </MenuItem>
          </Select>
        </FormControl>
      </div>
    );
  }

  function NonLiveItemsSetting() {
    return (
      <div className={classes.section}>
        <FormControl>
          <FormLabel>{searchPageSettingsStrings.allowNonLive}</FormLabel>
          <FormControlLabel
            disabled={errorMessage}
            onChange={(event, checked) => {
              setSearchSettings({
                ...searchSettings,
                searchingShowNonLiveCheckbox: checked
              });
            }}
            label={searchPageSettingsStrings.allowNonLiveLabel}
            control={
              <Checkbox
                id={"_showNonLiveCheckbox"}
                checked={searchSettings.searchingShowNonLiveCheckbox}
                className={classes.checkbox}
              />
            }
          />
        </FormControl>
      </div>
    );
  }

  function AuthenticatedFeedSetting() {
    return (
      <div className={classes.section}>
        <FormControl>
          <FormLabel>{searchPageSettingsStrings.authFeed}</FormLabel>
          <FormControlLabel
            disabled={errorMessage}
            onChange={(event, checked) => {
              setSearchSettings({
                ...searchSettings,
                authenticateFeedsByDefault: checked
              });
            }}
            label={searchPageSettingsStrings.authFeedLabel}
            control={
              <Checkbox
                id={"_authenticateByDefault"}
                checked={searchSettings.authenticateFeedsByDefault}
                className={classes.checkbox}
              />
            }
          />
        </FormControl>
      </div>
    );
  }

  function GalleryViewsSetting() {
    return (
      <div className={classes.section}>
        <FormControl>
          <FormLabel>{searchPageSettingsStrings.galleryViews}</FormLabel>
          <FormControlLabel
            disabled={errorMessage}
            onChange={(event, checked) => {
              setSearchSettings({
                ...searchSettings,
                searchingDisableGallery: checked
              });
            }}
            label={searchPageSettingsStrings.disableImages}
            control={
              <Checkbox
                id={"_disableGallery"}
                checked={searchSettings.searchingDisableGallery}
                className={classes.checkbox}
              />
            }
          />
          <FormControlLabel
            disabled={errorMessage}
            onChange={(event, checked) => {
              setSearchSettings({
                ...searchSettings,
                searchingDisableVideos: checked
              });
            }}
            label={searchPageSettingsStrings.disableVideos}
            control={
              <Checkbox
                checked={searchSettings.searchingDisableVideos}
                className={classes.checkbox}
              />
            }
          />
          <FormControlLabel
            disabled={errorMessage}
            onChange={(event, checked) => {
              setSearchSettings({
                ...searchSettings,
                fileCountDisabled: checked
              });
            }}
            label={searchPageSettingsStrings.disableFileCount}
            control={
              <Checkbox
                checked={searchSettings.fileCountDisabled}
                className={classes.checkbox}
              />
            }
          />
        </FormControl>
      </div>
    );
  }

  function CloudSearchSetting() {
    return (
      <div className={classes.section}>
        <FormControl>
          <FormLabel>{searchPageSettingsStrings.cloudSearching}</FormLabel>
          <FormHelperText>
            {searchPageSettingsStrings.cloudSearchingLabel}
          </FormHelperText>
          <FormControlLabel
            disabled={errorMessage}
            onChange={(event, checked) => {
              setCloudSettings({ ...cloudSettings, disabled: checked });
            }}
            label={searchPageSettingsStrings.disableCloud}
            control={
              <Checkbox
                id={"cs_dc"}
                checked={cloudSettings.disabled}
                className={classes.checkbox}
              />
            }
          />
        </FormControl>
      </div>
    );
  }

  function handleError(error: AxiosError) {
    setErrorMessage(true);
    if (error.response != undefined) {
      switch (error.response.status) {
        case 404:
          props.updateTemplate(
            templateError(
              generateNewErrorID(
                searchPageSettingsStrings.notFoundError,
                error.response.status,
                searchPageSettingsStrings.notFoundErrorDesc
              )
            )
          );
      }
    } else {
      props.updateTemplate(templateError(generateFromError(error)));
    }
  }

  function handleSubmitButton() {
    saveSearchSettingsToServer(searchSettings)
      .then(() => {
        saveCloudSettingsToServer(cloudSettings)
          .then(() => {
            setSuccessMessage(true);
          })
          .catch((error: AxiosError) => {
            handleError(error);
          });
      })
      .catch((error: AxiosError) => {
        handleError(error);
      });
  }

  return (
    <SettingsMenuContainer>
      <DefaultSortOrderSetting />
      <NonLiveItemsSetting />
      <AuthenticatedFeedSetting />
      <GalleryViewsSetting />
      <CloudSearchSetting />
      <Button
        id={"_saveButton"}
        disabled={errorMessage}
        className={classes.floatingButton}
        variant="contained"
        onClick={handleSubmitButton}
        size="large"
      >
        {searchPageSettingsStrings.save}
      </Button>
      <MessageInfo
        title={searchPageSettingsStrings.success}
        open={successMessage}
        onClose={() => setSuccessMessage(false)}
        variant={"success"}
      />
    </SettingsMenuContainer>
  );
}
