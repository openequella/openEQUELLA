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
import { Card, CardContent, Slider } from "@mui/material";
import { styled } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useCallback, useContext } from "react";
import { shallowEqual } from "shallow-equal-object";
import SettingPageTemplate from "../../components/SettingPageTemplate";
import SettingsList from "../../components/SettingsList";
import SettingsListControl from "../../components/SettingsListControl";
import { AppContext } from "../../mainui/App";
import { routes } from "../../mainui/routes";
import { templateDefaults, TemplateUpdateProps } from "../../mainui/Template";
import {
  defaultSearchSettings,
  getSearchSettingsFromServer,
  saveSearchSettingsToServer,
} from "../../modules/SearchSettingsModule";
import { languageStrings } from "../../util/langstrings";
import WebPageIndexSetting from "./components/WebPageIndexSetting";

const contentIndexSettingsStrings =
  languageStrings.settings.searching.contentIndexSettings;

const markStrings = contentIndexSettingsStrings.sliderMarks;

const TermBoostingSlider = styled(Slider)(({ theme }) => ({
  color: theme.palette.primary.main,
  height: 2,
  padding: "15px 0",
  "& .MuiSlider-track": {
    border: "none",
  },
}));

const ContentIndexSettings = ({ updateTemplate }: TemplateUpdateProps) => {
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
    [appErrorHandler],
  );

  const boostVals: { label: string; value: number }[] = [
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

  const getAriaLabel = (value: number): string => {
    return boostVals[value].label;
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
                  disabled={disableSettings}
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
                <TermBoostingSlider
                  disabled={disableSettings}
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
                <TermBoostingSlider
                  disabled={disableSettings}
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
                <TermBoostingSlider
                  disabled={disableSettings}
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
};

export default ContentIndexSettings;
