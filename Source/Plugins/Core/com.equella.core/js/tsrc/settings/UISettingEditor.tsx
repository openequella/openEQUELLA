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
import {
  fetchUISetting,
  saveUISetting,
} from "../modules/GeneralSettingsModule";
import {
  ExpansionPanelDetails,
  Button,
  FormControl,
  FormControlLabel,
  Switch,
  Grid,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import { Link } from "react-router-dom";
import { routes } from "../mainui/routes";
import { languageStrings } from "../util/langstrings";
import { Config } from "../config";
import { useEffect, useState } from "react";

const useStyles = makeStyles({
  fab: {
    position: "absolute",
    bottom: 0,
    right: 16,
  },
  enableNewUIColumn: {
    flexBasis: "33.3%",
  },
  enableNewSearchColumn: {
    flexBasis: "33.3%",
  },
});

interface UISettingEditorProps {
  refreshUser: () => void;
  handleError: (error: Error) => void;
}

const UISettingEditor = (props: UISettingEditorProps) => {
  const classes = useStyles();
  const { refreshUser, handleError } = props;
  const { uiconfig } = languageStrings;

  const [newUIEnabled, setNewUIEnabled] = useState<boolean>(true);
  const [newSearchEnabled, setNewSearchEnabled] = useState<boolean>(false);

  useEffect(() => {
    // Use a flag to prevent setting state when component is being unmounted
    let cleanupTriggered = false;
    fetchUISetting()
      .then((uiSetting) => {
        if (!cleanupTriggered) {
          const { enabled, newSearch } = uiSetting.newUI;
          setNewUIEnabled(enabled);
          setNewSearchEnabled(newSearch);
        }
      })
      .catch((error) => {
        handleError(error);
      });

    return () => {
      cleanupTriggered = true;
    };
  }, []);

  const setNewUI = (enabled: boolean) => {
    setNewUIEnabled(enabled);
    saveUISetting(enabled, newSearchEnabled)
      .then((_) => {
        window.location.href = Config.baseUrl + "access/settings.do";
      })
      .catch((error) => {
        handleError(error);
      });
  };

  const setNewSearch = (enabled: boolean) => {
    setNewSearchEnabled(enabled);
    saveUISetting(newUIEnabled, enabled)
      .then((_) => refreshUser())
      .catch((error) => {
        handleError(error);
      });
  };

  return (
    <ExpansionPanelDetails>
      <Grid container direction="column">
        <Grid item>
          <div className={classes.enableNewUIColumn}>
            <FormControl>
              <FormControlLabel
                control={
                  <Switch
                    checked={newUIEnabled}
                    onChange={(_, checked) => setNewUI(checked)}
                  />
                }
                label={uiconfig.enableNew}
              />
            </FormControl>
          </div>
        </Grid>

        <Grid item>
          <div className={classes.enableNewSearchColumn}>
            <FormControl>
              <FormControlLabel
                control={
                  <Switch
                    checked={newSearchEnabled}
                    disabled={!newUIEnabled}
                    onChange={(_, checked) => setNewSearch(checked)}
                  />
                }
                label={uiconfig.enableSearch}
              />
            </FormControl>
          </div>
        </Grid>

        <Grid item>
          <Link to={routes.ThemeConfig.path}>
            <Button variant="contained" disabled={!newUIEnabled}>
              {uiconfig.themeSettingsButton}
            </Button>
          </Link>
        </Grid>
      </Grid>
    </ExpansionPanelDetails>
  );
};

export default UISettingEditor;
