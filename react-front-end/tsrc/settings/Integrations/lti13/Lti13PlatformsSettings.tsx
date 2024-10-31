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
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import FileCopyIcon from "@mui/icons-material/FileCopy";
import {
  Card,
  CardContent,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemSecondaryAction,
  ListItemText,
  Tooltip,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { constTrue, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as R from "fp-ts/Record";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useCallback, useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { shallowEqual } from "shallow-equal-object";
import MessageDialog from "../../../components/MessageDialog";
import SettingPageTemplate from "../../../components/SettingPageTemplate";
import SettingsCardActions from "../../../components/SettingsCardActions";
import SettingsList from "../../../components/SettingsList";
import SettingsToggleSwitch from "../../../components/SettingsToggleSwitch";
import { TooltipCustomComponent } from "../../../components/TooltipCustomComponent";
import { TooltipIconButton } from "../../../components/TooltipIconButton";
import { AppContext } from "../../../mainui/App";
import { routes } from "../../../mainui/routes";
import {
  templateDefaults,
  TemplateUpdateProps,
} from "../../../mainui/Template";
import {
  deletePlatforms,
  getPlatforms,
  platformEq,
  platformOrd,
  providerDetails,
  updateEnabledPlatforms,
} from "../../../modules/Lti13PlatformsModule";
import { commonString } from "../../../util/commonstrings";
import { languageStrings } from "../../../util/langstrings";
import { pfTernary } from "../../../util/pointfree";

const {
  name: pageName,
  providerDetailsTitle,
  providerDetailsDesc,
  platformsTitle,
  platformsDesc,
  enabledSwitch: enabledSwitchLabel,
} = languageStrings.settings.integration.lti13PlatformsSettings;

const {
  add: addLabel,
  edit: editLabel,
  delete: deleteLabel,
  copy: copyLabel,
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
    enabledStatus: OEQ.LtiPlatform.LtiPlatformEnabledStatus[],
  ) => Promise<OEQ.BatchOperationResponse.BatchOperationResponse[]>;
  /**
   * Function to delete platforms.
   */
  deletePlatformsProvider?: (
    platformIds: string[],
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
  deletePlatformsProvider = deletePlatforms,
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
    [appErrorHandler, getPlatformsProvider],
  );

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(pageName)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  useEffect(() => {
    setupPlatformRelatedState();
  }, [setupPlatformRelatedState]);

  // used in delete platform task to fetch deleted platform
  const notExistInNewPlatforms = (
    initialPlatform: OEQ.LtiPlatform.LtiPlatform,
  ): boolean =>
    pipe(
      platforms,
      A.every(
        (newPlatform) => initialPlatform.platformId !== newPlatform.platformId,
      ),
    );

  // used in update platform task to fetch updated platform
  const notEqualWithOriginalPlatform = (
    newPlatform: OEQ.LtiPlatform.LtiPlatform,
  ): boolean =>
    pipe(
      // find out the original value for the provided platform
      initialPlatforms,
      A.findFirst(
        (originalPlatform) =>
          originalPlatform.platformId === newPlatform.platformId,
      ),
      // compare current value with original value
      O.match(
        constTrue,
        (originalPlatform) => !shallowEqual(newPlatform, originalPlatform),
      ),
    );

  // check each platform's settings, if any one of them has changed or deleted return true
  const changesUnsaved = pipe(
    initialPlatforms,
    A.difference(platformEq)(platforms),
    A.isNonEmpty,
  );

  const platformEntries = () =>
    platforms.map((platform: OEQ.LtiPlatform.LtiPlatform, index) => {
      const { platformId, enabled, name } = platform;

      return (
        <ListItemButton role="listitem" divider key={index}>
          <ListItemText primary={name} secondary={platformId} />
          <TooltipCustomComponent title={enabledSwitchLabel}>
            <SettingsToggleSwitch
              value={enabled}
              setValue={(value) =>
                pipe(
                  platforms,
                  A.updateAt(index, {
                    ...platform,
                    enabled: value,
                  }),
                  O.map(setPlatforms),
                )
              }
              id={`EnabledSwitch-${index}`}
            />
          </TooltipCustomComponent>

          <Tooltip title={editLabel} aria-label={`${editLabel} ${name}`}>
            <Link to={routes.EditLti13Platform.to(platformId)}>
              <IconButton color="secondary" size="large">
                <EditIcon />
              </IconButton>
            </Link>
          </Tooltip>

          <TooltipIconButton
            title={deleteLabel}
            onClick={(e) => {
              // delete platform from the list
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

  const SharedConfigurationList = () => (
    <List>
      {pipe(
        providerDetails,
        R.toEntries,
        A.map(([_, { name, value }]) => (
          <ListItem key={name}>
            <ListItemText primary={name} secondary={value} />
            <ListItemSecondaryAction>
              <TooltipIconButton
                edge="end"
                onClick={() => navigator.clipboard.writeText(value)}
                title={copyLabel}
              >
                <FileCopyIcon />
              </TooltipIconButton>
            </ListItemSecondaryAction>
          </ListItem>
        )),
      )}
    </List>
  );

  const handleOnSave = async () => {
    const deletePlatformsTask = (): TE.TaskEither<string, string[]> => {
      const deleteTask = (platformIds: string[]) =>
        pipe(
          TE.tryCatch(
            () => deletePlatformsProvider(platformIds),
            (e) => `Failed to delete platforms: ${e}`,
          ),
          TE.map(OEQ.BatchOperationResponse.groupErrorMessages),
        );

      return pipe(
        // get all ids of deleted platforms
        initialPlatforms,
        A.filter(notExistInNewPlatforms),
        A.map((p) => p.platformId),
        pfTernary(A.isNonEmpty, deleteTask, (_) => TE.right([])),
      );
    };

    const updateEnabledPlatformsTask = (): TE.TaskEither<string, string[]> => {
      const updateTask = (
        enabledStatus: OEQ.LtiPlatform.LtiPlatformEnabledStatus[],
      ) =>
        pipe(
          TE.tryCatch(
            () => updateEnabledPlatformsProvider(enabledStatus),
            (e) => `Failed to update platforms: ${e}`,
          ),
          TE.map(OEQ.BatchOperationResponse.groupErrorMessages),
        );

      return pipe(
        platforms,
        // get all changed platforms
        A.filter(notEqualWithOriginalPlatform),
        // generate enabled status request data
        A.map(({ platformId, enabled }) => ({
          platformId,
          enabled,
        })),
        pfTernary(A.isNonEmpty, updateTask, (_) => TE.right([])),
      );
    };

    // delete platforms
    await pipe(
      deletePlatformsTask(),
      TE.match(appErrorHandler, setErrorMessages),
    )();

    // update enabled status
    await pipe(
      updateEnabledPlatformsTask(),
      TE.match(appErrorHandler, (messages) =>
        // keep previous error messages in case delete platforms task is failed
        setErrorMessages([...errorMessages, ...messages]),
      ),
    )();

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
            <SettingsList subHeading={platformsTitle}>
              <ListItem>
                <ListItemText>{platformsDesc}</ListItemText>
              </ListItem>
              <List>{platformEntries()}</List>
            </SettingsList>
          </CardContent>

          <SettingsCardActions>
            <Link to={routes.CreateLti13Platform.path}>
              <IconButton aria-label={addLabel} color="primary" size="large">
                <AddCircleIcon fontSize="large" />
              </IconButton>
            </Link>
          </SettingsCardActions>
        </Card>

        <Card>
          <CardContent>
            <SettingsList subHeading={providerDetailsTitle}>
              <ListItem>
                <ListItemText>{providerDetailsDesc}</ListItemText>
              </ListItem>
              <SharedConfigurationList />
            </SettingsList>
          </CardContent>
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
