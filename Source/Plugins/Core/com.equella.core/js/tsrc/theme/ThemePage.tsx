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
  Button,
  Card,
  CardActions,
  CardContent,
  createStyles,
  Grid,
  Typography,
  WithStyles,
  withStyles,
} from "@material-ui/core";
import ColorPickerComponent from "./ColorPickerComponent";
import axios, { AxiosError } from "axios";
import { API_BASE_URL } from "../config";
import { languageStrings } from "../util/langstrings";
import { commonString } from "../util/commonstrings";
import {
  ErrorResponse,
  generateFromError,
  generateNewErrorID,
} from "../api/errors";
import {
  templateDefaults,
  templateError,
  TemplateUpdate,
} from "../mainui/Template";
import { IThemeSettings } from ".";
import SettingPageTemplate from "../components/SettingPageTemplate";
import SettingsListControl from "../components/SettingsListControl";
import SettingsList from "../components/SettingsList";

declare const themeSettings: IThemeSettings;
declare const logoURL: string;

/**
 * @author Samantha Fisher
 */
export const strings = languageStrings.newuisettings;

const styles = createStyles({
  fileName: {
    marginTop: "8px",
  },
  labels: {
    marginBottom: "4px",
  },
  button: {
    marginTop: "8px",
    marginBottom: "8px",
  },
});

interface ThemeColors {
  primary: string;
  secondary: string;
  background: string;
  menu: string;
  menuText: string;
  menuIcon: string;
  primaryText: string;
  secondaryText: string;
}

interface ThemePageProps {
  updateTemplate: (update: TemplateUpdate) => void;
}

interface ThemePageState extends ThemeColors {
  logoToUpload: File | null;
  fileName: string;
  changesUnsaved: boolean;
  logoURL: string;
  showSuccess: boolean;
}

class ThemePage extends React.Component<
  ThemePageProps & WithStyles<typeof styles>,
  ThemePageState
> {
  state = {
    primary: themeSettings.primaryColor,
    secondary: themeSettings.secondaryColor,
    background: themeSettings.backgroundColor,
    menu: themeSettings.menuItemColor,
    menuText: themeSettings.menuItemTextColor,
    menuIcon: themeSettings.menuItemIconColor,
    primaryText: themeSettings.primaryTextColor,
    secondaryText: themeSettings.menuTextColor,
    logoToUpload: null,
    fileName: "",
    changesUnsaved: false,
    logoURL: logoURL,
    showSuccess: false,
  };

  componentDidMount = () => {
    this.props.updateTemplate(templateDefaults(strings.title));
  };

  handleDefaultButton = () => {
    this.setState(
      {
        primary: "#186caf",
        secondary: "#ff9800",
        background: "#fafafa",
        menuText: "#000000",
        menu: "#ffffff",
        menuIcon: "#000000",
        primaryText: "#000000",
        secondaryText: "#444444",
        changesUnsaved: true,
      },
      () => this.submitTheme()
    );
  };

  setColorPickerDefaults = () => {
    this.setState({
      primary: themeSettings.primaryColor,
      secondary: themeSettings.secondaryColor,
      background: themeSettings.backgroundColor,
      menu: themeSettings.menuItemColor,
      menuText: themeSettings.menuItemTextColor,
      menuIcon: themeSettings.menuItemIconColor,
      primaryText: themeSettings.primaryTextColor,
      secondaryText: themeSettings.menuTextColor,
    });
  };

  reload = () => {
    window.location.reload();
  };

  handleColorChange = (themeColor: keyof ThemeColors) => (newColor: string) => {
    const stateUpdates = { ...this.state, changesUnsaved: true };
    stateUpdates[themeColor] = newColor;
    this.setState(stateUpdates);
  };

  handleImageChange = (e: HTMLInputElement) => {
    const reader = new FileReader();
    if (e.files != null) {
      const file = e.files[0];
      reader.readAsDataURL(file);
      reader.onloadend = () => {
        this.setState({
          logoToUpload: file,
          fileName: file.name,
          changesUnsaved: true,
        });
      };
    }
  };

  submitTheme = () =>
    axios.put(`${API_BASE_URL}/theme/settings/`, {
      primaryColor: this.state.primary,
      secondaryColor: this.state.secondary,
      backgroundColor: this.state.background,
      menuItemColor: this.state.menu,
      menuItemIconColor: this.state.menuIcon,
      menuItemTextColor: this.state.menuText,
      primaryTextColor: this.state.primaryText,
      menuTextColor: this.state.secondaryText,
      fontSize: 14,
    });

  submitLogo = () =>
    this.state.logoToUpload &&
    axios.put(`${API_BASE_URL}/theme/logo/`, this.state.logoToUpload);

  async saveChanges() {
    try {
      await this.submitLogo();
      await this.submitTheme();
      this.setState({ changesUnsaved: false, showSuccess: true });
      this.reload();
    } catch (error) {
      this.handleError(error);
    }
  }

  resetLogo = () => {
    axios
      .delete(`${API_BASE_URL}/theme/logo/`)
      .then(() => {
        this.setState({ changesUnsaved: false, showSuccess: true });
        this.reload();
      })
      .catch((error) => {
        this.handleError(error);
      });
  };

  handleError = (error: AxiosError) => {
    let errResponse: ErrorResponse;
    if (error.response != undefined) {
      switch (error.response.status) {
        case 500:
          errResponse = generateNewErrorID(
            strings.errors.invalidimagetitle,
            error.response.status,
            strings.errors.invalidimagedescription
          );
          break;
        case 403:
          errResponse = generateNewErrorID(
            strings.errors.permissiontitle,
            error.response.status,
            strings.errors.permissiondescription
          );
          break;
        default:
          errResponse = generateFromError(error);
          break;
      }
      if (errResponse) {
        this.props.updateTemplate(templateError(errResponse));
      }
    }
  };

  colorPicker = (themeColor: keyof ThemeColors) => (
    <ColorPickerComponent
      onColorChange={this.handleColorChange(themeColor)}
      currentColor={this.state[themeColor]}
    />
  );

  LogoPicker = () => {
    const { classes } = this.props;

    return (
      <Grid container spacing={2} direction="row" justify="flex-end">
        <Grid item>
          <Typography className={classes.fileName} color="textSecondary">
            {this.state.fileName ?? ""}
          </Typography>
        </Grid>
        <Grid item>
          <input
            accept="image/*"
            color={"textSecondary"}
            id="contained-button-file"
            onChange={(e) => this.handleImageChange(e.target)}
            type="file"
            style={{ display: "none" }} // to hide the native file control and allow us to MUI it
          />
          <label htmlFor="contained-button-file">
            <Button variant="outlined" component="span">
              {commonString.action.browse}
            </Button>
          </label>
        </Grid>
        <Grid item>
          <Button variant="outlined" onClick={this.resetLogo}>
            {commonString.action.resettodefault}
          </Button>
        </Grid>
      </Grid>
    );
  };

  ColorSchemeSettings = () => {
    return (
      <Card>
        <CardContent>
          <SettingsList subHeading={strings.colourschemesettings.title}>
            <SettingsListControl
              primaryText={strings.colourschemesettings.primarycolour}
              control={this.colorPicker("primary")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.primarytextcolour}
              control={this.colorPicker("primaryText")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.menubackgroundcolour}
              control={this.colorPicker("menu")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.secondarycolour}
              control={this.colorPicker("secondary")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.secondarytextcolour}
              control={this.colorPicker("secondaryText")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.backgroundcolour}
              control={this.colorPicker("background")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.sidebariconcolour}
              control={this.colorPicker("menuIcon")}
              divider
            />
            <SettingsListControl
              primaryText={strings.colourschemesettings.sidebartextcolour}
              control={this.colorPicker("menuText")}
            />
          </SettingsList>
        </CardContent>

        <CardActions>
          <Button variant="outlined" onClick={this.handleDefaultButton}>
            {commonString.action.resettodefault}
          </Button>
          <Button variant="outlined" onClick={this.setColorPickerDefaults}>
            {commonString.action.undo}
          </Button>
        </CardActions>
      </Card>
    );
  };

  LogoSettings = () => {
    return (
      <Card>
        <CardContent>
          <SettingsList subHeading={strings.logoSettings.title}>
            <SettingsListControl
              primaryText={strings.logoSettings.siteLogo}
              secondaryText={strings.logoSettings.siteLogoDescription}
              control={<this.LogoPicker />}
            />
          </SettingsList>
        </CardContent>
      </Card>
    );
  };

  render() {
    return (
      <SettingPageTemplate
        onSave={() => this.saveChanges()}
        saveButtonDisabled={!this.state.changesUnsaved}
        snackbarOpen={this.state.showSuccess}
        snackBarOnClose={() => {
          this.setState({ showSuccess: false });
        }}
        preventNavigation={this.state.changesUnsaved}
      >
        <this.ColorSchemeSettings />
        <this.LogoSettings />
      </SettingPageTemplate>
    );
  }
}

export default withStyles(styles)(ThemePage);
