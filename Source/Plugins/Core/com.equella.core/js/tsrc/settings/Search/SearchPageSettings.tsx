import * as React from "react";
import {
  templateDefaults,
  TemplateUpdate,
  TemplateUpdateProps,
} from "../../mainui/Template";
import { routes } from "../../mainui/routes";
import { Button, Card } from "@material-ui/core";
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
} from "./SearchSettingsModule";
import MessageInfo from "../../components/MessageInfo";
import DefaultSortOrderSetting from "./components/DefaultSortOrderSetting";
import SettingsToggleSwitch from "../../components/SettingsToggleSwitch";
import { Save } from "@material-ui/icons";
import SettingsList from "../../components/SettingsList";
import SettingsListControl from "../../components/SettingsListControl";

const useStyles = makeStyles({
  floatingButton: {
    position: "fixed",
    top: 0,
    right: 0,
    marginTop: "80px",
    marginRight: "16px",
    width: "calc(25% - 112px)",
  },
  spacedCards: {
    margin: "16px",
    width: "75%",
    padding: "16px",
    float: "left",
  },
});

function SearchPageSettings({ updateTemplate }: TemplateUpdateProps) {
  const [searchSettings, setSearchSettings] = React.useState<SearchSettings>(
    defaultSearchSettings
  );
  const [cloudSettings, setCloudSettings] = React.useState<CloudSettings>({
    disabled: false,
  });
  const [showError, setShowError] = React.useState<boolean>(false);
  const [showSuccess, setShowSuccess] = React.useState<boolean>(false);

  const searchPageSettingsStrings =
    languageStrings.settings.searching.searchPageSettings;
  const classes = useStyles();

  React.useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchPageSettingsStrings.name)(tp),
      backRoute: routes.Settings.to,
    }));
    getSearchSettingsFromServer()
      .then((settings: SearchSettings) => setSearchSettings(settings))
      .then(() =>
        getCloudSettingsFromServer().then((settings: CloudSettings) =>
          setCloudSettings(settings)
        )
      )
      .catch((error) => handleError(error));
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
    <>
      <Card className={classes.spacedCards}>
        <SettingsList subHeading={searchPageSettingsStrings.general}>
          {/*Default Sort Order*/}
          <SettingsListControl
            divider
            primaryText={searchPageSettingsStrings.defaultSortOrder}
            secondaryText={searchPageSettingsStrings.defaultSortOrderDesc}
            control={
              <DefaultSortOrderSetting
                disabled={showError}
                value={searchSettings.defaultSearchSort}
                setValue={(order) =>
                  setSearchSettings({
                    ...searchSettings,
                    defaultSearchSort: order,
                  })
                }
              />
            }
          />
          {/*Non-Live Results*/}
          <SettingsListControl
            divider
            primaryText={searchPageSettingsStrings.allowNonLive}
            secondaryText={searchPageSettingsStrings.allowNonLiveLabel}
            control={
              <SettingsToggleSwitch
                value={searchSettings.searchingShowNonLiveCheckbox}
                setValue={(value) =>
                  setSearchSettings({
                    ...searchSettings,
                    searchingShowNonLiveCheckbox: value,
                  })
                }
                disabled={showError}
                id={"_showNonLiveCheckbox"}
              />
            }
          />
          {/*Authenticate Feeds*/}
          <SettingsListControl
            divider
            primaryText={searchPageSettingsStrings.authFeed}
            secondaryText={searchPageSettingsStrings.authFeedLabel}
            control={
              <SettingsToggleSwitch
                value={searchSettings.authenticateFeedsByDefault}
                setValue={(value) =>
                  setSearchSettings({
                    ...searchSettings,
                    authenticateFeedsByDefault: value,
                  })
                }
                disabled={showError}
                id={"_authenticateByDefault"}
              />
            }
          />
          {/*Cloud Searching*/}
          <SettingsListControl
            primaryText={searchPageSettingsStrings.cloudSearching}
            secondaryText={searchPageSettingsStrings.cloudSearchingLabel}
            control={
              <SettingsToggleSwitch
                value={cloudSettings.disabled}
                setValue={(value) =>
                  setCloudSettings({ ...cloudSettings, disabled: value })
                }
                disabled={showError}
                id={"cs_dc"}
              />
            }
          />
        </SettingsList>
      </Card>
      <Card className={classes.spacedCards}>
        {/*Gallery view Settings*/}
        <SettingsList subHeading={searchPageSettingsStrings.gallery}>
          <SettingsListControl
            divider
            primaryText={searchPageSettingsStrings.disableImages}
            secondaryText={searchPageSettingsStrings.disableImagesDesc}
            control={
              <SettingsToggleSwitch
                value={searchSettings.searchingDisableGallery}
                setValue={(value) =>
                  setSearchSettings({
                    ...searchSettings,
                    searchingDisableGallery: value,
                  })
                }
                disabled={showError}
                id={"_disableGallery"}
              />
            }
          />
          <SettingsListControl
            divider
            primaryText={searchPageSettingsStrings.disableVideos}
            secondaryText={searchPageSettingsStrings.disableVideosDesc}
            control={
              <SettingsToggleSwitch
                value={searchSettings.searchingDisableVideos}
                setValue={(value) =>
                  setSearchSettings({
                    ...searchSettings,
                    searchingDisableVideos: value,
                  })
                }
                disabled={showError}
                id={"_disableVideos"}
              />
            }
          />
          <SettingsListControl
            primaryText={searchPageSettingsStrings.disableFileCount}
            secondaryText={searchPageSettingsStrings.disableFileCountDesc}
            control={
              <SettingsToggleSwitch
                value={searchSettings.fileCountDisabled}
                setValue={(value) =>
                  setSearchSettings({
                    ...searchSettings,
                    fileCountDisabled: value,
                  })
                }
                disabled={showError}
                id={"_disableFileCount"}
              />
            }
          />
        </SettingsList>
      </Card>
      {/*Save Button*/}
      <Button
        color={"primary"}
        id={"_saveButton"}
        disabled={showError}
        className={classes.floatingButton}
        variant="contained"
        onClick={handleSubmitButton}
        size="large"
      >
        <Save />
        {searchPageSettingsStrings.save}
      </Button>
      {/*Snackbar*/}
      <MessageInfo
        title={searchPageSettingsStrings.success}
        open={showSuccess}
        onClose={() => setShowSuccess(false)}
        variant={"success"}
      />
    </>
  );
}

export default SearchPageSettings;
