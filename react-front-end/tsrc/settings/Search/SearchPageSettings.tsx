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
import { Card, CardContent } from "@mui/material";
import { useCallback, useContext } from "react";
import * as React from "react";
import { shallowEqual } from "shallow-equal-object";
import SettingPageTemplate from "../../components/SettingPageTemplate";
import SettingsList from "../../components/SettingsList";
import SettingsListControl from "../../components/SettingsListControl";
import SettingsToggleSwitch from "../../components/SettingsToggleSwitch";
import { AppContext } from "../../mainui/App";
import { routes } from "../../mainui/routes";
import { templateDefaults, TemplateUpdateProps } from "../../mainui/Template";
import {
  defaultSearchSettings,
  getSearchSettingsFromServer,
  saveSearchSettingsToServer,
} from "../../modules/SearchSettingsModule";
import { languageStrings } from "../../util/langstrings";
import DefaultSortOrderSetting from "./components/DefaultSortOrderSetting";
import * as OEQ from "@openequella/rest-api-client";

const searchPageSettingsStrings =
  languageStrings.settings.searching.searchPageSettings;

const SearchPageSettings = ({ updateTemplate }: TemplateUpdateProps) => {
  const [searchSettings, setSearchSettings] =
    React.useState<OEQ.SearchSettings.Settings>(defaultSearchSettings);

  const [initialSearchSettings, setInitialSearchSettings] =
    React.useState<OEQ.SearchSettings.Settings>(defaultSearchSettings);

  const [loadSettings, setLoadSettings] = React.useState<boolean>(true);
  const [showSuccess, setShowSuccess] = React.useState<boolean>(false);
  const [disableSettings, setDisableSettings] = React.useState<boolean>(false);
  const { appErrorHandler } = useContext(AppContext);

  const setError = useCallback(
    (error: string | Error) => {
      appErrorHandler(error);
      setDisableSettings(true);
    },
    [appErrorHandler]
  );

  const changesUnsaved = !shallowEqual(searchSettings, initialSearchSettings);

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
      .catch(setError);
  }, [loadSettings, setError]);

  function handleSubmitButton() {
    saveSearchSettingsToServer(searchSettings)
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
