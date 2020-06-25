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
import { Card, CardContent, Mark, Slider } from "@material-ui/core";
import { languageStrings } from "../../util/langstrings";
import {
  defaultSearchSettings,
  getSearchSettingsFromServer,
  saveSearchSettingsToServer,
  SearchSettings,
} from "./SearchSettingsModule";
import SettingsList from "../../components/SettingsList";
import SettingsListControl from "../../components/SettingsListControl";
import WebPageIndexSetting from "./components/WebPageIndexSetting";
import SettingPageTemplate from "../../components/SettingPageTemplate";
import { shallowEqual } from "shallow-equal-object";

function ContentIndexSettings({ updateTemplate }: TemplateUpdateProps) {
  const [searchSettings, setSearchSettings] = React.useState<SearchSettings>(
    defaultSearchSettings
  );
  const [initialSearchSettings, setInitialSearchSettings] = React.useState<
    SearchSettings
  >(defaultSearchSettings);
  const [showError, setShowError] = React.useState<boolean>(false);
  const [showSuccess, setShowSuccess] = React.useState<boolean>(false);

  const contentIndexSettingsStrings =
    languageStrings.settings.searching.contentIndexSettings;

  const markStrings = contentIndexSettingsStrings.sliderMarks;

  const boostVals: Mark[] = [
    { label: markStrings.off, value: 0 },
    { label: "x0.25", value: 1 },
    { label: "x0.5", value: 2 },
    { label: markStrings.noBoost, value: 3 },
    { label: "x1.5", value: 4 },
    { label: "x2", value: 5 },
    { label: "x4", value: 6 },
    { label: "x8", value: 7 },
  ];

  const changesUnsaved = !shallowEqual(searchSettings, initialSearchSettings);

  React.useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(contentIndexSettingsStrings.name)(tp),
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
      .catch((error) => handleError(error));
  }

  function handleError(error: TemplateUpdate) {
    setShowError(true);
    updateTemplate(error);
  }

  function handleSubmitButton() {
    saveSearchSettingsToServer(searchSettings)
      .then(() => setShowSuccess(true))
      .catch((error: TemplateUpdate) => handleError(error))
      .finally(() => getSettings());
  }

  const handleSliderChange = (newValue: number | number[], prop: string) => {
    // Because our usage of Sliders do not produce multiple numbers, throw a
    // TypeError if newValue is not a number.
    if (typeof newValue !== "number") {
      throw new TypeError("slider value must be a number");
    }
    setSearchSettings({
      ...searchSettings,
      [prop]: newValue,
    });
  };

  const getAriaLabel = (value: number, index: number): string => {
    return boostVals[value].label as string;
  };

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
          <SettingsList subHeading={contentIndexSettingsStrings.general}>
            <SettingsListControl
              primaryText={contentIndexSettingsStrings.name}
              secondaryText={contentIndexSettingsStrings.description}
              control={
                <WebPageIndexSetting
                  disabled={showError}
                  value={searchSettings.urlLevel}
                  setValue={(level) =>
                    setSearchSettings({
                      ...searchSettings,
                      urlLevel: level,
                    })
                  }
                />
              }
            />
          </SettingsList>
        </CardContent>
      </Card>
      <Card>
        <CardContent>
          <SettingsList subHeading={contentIndexSettingsStrings.boosting}>
            <SettingsListControl
              primaryText={contentIndexSettingsStrings.titleBoostingTitle}
              divider
              control={
                <Slider
                  disabled={showError}
                  marks={boostVals}
                  min={0}
                  max={7}
                  getAriaValueText={getAriaLabel}
                  aria-label={contentIndexSettingsStrings.titleBoostingTitle}
                  value={searchSettings.titleBoost}
                  onChange={(event, value) =>
                    handleSliderChange(value, "titleBoost")
                  }
                />
              }
            />
            <SettingsListControl
              primaryText={contentIndexSettingsStrings.metaBoostingTitle}
              divider
              control={
                <Slider
                  disabled={showError}
                  marks={boostVals}
                  min={0}
                  max={7}
                  getAriaValueText={getAriaLabel}
                  aria-label={contentIndexSettingsStrings.metaBoostingTitle}
                  value={searchSettings.descriptionBoost}
                  onChange={(event, value) =>
                    handleSliderChange(value, "descriptionBoost")
                  }
                />
              }
            />
            <SettingsListControl
              primaryText={contentIndexSettingsStrings.attachmentBoostingTitle}
              control={
                <Slider
                  disabled={showError}
                  marks={boostVals}
                  min={0}
                  max={7}
                  getAriaValueText={getAriaLabel}
                  aria-label={
                    contentIndexSettingsStrings.attachmentBoostingTitle
                  }
                  value={searchSettings.attachmentBoost}
                  onChange={(event, value) =>
                    handleSliderChange(value, "attachmentBoost")
                  }
                />
              }
            />
          </SettingsList>
        </CardContent>
      </Card>
    </SettingPageTemplate>
  );
}

export default ContentIndexSettings;
