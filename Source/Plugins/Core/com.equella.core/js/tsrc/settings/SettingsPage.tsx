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
  Theme,
  Typography,
} from "@material-ui/core";
import MUILink from "@material-ui/core/Link";
import { makeStyles } from "@material-ui/core/styles";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { generateFromError } from "../api/errors";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps,
} from "../mainui/Template";
import { fetchSettings } from "../modules/GeneralSettingsModule";
import AdminDownloadDialog from "../settings/AdminDownloadDialog";
import { languageStrings } from "../util/langstrings";
import useError from "../util/useError";
import { groupMap, SettingGroup } from "./SettingGroups";
import UISettingEditor from "./UISettingEditor";

const useStyles = makeStyles((theme: Theme) => {
  return {
    heading: {
      fontSize: theme.typography.pxToRem(15),
      flexBasis: "33.33%",
      flexShrink: 0,
    },
    secondaryHeading: {
      fontSize: theme.typography.pxToRem(15),
    },
    progress: {
      display: "flex",
      marginTop: theme.spacing(4),
      justifyContent: "center",
    },
  };
});

interface SettingsPageProps extends TemplateUpdateProps {
  refreshUser: () => void;
  isReloadNeeded: boolean;
}

const SettingsPage = ({
  refreshUser,
  updateTemplate,
  isReloadNeeded,
}: SettingsPageProps) => {
  const classes = useStyles();

  const [adminDialogOpen, setAdminDialogOpen] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [settingGroups, setSettingGroups] = useState<SettingGroup[]>([]);
  const setError = useError((error: Error) =>
    updateTemplate(templateError(generateFromError(error)))
  );

  React.useEffect(() => {
    if (isReloadNeeded) {
      window.location.reload();
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
        setError(error);
        setLoading(false);
      });

    return () => {
      cleanupTriggered = true;
    };
  }, [setError]);

  /**
   * Create the UI content for setting category
   * @param category - One of the pre-defined categories
   * @param settings - settings of the category
   * @returns {ReactElement} Either a List or UISettingEditor, depending on the category
   */
  const AccordionContent = ({ category, settings }: SettingGroup) => {
    if (category.name === languageStrings.settings.ui.name) {
      return (
        <UISettingEditor refreshUser={refreshUser} handleError={setError} />
      );
    }
    return (
      <AccordionDetails>
        <List>
          {settings.map((setting) => (
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
  };

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
    <div id="settingsPage">
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
                <AccordionContent {...group} />
              </Accordion>
            );
          })
        )
      }
    </div>
  );
};

export default SettingsPage;
