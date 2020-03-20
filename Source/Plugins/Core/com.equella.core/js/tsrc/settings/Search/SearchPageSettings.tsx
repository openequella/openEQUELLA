import * as React from "react";
import SettingsMenuContainer from "../../components/SettingsMenuContainer";
import {
  templateDefaults,
  TemplateUpdate,
  TemplateUpdateProps
} from "../../mainui/Template";
import { routes } from "../../mainui/routes";
import { Button, Grid } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import { languageStrings } from "../../util/langstrings";
import {
  CloudSettings,
  defaultSearchSettings,
  getCloudSettingsFromServer,
  getSearchSettingsFromServer,
  saveCloudSettingsToServer,
  saveSearchSettingsToServer,
  SearchSettings
} from "./SearchSettingsModule";
import MessageInfo from "../../components/MessageInfo";
import DefaultSortOrderSetting from "./components/DefaultSortOrderSetting";
import GalleryViewsSettings from "./components/GalleryViewsSettings";
import SettingsCheckbox from "../../components/SettingsCheckbox";

const useStyles = makeStyles({
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
  const [showError, setShowError] = React.useState<boolean>(false);
  const [showSuccess, setShowSuccess] = React.useState<boolean>(false);

  const searchPageSettingsStrings =
    languageStrings.settings.searching.searchPageSettings;
  const classes = useStyles();

  React.useEffect(() => {
    updateTemplate(tp => ({
      ...templateDefaults(searchPageSettingsStrings.name)(tp),
      backRoute: routes.Settings.to
    }));
    getSearchSettingsFromServer
      .then((settings: SearchSettings) => setSearchSettings(settings))
      .then(() =>
        getCloudSettingsFromServer.then((settings: CloudSettings) =>
          setCloudSettings(settings)
        )
      )
      .catch(error => handleError(error));
  }, []);

  function handleError(error: TemplateUpdate) {
    setShowError(true);
    updateTemplate(error);
  }

  function handleSubmitButton() {
    saveSearchSettingsToServer(searchSettings)
      .then(() => saveCloudSettingsToServer(cloudSettings))
      .then(() => setShowSuccess(true))
      .catch((error: TemplateUpdate) => handleError(error));
  }

  return (
    <SettingsMenuContainer>
      <Grid container direction={"column"} spacing={8}>
        {/*Default Sort Order*/}
        <Grid item>
          <DefaultSortOrderSetting
            disabled={showError}
            value={searchSettings.defaultSearchSort}
            setValue={order =>
              setSearchSettings({ ...searchSettings, defaultSearchSort: order })
            }
          />
        </Grid>

        {/*Include Non-Live checkbox*/}
        <Grid item>
          <SettingsCheckbox
            value={searchSettings.searchingShowNonLiveCheckbox}
            setValue={value =>
              setSearchSettings({
                ...searchSettings,
                searchingShowNonLiveCheckbox: value
              })
            }
            label={searchPageSettingsStrings.allowNonLiveLabel}
            disabled={showError}
            title={searchPageSettingsStrings.allowNonLive}
            id={"_showNonLiveCheckbox"}
          />
        </Grid>

        {/*Authenticate By Default*/}
        <Grid item>
          <SettingsCheckbox
            value={searchSettings.authenticateFeedsByDefault}
            setValue={value =>
              setSearchSettings({
                ...searchSettings,
                authenticateFeedsByDefault: value
              })
            }
            label={searchPageSettingsStrings.authFeedLabel}
            disabled={showError}
            title={searchPageSettingsStrings.authFeed}
            id={"_authenticateByDefault"}
          />
        </Grid>

        {/*Gallery views*/}
        <Grid item>
          <GalleryViewsSettings
            disabled={showError}
            setSearchSettings={setSearchSettings}
            searchSettings={searchSettings}
          />
        </Grid>

        {/*Cloud Settings*/}
        <Grid item>
          <SettingsCheckbox
            value={cloudSettings.disabled}
            setValue={value =>
              setCloudSettings({ ...cloudSettings, disabled: value })
            }
            label={searchPageSettingsStrings.cloudSearchingLabel}
            disabled={showError}
            title={searchPageSettingsStrings.cloudSearching}
            id={"cs_dc"}
          />
        </Grid>
        <Grid item>
          {/*Save Button*/}
          <Button
            id={"_saveButton"}
            disabled={showError}
            className={classes.floatingButton}
            variant="contained"
            onClick={handleSubmitButton}
            size="large"
          >
            {searchPageSettingsStrings.save}
          </Button>
        </Grid>
      </Grid>

      {/*Snackbar*/}
      <MessageInfo
        title={searchPageSettingsStrings.success}
        open={showSuccess}
        onClose={() => setShowSuccess(false)}
        variant={"success"}
      />
    </SettingsMenuContainer>
  );
}

export default SearchPageSettings;
