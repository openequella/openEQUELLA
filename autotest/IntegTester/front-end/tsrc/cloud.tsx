/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { AppBar, Button, Toolbar, Typography } from "@mui/material";
import axios from "axios";
import "babel-polyfill";
import { ProviderRegistrationResponse } from "oeq-cloudproviders/registration";
import { parse } from "query-string";
import * as React from "react";
import { useState } from "react";
import { renderIntegTesterPage } from "./app";
import { createRegistration } from "./registration";
import { StyledBody, StyledPaper, StyledRoot } from "./theme";

interface QueryProps {
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
  firstName: string;
  lastName: string;
}

interface CloudProviderProps {
  query: QueryProps;
}

function CloudProvider({ query }: CloudProviderProps) {
  const [error, setError] = useState<Error | null>(null);
  const [response, setResponse] = useState<ProviderRegistrationResponse | null>(
    null,
  );
  const [currentUser, setCurrentUser] = useState<CurrentUserDetails | null>(
    null,
  );

  async function registerCP(url: string) {
    const pr = createRegistration({
      name: query.name!,
      description: query.description!,
      iconUrl: query.iconUrl,
    });
    await axios
      .post<ProviderRegistrationResponse>(url, pr)
      .then((resp) => setResponse(resp.data))
      .catch((error: Error) => setError(error));
  }

  async function talkToEQUELLA(registration: ProviderRegistrationResponse) {
    const oeqAuth = registration.instance.oeqAuth;
    await axios
      .get<TokenResponse>(query.institution + "oauth/access_token", {
        params: {
          grant_type: "client_credentials",
          client_id: oeqAuth.clientId,
          client_secret: oeqAuth.clientSecret,
        },
      })
      .then((tokenResponse) =>
        axios
          .get<CurrentUserDetails>(
            query.institution + "api/content/currentuser",
            {
              headers: {
                "X-Authorization":
                  "access_token=" + tokenResponse.data.access_token,
              },
            },
          )
          .then((resp) => setCurrentUser(resp.data)),
      )
      .catch((error: Error) => setError(error));
  }

  return (
    <StyledRoot id="testCloudProvider">
      <AppBar position="static" color="primary">
        <Toolbar>
          <Typography variant="h6" color="inherit">
            Test Cloud Provider
          </Typography>
        </Toolbar>
      </AppBar>
      <StyledPaper>
        <StyledBody>
          {response ? (
            <Typography>Successfully registered.</Typography>
          ) : (
            <Typography>
              Institution <b>'{query.institution}'</b> has request we register a
              cloud provider.
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
        </StyledBody>
        <div>
          {response ? (
            <div>
              <Button
                id="testOEQAuth"
                variant="contained"
                color="secondary"
                onClick={() => talkToEQUELLA(response)}
              >
                Talk to openEQUELLA
              </Button>
              <Button
                id="returnButton"
                variant="contained"
                color="secondary"
                onClick={() => (window.location.href = response.forwardUrl)}
              >
                Back to openEQUELLA
              </Button>
            </div>
          ) : (
            <Button
              id="registerButton"
              variant="contained"
              color="secondary"
              onClick={() => registerCP(query.institution! + query.register!)}
            >
              Register
            </Button>
          )}
        </div>
      </StyledPaper>
    </StyledRoot>
  );
}

renderIntegTesterPage(
  "cloud",
  <CloudProvider query={parse(window.location.search)} />,
);
