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
import {
  AccordionDetails,
  Button,
  FormControl,
  FormControlLabel,
  Grid,
  Switch,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import * as React from "react";
import { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getBaseUrl } from "../AppConfig";
import { AppContext } from "../mainui/App";
import { routes } from "../mainui/routes";
import {
  fetchUISetting,
  saveUISetting,
} from "../modules/GeneralSettingsModule";
import { languageStrings } from "../util/langstrings";

const PREFIX = "UISettingEditor";

const classes = {
  fab: `${PREFIX}-fab`,
  enableNewUIColumn: `${PREFIX}-enableNewUIColumn`,
  enableNewSearchColumn: `${PREFIX}-enableNewSearchColumn`,
};

const StyledAccordionDetails = styled(AccordionDetails)({
  [`& .${classes.fab}`]: {
    position: "absolute",
    bottom: 0,
    right: 16,
  },
  [`& .${classes.enableNewUIColumn}`]: {
    flexBasis: "33.3%",
  },
  [`& .${classes.enableNewSearchColumn}`]: {
    flexBasis: "33.3%",
  },
});

const UISettingEditor = () => {
  const { uiconfig } = languageStrings;

  const [newUIEnabled, setNewUIEnabled] = useState<boolean>(true);
  const [newSearchEnabled, setNewSearchEnabled] = useState<boolean>(false);
  const { appErrorHandler } = useContext(AppContext);

  useEffect(() => {
    fetchUISetting()
      .then((uiSetting) => {
        const { enabled, newSearch } = uiSetting.newUI;
        setNewUIEnabled(enabled);
        setNewSearchEnabled(newSearch);
      })
      .catch(appErrorHandler);
  }, [appErrorHandler]);

  const setNewUI = (enabled: boolean) => {
    saveUISetting(enabled, newSearchEnabled)
      .then((_) => {
        setNewUIEnabled(enabled);
        window.location.href = getBaseUrl() + "access/settings.do";
      })
      .catch(appErrorHandler);
  };

  const setNewSearch = (enabled: boolean) => {
    saveUISetting(newUIEnabled, enabled)
      .then((_) => {
        setNewSearchEnabled(enabled);
      })
      .catch(appErrorHandler);
  };

  return (
    <StyledAccordionDetails>
      <Grid container direction="column">
        <Grid>
          <div className={classes.enableNewUIColumn}>
            <FormControl>
              <FormControlLabel
                control={
                  <Switch
                    checked={newUIEnabled}
                    onChange={(_, checked) => setNewUI(checked)}
                    color="secondary"
                  />
                }
                label={uiconfig.enableNew}
              />
            </FormControl>
          </div>
        </Grid>

        <Grid>
          <div className={classes.enableNewSearchColumn}>
            <FormControl>
              <FormControlLabel
                control={
                  <Switch
                    checked={newSearchEnabled}
                    disabled={!newUIEnabled}
                    onChange={(_, checked) => setNewSearch(checked)}
                    color="secondary"
                  />
                }
                label={uiconfig.enableSearch}
              />
            </FormControl>
          </div>
        </Grid>

        <Grid>
          <Link to={routes.ThemeConfig.path}>
            <Button variant="contained" disabled={!newUIEnabled}>
              {uiconfig.themeSettingsButton}
            </Button>
          </Link>
        </Grid>
      </Grid>
    </StyledAccordionDetails>
  );
};

export default UISettingEditor;
