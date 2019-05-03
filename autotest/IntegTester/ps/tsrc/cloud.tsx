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

interface ServiceUri {
  uri: string;
  authenticated: boolean;
}

interface ProviderRegistration {
  name: string;
  description?: string;
  vendorId: String;
  baseUrl: string;
  iconUrl?: string;
  providerAuth: OAuthCredentials;
  serviceUris: { [key: string]: ServiceUri };
  viewers: object;
}

interface ProviderRegistrationInstance extends ProviderRegistration {
  oeqAuth: OAuthCredentials;
}

interface ProviderRegistrationResponse {
  instance: ProviderRegistrationInstance;
  forwardUrl: string;
}

interface TokenResponse {
  access_token: string;
}

interface CurrentUserDetails {
  firstName: String;
  lastName: String;
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

const baseUrl = "http://localhost:8083/provider/";

function CloudProvider(props: { query: Props }) {
  const q = props.query;
  const [error, setError] = useState<Error | null>(null);
  const [response, setResponse] = useState<ProviderRegistrationResponse | null>(
    null
  );
  const [currentUser, setCurrentUser] = useState<CurrentUserDetails | null>(
    null
  );

  async function registerCP(url: string) {
    const pr: ProviderRegistration = {
      name: q.name!,
      description: q.description,
      iconUrl: q.iconUrl,
      baseUrl: baseUrl,
      vendorId: "oeq_autotest",
      providerAuth: { clientId: q.name!, clientSecret: q.name! },
      serviceUris: {
        oauth: { uri: "${baseurl}access_token", authenticated: false },
        controls: { uri: "${baseurl}controls", authenticated: true },
        itemNotification: {
          uri: "${baseurl}itemNotification?uuid=${uuid}&version=${version}",
          authenticated: true
        },
        control_testcontrol: {
          uri: "${baseurl}control.js",
          authenticated: false
        },
        myService: {
          uri:
            "${baseurl}myService?param1=${param1}&param2=${param2}&from=${userid}",
          authenticated: true
        }
      },
      viewers: {}
    };
    await axios
      .post<ProviderRegistrationResponse>(url, pr)
      .then(resp => setResponse(resp.data))
      .catch((error: Error) => setError(error));
  }

  async function talkToEQUELLA(registration: ProviderRegistrationResponse) {
    const oeqAuth = registration.instance.oeqAuth;
    await axios
      .get<TokenResponse>(props.query.institution + "oauth/access_token", {
        params: {
          grant_type: "client_credentials",
          client_id: oeqAuth.clientId,
          client_secret: oeqAuth.clientSecret
        }
      })
      .then(tokenResponse =>
        axios
          .get<CurrentUserDetails>(
            props.query.institution + "api/content/currentuser",
            {
              headers: {
                "X-Authorization":
                  "access_token=" + tokenResponse.data.access_token
              }
            }
          )
          .then(resp => setCurrentUser(resp.data))
      )
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
          {currentUser && (
            <div>
              <Typography id="firstName">{currentUser.firstName}</Typography>
              <Typography id="lastName">{currentUser.lastName}</Typography>
            </div>
          )}
        </div>
        <div>
          {response ? (
            <div>
              <Button
                id="testOEQAuth"
                variant="contained"
                color="secondary"
                onClick={_ => talkToEQUELLA(response)}
              >
                Talk to openEQUELLA
              </Button>
              <Button
                id="returnButton"
                variant="contained"
                color="secondary"
                onClick={_ => (window.location.href = response.forwardUrl)}
              >
                Back to openEQUELLA
              </Button>
            </div>
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
