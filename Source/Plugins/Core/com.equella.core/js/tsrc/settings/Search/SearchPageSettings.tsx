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
import * as React from "react";
import {
  templateDefaults,
  TemplateUpdate,
  TemplateUpdateProps,
} from "../../mainui/Template";
import { routes } from "../../mainui/routes";
import { Card } from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
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
import DefaultSortOrderSetting from "./components/DefaultSortOrderSetting";
import SettingsToggleSwitch from "../../components/SettingsToggleSwitch";
import SettingsList from "../../components/SettingsList";
import SettingsListControl from "../../components/SettingsListControl";
import SettingPageTemplate from "../../components/SettingPageTemplate";
import { shallowEqual } from "shallow-equal-object";

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

  const [initialSearchSettings, setInitialSearchSettings] = React.useState<
    SearchSettings
  >(defaultSearchSettings);
  const [initialCloudSettings, setInitialCloudSettings] = React.useState<
    CloudSettings
  >({
    disabled: false,
  });

  const [showError, setShowError] = React.useState<boolean>(false);
  const [showSuccess, setShowSuccess] = React.useState<boolean>(false);

  const searchPageSettingsStrings =
    languageStrings.settings.searching.searchPageSettings;
  const classes = useStyles();

  const changesUnsaved =
    initialCloudSettings.disabled !== cloudSettings.disabled ||
    !shallowEqual(searchSettings, initialSearchSettings);

  React.useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchPageSettingsStrings.name)(tp),
      backRoute: routes.Settings.to,
    }));
    getSettings();
  }, []);

  function getSettings() {
    getSearchSettingsFromServer()
      .then((settings: SearchSettings) => {
        setSearchSettings(settings);
        setInitialSearchSettings(settings);
      })
      .then(() =>
        getCloudSettingsFromServer().then((settings: CloudSettings) => {
          setCloudSettings(settings);
          setInitialCloudSettings(settings);
        })
      )
      .catch((error) => handleError(error));
  }

  function handleError(error: TemplateUpdate) {
    setShowError(true);
    updateTemplate(error);
  }

  function handleSubmitButton() {
    saveSearchSettingsToServer(searchSettings)
      .then(() => saveCloudSettingsToServer(cloudSettings))
      .then(() => setShowSuccess(true))
      .catch((error: TemplateUpdate) => handleError(error))
      .finally(() => getSettings());
  }

  return (
    <SettingPageTemplate
      onSave={handleSubmitButton}
      saveButtonDisabled={!changesUnsaved}
      snackbarOpen={showSuccess}
      snackBarOnClose={() => setShowSuccess(false)}
      preventNavigation={changesUnsaved}
    >
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
                id="_showNonLiveCheckbox"
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
                id="_authenticateByDefault"
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
                id="cs_dc"
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
                id="_disableGallery"
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
                id="_disableVideos"
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
                id="_disableFileCount"
              />
            }
          />
        </SettingsList>
      </Card>
    </SettingPageTemplate>
  );
}

export default SearchPageSettings;
