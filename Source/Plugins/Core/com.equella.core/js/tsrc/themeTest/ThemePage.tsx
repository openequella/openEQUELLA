import * as React from 'react';
import {Bridge} from "../api/bridge";
import FormControl from "@material-ui/core/FormControl/FormControl";
import Button from "@material-ui/core/Button/Button";
import ColorPickerComponent from "./ColorPickerComponent";
import Typography from "@material-ui/core/Typography/Typography";
import axios from 'axios';
import {Config} from "../config";
import {WithStyles} from "@material-ui/core";
import createStyles from "@material-ui/core/styles/createStyles";
import withStyles from "@material-ui/core/styles/withStyles";
import Grid from "@material-ui/core/Grid/Grid";
import CardContent from "@material-ui/core/CardContent/CardContent";
import CardActions from "@material-ui/core/CardActions/CardActions";
import Card from "@material-ui/core/Card/Card";
import Divider from "@material-ui/core/Divider/Divider";

declare var themeSettings: any;
declare var isCustomLogo: boolean;
const styles = createStyles({
  card: {
    marginTop: '16px',
    overflow: 'visible'
  },
  labels: {
    marginTop: '4px',
    marginBottom: '4px',
  },
  input: {
    display: 'none'
  }
});

interface ThemePageProps {
  bridge: Bridge;
}

class ThemePage extends React.Component<ThemePageProps & WithStyles<typeof styles>> {

  state = {
    primary: themeSettings['primaryColor'],
    secondary: themeSettings['secondaryColor'],
    background: themeSettings['backgroundColor'],
    menu: themeSettings['menuItemColor'],
    menuText: themeSettings['menuItemTextColor'],
    menuIcon: themeSettings['menuItemIconColor'],
    text: themeSettings['menuTextColor'],
    logoToUpload: '',
    imagePreviewUrl: '',
    customLogo: isCustomLogo,
    fileName: ""
  };
  handleDefaultButton = () => {
    this.setState({
      primary: '#2196f3',
      secondary: '#ff9800',
      background: '#fafafa',
      menu: '#ffffff',
      menuText: '#000000',
      menuIcon: '#000000',
      text: '#000000',
    });
  };
  handleUndoButton = () => {
    this.setState({
      primary: themeSettings['primaryColor'],
      secondary: themeSettings['secondaryColor'],
      background: themeSettings['backgroundColor'],
      menu: themeSettings['menuItemColor'],
      menuText: themeSettings['menuItemTextColor'],
      menuIcon: themeSettings['menuItemIconColor'],
      text: themeSettings['menuTextColor']
    });
  };
  handlePrimaryChange = (color: string) => {
    this.setState({primary: color});
    //  this.setState({expanded: !this.state.expanded});
  };
  handleSecondaryChange = (color: string) => {
    this.setState({secondary: color});
    //  this.setState({expanded: !this.state.expanded});
  };
  handleBackgroundChange = (color: string) => {
    this.setState({background: color});
    //  this.setState({expanded: !this.state.expanded});
  };
  handleMenuChange = (color: string) => {
    this.setState({menu: color});
    //  this.setState({expanded: !this.state.expanded});
  };
  handleMenuTextChange = (color: string) => {
    this.setState({menuText: color});
    //  this.setState({expanded: !this.state.expanded});
  };
  handleMenuIconChange = (color: string) => {
    this.setState({menuIcon: color});
    // this.setState({expanded: !this.state.expanded});
  };
  handleTextChange = (color: string) => {
    this.setState({text: color});
    //  this.setState({expanded: !this.state.expanded});
  };

  _handleImageChange(e: any) {
    e.preventDefault();
    console.log(this.state.logoToUpload);
    let reader = new FileReader();

    let file = e.target.files[0];
    reader.readAsDataURL(file);
    reader.onloadend = () => {
      this.setState({
        logoToUpload: file,
        fileName: file.name
      });
      console.log(this.state.logoToUpload);
    };
  };

  submitTheme = () => {
    axios.put(`${Config.baseUrl}api/themeresource/update/`,
      "{\"primaryColor\":\"" + this.state.primary + "\"," +
      "\"secondaryColor\":\"" + this.state.secondary + "\", " +
      "\"backgroundColor\":\"" + this.state.background + "\", " +
      "\"menuItemColor\":\"" + this.state.menu + "\", " +
      "\"menuItemTextColor\": \"" + this.state.menuText + "\", " +
      "\"menuItemIconColor\": \"" + this.state.menuIcon + "\", " +
      "\"menuTextColor\": \"" + this.state.text + "\", " +
      "\"fontSize\": 14}").then(function () {
        window.location.reload();
      }
    );
  };
  resetLogo = () => {
    axios.delete(`${Config.baseUrl}api/themeresource/resetlogo/`).then(function () {
      window.location.reload();
    });
  };
  submitLogo = () => {
    axios.put(`${Config.baseUrl}api/themeresource/updatelogo/`, this.state.logoToUpload).then(function () {
      window.location.reload();
    });
  };

  render() {
    const {Template} = this.props.bridge;
    return (
      <Template title={"New UI Settings"}>
        <Card raised={true} className={this.props.classes.card}>
          <CardContent>
            <Typography variant={"display1"}>Colour Scheme</Typography>
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
          {/*<Divider light={false}/>*/}
          <CardActions>
            <Button variant="text" onClick={this.handleDefaultButton}>
              Reset to Default
            </Button>
            <Button variant="outlined" onClick={this.handleUndoButton}>
              Undo
            </Button>
            <Button variant="contained" type={"submit"}
                    onClick={this.submitTheme}>
              Apply
            </Button>
          </CardActions>
          <Divider light={true}/>
          <CardContent>
            <Typography variant={'display1'}>Logo Settings</Typography>
            <Typography className={this.props.classes.labels} color={"textSecondary"}>
              Use a PNG file of 230x36 pixels for best results.
            </Typography>
            <div>
              <label>

                <input
                  accept="image/*"
                  className={this.props.classes.input}
                  color={"textSecondary"}
                  id="contained-button-file"
                  onChange={(e) => this._handleImageChange(e)}
                  type="file"
                />
                <label htmlFor="contained-button-file">
                  <Button variant="outlined" component="span">
                    Browse...
                  </Button>
                </label>
                  <Typography className={this.props.classes.labels}
                              color={"textSecondary"}>{this.state.fileName ? this.state.fileName : "No file selected."}</Typography>

              </label>
              <div>
                <Typography className={this.props.classes.labels} color={"textSecondary"}>Current Logo: </Typography>
                <img
                  src={this.state.customLogo ? `${Config.baseUrl}api/themeresource/newLogo.png` : `${Config.baseUrl}p/r/6.7.r88/com.equella.core/images/new-equella-logo.png`}/>
              </div>
            </div>
          </CardContent>
          <CardActions>
            <Button variant="text"
                    onClick={this.resetLogo}>
              Reset to Default
            </Button>
            <Button variant="contained" type={"submit"}
                    onClick={this.submitLogo}>
              Apply
            </Button>
          </CardActions>
        </Card>
      </Template>
    );
  }
}

export default withStyles(styles)(ThemePage);
