import * as React from 'react';
import {Bridge} from "../api/bridge";
import Radio from '@material-ui/core/Radio';
import FormControl from "@material-ui/core/FormControl/FormControl";
import RadioGroup from "@material-ui/core/RadioGroup/RadioGroup";
import FormControlLabel from "@material-ui/core/FormControlLabel/FormControlLabel";
import Button from "@material-ui/core/Button/Button";
import ColorPickerComponent from "./ColorPickerComponent";
import Typography from "@material-ui/core/Typography/Typography";
import axios from 'axios';
import {Config} from "../config";
import {Theme, WithStyles} from "@material-ui/core";
import createStyles from "@material-ui/core/styles/createStyles";
import withStyles from "@material-ui/core/styles/withStyles";
declare var themeSettings: any;
const styles = (theme: Theme) => createStyles({
  container: {
    position: 'relative',
  },
  paper: {
    position: 'absolute',
    zIndex: 1,
    marginTop: theme.spacing.unit * 2,
    left: 0,
    right: 0,
    marginBottom: theme.spacing.unit,
  },
  chip: {
    margin: `${theme.spacing.unit / 2}px ${theme.spacing.unit / 4}px`,
  },
  inputRoot: {
    flexWrap: 'wrap',
  },
  divider: {
    height: theme.spacing.unit * 2,
  },
});

interface ThemePageProps {
  bridge: Bridge;
}

class ThemePage extends React.Component<ThemePageProps & WithStyles<typeof styles>> {
  state = {
    selectedThemeOption: 'standard',
    primary: '#2196f3',
    secondary: '#ff9800',
    background: '#fafafa',
    menu: '#ffffff',
    menuText: '#000000',
    menuIcon: '#000000',
    text: '#000000'
  };
  handleThemeChange = (event: any) => {
    switch (event.target.value) {
      case 'standard':
        this.setState({
          primary: '#2196f3',
          secondary: '#ff9800',
          background: '#fafafa',
          menu: '#ffffff',
          menuText: '#000000',
          menuIcon: '#000000',
          text: '#000000'
        });
        break;
      case 'dark':
        this.setState({
          primary: '#4e4e51',
          secondary: '#ffffff',
          background: '#dfdbdb',
          menu: '#919191',
          menuText: '#ffffff',
          menuIcon: '#ffffff',
          text: '#ffffff'
        });
        break;
      case 'custom':
        this.setState({
          primary: themeSettings['primaryColor'],
          secondary: themeSettings['secondaryColor'],
          background: themeSettings['backgroundColor'],
          menu: themeSettings['menuItemColor'],
          menuText: themeSettings['menuItemTextColor'],
          menuIcon: themeSettings['menuItemIconColor'],
          text: themeSettings['menuTextColor']
        });
        break;
    }
    this.setState({selectedThemeOption: event.target.value});

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

  handleBackButton = () => {
    window.history.back();
  };

  render() {
    const {Template} = this.props.bridge;
    return (
      <Template title={"Institution Theme Settings"}>
        <div>
          <FormControl>
            <RadioGroup
              aria-label="ThemeSelect"
              name="themeSelect"
              row={true}
              value={this.state.selectedThemeOption}
              onChange={this.handleThemeChange}
            >
              <FormControlLabel value="standard" control={<Radio/>} label="Default"/>
              <FormControlLabel value="dark" control={<Radio/>} label="Dark"/>
              <FormControlLabel value="custom" control={<Radio/>} label="Custom..."/>
            </RadioGroup>

            <Typography>Primary Colour</Typography>
            <ColorPickerComponent changeColor={this.handlePrimaryChange} color={this.state.primary}/>

            <Typography>Secondary Colour - Elements</Typography>
            <ColorPickerComponent changeColor={this.handleSecondaryChange} color={this.state.secondary}/>

            <Typography>Secondary Colour - Text</Typography>
            <ColorPickerComponent changeColor={this.handleTextChange} color={this.state.text}/>

            <Typography>Background Colour</Typography>
            <ColorPickerComponent changeColor={this.handleBackgroundChange} color={this.state.background}/>

            <Typography>Menu Item Colour</Typography>
            <ColorPickerComponent changeColor={this.handleMenuChange} color={this.state.menu}/>

            <Typography>Menu Item Text Colour</Typography>
            <ColorPickerComponent changeColor={this.handleMenuTextChange} color={this.state.menuText}/>

            <Typography>Menu Item Icon Colour</Typography>
            <ColorPickerComponent changeColor={this.handleMenuIconChange} color={this.state.menuIcon}/>

            <Button variant="contained" type={"submit"} onClick={this.submitTheme}>
              Apply Theming
            </Button>
            <Button variant="contained" onClick={this.handleBackButton}>
              Go Back
            </Button>
          </FormControl>
        </div>
      </Template>
    );
  }
}

export default withStyles(styles)(ThemePage);
