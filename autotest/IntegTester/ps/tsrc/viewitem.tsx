import * as ReactDOM from "react-dom";
import * as React from "react";
import "babel-polyfill";
import { parse } from "query-string";
import { CssBaseline } from "@material-ui/core";
import { ThemeProvider } from "@material-ui/styles";
import { theme, useStyles } from "./theme";

interface Props {
  attachment?: string;
  item?: string;
  version?: string;
}

declare const postValues: Props;

function ViewItem(props: { query: Props }) {
  const q = props.query;
  const classes = useStyles();

  return (
    <div id="testCloudProvider" className={classes.root}>
      {JSON.stringify(q)}
    </div>
  );
}

ReactDOM.render(
  <React.Fragment>
    <CssBaseline />
    <ThemeProvider theme={theme}>
      <ViewItem query={postValues} />
    </ThemeProvider>
  </React.Fragment>,
  document.getElementById("app")
);
