import * as React from 'react';
// import { Bridge } from "../api/bridge";
// import Radio from '@material-ui/core/Radio';
// import FormLabel from "@material-ui/core/FormLabel/FormLabel";
// import FormControl from "@material-ui/core/FormControl/FormControl";
// import RadioGroup from "@material-ui/core/RadioGroup/RadioGroup";
// import FormControlLabel from "@material-ui/core/FormControlLabel/FormControlLabel";
// import CheckBox from "@material-ui/core/Checkbox/Checkbox";
// import Button from "@material-ui/core/Button/Button";
// import {createStyles, Theme} from "@material-ui/core";
// import {WithStyles} from "@material-ui/core/styles";
// import withStyles from "@material-ui/core/styles/withStyles";
// import ColorPickerComponent from "./ColorPickerComponent";
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
// interface ThemePageProps
// {
//   bridge: Bridge;
// }

// class ThemePage extends React.Component<ThemePageProps&WithStyles<typeof styles>>
class ThemePage extends React.Component
{
  // state = {
  //   selectedThemeOption: 'standard',
  //   selectedLogoOption: 'standard',
  //   customThemeEnabled: false,
  //   customLogoEnabled: false,
  //   grabbedTheme:'notGrabbed'
  // };
  //
  // handleThemeChange = (event:any) => {
  //   this.setState({ selectedThemeOption: event.target.value });
  // };
  //
  // handleLogoChange = (event:any) => {
  //   this.setState({selectedLogoOption: event.target.value });
  // };
  //
  // handleThemeCheckBox = (event:any) => {
  //   this.setState({customThemeEnabled:  event.target.checked});
  //   this.setState({selectedThemeOption: 'standard'});
  // };
  //
  // handleLogoCheckBox = (event:any) => {
  //   this.setState({customLogoEnabled:  event.target.checked});
  //   this.setState({selectedLogoOption: 'standard'});
  // };
  //
  // componentDidMount() {
  //   //TODO: Grab current theme info and autofill form with it
  //   // axios
  //   //   .get(`${Config.baseUrl}api/themeresource/theme/`)
  //   //   .then((response:any) => {
  //   //     this.setState({grabbedTheme: response.data})
  //   //   })
  //   //   .catch((error:Error) => console.log(error));
  // }
  render() {
    // const {Template} = this.props.bridge;
    return (<div>empty</div>);
  }

}
export default ThemePage;
