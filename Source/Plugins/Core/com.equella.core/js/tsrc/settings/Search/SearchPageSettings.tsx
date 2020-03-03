import * as React from "react";
import SettingsMenuContainer from "../../components/SettingsMenuContainer";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps
} from "../../mainui/Template";
import { routes } from "../../mainui/routes";
import { Button, Checkbox, Grid } from "@material-ui/core";
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
import SearchSettingFormControl from "../../components/SearchSettingFormControl";
import DefaultSortOrderSetting from "./components/DefaultSortOrderSetting";
import GalleryViewsSettings from "./components/GalleryViewsSettings";

let useStyles = makeStyles({
  floatingButton: {
    right: "16px",
    bottom: "16px",
    position: "fixed"
  }
});

function SearchPageSettings({ updateTemplate }: TemplateUpdateProps) {
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
    updateTemplate(tp => ({
      ...templateDefaults(searchPageSettingsStrings.name)(tp),
      backRoute: routes.Settings.to
    }));
    getSearchSettingsFromServer()
      .then((settings: AxiosResponse<SearchSettings>) =>
        setSearchSettings(settings.data)
      )
      .then(() => getCloudSettingsFromServer())
      .then((settings: AxiosResponse<CloudSettings>) =>
        setCloudSettings(settings.data)
      )
      .catch((error: AxiosError) => handleError(error));
  }, []);

  function handleError(error: AxiosError) {
    setErrorMessage(true);
    if (error.response) {
      //axios errors
      switch (error.response.status) {
        case 403:
          updateTemplate(
            templateError(
              generateNewErrorID(
                searchPageSettingsStrings.permissionsError,
                error.response.status,
                searchPageSettingsStrings.permissionsError
              )
            )
          );
          break;
        case 404:
          updateTemplate(
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
      //non axios or unhandled errors
      updateTemplate(templateError(generateFromError(error)));
    }
  }

  function handleSubmitButton() {
    saveSearchSettingsToServer(searchSettings)
      .then(() => saveCloudSettingsToServer(cloudSettings))
      .then(() => setSuccessMessage(true))
      .catch((error: AxiosError) => handleError(error));
  }

  function handleSearchSettingsChange(
    key: keyof SearchSettings,
    value: boolean | SortOrder
  ) {
    setSearchSettings({
      ...searchSettings,
      [key]: value
    });
  }

  function handleCloudSettingsChange(key: keyof CloudSettings, value: boolean) {
    setCloudSettings({
      ...cloudSettings,
      [key]: value
    });
  }

  return (
    <SettingsMenuContainer>
      <Grid container direction={"column"} spacing={8}>
        {/*Default Sort Order*/}
        <Grid item>
          <DefaultSortOrderSetting
            disabled={errorMessage}
            searchSettings={searchSettings}
            handleSearchSettingsChange={handleSearchSettingsChange}
          />
        </Grid>

        {/*Include Non-Live checkbox*/}
        <Grid item>
          <SearchSettingFormControl
            control={
              <Checkbox
                id={"_showNonLiveCheckbox"}
                checked={searchSettings.searchingShowNonLiveCheckbox}
              />
            }
            label={searchPageSettingsStrings.allowNonLiveLabel}
            disabled={errorMessage}
            onChange={(_, checked) =>
              handleSearchSettingsChange(
                "searchingShowNonLiveCheckbox",
                checked
              )
            }
            title={searchPageSettingsStrings.allowNonLive}
          />
        </Grid>

        {/*Authenticate By Default*/}
        <Grid item>
          <SearchSettingFormControl
            control={
              <Checkbox
                id={"_authenticateByDefault"}
                checked={searchSettings.authenticateFeedsByDefault}
              />
            }
            label={searchPageSettingsStrings.authFeedLabel}
            disabled={errorMessage}
            onChange={(_, checked) =>
              handleSearchSettingsChange("authenticateFeedsByDefault", checked)
            }
            title={searchPageSettingsStrings.authFeed}
          />
        </Grid>

        {/*Gallery views*/}
        <Grid item>
          <GalleryViewsSettings
            disabled={errorMessage}
            handleSearchSettingsChange={handleSearchSettingsChange}
            searchSettings={searchSettings}
          />
        </Grid>

        {/*Cloud Settings*/}
        <Grid item>
          <SearchSettingFormControl
            control={<Checkbox id={"cs_dc"} checked={cloudSettings.disabled} />}
            label={searchPageSettingsStrings.cloudSearchingLabel}
            disabled={errorMessage}
            onChange={(_, checked) =>
              handleCloudSettingsChange("disabled", checked)
            }
            helperText={searchPageSettingsStrings.cloudSearchingLabel}
            title={searchPageSettingsStrings.cloudSearching}
          />
        </Grid>
      </Grid>

      {/*Save Button*/}
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

      {/*Snackbar*/}
      <MessageInfo
        title={searchPageSettingsStrings.success}
        open={successMessage}
        onClose={() => setSuccessMessage(false)}
        variant={"success"}
      />
    </SettingsMenuContainer>
  );
}
export default SearchPageSettings;
