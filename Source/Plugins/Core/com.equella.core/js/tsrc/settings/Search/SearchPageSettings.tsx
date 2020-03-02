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
  Grid,
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
  floatingButton: {
    right: "16px",
    bottom: "16px",
    position: "fixed"
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
      <Grid item>
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
      </Grid>
    );
  }

  function NonLiveItemsSetting() {
    return (
      <Grid item>
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
              />
            }
          />
        </FormControl>
      </Grid>
    );
  }

  function AuthenticatedFeedSetting() {
    return (
      <Grid item>
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
              />
            }
          />
        </FormControl>
      </Grid>
    );
  }

  function GalleryViewsSetting() {
    return (
      <Grid item>
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
              <Checkbox checked={searchSettings.searchingDisableVideos} />
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
            control={<Checkbox checked={searchSettings.fileCountDisabled} />}
          />
        </FormControl>
      </Grid>
    );
  }

  function CloudSearchSetting() {
    return (
      <Grid item>
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
            control={<Checkbox id={"cs_dc"} checked={cloudSettings.disabled} />}
          />
        </FormControl>
      </Grid>
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
      <Grid container direction={"column"} spacing={8}>
        <DefaultSortOrderSetting />
        <NonLiveItemsSetting />
        <AuthenticatedFeedSetting />
        <GalleryViewsSetting />
        <CloudSearchSetting />
      </Grid>
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
