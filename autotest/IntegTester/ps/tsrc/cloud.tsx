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
  CssBaseline
} from "@material-ui/core";
import { ThemeProvider } from "@material-ui/styles";
import { ProviderRegistrationResponse } from "oeq-cloudproviders/registration";
import { createRegistration, UpdateRegistration } from "./registration";
import { theme, useStyles } from "./theme";

interface Props {
  register?: string;
  institution?: string;
  name?: string;
  description?: string;
  iconUrl?: string;
}

interface TokenResponse {
  access_token: string;
}

interface CurrentUserDetails {
  firstName: String;
  lastName: String;
}

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
    const pr = createRegistration({
      name: q.name!,
      description: q.description!,
      iconUrl: q.iconUrl
    });
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

ReactDOM.render(
  <React.Fragment>
    <CssBaseline />
    <ThemeProvider theme={theme}>
      <CloudProvider query={parse(location.search)} />
    </ThemeProvider>
  </React.Fragment>,
  document.getElementById("app")
);
