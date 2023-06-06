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
import { constant, identity, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as TE from "fp-ts/TaskEither";
import * as T from "fp-ts/Task";
import * as React from "react";
import { useCallback, useContext, useEffect, useState } from "react";
import { shallowEqual } from "shallow-equal-object";
import MessageDialog from "../../components/MessageDialog";
import SettingPageTemplate from "../../components/SettingPageTemplate";
import SettingsCardActions from "../../components/SettingsCardActions";
import SettingsList from "../../components/SettingsList";
import SettingsToggleSwitch from "../../components/SettingsToggleSwitch";
import { TooltipCustomComponent } from "../../components/TooltipCustomComponent";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { AppContext } from "../../mainui/App";
import { routes } from "../../mainui/routes";
import { templateDefaults, TemplateUpdateProps } from "../../mainui/Template";
import {
  getPlatforms,
  platformOrd,
  updateEnabledPlatforms,
} from "../../modules/Lti13PlatformsModule";
import { commonString } from "../../util/commonstrings";
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
  /**
   * Function to update enabled status for platforms.
   */
  updateEnabledPlatformsProvider?: (
    enabledStatus: OEQ.LtiPlatform.LtiPlatformEnabledStatus[]
  ) => Promise<OEQ.BatchOperationResponse.BatchOperationResponse[]>;
}

/**
 * The settings page for LTI 1.3 platforms which will display all LTI 1.3 platforms in a list.
 * Each platform has an entry in the list, including name, platform ID, a toggle switch for enabling/disabling,
 * a button for editing details, and a button for deleting the platform.
 * Additionally, this page provides an 'Add' button that navigates to a page for creating a new platform.
 */
const Lti13PlatformsSettings = ({
  updateTemplate,
  getPlatformsProvider = getPlatforms,
  updateEnabledPlatformsProvider = updateEnabledPlatforms,
}: Lti13PlatformsSettingsProps) => {
  const [platforms, setPlatforms] = useState<OEQ.LtiPlatform.LtiPlatform[]>([]);
  const [initialPlatforms, setInitialPlatforms] = React.useState<
    OEQ.LtiPlatform.LtiPlatform[]
  >([]);

  const [errorMessages, setErrorMessages] = React.useState<string[]>([]);

  const { appErrorHandler } = useContext(AppContext);

  const setupPlatformRelatedState = useCallback(
    () =>
      getPlatformsProvider()
        .then((p) => {
          const orderedPlatforms = A.sort(platformOrd)(p);
          setPlatforms(orderedPlatforms);
          setInitialPlatforms(orderedPlatforms);
        })
        .catch(appErrorHandler),
    [appErrorHandler, getPlatformsProvider]
  );

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(lti13PlatformsSettingsStrings.name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  useEffect(() => {
    setupPlatformRelatedState();
  }, [setupPlatformRelatedState]);

  const isEqualWithOriginalPlatform = (
    newPlatform: OEQ.LtiPlatform.LtiPlatform
  ) =>
    pipe(
      // find out the original value for the provided platform
      initialPlatforms,
      A.findFirst(
        (originalPlatform) =>
          originalPlatform.platformId === newPlatform.platformId
      ),
      // compare current value with original value
      O.match(
        constant(false),
        (originalPlatform) => !shallowEqual(newPlatform, originalPlatform)
      )
    );

  // check each platform's settings, if any one of them has changed return true
  const changesUnsaved = pipe(
    platforms,
    A.map(isEqualWithOriginalPlatform),
    A.some(identity)
  );

  const platformEntries = () =>
    platforms.map((platform: OEQ.LtiPlatform.LtiPlatform, index) => {
      const { platformId, enabled, name } = platform;

      return (
        <ListItemButton role="listitem" divider key={index}>
          <ListItemText primary={name} secondary={platformId} />
          <TooltipCustomComponent
            title={lti13PlatformsSettingsStrings.enabledSwitch}
          >
            <SettingsToggleSwitch
              value={enabled}
              setValue={(value) =>
                pipe(
                  platforms,
                  A.updateAt(index, {
                    ...platform,
                    enabled: value,
                  }),
                  O.map(setPlatforms)
                )
              }
              id={`EnabledSwitch-${index}`}
            />
          </TooltipCustomComponent>
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
      );
    });

  const handleOnSave = async () => {
    const updateEnabledPlatformsTask = (
      enabledStatus: OEQ.LtiPlatform.LtiPlatformEnabledStatus[]
    ): T.Task<string[]> =>
      pipe(
        TE.tryCatch(
          () => updateEnabledPlatformsProvider(enabledStatus),
          (e) => {
            appErrorHandler(`Failed to update platforms: ${e}`);
            return [];
          }
        ),
        TE.match(identity, OEQ.BatchOperationResponse.groupErrorMessages)
      );

    const enabledStatus = pipe(
      platforms,
      // get all changed platforms
      A.filter(isEqualWithOriginalPlatform),
      A.map(({ platformId, enabled }) => ({
        platformId,
        enabled,
      }))
    );

    pipe(
      await updateEnabledPlatformsTask(enabledStatus)(),
      O.fromPredicate(A.isNonEmpty),
      O.map(setErrorMessages)
    );

    setupPlatformRelatedState();
  };

  return (
    <>
      <SettingPageTemplate
        onSave={handleOnSave}
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

      <MessageDialog
        open={A.isNonEmpty(errorMessages)}
        messages={errorMessages}
        title={commonString.result.errors}
        close={() => setErrorMessages([])}
      />
    </>
  );
};

export default Lti13PlatformsSettings;
