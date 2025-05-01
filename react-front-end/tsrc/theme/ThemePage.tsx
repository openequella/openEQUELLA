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
import {
  Button,
  Card,
  CardActions,
  CardContent,
  Grid,
  Typography,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { useContext, useEffect, useState } from "react";
import { API_BASE_URL } from "../AppConfig";
import SettingPageTemplate from "../components/SettingPageTemplate";
import SettingsList from "../components/SettingsList";
import SettingsListControl from "../components/SettingsListControl";
import { AppContext } from "../mainui/App";
import { routes } from "../mainui/routes";
import { templateDefaults, TemplateUpdateProps } from "../mainui/Template";
import { commonString } from "../util/commonstrings";
import { languageStrings } from "../util/langstrings";
import ColorPickerComponent from "./ColorPickerComponent";

const PREFIX = "ThemePage";

const classes = {
  fileName: `${PREFIX}-fileName`,
  labels: `${PREFIX}-labels`,
  button: `${PREFIX}-button`,
};

const StyledSettingPageTemplate = styled(SettingPageTemplate)({
  [`& .${classes.fileName}`]: {
    marginTop: "8px",
  },
  [`& .${classes.labels}`]: {
    marginBottom: "4px",
  },
  [`& .${classes.button}`]: {
    marginTop: "8px",
    marginBottom: "8px",
  },
});

declare const themeSettings: OEQ.Theme.ThemeSettings;
declare const logoURL: string;

/**
 * @author Samantha Fisher
 */
export const strings = languageStrings.newuisettings;

interface ThemeColors {
  primary: string;
  secondary: string;
  background: string;
  paper: string;
  menu: string;
  menuText: string;
  menuIcon: string;
  primaryText: string;
  secondaryText: string;
}

export interface ThemePageProps extends TemplateUpdateProps {}

interface LogoSettings {
  logoURL: string;
  logoToUpload: File | null;
  fileName: string;
}

export const ThemePage = ({ updateTemplate }: ThemePageProps) => {
  const { appErrorHandler } = useContext(AppContext);

  const mapSettingsToColors = (
    settings: OEQ.Theme.ThemeSettings,
  ): ThemeColors => ({
    primary: settings.primaryColor,
    secondary: settings.secondaryColor,
    background: settings.backgroundColor,
    paper: settings.paperColor,
    menu: settings.menuItemColor,
    menuText: settings.menuItemTextColor,
    menuIcon: settings.menuItemIconColor,
    primaryText: settings.primaryTextColor,
    secondaryText: settings.menuTextColor,
  });

  const mapColorsToSettings = (
    colors: ThemeColors,
    fontSize = 14,
  ): OEQ.Theme.ThemeSettings => ({
    primaryColor: colors.primary,
    secondaryColor: colors.secondary,
    backgroundColor: colors.background,
    paperColor: colors.paper,
    menuItemColor: colors.menu,
    menuItemIconColor: colors.menuIcon,
    menuItemTextColor: colors.menuText,
    primaryTextColor: colors.primaryText,
    menuTextColor: colors.secondaryText,
    fontSize: fontSize,
  });

  const [logoSettings, setLogoSettings] = useState<LogoSettings>({
    logoURL: logoURL,
    logoToUpload: null,
    fileName: "",
  });

  const [themeColors, setThemeColors] = useState<ThemeColors>(
    mapSettingsToColors(themeSettings),
  );
  const [isChangesUnsaved, setIsChangesUnsaved] = useState(false);
  const [isShowSuccess, setIsShowSuccess] = useState(false);

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(strings.title)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  const handleDefaultButton = () => {
    const defaultThemeColors: ThemeColors = {
      primary: "#186caf",
      secondary: "#ff9800",
      background: "#fafafa",
      paper: "#ffffff",
      menuText: "#000000",
      menu: "#ffffff",
      menuIcon: "#000000",
      primaryText: "#000000",
      secondaryText: "#444444",
    };
    setThemeColors(defaultThemeColors);
    setIsChangesUnsaved(true);
  };

  const setColorPickerDefaults = () => {
    pipe(mapSettingsToColors(themeSettings), setThemeColors);
  };

  const reload = () => window.location.reload();

  const handleColorChange =
    (themeColor: keyof ThemeColors) => (newColor: string) => {
      const themeColorsUpdates = { ...themeColors };
      themeColorsUpdates[themeColor] = newColor;
      setThemeColors(themeColorsUpdates);
      setIsChangesUnsaved(true);
    };

  const handleImageChange = (e: HTMLInputElement) => {
    const reader = new FileReader();
    if (e.files != null) {
      const file = e.files[0];
      reader.readAsDataURL(file);
      reader.onloadend = () => {
        setLogoSettings({
          logoToUpload: file,
          fileName: file.name,
          logoURL: logoURL,
        });
        setIsChangesUnsaved(true);
      };
    }
  };

  const submitTheme = async (themeSettings: OEQ.Theme.ThemeSettings) =>
    await OEQ.Theme.updateThemeSettings(API_BASE_URL, themeSettings);

  const submitLogo = async () =>
    logoSettings.logoToUpload &&
    (await OEQ.Theme.updateThemeLogo(API_BASE_URL, logoSettings.logoToUpload));

  const saveChanges = async () => {
    try {
      await submitLogo();
      await submitTheme(mapColorsToSettings(themeColors));
      setIsChangesUnsaved(false);
      setIsShowSuccess(true);
      reload();
    } catch (error) {
      if (error instanceof Error) {
        appErrorHandler(error);
      } else {
        console.error("Unexpected non-Error caught in saveChanges(): " + error);
      }
    }
  };

  const resetLogo = () => {
    OEQ.Theme.deleteLogo(API_BASE_URL)
      .then(() => {
        setIsChangesUnsaved(false);
        setIsShowSuccess(true);
        reload();
      })
      .catch(appErrorHandler);
  };

  const colorPicker = (themeColor: keyof ThemeColors) => (
    <ColorPickerComponent
      onColorChange={handleColorChange(themeColor)}
      currentColor={themeColors[themeColor]}
    />
  );

  const LogoPicker = () => {
    return (
      <Grid container spacing={2} direction="row" justifyContent="flex-end">
        <Grid>
          <Typography className={classes.fileName} color="textSecondary">
            {logoSettings.fileName ?? ""}
          </Typography>
        </Grid>
        <Grid>
          <input
            accept="image/*"
            color="textSecondary"
            id="contained-button-file"
            onChange={(e) => handleImageChange(e.target)}
            type="file"
            style={{ display: "none" }} // to hide the native file control and allow us to MUI it
          />
          <label htmlFor="contained-button-file">
            <Button variant="outlined" component="span">
              {commonString.action.browse}
            </Button>
          </label>
        </Grid>
        <Grid>
          <Button variant="outlined" onClick={resetLogo}>
            {commonString.action.resettodefault}
          </Button>
        </Grid>
      </Grid>
    );
  };

  const ColorSchemeSettings = () => {
    return (
      <Card>
        <CardContent>
          <SettingsList subHeading={strings.colourschemesettings.title}>
            <SettingsListControl
              primaryText={strings.colourschemesettings.primarycolour}
              control={colorPicker("primary")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.primarytextcolour}
              control={colorPicker("primaryText")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.menubackgroundcolour}
              control={colorPicker("menu")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.secondarycolour}
              control={colorPicker("secondary")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.secondarytextcolour}
              control={colorPicker("secondaryText")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.backgroundcolour}
              control={colorPicker("background")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.paperColor}
              control={colorPicker("paper")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.sidebariconcolour}
              control={colorPicker("menuIcon")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.sidebartextcolour}
              control={colorPicker("menuText")}
            />
          </SettingsList>
        </CardContent>

        <CardActions>
          <Button variant="outlined" onClick={handleDefaultButton}>
            {commonString.action.resettodefault}
          </Button>
          <Button variant="outlined" onClick={setColorPickerDefaults}>
            {commonString.action.undo}
          </Button>
        </CardActions>
      </Card>
    );
  };

  const LogoSettings = () => {
    return (
      <Card>
        <CardContent>
          <SettingsList subHeading={strings.logoSettings.title}>
            <SettingsListControl
              primaryText={strings.logoSettings.siteLogo}
              secondaryText={strings.logoSettings.siteLogoDescription}
              control={<LogoPicker />}
            />
          </SettingsList>
        </CardContent>
      </Card>
    );
  };

  return (
    <StyledSettingPageTemplate
      onSave={saveChanges}
      saveButtonDisabled={!isChangesUnsaved}
      snackbarOpen={isShowSuccess}
      snackBarOnClose={() => {
        setIsShowSuccess(false);
      }}
      preventNavigation={isChangesUnsaved}
    >
      <ColorSchemeSettings />
      <LogoSettings />
    </StyledSettingPageTemplate>
  );
};

export default ThemePage;
