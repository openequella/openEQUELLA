import * as React from 'react';
import {Bridge} from "../api/bridge";
import FormControl from "@material-ui/core/FormControl/FormControl";
import Button from "@material-ui/core/Button/Button";
import ColorPickerComponent from "./ColorPickerComponent";
import Typography from "@material-ui/core/Typography/Typography";
import axios from 'axios';
import {Config} from "../config";
import {Theme, WithStyles} from "@material-ui/core";
import createStyles from "@material-ui/core/styles/createStyles";
import withStyles from "@material-ui/core/styles/withStyles";
import Grid from "@material-ui/core/Grid/Grid";
import CardContent from "@material-ui/core/CardContent/CardContent";
import CardActions from "@material-ui/core/CardActions/CardActions";

declare var themeSettings: any;
const styles = (theme: Theme) => createStyles({
  container: {
    position: 'relative',
    marginLeft: '4px',
    marginTop: '4px',
    marginBottom: '2px',
  },
  card: {
    display: 'flex',
    marginTop: '16px',
    marginLeft:'230px'
  },
  labels: {
    marginTop: '4px',
    marginBottom: '4px',
  },
  buttons: {
    align: 'right',
    marginLeft: '4px',
    marginTop: '4px',
    marginBottom: '4px',

  },
  buttonContainer: {
    width: "250px",
    alignSelf: 'centre'
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
    text: themeSettings['menuTextColor']
  };
  handleDefaultButton = () => {
    this.setState({
      primary: '#2196f3',
      secondary: '#ff9800',
      background: '#fafafa',
      menu: '#ffffff',
      menuText: '#000000',
      menuIcon: '#000000',
      text: '#000000'
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
    });
  };

  render() {
    const {Template} = this.props.bridge;
    return (
      <Template title={"New UI Settings"}>
        {/*<Card raised={true} className={this.props.classes.card}>*/}

        <CardContent>
          <FormControl>
            <Grid container spacing={16}>
              <Grid item>
                <Typography className={this.props.classes.labels} color={"textSecondary"}>
                  Primary Colour
                </Typography>
                <ColorPickerComponent changeColor={this.handlePrimaryChange} color={this.state.primary}/>

                <Typography className={this.props.classes.labels} color={"textSecondary"}>
                  Menu Item Colour
                </Typography>
                <ColorPickerComponent changeColor={this.handleMenuChange} color={this.state.menu}/>

                <Typography className={this.props.classes.labels} color={"textSecondary"}>
                  Background Colour
                </Typography>
                <ColorPickerComponent changeColor={this.handleBackgroundChange} color={this.state.background}/>

              </Grid>
              <Grid item>
                <Typography className={this.props.classes.labels} color={"textSecondary"}>
                  Secondary Colour - Elements
                </Typography>
                <ColorPickerComponent changeColor={this.handleSecondaryChange} color={this.state.secondary}/>

                <Typography className={this.props.classes.labels} color={"textSecondary"}>
                  Menu Item Text Colour
                </Typography>
                <ColorPickerComponent changeColor={this.handleMenuTextChange} color={this.state.menuText}/>
                <div></div>

              </Grid>

              <Grid item>
                <Typography className={this.props.classes.labels} color={"textSecondary"}>
                  Secondary Colour - Text
                </Typography>
                <ColorPickerComponent changeColor={this.handleTextChange} color={this.state.text}/>

                <Typography className={this.props.classes.labels} color={"textSecondary"}>
                  Menu Item Icon Colour
                </Typography>
                <ColorPickerComponent changeColor={this.handleMenuIconChange} color={this.state.menuIcon}/>
                <div></div>

              </Grid>
            </Grid>
          </FormControl>
        </CardContent>
        <CardActions className={this.props.classes.card}>
          <Button variant="text" onClick={this.handleDefaultButton}>
            Default
          </Button>

          <Button variant="outlined" onClick={this.handleUndoButton}>
            Undo
          </Button>
          <Button variant="contained" type={"submit"}
                  onClick={this.submitTheme}>
            Apply
          </Button>
        </CardActions>
      </Template>
    );
  }
}

export default withStyles(styles)(ThemePage);
