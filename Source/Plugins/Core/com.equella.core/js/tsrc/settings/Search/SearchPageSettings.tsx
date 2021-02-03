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
import { Card, CardContent } from "@material-ui/core";
import * as React from "react";
import { shallowEqual } from "shallow-equal-object";
import { generateFromError } from "../../api/errors";
import SettingPageTemplate from "../../components/SettingPageTemplate";
import SettingsList from "../../components/SettingsList";
import SettingsListControl from "../../components/SettingsListControl";
import SettingsToggleSwitch from "../../components/SettingsToggleSwitch";
import { routes } from "../../mainui/routes";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps,
} from "../../mainui/Template";
import {
  defaultSearchSettings,
  getCloudSettingsFromServer,
  getSearchSettingsFromServer,
  saveCloudSettingsToServer,
  saveSearchSettingsToServer,
} from "../../modules/SearchSettingsModule";
import { languageStrings } from "../../util/langstrings";
import useError from "../../util/useError";
import DefaultSortOrderSetting from "./components/DefaultSortOrderSetting";
import * as OEQ from "@openequella/rest-api-client";

const searchPageSettingsStrings =
  languageStrings.settings.searching.searchPageSettings;

const SearchPageSettings = ({ updateTemplate }: TemplateUpdateProps) => {
  const [
    searchSettings,
    setSearchSettings,
  ] = React.useState<OEQ.SearchSettings.Settings>(defaultSearchSettings);
  const [
    cloudSettings,
    setCloudSettings,
  ] = React.useState<OEQ.SearchSettings.CloudSettings>({
    disabled: false,
  });

  const [
    initialSearchSettings,
    setInitialSearchSettings,
  ] = React.useState<OEQ.SearchSettings.Settings>(defaultSearchSettings);
  const [
    initialCloudSettings,
    setInitialCloudSettings,
  ] = React.useState<OEQ.SearchSettings.CloudSettings>({
    disabled: false,
  });

  const [loadSettings, setLoadSettings] = React.useState<boolean>(true);
  const [showSuccess, setShowSuccess] = React.useState<boolean>(false);
  const [disableSettings, setDisableSettings] = React.useState<boolean>(false);

  const setError = useError((error: Error) => {
    updateTemplate(templateError(generateFromError(error)));
    setDisableSettings(true);
  });

  const changesUnsaved =
    initialCloudSettings.disabled !== cloudSettings.disabled ||
    !shallowEqual(searchSettings, initialSearchSettings);

  React.useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchPageSettingsStrings.name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  React.useEffect(() => {
    getSearchSettingsFromServer()
      .then((settings: OEQ.SearchSettings.Settings) => {
        setSearchSettings(settings);
        setInitialSearchSettings(settings);
      })
      .then(() =>
        getCloudSettingsFromServer().then(
          (settings: OEQ.SearchSettings.CloudSettings) => {
            setCloudSettings(settings);
            setInitialCloudSettings(settings);
          }
        )
      )
      .catch(setError);
  }, [loadSettings, setError]);

  function handleSubmitButton() {
    saveSearchSettingsToServer(searchSettings)
      .then(() => saveCloudSettingsToServer(cloudSettings))
      .then(() => setShowSuccess(true))
      .catch(setError)
      .finally(() => setLoadSettings(!loadSettings));
  }

  return (
    <SettingPageTemplate
      onSave={handleSubmitButton}
      saveButtonDisabled={!changesUnsaved}
      snackbarOpen={showSuccess}
      snackBarOnClose={() => setShowSuccess(false)}
      preventNavigation={changesUnsaved}
    >
      <Card>
        <CardContent>
          <SettingsList subHeading={searchPageSettingsStrings.general}>
            {/*Default Sort Order*/}
            <SettingsListControl
              divider
              primaryText={searchPageSettingsStrings.defaultSortOrder}
              secondaryText={searchPageSettingsStrings.defaultSortOrderDesc}
              control={
                <DefaultSortOrderSetting
                  disabled={disableSettings}
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
              primaryText={searchPageSettingsStrings.allowStatusControl}
              secondaryText={searchPageSettingsStrings.allowStatusControlLabel}
              control={
                <SettingsToggleSwitch
                  value={searchSettings.searchingShowNonLiveCheckbox}
                  setValue={(value) =>
                    setSearchSettings({
                      ...searchSettings,
                      searchingShowNonLiveCheckbox: value,
                    })
                  }
                  disabled={disableSettings}
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
                  disabled={disableSettings}
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
                  disabled={disableSettings}
                  id="cs_dc"
                />
              }
            />
          </SettingsList>
        </CardContent>
      </Card>
      <Card>
        <CardContent>
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
                  disabled={disableSettings}
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
                  disabled={disableSettings}
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
                  disabled={disableSettings}
                  id="_disableFileCount"
                />
              }
            />
          </SettingsList>
        </CardContent>
      </Card>
    </SettingPageTemplate>
  );
};

export default SearchPageSettings;
