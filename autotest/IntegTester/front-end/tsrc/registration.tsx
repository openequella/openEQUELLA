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
import * as React from "react";
import { useState } from "react";
import { Button, AppBar, Toolbar, Typography } from "@mui/material";
import { ProviderRegistration } from "oeq-cloudproviders/registration";
import axios from "axios";
import { StyledBody, StyledPaper, StyledRoot, StyledTextField } from "./theme";

export const serverBase = "http://localhost:8083/";
export const baseUrl = serverBase + "provider/";

export function UpdateRegistration() {
  const [institutionUrl, setInstitutionUrl] = useState(
    "http://doolse-sabre:8080/workflow/",
  );
  const [providerId, setProviderId] = useState(
    "ba87a9bb-0281-4c0d-9397-772ba036e85b",
  );
  const [token, setToken] = useState("a63b07f9-204b-4507-87d0-d220d7aade8a");
  const [name, setName] = useState("Updated provider");
  const [description, setDescription] = useState("");
  function update() {
    axios.put(
      institutionUrl +
        "api/cloudprovider/provider/" +
        encodeURIComponent(providerId),
      createRegistration({ name, description }),
      {
        headers: {
          "X-Authorization": "access_token=" + token,
        },
      },
    );
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
          <div>
            <StyledTextField
              label="Institution URL"
              value={institutionUrl}
              onChange={(e) => setInstitutionUrl(e.target.value)}
            />
            <StyledTextField
              label="Provider ID"
              value={providerId}
              onChange={(e) => setProviderId(e.target.value)}
            />
            <StyledTextField
              label="Token"
              value={token}
              onChange={(e) => setToken(e.target.value)}
            />
          </div>
          <div>
            <StyledTextField
              label="Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            <StyledTextField
              label="Description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
          <Button
            variant="contained"
            color="secondary"
            onClick={(_) => update()}
          >
            Update details
          </Button>
        </StyledBody>
      </StyledPaper>
    </StyledRoot>
  );
}

export function createRegistration(params: {
  name: string;
  description: string;
  iconUrl?: string;
}): ProviderRegistration {
  return {
    name: params.name,
    description: params.description,
    iconUrl: params.iconUrl,
    baseUrl: baseUrl,
    vendorId: "oeq_autotest",
    providerAuth: { clientId: params.name, clientSecret: params.name },
    serviceUrls: {
      oauth: { url: "${baseUrl}access_token", authenticated: false },
      controls: { url: "${baseUrl}controls", authenticated: true },
      itemNotification: {
        url: "${baseUrl}itemNotification?uuid=${uuid}&version=${version}",
        authenticated: true,
      },
      control_testcontrol: {
        url: "${baseUrl}control.js",
        authenticated: false,
      },
      myService: {
        url: "${baseUrl}myService?param1=${param1}&param2=${param2}&from=${userid}",
        authenticated: true,
      },
      viewattachment: {
        url:
          serverBase +
          "viewitem.html?attachment=${attachment}&item=${item}&version=${version}&viewer=${viewer}",
        authenticated: true,
      },
    },
    viewers: {
      simple: {
        "": { name: "Default", serviceId: "viewattachment" },
        other: { name: "Other viewer", serviceId: "viewattachment" },
      },
    },
  };
}
