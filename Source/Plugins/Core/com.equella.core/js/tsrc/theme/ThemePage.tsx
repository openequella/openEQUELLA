import * as React from "react";
import {Bridge} from "../api/bridge";
import {
  Button, CardContent, CardActions,
  Card, Divider, FormControl, Typography,
  WithStyles, withStyles, createStyles,
  Grid, Snackbar, IconButton
} from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import ColorPickerComponent from "./ColorPickerComponent";
import axios, {AxiosError} from "axios";
import {Config} from "../config";
import {prepLangStrings} from "../util/langstrings";
import {commonString} from '../util/commonstrings';
import {generateFromAxiosError, generateNewErrorID} from "../api/errors";

interface IThemeSettings {
  primaryColor: string,
  secondaryColor: string,
  backgroundColor: string,
  menuItemColor: string,
  menuItemTextColor: string,
  menuItemIconColor: string,
  menuTextColor: string,
  fontSize: number
}

declare const themeSettings: IThemeSettings;
declare const logoURL: string;

/**
 * @author Samantha Fisher
 */
export const strings = prepLangStrings("newuisettings",
  {
    title: "Theme Settings",
    colourschemesettings: {
      title: "Colour Scheme",
      primarycolour: "Primary Colour",
      menubackgroundcolour: "Menu Background Colour",
      backgroundcolour: "Background Colour",
      secondarycolour: "Secondary Colour",
      sidebartextcolour: "Sidebar Text Colour",
      textcolour: "Text Colour",
      sidebariconcolour: "Sidebar Icon Colour"
    },
    logosettings: {
      title: "Logo Settings",
      imagespeclabel: "Use a PNG file of 230x36 pixels for best results.",
      current: "Current Logo: ",
      nofileselected: "No file selected."
    },
    errors: {
      invalidimagetitle: "Image Processing Error",
      invalidimagedescription: "Invalid image file. Please check the integrity of your file and try again.",
      nofiledescription: "Please select an image file to upload.",
      permissiontitle: "Permission Error",
      permissiondescription: "You do not have permission to edit the settings."
    }
  }
);

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
    marginBottom: "8px",
  }
});

interface ThemePageProps {
  bridge: Bridge;
}

class ThemePage extends React.Component<ThemePageProps & WithStyles<typeof styles>> {

  state = {
    primary: "",
    secondary: "",
    background: "",
    menu: "",
    menuText: "",
    menuIcon: "",
    text: "",
    logoToUpload: "",
    fileName: "",
    noFileNotification: false,
    error: null,
    logoURL: logoURL
  };

  componentDidMount = () => {
    this.setColorPickerDefaults();
  };

  handleDefaultButton = () => {
    this.setState({
      primary: "#2196f3",
      secondary: "#ff9800",
      background: "#fafafa",
      menu: "#ffffff",
      menuText: "#000000",
      menuIcon: "#000000",
      text: "#000000"
    },()=>this.submitTheme());
  };

  setColorPickerDefaults = () => {
    this.setState({
      primary: themeSettings.primaryColor,
      secondary: themeSettings.secondaryColor,
      background: themeSettings.backgroundColor,
      menu: themeSettings.menuItemColor,
      menuText: themeSettings.menuItemTextColor,
      menuIcon: themeSettings.menuItemIconColor,
      text: themeSettings.menuTextColor
    });
  };

  reload = () => {
    window.location.reload();
  };
  handlePrimaryChange = (color: string) => {
    this.setState({primary: color});
  };

  handleSecondaryChange = (color: string) => {
    this.setState({secondary: color});
  };

  handleBackgroundChange = (color: string) => {
    this.setState({background: color});
  };

  handleMenuChange = (color: string) => {
    this.setState({menu: color});
  };

  handleMenuTextChange = (color: string) => {
    this.setState({menuText: color});
  };

  handleMenuIconChange = (color: string) => {
    this.setState({menuIcon: color});
  };

  handleTextChange = (color: string) => {
    this.setState({text: color});
  };

  handleImageChange = (e: HTMLInputElement) => {
    let reader = new FileReader();
    if (e.files != null) {
      let file = e.files[0];
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
    axios.put(`${Config.baseUrl}api/theme/settings/`,
      {
        primaryColor: this.state.primary,
        secondaryColor: this.state.secondary,
        backgroundColor: this.state.background,
        menuItemColor: this.state.menu,
        menuItemTextColor: this.state.menuText,
        menuItemIconColor: this.state.menuIcon,
        menuTextColor: this.state.text,
        fontSize: 14
      })
      .then(() => {
          this.reload();
        }
      )
      .catch((error) => {
        this.handleError(error);
      });
  };

  resetLogo = () => {
    axios.delete(`${Config.baseUrl}api/theme/logo/`)
      .then(() => {
        this.reload();
      })
      .catch((error) => {
        this.handleError(error);
      });
  };

  submitLogo = () => {
    if (this.state.logoToUpload != "") {
      axios.put(`${Config.baseUrl}api/theme/logo/`, this.state.logoToUpload).then(() => {
        this.reload();
      }).catch((error) => {
        this.handleError(error);
      });
    } else {
      this.setState({noFileNotification: true});
    }
  };

  handleError = (error:AxiosError) => {
    if(error.response!=undefined){
      switch (error.response.status) {
        case 500:
          this.setState({error: generateNewErrorID(strings.errors.invalidimagetitle,error.response.status,strings.errors.invalidimagedescription)});
          break;
        case 403:
          this.setState({error: generateNewErrorID(strings.errors.permissiontitle,error.response.status,strings.errors.permissiondescription)});
          break;
        default:
          this.setState({error: generateFromAxiosError(error)});
          break;
      }
    }
  };

  handleNoFileNotificationClose = () => {
    this.setState({noFileNotification: false});
  };


  colorPicker = (label: string, changeColor: (color: string) => void, color: string) => {
    const {classes} = this.props;
    return (
      <div>
        <Typography className={classes.labels} color={"textSecondary"}>
          {label}
        </Typography>
        <ColorPickerComponent changeColor={changeColor} color={color}/>
      </div>
    )
  };

  ColorSchemeSettings = () => {
    const {classes} = this.props;
    return (
      <div>
        <CardContent className={classes.cardContent}>
          <FormControl>
            <Typography variant={"display1"}>
              {strings.colourschemesettings.title}
            </Typography>
            <Grid container spacing={16}>
              <Grid item>
                {this.colorPicker(strings.colourschemesettings.primarycolour, this.handlePrimaryChange, this.state.primary)}
                {this.colorPicker(strings.colourschemesettings.menubackgroundcolour, this.handleMenuChange, this.state.menu)}
                {this.colorPicker(strings.colourschemesettings.backgroundcolour, this.handleBackgroundChange, this.state.background)}
              </Grid>

              <Grid item>
                {this.colorPicker(strings.colourschemesettings.secondarycolour, this.handleSecondaryChange, this.state.secondary)}
                {this.colorPicker(strings.colourschemesettings.sidebartextcolour, this.handleMenuTextChange, this.state.menuText)}
              </Grid>

              <Grid item>
                {this.colorPicker(strings.colourschemesettings.textcolour, this.handleTextChange, this.state.text)}
                {this.colorPicker(strings.colourschemesettings.sidebariconcolour, this.handleMenuIconChange, this.state.menuIcon)}
              </Grid>
            </Grid>
          </FormControl>
        </CardContent>

        <CardActions>
          <Button variant="text" onClick={this.handleDefaultButton}>
            {commonString.action.resettodefault}
          </Button>
          <Button variant="outlined" onClick={this.setColorPickerDefaults}>
            {commonString.action.undo}
          </Button>
          <Button
            className={classes.button}
            variant="contained"
            type={"submit"}
            onClick={this.submitTheme}>
            {commonString.action.apply}
          </Button>
        </CardActions>
      </div>
    )
  };

  LogoSettings = () => {
    const {classes} = this.props;
    return (
      <div>
        <CardContent className={classes.cardContent}>
          <Typography variant={"display1"}>
            {strings.logosettings.title}
          </Typography>
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
                    component="span">
                    {commonString.action.browse}
                  </Button>
                </Grid>
                <Grid item>
                  <Typography className={classes.fileName} color={"textSecondary"}>
                    {this.state.fileName ? this.state.fileName : strings.logosettings.nofileselected}
                  </Typography>
                </Grid>
              </Grid>
            </label>
          </label>

          <Grid container spacing={8} direction={"row"}>
            <Grid item>
              <Typography className={classes.button}
                          color={"textSecondary"}>{strings.logosettings.current}</Typography>
            </Grid>
            <Grid item>
              <img
                src={this.state.logoURL}/>
            </Grid>
          </Grid>
        </CardContent>

        <CardActions className={classes.cardBottom}>
          <Button
            variant="text"
            onClick={this.resetLogo}>
            {commonString.action.resettodefault}
          </Button>
          <Button
            className={classes.button}
            variant="contained"
            type={"submit"}
            onClick={this.submitLogo}>
            {commonString.action.apply}
          </Button>
        </CardActions>
      </div>
    )
  };

  Notifications = () => {
    return (
      <div>
        <Snackbar anchorOrigin={{vertical: "bottom", horizontal: "right"}}
                  autoHideDuration={5000}
                  message={<span id="message-id">{strings.errors.nofiledescription}</span>}
                  open={this.state.noFileNotification}
                  onClose={this.handleNoFileNotificationClose}
                  action={[
                    <IconButton
                      key="close"
                      color="inherit"
                      onClick={this.handleNoFileNotificationClose}
                    >
                      <CloseIcon/>
                    </IconButton>
                  ]}
        />
      </div>);
  };

  render() {
    const {Template} = this.props.bridge;
    const {classes} = this.props;
    return (
      <Template title={strings.title} errorResponse={this.state.error||undefined}>

        <Card raised className={classes.card}>
          <this.ColorSchemeSettings/>
          <Divider light={true}/>
          <this.LogoSettings/>
        </Card>
        <this.Notifications/>

      </Template>
    );
  }
}

export default withStyles(styles)(ThemePage);
