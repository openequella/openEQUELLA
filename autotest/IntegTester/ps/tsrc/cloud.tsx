import * as ReactDOM from "react-dom";
import * as React from "react";
import axios from "axios";
import "babel-polyfill";
import { useState } from "react";
import { parse } from "query-string";
import {
  AppBar,
  Typography,
  Toolbar,
  Paper,
  Button,
  Theme,
  createMuiTheme,
  CssBaseline
} from "@material-ui/core";
import { makeStyles, ThemeProvider } from "@material-ui/styles";
import { deepOrange, deepPurple } from "@material-ui/core/colors";

interface Props {
  register?: string;
  institution?: string;
  name?: string;
  description?: string;
  iconUrl?: string;
}

interface OAuthCredentials {
  clientId: string;
  clientSecret: string;
}

interface ProviderRegistration {
  name: string;
  description?: string;
  baseUrl: string;
  iconUrl?: string;
  providerAuth: OAuthCredentials;
  serviceUris: object;
  viewers: object;
}

interface ProviderRegistrationInstance extends ProviderRegistration {
  oeqAuth: OAuthCredentials;
}

interface ProviderRegistrationResponse {
  instance: ProviderRegistrationInstance;
  forwardUrl: string;
}

const useStyles = makeStyles((theme: Theme) => {
  return {
    content: {
      display: "flex",
      flexDirection: "column",
      flexGrow: 1
    },
    root: {
      height: "100vh",
      display: "flex",
      flexDirection: "column"
    },
    body: {
      margin: theme.spacing.unit * 2
    }
  };
});

function CloudProvider(props: { query: Props }) {
  const q = props.query;
  const [error, setError] = useState<Error | null>(null);
  const [response, setResponse] = useState<ProviderRegistrationResponse | null>(
    null
  );

  async function registerCP(url: string) {
    const pr: ProviderRegistration = {
      name: q.name!,
      description: q.description,
      iconUrl: q.iconUrl,
      baseUrl: document.location.href,
      providerAuth: { clientId: "MyClient", clientSecret: "HEI" },
      serviceUris: {},
      viewers: {}
    };
    await axios
      .post<ProviderRegistrationResponse>(url, pr)
      .then(resp => setResponse(resp.data))
      .catch((error: Error) => setError(error));
  }
  const classes = useStyles();

  return (
    <div id="testCloudProvider" className={classes.root}>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Typography variant="h6" color="inherit">
            Test Cloud Provider
          </Typography>
        </Toolbar>
      </AppBar>
      <Paper className={classes.content}>
        <div className={classes.body}>
          {response ? (
            <Typography>Successfully registered.</Typography>
          ) : (
            <Typography>
              Institution <b>'{props.query.institution}'</b> has request we
              register a cloud provider.
            </Typography>
          )}
          {error && (
            <Typography id="errorMessage" color="error">
              {error.message}
            </Typography>
          )}
        </div>
        <div>
          {response ? (
            <Button
              id="returnButton"
              variant="contained"
              color="secondary"
              onClick={_ => (window.location.href = response.forwardUrl)}
            >
              Back to openEQUELLA
            </Button>
          ) : (
            <Button
              id="registerButton"
              variant="contained"
              color="secondary"
              onClick={() => registerCP(q.institution! + q.register!)}
            >
              Register
            </Button>
          )}
        </div>
      </Paper>
    </div>
  );
}

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
      <CloudProvider query={parse(location.search)} />
    </ThemeProvider>
  </React.Fragment>,
  document.getElementById("app")
);
