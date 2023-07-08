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
import * as OEQ from "@openequella/rest-api-client";
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as R from "fp-ts/Record";
import * as RS from "fp-ts/ReadonlySet";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useContext, useState } from "react";
import { useParams } from "react-router-dom";
import LoadingCircle from "../../../components/LoadingCircle";
import { AppContext } from "../../../mainui/App";
import { ACLEntityResolversMulti } from "../../../modules/ACLEntityModule";
import { defaultACLEntityMultiResolvers } from "../../../modules/ACLExpressionModule";
import { ACLRecipientTypes } from "../../../modules/ACLRecipientModule";
import {
  getPlatform,
  updatePlatform,
} from "../../../modules/Lti13PlatformsModule";
import { languageStrings } from "../../../util/langstrings";
import ConfigureLti13Platform, {
  ConfigureLti13PlatformProps,
  ConfigurePlatformValue,
} from "./ConfigureLti13Platform";
import {
  generateCustomRoles,
  getGroupsTask,
  getRolesTask,
} from "./EditLti13PlatformHelper";
import { generalDetailsDefaultRenderOption } from "./GeneralDetailsSection";

const { name: editPageName, wrongURL } =
  languageStrings.settings.integration.lti13PlatformsSettings.editPage;

export interface EditLti13PlatformProps
  extends Omit<
    ConfigureLti13PlatformProps,
    "pageName" | "configurePlatformProvider"
  > {
  /**
   * Function to get platform by ID.
   */
  getPlatformProvider?: (
    platformId: string
  ) => Promise<OEQ.LtiPlatform.LtiPlatform>;
  /**
   * Function to update platform.
   */
  updatePlatformProvider?: (
    platform: OEQ.LtiPlatform.LtiPlatform
  ) => Promise<void>;
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
  ...configureLti13PlatformProps
}: EditLti13PlatformProps) => {
  const { platformIdBase64 } = useParams<{
    platformIdBase64: string;
  }>();

  const { appErrorHandler } = useContext(AppContext);
  const [platform, setPlatform] = useState<
    OEQ.LtiPlatform.LtiPlatform | undefined
  >();

  const [configurePlatformValue, setConfigurePlatformValue] = useState<
    ConfigurePlatformValue | undefined
  >();

  React.useEffect(() => {
    // decode platform ID
    const pid: O.Option<string> = pipe(
      E.tryCatch(
        () => atob(platformIdBase64),
        (e) => appErrorHandler(`${wrongURL}: ${e}`)
      ),
      O.fromEither
    );

    const getPlatformTask = (platformId: string): T.Task<void> =>
      pipe(
        TE.tryCatch<string, OEQ.LtiPlatform.LtiPlatform>(
          () => getPlatformProvider(platformId),
          String
        ),
        TE.match(appErrorHandler, setPlatform)
      );

    (async () => {
      // execute get platform task if platform ID is not None
      await pipe(
        pid,
        O.map(getPlatformTask),
        O.getOrElseW(() => T.of(undefined))
      )();
    })();
  }, [getPlatformProvider, appErrorHandler, platformIdBase64]);

  // get role and group details from initial platform value and update the related states
  React.useEffect(() => {
    (async () => {
      if (!platform) {
        return;
      }
      // initialize groups and roles details
      const unknownUserDefaultGroups: ReadonlySet<OEQ.UserQuery.GroupDetails> =
        pipe(
          await getGroupsTask(
            platform.unknownUserDefaultGroups ?? new Set(),
            aclEntityResolversMultiProvider.resolveGroupsProvider
          )(),
          E.getOrElseW((e) => {
            console.warn(e);
            return RS.empty;
          })
        );

      const instructorRoles: ReadonlySet<OEQ.UserQuery.RoleDetails> = pipe(
        await getRolesTask(
          platform.instructorRoles,
          aclEntityResolversMultiProvider.resolveRolesProvider
        )(),
        E.getOrElseW((e) => {
          console.warn(e);
          return RS.empty;
        })
      );

      const customRoles = await generateCustomRoles(
        platform.customRoles,
        aclEntityResolversMultiProvider.resolveRolesProvider
      )();

      const unknownRoles: ReadonlySet<OEQ.UserQuery.RoleDetails> = pipe(
        await getRolesTask(
          platform.unknownRoles,
          aclEntityResolversMultiProvider.resolveRolesProvider
        )(),
        E.getOrElseW((e) => {
          console.warn(e);
          return RS.empty;
        })
      );

      // initialize general details render options, using the default values and replacing those
      // in the retrieved 'platform'.
      const generalDetailsRenderOptions = pipe(
        generalDetailsDefaultRenderOption,
        R.mapWithIndex((key, data) => ({
          ...data,
          value: platform[key] ?? "",
          // disable platform ID field since ID can't be changed in edit page
          disabled: key === "platformId",
        }))
      );

      setConfigurePlatformValue({
        generalDetailsRenderOptions,
        aclExpression: platform.allowExpression ?? ACLRecipientTypes.Everyone,
        unknownRoles,
        instructorRoles,
        customRoles,
        unknownUserHandlingData: {
          selection: platform.unknownUserHandling,
          groups: unknownUserDefaultGroups,
        },
        enabled: platform.enabled,
      });
    })();
  }, [aclEntityResolversMultiProvider, platform]);

  return !configurePlatformValue ? (
    <LoadingCircle />
  ) : (
    <ConfigureLti13Platform
      {...configureLti13PlatformProps}
      pageName={editPageName}
      value={configurePlatformValue}
      configurePlatformProvider={updatePlatformProvider}
    />
  );
};

export default EditLti13Platform;
