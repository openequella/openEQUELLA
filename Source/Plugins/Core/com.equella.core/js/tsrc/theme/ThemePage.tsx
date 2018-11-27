import * as React from "react";
import {Bridge} from "../api/bridge";
import FormControl from "@material-ui/core/FormControl/FormControl";
import Button from "@material-ui/core/Button/Button";
import ColorPickerComponent from "./ColorPickerComponent";
import Typography from "@material-ui/core/Typography/Typography";
import axios from "axios";
import {Config} from "../config";
import {WithStyles} from "@material-ui/core";
import createStyles from "@material-ui/core/styles/createStyles";
import withStyles from "@material-ui/core/styles/withStyles";
import Grid from "@material-ui/core/Grid/Grid";
import CardContent from "@material-ui/core/CardContent/CardContent";
import CardActions from "@material-ui/core/CardActions/CardActions";
import Card from "@material-ui/core/Card/Card";
import Divider from "@material-ui/core/Divider/Divider";
import SnackBar from "@material-ui/core/Snackbar";
import IconButton from "@material-ui/core/IconButton/IconButton";
import CloseIcon from "@material-ui/icons/Close";
import Dialog from "@material-ui/core/Dialog/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent/DialogContent";
import DialogContentText from "@material-ui/core/DialogContentText/DialogContentText";
import DialogActions from "@material-ui/core/DialogActions/DialogActions";
import Slide from "@material-ui/core/Slide/Slide";

declare var themeSettings: any;
declare var isCustomLogo: boolean;

/**
 * @author Samantha Fisher
 */

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

function transition(props: any) {
  return <Slide direction="up" {...props} />;
}

class ThemePage extends React.Component<ThemePageProps & WithStyles<typeof styles>> {

  state = {
    primary: themeSettings["primaryColor"],
    secondary: themeSettings["secondaryColor"],
    background: themeSettings["backgroundColor"],
    menu: themeSettings["menuItemColor"],
    menuText: themeSettings["menuItemTextColor"],
    menuIcon: themeSettings["menuItemIconColor"],
    text: themeSettings["menuTextColor"],
    logoToUpload: "",
    customLogo: isCustomLogo,
    fileName: "",
    noFileError: false,
    invalidFileError: false
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
    });
  };

  handleUndoButton = () => {
    this.setState({
      primary: themeSettings["primaryColor"],
      secondary: themeSettings["secondaryColor"],
      background: themeSettings["backgroundColor"],
      menu: themeSettings["menuItemColor"],
      menuText: themeSettings["menuItemTextColor"],
      menuIcon: themeSettings["menuItemIconColor"],
      text: themeSettings["menuTextColor"]
    });
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

  handleImageChange = (e: any) => {
    e.preventDefault();
    let reader = new FileReader();

    let file = e.target.files[0];
    reader.readAsDataURL(file);
    reader.onloadend = () => {
      this.setState({
        logoToUpload: file,
        fileName: file.name
      });
    };
  };

  submitTheme = () => {
    //TODO: map this in a more elegant way
    axios.put(`${Config.baseUrl}api/themeresource/update/`,
      "{\"primaryColor\":\"" + this.state.primary + "\"," +
      "\"secondaryColor\":\"" + this.state.secondary + "\", " +
      "\"backgroundColor\":\"" + this.state.background + "\", " +
      "\"menuItemColor\":\"" + this.state.menu + "\", " +
      "\"menuItemTextColor\": \"" + this.state.menuText + "\", " +
      "\"menuItemIconColor\": \"" + this.state.menuIcon + "\", " +
      "\"menuTextColor\": \"" + this.state.text + "\", " +
      "\"fontSize\": 14}").then(() => {
        window.location.reload();
      }
    );
  };

  resetLogo = () => {
    axios.delete(`${Config.baseUrl}api/themeresource/resetlogo/`).then( () => {
      window.location.reload();
    });
  };

  submitLogo = () => {
    if (this.state.logoToUpload != "") {
      axios.put(`${Config.baseUrl}api/themeresource/updatelogo/`, this.state.logoToUpload).then(() => {
        window.location.reload();
      }).catch(() => {
        this.setState({invalidFileError: true})
      });
    } else {
      this.setState({noFileError: true});
    }
  };

  handleInvalidFileErrorClose = () => {
    this.setState({invalidFileError: false});
  };

  handleNoFileErrorClose = () => {
    this.setState({noFileError: false});
  };

  render() {
    const {Template} = this.props.bridge;

    return (
      <Template title={"New UI Settings"}>
        <Card raised className={this.props.classes.card}>
          <CardContent className={this.props.classes.cardContent}>

            {/*COLOUR SCHEME SETTINGS*/}

            <Typography variant={"display1"}>
              Colour Scheme
            </Typography>
            <FormControl>
              <Grid container spacing={16}>
                <Grid item>
                  <Typography className={this.props.classes.labels} color={"textSecondary"}>
                    Primary Colour
                  </Typography>
                  <ColorPickerComponent changeColor={this.handlePrimaryChange} color={this.state.primary}/>

                  <Typography className={this.props.classes.labels} color={"textSecondary"}>
                    Menu Background Colour
                  </Typography>
                  <ColorPickerComponent changeColor={this.handleMenuChange} color={this.state.menu}/>

                  <Typography className={this.props.classes.labels} color={"textSecondary"}>
                    Background Colour
                  </Typography>
                  <ColorPickerComponent changeColor={this.handleBackgroundChange} color={this.state.background}/>
                </Grid>

                <Grid item>
                  <Typography className={this.props.classes.labels} color={"textSecondary"}>
                    Secondary Colour
                  </Typography>
                  <ColorPickerComponent changeColor={this.handleSecondaryChange} color={this.state.secondary}/>

                  <Typography className={this.props.classes.labels} color={"textSecondary"}>
                    Sidebar Text Colour
                  </Typography>
                  <ColorPickerComponent changeColor={this.handleMenuTextChange} color={this.state.menuText}/>
                </Grid>

                <Grid item>
                  <Typography className={this.props.classes.labels} color={"textSecondary"}>
                    Text Colour
                  </Typography>
                  <ColorPickerComponent changeColor={this.handleTextChange} color={this.state.text}/>

                  <Typography className={this.props.classes.labels} color={"textSecondary"}>
                    Sidebar Icon Colour
                  </Typography>
                  <ColorPickerComponent changeColor={this.handleMenuIconChange} color={this.state.menuIcon}/>
                </Grid>
              </Grid>
            </FormControl>
          </CardContent>

          <CardActions>
            <Button variant="text" onClick={this.handleDefaultButton}>
              Reset to Default
            </Button>
            <Button variant="outlined" onClick={this.handleUndoButton}>
              Undo
            </Button>
            <Button
              className={this.props.classes.button}
              variant="contained"
              type={"submit"}
              onClick={this.submitTheme}>
              Apply
            </Button>
          </CardActions>

          <Divider light={true}/>

          {/*LOGO SETTINGS*/}

          <CardContent className={this.props.classes.cardContent}>
            <Typography variant={"display1"}>
              Logo Settings
            </Typography>
            <Typography className={this.props.classes.labels} color={"textSecondary"}>
              Use a PNG file of 230x36 pixels for best results.
            </Typography>

            <label>
              <input
                accept="image/*"
                className={this.props.classes.input}
                color={"textSecondary"}
                id="contained-button-file"
                onChange={(e) => this.handleImageChange(e)}
                type="file"
              />
              <label htmlFor="contained-button-file">
                <Grid container spacing={8} direction={"row"}>
                  <Grid item>
                    <Button
                      className={this.props.classes.button}
                      variant="outlined"
                      component="span">
                      Browse...
                    </Button>
                  </Grid>
                  <Grid item>
                    <Typography className={this.props.classes.fileName} color={"textSecondary"}>
                      {this.state.fileName ? this.state.fileName : "No file selected."}
                    </Typography>
                  </Grid>
                </Grid>
              </label>
            </label>

            <Grid container spacing={8} direction={"row"}>
              <Grid item>
                <Typography className={this.props.classes.button} color={"textSecondary"}>Current Logo: </Typography>
              </Grid>
              <Grid item>
                <img
                  src={this.state.customLogo ? `${Config.baseUrl}api/themeresource/newLogo.png` : `${Config.baseUrl}p/r/6.7.r88/com.equella.core/images/new-equella-logo.png`}/>
              </Grid>
            </Grid>
          </CardContent>

          <CardActions className={this.props.classes.cardBottom}>
            <Button
              variant="text"
              onClick={this.resetLogo}>
              Reset to Default
            </Button>
            <Button
              className={this.props.classes.button}
              variant="contained"
              type={"submit"}
              onClick={this.submitLogo}>
              Apply
            </Button>
          </CardActions>
        </Card>

        {/*ERROR MESSAGES*/}

        <Dialog
          open={this.state.invalidFileError}
          TransitionComponent={transition}
          keepMounted
          onClose={this.handleInvalidFileErrorClose}
        >
          <DialogTitle id="alert-dialog-slide-title" color="primary">
            <Typography variant={"display1"} color={"textSecondary"}>
              Image Processing Error
            </Typography>
          </DialogTitle>
          <DialogContent>
            <DialogContentText id="alert-dialog-slide-description">
              Invalid image file. Please check the integrity of your file and try again.
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={this.handleInvalidFileErrorClose} color="primary">
              Dismiss
            </Button>
          </DialogActions>
        </Dialog>

        <SnackBar anchorOrigin={{vertical: "bottom", horizontal: "right"}}
                  autoHideDuration={5000}
                  message={<span id="message-id">Please select an image file to upload.</span>}
                  open={this.state.noFileError}
                  onClose={this.handleNoFileErrorClose}
                  action={[
                    <IconButton
                      key="close"
                      color="inherit"
                      onClick={this.handleNoFileErrorClose}
                    >
                      <CloseIcon/>
                    </IconButton>
                  ]}
        />
      </Template>
    );
  }
}

export default withStyles(styles)(ThemePage);
