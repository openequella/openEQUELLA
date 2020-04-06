import * as React from "react";
import {
  Button,
  CardContent,
  CardActions,
  Card,
  Divider,
  FormControl,
  Typography,
  WithStyles,
  withStyles,
  createStyles,
  Grid,
  Snackbar,
  IconButton
} from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import ColorPickerComponent from "./ColorPickerComponent";
import axios, { AxiosError } from "axios";
import { Config } from "../config";
import { languageStrings } from "../util/langstrings";
import { commonString } from "../util/commonstrings";
import {
  generateFromError,
  generateNewErrorID,
  ErrorResponse
} from "../api/errors";
import {
  templateDefaults,
  TemplateUpdate,
  templateError
} from "../mainui/Template";
import { IThemeSettings } from ".";

declare const themeSettings: IThemeSettings;
declare const logoURL: string;

/**
 * @author Samantha Fisher
 */
export const strings = languageStrings.newuisettings;

const styles = createStyles({
  card: {
    marginTop: "16px",
    marginBottom: "16px",
    overflow: "visible"
  },
  fileName: {
    marginTop: "16px",
    marginBottom: "16px",
    marginLeft: "4px"
  },
  labels: {
    marginBottom: "4px"
  },
  input: {
    display: "none"
  },
  cardContent: {
    marginBottom: "4px"
  },
  cardBottom: {
    marginBottom: "16px"
  },
  button: {
    marginTop: "8px",
    marginBottom: "8px"
  }
});

interface ThemePageProps {
  updateTemplate: (update: TemplateUpdate) => void;
}

class ThemePage extends React.Component<
  ThemePageProps & WithStyles<typeof styles>
> {
  state = {
    primary: "",
    secondary: "",
    background: "",
    menu: "",
    menuText: "",
    menuIcon: "",
    primaryText: "",
    secondaryText: "",
    logoToUpload: "",
    fileName: "",
    noFileNotification: false,
    logoURL: logoURL
  };

  componentDidMount = () => {
    this.setColorPickerDefaults();
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
        secondaryText: "#444444"
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
      secondaryText: themeSettings.menuTextColor
    });
  };

  reload = () => {
    window.location.reload();
  };
  handlePrimaryChange = (color: string) => {
    this.setState({ primary: color });
  };

  handleSecondaryChange = (color: string) => {
    this.setState({ secondary: color });
  };

  handleBackgroundChange = (color: string) => {
    this.setState({ background: color });
  };

  handleMenuChange = (color: string) => {
    this.setState({ menu: color });
  };

  handleMenuIconChange = (color: string) => {
    this.setState({ menuIcon: color });
  };

  handleMenuTextChange = (color: string) => {
    this.setState({ menuText: color });
  };

  handlePrimaryTextChange = (color: string) => {
    this.setState({ primaryText: color });
  };

  handleSecondaryTextChange = (color: string) => {
    this.setState({ secondaryText: color });
  };

  handleImageChange = (e: HTMLInputElement) => {
    const reader = new FileReader();
    if (e.files != null) {
      const file = e.files[0];
      reader.readAsDataURL(file);
      reader.onloadend = () => {
        this.setState({
          logoToUpload: file,
          fileName: file.name
        });
      };
    }
  };

  submitTheme = () => {
    axios
      .put(`${Config.baseUrl}api/theme/settings/`, {
        primaryColor: this.state.primary,
        secondaryColor: this.state.secondary,
        backgroundColor: this.state.background,
        menuItemColor: this.state.menu,
        menuItemIconColor: this.state.menuIcon,
        menuItemTextColor: this.state.menuText,
        primaryTextColor: this.state.primaryText,
        menuTextColor: this.state.secondaryText,
        fontSize: 14
      })
      .then(() => {
        this.reload();
      })
      .catch(error => {
        this.handleError(error);
      });
  };

  resetLogo = () => {
    axios
      .delete(`${Config.baseUrl}api/theme/logo/`)
      .then(() => {
        this.reload();
      })
      .catch(error => {
        this.handleError(error);
      });
  };

  submitLogo = () => {
    if (this.state.logoToUpload != "") {
      axios
        .put(`${Config.baseUrl}api/theme/logo/`, this.state.logoToUpload)
        .then(() => {
          this.reload();
        })
        .catch(error => {
          this.handleError(error);
        });
    } else {
      this.setState({ noFileNotification: true });
    }
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

  handleNoFileNotificationClose = () => {
    this.setState({ noFileNotification: false });
  };

  colorPicker = (
    label: string,
    changeColor: (color: string) => void,
    color: string
  ) => {
    const { classes } = this.props;
    return (
      <div>
        <Typography className={classes.labels} color={"textSecondary"}>
          {label}
        </Typography>
        <ColorPickerComponent changeColor={changeColor} color={color} />
      </div>
    );
  };

  ColorSchemeSettings = () => {
    const { classes } = this.props;
    return (
      <div>
        <CardContent className={classes.cardContent}>
          <FormControl>
            <Typography variant={"h4"}>
              {strings.colourschemesettings.title}
            </Typography>
            <Grid container spacing={8}>
              <Grid item>
                {this.colorPicker(
                  strings.colourschemesettings.primarycolour,
                  this.handlePrimaryChange,
                  this.state.primary
                )}
                {this.colorPicker(
                  strings.colourschemesettings.primarytextcolour,
                  this.handlePrimaryTextChange,
                  this.state.primaryText
                )}
                {this.colorPicker(
                  strings.colourschemesettings.menubackgroundcolour,
                  this.handleMenuChange,
                  this.state.menu
                )}
              </Grid>

              <Grid item>
                {this.colorPicker(
                  strings.colourschemesettings.secondarycolour,
                  this.handleSecondaryChange,
                  this.state.secondary
                )}
                {this.colorPicker(
                  strings.colourschemesettings.secondarytextcolour,
                  this.handleSecondaryTextChange,
                  this.state.secondaryText
                )}
                {this.colorPicker(
                  strings.colourschemesettings.backgroundcolour,
                  this.handleBackgroundChange,
                  this.state.background
                )}
              </Grid>

              <Grid item>
                {this.colorPicker(
                  strings.colourschemesettings.sidebariconcolour,
                  this.handleMenuIconChange,
                  this.state.menuIcon
                )}
                {this.colorPicker(
                  strings.colourschemesettings.sidebartextcolour,
                  this.handleMenuTextChange,
                  this.state.menuText
                )}
              </Grid>
            </Grid>
          </FormControl>
        </CardContent>

        <CardActions>
          <Button variant="outlined" onClick={this.handleDefaultButton}>
            {commonString.action.resettodefault}
          </Button>
          <Button variant="outlined" onClick={this.setColorPickerDefaults}>
            {commonString.action.undo}
          </Button>
          <Button
            className={classes.button}
            variant="contained"
            type={"submit"}
            onClick={this.submitTheme}
          >
            {commonString.action.apply}
          </Button>
        </CardActions>
      </div>
    );
  };

  LogoSettings = () => {
    const { classes } = this.props;
    return (
      <div>
        <CardContent className={classes.cardContent}>
          <Typography variant={"h4"}>{strings.logosettings.title}</Typography>
          <Typography className={classes.labels} color={"textSecondary"}>
            {strings.logosettings.imagespeclabel}
          </Typography>

          <label>
            <input
              accept="image/*"
              className={classes.input}
              color={"textSecondary"}
              id="contained-button-file"
              onChange={e => this.handleImageChange(e.target)}
              type="file"
            />
            <label htmlFor="contained-button-file">
              <Grid container spacing={8} direction={"row"}>
                <Grid item>
                  <Button
                    className={classes.button}
                    variant="outlined"
                    component="span"
                  >
                    {commonString.action.browse}
                  </Button>
                </Grid>
                <Grid item>
                  <Typography
                    className={classes.fileName}
                    color={"textSecondary"}
                  >
                    {this.state.fileName
                      ? this.state.fileName
                      : strings.logosettings.nofileselected}
                  </Typography>
                </Grid>
              </Grid>
            </label>
          </label>

          <Grid container spacing={8} direction={"row"}>
            <Grid item>
              <Typography className={classes.button} color={"textSecondary"}>
                {strings.logosettings.current}
              </Typography>
            </Grid>
            <Grid item>
              <img src={this.state.logoURL} alt={strings.logosettings.alt} />
            </Grid>
          </Grid>
        </CardContent>

        <CardActions className={classes.cardBottom}>
          <Button variant="outlined" onClick={this.resetLogo}>
            {commonString.action.resettodefault}
          </Button>
          <Button
            className={classes.button}
            variant="contained"
            type={"submit"}
            onClick={this.submitLogo}
          >
            {commonString.action.apply}
          </Button>
        </CardActions>
      </div>
    );
  };

  Notifications = () => {
    return (
      <div>
        <Snackbar
          anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
          autoHideDuration={5000}
          message={
            <span id="message-id">{strings.errors.nofiledescription}</span>
          }
          open={this.state.noFileNotification}
          onClose={this.handleNoFileNotificationClose}
          action={[
            <IconButton
              key="close"
              color="inherit"
              onClick={this.handleNoFileNotificationClose}
            >
              <CloseIcon />
            </IconButton>
          ]}
        />
      </div>
    );
  };

  render() {
    const { classes } = this.props;
    return (
      <React.Fragment>
        <Card raised className={classes.card}>
          <this.ColorSchemeSettings />
          <Divider light={true} />
          <this.LogoSettings />
        </Card>
        <this.Notifications />
      </React.Fragment>
    );
  }
}

export default withStyles(styles)(ThemePage);
