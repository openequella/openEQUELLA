import * as React from 'react';
import {Bridge} from "../api/bridge";
import Radio from '@material-ui/core/Radio';
// import FormLabel from "@material-ui/core/FormLabel/FormLabel";
import FormControl from "@material-ui/core/FormControl/FormControl";
import RadioGroup from "@material-ui/core/RadioGroup/RadioGroup";
import FormControlLabel from "@material-ui/core/FormControlLabel/FormControlLabel";
// import CheckBox from "@material-ui/core/Checkbox/Checkbox";
import Button from "@material-ui/core/Button/Button";
// import {createStyles, Theme} from "@material-ui/core";
// import {WithStyles} from "@material-ui/core/styles";
// import withStyles from "@material-ui/core/styles/withStyles";
import ColorPickerComponent from "./ColorPickerComponent";
import Typography from "@material-ui/core/Typography/Typography";
import axios from 'axios';
import {Config} from "../config";
// const styles = (theme : Theme) => createStyles({
//   container: {
//     position: 'relative',
//   },
//   paper: {
//     position: 'absolute',
//     zIndex: 1,
//     marginTop: theme.spacing.unit*2,
//     left: 0,
//     right: 0,
//     marginBottom: theme.spacing.unit,
//   },
//   chip: {
//     margin: `${theme.spacing.unit / 2}px ${theme.spacing.unit / 4}px`,
//   },
//   inputRoot: {
//     flexWrap: 'wrap',
//   },
//   divider: {
//     height: theme.spacing.unit * 2,
//   },
// });
interface ThemePageProps {
  bridge: Bridge;
}

class ThemePage extends React.Component<ThemePageProps> {
  state = {
    selectedThemeOption: 'standard',
    primary: '#2196f3',
    secondary: '#ff9800',
    background: '#fafafa',
    menu: '#ffffff',
    menuText: '#000000',
    grabbed:"nope"
  };
  //
  handleThemeChange = (event: any) => {
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
  submitTheme = () => {
    axios.put(`${Config.baseUrl}api/themeresource/update/`,
      "{\"primaryColor\":\"" + this.state.primary + "\"," +
      "\"secondaryColor\":\"" + this.state.secondary + "\", " +
      "\"backgroundColor\":\"" + this.state.background + "\", " +
      "\"menuItemColor\":\"" + this.state.menu + "\", " +
      "\"menuItemTextColor\": \"" + this.state.menuText + "\", " +
      "\"fontSize\": 14}").then(function(){window.location.reload()});
  };
componentDidMount(){
  axios.get(`${Config.baseUrl}api/themeresource/theme.js`)
    .then(function(response:any)
    {
      console.log(response.data);});
}
  render() {
    const {Template} = this.props.bridge;
    return (
      <Template title={"Institution Theme Settings"}>
        <FormControl>
          <RadioGroup
            aria-label="ThemeSelect"
            name="themeSelect"
            row={true}
            value={this.state.selectedThemeOption}
            onChange={this.handleThemeChange}
          >
            <FormControlLabel value="standard" control={<Radio/>} label="Default"/>
            <FormControlLabel value="night" control={<Radio/>} label="Dark"/>
            <FormControlLabel value="colorblind" control={<Radio/>} label="Custom..."/>
          </RadioGroup>

          <Typography>Primary Colour  {this.state.grabbed}</Typography>
          <ColorPickerComponent changeColor={this.handlePrimaryChange} color={this.state.primary}/>

          <Typography>Secondary Colour</Typography>
          <ColorPickerComponent changeColor={this.handleSecondaryChange} color={this.state.secondary}/>

          <Typography>Background Colour</Typography>
          <ColorPickerComponent changeColor={this.handleBackgroundChange} color={this.state.background}/>

          <Typography>Menu Item Colour</Typography>
          <ColorPickerComponent changeColor={this.handleMenuChange} color={this.state.menu}/>

          <Typography>Menu Item Text Colour</Typography>
          <ColorPickerComponent changeColor={this.handleMenuTextChange} color={this.state.menuText}/>

          <Button variant="contained" type={"submit"} onClick={this.submitTheme}>
            Apply Theming
          </Button>
          <Button variant="contained">
            Go Back
          </Button>
        </FormControl>
      </Template>
    );
  }

}

export default ThemePage;
