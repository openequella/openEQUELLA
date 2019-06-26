import * as React from "react";
import { useState } from "react";
import {
  TextField,
  Button,
  AppBar,
  Toolbar,
  Typography,
  Paper
} from "@material-ui/core";
import { ProviderRegistration } from "oeq-cloudproviders/registration";
import axios from "axios";
import { useStyles } from "./theme";

export const serverBase = "http://localhost:8083/";
export const baseUrl = serverBase + "provider/";

export function UpdateRegistration() {
  const [institutionUrl, setInstitutionUrl] = useState(
    "http://doolse-sabre:8080/workflow/"
  );
  const [providerId, setProviderId] = useState(
    "ba87a9bb-0281-4c0d-9397-772ba036e85b"
  );
  const [token, setToken] = useState("a63b07f9-204b-4507-87d0-d220d7aade8a");
  const [name, setName] = useState("Updated provider");
  const [description, setDescription] = useState("");
  const classes = useStyles();
  function update() {
    axios.put(
      institutionUrl +
        "api/cloudprovider/provider/" +
        encodeURIComponent(providerId),
      createRegistration({ name, description }),
      {
        headers: {
          "X-Authorization": "access_token=" + token
        }
      }
    );
  }

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
          <div>
            <TextField
              label="Institution URL"
              value={institutionUrl}
              className={classes.textField}
              onChange={e => setInstitutionUrl(e.target.value)}
            />
            <TextField
              label="Provider ID"
              value={providerId}
              className={classes.textField}
              onChange={e => setProviderId(e.target.value)}
            />
            <TextField
              label="Token"
              value={token}
              className={classes.textField}
              onChange={e => setToken(e.target.value)}
            />
          </div>
          <div>
            <TextField
              label="Name"
              value={name}
              className={classes.textField}
              onChange={e => setName(e.target.value)}
            />
            <TextField
              label="Description"
              value={description}
              className={classes.textField}
              onChange={e => setDescription(e.target.value)}
            />
          </div>
          <Button variant="contained" color="secondary" onClick={_ => update()}>
            Update details
          </Button>
        </div>
      </Paper>
    </div>
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
        authenticated: true
      },
      control_testcontrol: {
        url: "${baseUrl}control.js",
        authenticated: false
      },
      myService: {
        url:
          "${baseUrl}myService?param1=${param1}&param2=${param2}&from=${userid}",
        authenticated: true
      },
      viewattachment: {
        url:
          serverBase +
          "viewitem.html?attachment=${attachment}&item=${item}&version=${version}&viewer=${viewer}",
        authenticated: true
      }
    },
    viewers: {
      simple: {
        "": { name: "Default", serviceId: "viewattachment" },
        other: { name: "Other viewer", serviceId: "viewattachment" }
      }
    }
  };
}
