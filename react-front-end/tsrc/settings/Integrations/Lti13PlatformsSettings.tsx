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
import AddCircleIcon from "@mui/icons-material/AddCircle";
import ArticleIcon from "@mui/icons-material/Article";
import DeleteIcon from "@mui/icons-material/Delete";
import {
  Card,
  CardContent,
  IconButton,
  List,
  ListItemButton,
  ListItemText,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { identity, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import { useContext, useEffect, useState } from "react";
import { shallowEqual } from "shallow-equal-object";
import SettingPageTemplate from "../../components/SettingPageTemplate";
import SettingsCardActions from "../../components/SettingsCardActions";
import SettingsList from "../../components/SettingsList";
import SettingsToggleSwitch from "../../components/SettingsToggleSwitch";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { AppContext } from "../../mainui/App";
import { routes } from "../../mainui/routes";
import { templateDefaults, TemplateUpdateProps } from "../../mainui/Template";
import { getPlatforms } from "../../modules/Lti13PlatformsModule";
import { languageStrings } from "../../util/langstrings";

const lti13PlatformsSettingsStrings =
  languageStrings.settings.integration.lti13PlatformsSettings;
const {
  add: addLabel,
  delete: deleteLabel,
  view: viewLabel,
} = languageStrings.common.action;

export interface Lti13PlatformsSettingsProps extends TemplateUpdateProps {
  /**
   * Function to provide a list of platforms.
   */
  getPlatformsProvider?: () => Promise<OEQ.LtiPlatform.LtiPlatform[]>;
}

const Lti13PlatformsSettings = ({
  updateTemplate,
  getPlatformsProvider = getPlatforms,
}: Lti13PlatformsSettingsProps) => {
  const [platforms, setPlatforms] = useState<OEQ.LtiPlatform.LtiPlatform[]>([]);
  const [initialPlatforms, setInitialPlatforms] = React.useState<
    OEQ.LtiPlatform.LtiPlatform[]
  >([]);

  const { appErrorHandler } = useContext(AppContext);

  React.useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(lti13PlatformsSettingsStrings.name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  useEffect(() => {
    getPlatformsProvider()
      .then((p) => {
        setPlatforms(p);
        setInitialPlatforms(p);
      })
      .catch(appErrorHandler);
  }, [appErrorHandler, getPlatformsProvider]);

  // check each platform's settings, if any one of them has changed return true
  const changesUnsaved = pipe(
    platforms,
    A.mapWithIndex((index, p) => !shallowEqual(p, initialPlatforms[index])),
    A.some(identity)
  );

  const platformEntries = () =>
    platforms.map(
      ({ name, platformId }: OEQ.LtiPlatform.LtiPlatform, index) => (
        <ListItemButton role="listitem" divider key={index}>
          <ListItemText primary={name} secondary={platformId} />
          <SettingsToggleSwitch
            setValue={() => {
              // TODO: update platform `enabled` attribute
            }}
            id="enabledSwitch"
          />
          <TooltipIconButton
            title={viewLabel}
            onClick={(e) => {
              // TODO: show platform details
              e.stopPropagation();
            }}
            aria-label={`${viewLabel} ${name}`}
            color="secondary"
            size="large"
          >
            <ArticleIcon />
          </TooltipIconButton>
          <TooltipIconButton
            title={deleteLabel}
            onClick={(e) => {
              // TODO: delete platform
              pipe(platforms, A.deleteAt(index), O.map(setPlatforms));
              e.stopPropagation();
            }}
            aria-label={`${deleteLabel} ${name}`}
            color="secondary"
            size="large"
          >
            <DeleteIcon />
          </TooltipIconButton>
        </ListItemButton>
      )
    );

  return (
    <SettingPageTemplate
      onSave={() => {}}
      preventNavigation={false}
      saveButtonDisabled={!changesUnsaved}
      snackBarOnClose={() => {}}
      snackbarOpen={false}
    >
      <Card>
        <CardContent>
          <SettingsList
            subHeading={lti13PlatformsSettingsStrings.platformsTitle}
          >
            <List>{platformEntries()}</List>
          </SettingsList>
        </CardContent>

        <SettingsCardActions>
          <IconButton
            onClick={() => {
              // TODO: jump to `add platform` page
            }}
            aria-label={addLabel}
            color="primary"
            size="large"
          >
            <AddCircleIcon fontSize="large" />
          </IconButton>
        </SettingsCardActions>
      </Card>
    </SettingPageTemplate>
  );
};

export default Lti13PlatformsSettings;
