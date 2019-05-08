import * as ReactDOM from "react-dom";
import * as React from "react";
import "babel-polyfill";
import { createMuiTheme, CssBaseline } from "@material-ui/core";
import { ThemeProvider } from "@material-ui/styles";
import { deepOrange, deepPurple } from "@material-ui/core/colors";
import { UpdateRegistration } from "./registration";

const theme = createMuiTheme({
  palette: {
    primary: deepOrange,
    secondary: deepPurple
  },
  typography: {
    useNextVariants: true
  }
});

ReactDOM.render(
  <React.Fragment>
    <CssBaseline />
    <ThemeProvider theme={theme}>
      <UpdateRegistration />
    </ThemeProvider>
  </React.Fragment>,
  document.getElementById("app")
);
