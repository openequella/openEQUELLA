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
  Accordion,
  AccordionDetails,
  AccordionSummary,
  CircularProgress,
  List,
  ListItem,
  ListItemText,
  Typography,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import MUILink from "@mui/material/Link";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getBaseUrl } from "../AppConfig";
import { AppContext } from "../mainui/App";
import { templateDefaults, TemplateUpdateProps } from "../mainui/Template";
import { fetchSettings } from "../modules/GeneralSettingsModule";
import AdminDownloadDialog from "../settings/AdminDownloadDialog";
import { languageStrings } from "../util/langstrings";
import { groupMap, SettingGroup } from "./SettingGroups";
import UISettingEditor from "./UISettingEditor";

const PREFIX = "SettingsPage";

const classes = {
  heading: `${PREFIX}-heading`,
  secondaryHeading: `${PREFIX}-secondaryHeading`,
  progress: `${PREFIX}-progress`,
};

const Root = styled("div")(({ theme }) => {
  return {
    [`& .${classes.heading}`]: {
      fontSize: theme.typography.pxToRem(15),
      flexBasis: "33.33%",
      flexShrink: 0,
    },
    [`& .${classes.secondaryHeading}`]: {
      fontSize: theme.typography.pxToRem(15),
    },
    [`& .${classes.progress}`]: {
      display: "flex",
      marginTop: theme.spacing(4),
      justifyContent: "center",
    },
  };
});

interface SettingsPageProps extends TemplateUpdateProps {
  isReloadNeeded: boolean;
}

const SettingsPage = ({
  updateTemplate,
  isReloadNeeded,
}: SettingsPageProps) => {
  const [adminDialogOpen, setAdminDialogOpen] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [settingGroups, setSettingGroups] = useState<SettingGroup[]>([]);
  const { appErrorHandler } = useContext(AppContext);

  React.useEffect(() => {
    if (isReloadNeeded) {
      window.location.href = getBaseUrl() + "access/settings.do";
    }
  }, [isReloadNeeded]);

  useEffect(() => {
    updateTemplate(templateDefaults(languageStrings["com.equella.core"].title));
  }, [updateTemplate]);

  useEffect(() => {
    // Use a flag to prevent setting state when component is being unmounted
    let cleanupTriggered = false;
    fetchSettings()
      .then((settings) => {
        if (!cleanupTriggered) {
          setSettingGroups(groupMap(settings));
          setLoading(false);
        }
      })
      .catch((error) => {
        appErrorHandler(error);
        setLoading(false);
      });

    return () => {
      cleanupTriggered = true;
    };
  }, [appErrorHandler]);

  /**
   * Given a SettingGroup determine when the UI Settings Editor should be displayed.
   * This is done by checking the `group` is for the Settings Editor, and that there are settings
   * present to be managed by it.
   */
  const showUiSettings = ({
    category: { name },
    settings: { length },
  }: SettingGroup) => name === languageStrings.settings.ui.name && length > 0;

  /**
   * Create the UI content for setting category
   * @param settingGroup Contains pre-defined `categories` and `settings` of the category
   * @returns {ReactElement} Either a List or UISettingEditor, depending on the category
   */
  const buildAccordionContent = (settingGroup: SettingGroup) =>
    showUiSettings(settingGroup) ? (
      <UISettingEditor />
    ) : (
      <AccordionDetails>
        <List>
          {settingGroup.settings.map((setting) => (
            <ListItem key={setting.id}>
              <ListItemText
                primary={<SettingLink {...setting} />}
                secondary={setting.description}
              />
            </ListItem>
          ))}
        </List>
      </AccordionDetails>
    );

  /**
   * Create a link for each setting
   * @param {GeneralSetting} setting - A oEQ general setting
   * @returns {ReactElement} A link to the setting's page
   */
  const SettingLink = (setting: OEQ.Settings.GeneralSetting) => {
    let link = <div />;
    if (setting.links.route) {
      link = <Link to={setting.links.route}>{setting.name}</Link>;
    } else if (setting.links.href) {
      link = <MUILink href={setting.links.href}>{setting.name}</MUILink>;
    } else if (setting.id === languageStrings.adminconsoledownload.id) {
      link = (
        <MUILink
          style={{ cursor: "pointer" }}
          onClick={(
            e:
              | React.MouseEvent<HTMLAnchorElement>
              | React.MouseEvent<HTMLSpanElement>
          ) => {
            e.preventDefault();
            setAdminDialogOpen(true);
          }}
        >
          {setting.name}
        </MUILink>
      );
    }

    return link;
  };

  return (
    <Root id="settingsPage">
      <AdminDownloadDialog
        open={adminDialogOpen}
        onClose={() => setAdminDialogOpen(false)}
      />
      {
        // Display a circular Progress Bar or the Setting menu, depending on the state of 'loading'
        loading ? (
          <div className={classes.progress}>
            <CircularProgress variant="indeterminate" />
          </div>
        ) : (
          settingGroups.map((group) => {
            const { name, desc } = group.category;
            return (
              <Accordion key={name}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography className={classes.heading}>{name}</Typography>
                  <Typography className={classes.secondaryHeading}>
                    {desc}
                  </Typography>
                </AccordionSummary>
                {buildAccordionContent(group)}
              </Accordion>
            );
          })
        )
      }
    </Root>
  );
};

export default SettingsPage;
