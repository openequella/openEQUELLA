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
import { Button } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";
import * as TE from "../../../../util/TaskEither.extended";
import * as React from "react";
import { useContext, useState } from "react";
import { useParams } from "react-router-dom";
import LoadingCircle from "../../../../components/LoadingCircle";
import MessageInfo from "../../../../components/MessageInfo";
import SettingsList from "../../../../components/SettingsList";
import SettingsListControl from "../../../../components/SettingsListControl";
import SimpleConfirmDialog from "../../../../components/SimpleConfirmDialog";
import { AppContext } from "../../../../mainui/App";
import { ACLEntityResolversMulti } from "../../../../modules/ACLEntityModule";
import { defaultACLEntityMultiResolvers } from "../../../../modules/ACLExpressionModule";
import { ACLRecipientTypes } from "../../../../modules/ACLRecipientModule";
import {
  getPlatform,
  updatePlatform,
  rotateKeyPair,
} from "../../../../modules/Lti13PlatformsModule";
import { languageStrings } from "../../../../util/langstrings";
import ConfigureLti13Platform, {
  ConfigureLti13PlatformProps,
  ConfigurePlatformValue,
  LtiGeneralDetails,
  WarningMessages,
} from "./ConfigureLti13Platform";
import {
  generateCustomRoles,
  getGroupsTask,
  getRolesTask,
} from "./EditLti13PlatformHelper";

const {
  name: editPageName,
  wrongURL,
  security: {
    title,
    keyPair: keyPairLabel,
    keyPairDesc,
    rotateKeyPair: rotateKeyPairLabel,
    rotateKeyPairConfirmText,
    rotateKeyPairSuccess,
  },
} = languageStrings.settings.integration.lti13PlatformsSettings.editPage;

export interface EditLti13PlatformProps
  extends Omit<
    ConfigureLti13PlatformProps,
    "pageName" | "configurePlatformProvider"
  > {
  /**
   * Function to get platform by ID.
   */
  getPlatformProvider?: (
    platformId: string,
  ) => Promise<OEQ.LtiPlatform.LtiPlatform>;
  /**
   * Function to update platform.
   */
  updatePlatformProvider?: (
    platform: OEQ.LtiPlatform.LtiPlatform,
  ) => Promise<void>;
  /**
   * Function to rotate keypair for platform.
   */
  rotateKeyPairProvider?: (platformId: string) => Promise<string>;
  /**
   * Object includes functions to find known entities by ids.
   */
  aclEntityResolversMultiProvider?: ACLEntityResolversMulti;
}

/**
 * The component is responsible for rendering the page for editing existed LTI 1.3 platform configurations.
 */
const EditLti13Platform = ({
  getPlatformProvider = getPlatform,
  updatePlatformProvider = updatePlatform,
  aclEntityResolversMultiProvider = defaultACLEntityMultiResolvers,
  rotateKeyPairProvider = rotateKeyPair,
  ...configureLti13PlatformProps
}: EditLti13PlatformProps) => {
  const { platformIdBase64 } = useParams<{
    platformIdBase64: string;
  }>();

  const { appErrorHandler } = useContext(AppContext);
  const [platform, setPlatform] = useState<
    OEQ.LtiPlatform.LtiPlatform | undefined
  >();

  const [openRotateKeyPairDialog, setOpenRotateKeyPairDialog] = useState(false);
  const [showKeyRotatedMessage, setShowKeyRotatedMessage] = useState(false);

  const [configurePlatformValue, setConfigurePlatformValue] = useState<
    ConfigurePlatformValue | undefined
  >();
  const [warningMessages, setWarningMessages] = useState<
    WarningMessages | undefined
  >();

  React.useEffect(() => {
    // decode platform ID and update state
    const pid: E.Either<string, string> = E.tryCatch(
      () => atob(platformIdBase64),
      (e) => `${wrongURL}: ${e}`,
    );

    const getPlatformTask = (
      platformId: string,
    ): TE.TaskEither<string, OEQ.LtiPlatform.LtiPlatform> =>
      TE.tryCatch<string, OEQ.LtiPlatform.LtiPlatform>(
        () => getPlatformProvider(platformId),
        (e) => `Failed to get platform: ${e}`,
      );

    // execute get platform task if platform ID is not None
    pipe(
      pid,
      TE.fromEither,
      TE.chain(getPlatformTask),
      TE.match(appErrorHandler, setPlatform),
    )();
  }, [getPlatformProvider, appErrorHandler, platformIdBase64]);

  // get role and group details from initial platform value and update the related states
  React.useEffect(() => {
    (async () => {
      if (!platform) {
        return;
      }

      // Get all general details value.
      const generalDetailsValue: LtiGeneralDetails = {
        platformId: platform.platformId,
        name: platform.name,
        clientId: platform.clientId,
        authUrl: platform.authUrl,
        keysetUrl: platform.keysetUrl,
        usernameClaim: platform.usernameClaim,
        usernamePrefix: platform.usernamePrefix,
        usernameSuffix: platform.usernameSuffix,
      };

      // initialize groups and roles details
      const unknownUserDefaultGroupsWithMsgTask = pipe(
        getGroupsTask(
          platform.unknownUserDefaultGroups ?? new Set(),
          aclEntityResolversMultiProvider.resolveGroupsProvider,
        ),
        TE.getOrThrow,
      );

      const instructorRolesWithMsgTask = pipe(
        getRolesTask(
          platform.instructorRoles,
          aclEntityResolversMultiProvider.resolveRolesProvider,
        ),
        TE.getOrThrow,
      );

      const customRolesWithMsgTask = pipe(
        generateCustomRoles(
          platform.customRoles,
          aclEntityResolversMultiProvider.resolveRolesProvider,
        ),
        TE.getOrThrow,
      );

      const unknownRolesWithMsgTask = pipe(
        getRolesTask(
          platform.unknownRoles,
          aclEntityResolversMultiProvider.resolveRolesProvider,
        ),
        TE.getOrThrow,
      );

      // execute tasks
      await Promise.all([
        unknownUserDefaultGroupsWithMsgTask(),
        instructorRolesWithMsgTask(),
        customRolesWithMsgTask(),
        unknownRolesWithMsgTask(),
      ])
        .then(
          ([
            unknownUserDefaultGroupsWithMsg,
            instructorRolesWithMsg,
            customRolesWithMsg,
            unknownRolesWithMsg,
          ]) => {
            setConfigurePlatformValue({
              generalDetails: generalDetailsValue,
              aclExpression:
                platform.allowExpression ?? ACLRecipientTypes.Everyone,
              unknownRoles: unknownRolesWithMsg.entities,
              instructorRoles: instructorRolesWithMsg.entities,
              customRoles: customRolesWithMsg.mappings,
              unknownUserHandlingData: {
                selection: platform.unknownUserHandling,
                groups: unknownUserDefaultGroupsWithMsg.entities,
              },
              enabled: platform.enabled,
            });
            setWarningMessages({
              unknownRoles: unknownRolesWithMsg.warning,
              instructorRoles: instructorRolesWithMsg.warning,
              customRolesMapping: customRolesWithMsg.warnings,
              warningMessageForGroups: unknownUserDefaultGroupsWithMsg.warning,
            });
          },
        )
        .catch((e) => {
          console.warn(e);
          appErrorHandler(e);
        });
    })();
  }, [aclEntityResolversMultiProvider, appErrorHandler, platform]);

  const handleConfirmRotateKeyPair = async () => {
    const task = pipe(
      platform?.platformId,
      TE.fromNullable(
        "Platform ID is missing in the provided LTI 1.3 Platform",
      ),
      TE.chain((id) =>
        TE.tryCatch(
          () => rotateKeyPairProvider(id),
          (e) => `Failed to rotate key pair: ${e}`,
        ),
      ),
    );

    pipe(
      await task(),
      E.foldW(appErrorHandler, () => setShowKeyRotatedMessage(true)),
      () => setOpenRotateKeyPairDialog(false),
    );
  };

  const keyRotationSection = () => (
    <>
      <SettingsList subHeading={title}>
        <SettingsListControl
          primaryText={keyPairLabel}
          secondaryText={keyPairDesc}
          control={
            <Button onClick={() => setOpenRotateKeyPairDialog(true)}>
              {rotateKeyPairLabel}
            </Button>
          }
        />
      </SettingsList>

      <SimpleConfirmDialog
        open={openRotateKeyPairDialog}
        title={rotateKeyPairConfirmText}
        onConfirm={handleConfirmRotateKeyPair}
        onCancel={() => setOpenRotateKeyPairDialog(false)}
      />

      <MessageInfo
        title={rotateKeyPairSuccess}
        open={showKeyRotatedMessage}
        onClose={() => setShowKeyRotatedMessage(false)}
        variant="success"
      />
    </>
  );

  return !configurePlatformValue ? (
    <LoadingCircle />
  ) : (
    <ConfigureLti13Platform
      {...configureLti13PlatformProps}
      pageName={editPageName}
      value={configurePlatformValue}
      configurePlatformProvider={updatePlatformProvider}
      warningMessages={warningMessages}
      KeyRotationSection={keyRotationSection()}
    />
  );
};

export default EditLti13Platform;
