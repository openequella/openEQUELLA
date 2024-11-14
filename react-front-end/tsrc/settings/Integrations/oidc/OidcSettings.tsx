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
  Card,
  CardContent,
  Divider,
  Grid,
  ListItem,
  ListItemText,
} from "@mui/material";
import { RoleDetails } from "@openequella/rest-api-client/dist/UserQuery";
import { constVoid, flow, identity, pipe } from "fp-ts/function";
import * as T from "fp-ts/Task";
import { isEqual } from "lodash";
import { useContext, useEffect, useState } from "react";
import * as React from "react";
import { getBaseUrl } from "../../../AppConfig";
import {
  CustomRolesMapping,
  transformCustomRoleMapping,
} from "../../../components/CustomRoleHelper";
import CustomRolesMappingControl from "../../../components/CustomRolesMappingControl";
import GeneralDetailsSection, {
  checkValidations,
  textFiledComponent,
} from "../../../components/GeneralDetailsSection";
import SettingPageTemplate from "../../../components/SettingPageTemplate";
import SettingsList from "../../../components/SettingsList";
import SettingsListControl from "../../../components/SettingsListControl";
import SettingsListConfiguration from "../../../components/SettingsListConfiguration";
import SettingsListAlert from "../../../components/SettingsListAlert";
import { AppContext } from "../../../mainui/App";
import { routes } from "../../../mainui/routes";
import {
  templateDefaults,
  TemplateUpdateProps,
} from "../../../mainui/Template";
import {
  getOidcSettings,
  updateOidcSettings,
} from "../../../modules/OidcModule";
import { roleIds } from "../../../modules/RoleModule";
import { languageStrings } from "../../../util/langstrings";
import * as OEQ from "@openequella/rest-api-client";
import * as TE from "fp-ts/TaskEither";
import * as RS from "fp-ts/ReadonlySet";
import * as E from "fp-ts/Either";

import SelectRoleControl from "../lti13/components/SelectRoleControl";
import {
  defaultGeneralDetails,
  defaultGenericApiDetails,
  defaultApiDetailsMap,
  generateGeneralDetails,
  generateApiDetails,
  ApiDetails,
  generatePlatform,
} from "./OidcSettingsHelper";
import * as O from "fp-ts/Option";
import * as R from "fp-ts/Record";

const {
  name,
  generalDetails: { title: generalDetailsTitle },
  apiDetails: { title: apiDetailsTitle, desc: apiDetailsDesc },
  roleMappings: {
    title: roleMappingsTitle,
    defaultRole: defaultRoleTitle,
    defaultRoleDesc,
    roleClaim: roleClaimTitle,
    roleClaimDesc,
    customRoleDialog: customRoleDialogStrings,
  },
  oeqDetails: {
    title: oeqDetailsTitle,
    desc: oeqDetailsDesc,
    redirect: redirectTitle,
  },
} = languageStrings.settings.integration.oidc;
const { edit: editLabel } = languageStrings.common.action;
const { checkForm } = languageStrings.common.result;

const redirectUrl = getBaseUrl() + "oidc/callback";

export interface OidcSettingsProps extends TemplateUpdateProps {
  /**
   * Function used to search oEQ roles.
   */
  searchRoleProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

/**
 * Show warning message for role selector related controls,
 * if the IDs of role details fetched form server doesn't match with the initial group/groups IDs.
 */
interface RoleWarningMessages {
  defaultRoles?: string[];
  customRoles?: string[];
}

const OidcSettings = ({
  updateTemplate,
  searchRoleProvider,
}: OidcSettingsProps) => {
  const { appErrorHandler } = useContext(AppContext);
  const [showSnackBar, setShowSnackBar] = useState(false);
  const [showValidationErrors, setShowValidationErrors] = useState(false);

  // States for values displayed in different sections.
  const [generalDetails, setGeneralDetails] =
    useState<OEQ.Oidc.IdentityProvider>(defaultGeneralDetails);
  const [apiDetails, setApiDetails] = useState<ApiDetails>(
    defaultGenericApiDetails,
  );
  const [defaultRoles, setDefaultRoles] = useState<ReadonlySet<RoleDetails>>(
    new Set(),
  );
  const [customRoles, setCustomRoles] = useState<CustomRolesMapping>(new Map());
  // Warning messages if the oeq roles can't be found in the server.
  const [
    { defaultRoles: defaultRolesWarnings, customRoles: customRolesWarnings },
  ] = useState<RoleWarningMessages>({
    defaultRoles: undefined,
    customRoles: undefined,
  });

  // Build final submit values for OIDC.
  const currentOidcValue = {
    ...generalDetails,
    ...apiDetails,
    defaultRoles: pipe(roleIds(defaultRoles), RS.toSet),
    roleConfig: generalDetails.roleConfig?.roleClaim
      ? {
          roleClaim: generalDetails.roleConfig.roleClaim,
          customRoles: transformCustomRoleMapping(customRoles),
        }
      : undefined,
  };

  // Initial configuration retrieved from server, but before the config is returned, use the defaults values of each state listed above.
  const [initialIdpDetails, setInitialIdpDetails] =
    useState<OEQ.Oidc.IdentityProvider>(currentOidcValue);

  const configurationChanged = !isEqual(initialIdpDetails, currentOidcValue);

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  // Fetch OIDC settings from the server.
  useEffect(() => {
    // Get the OIDC settings from the server and for the 404 error only indicates the settings are not set yet,
    // thus return None, otherwise for other error throw it.
    const getOidcSettingsFromServer = (): Promise<
      O.Option<OEQ.Oidc.IdentityProvider>
    > =>
      pipe(
        TE.tryCatch(getOidcSettings, identity),
        TE.fold(
          (error) =>
            OEQ.Errors.isApiError(error) && error.status === 404
              ? T.of(O.none)
              : T.fromIO(() => {
                  throw error;
                }),
          (result) => T.of(O.some(result)),
        ),
      )();

    pipe(
      TE.tryCatch(() => getOidcSettingsFromServer(), String),
      TE.match(
        appErrorHandler,
        flow(
          O.fold(constVoid, (idp) => {
            setInitialIdpDetails(idp);
            setGeneralDetails(idp);
            //TODO: process role mappings value to display existing settings
          }),
        ),
      ),
    )();
  }, [appErrorHandler]);

  // Update the corresponding value in generalDetails state based on the provided key.
  const onGeneralDetailsChange = (key: string, newValue: unknown) =>
    setGeneralDetails({
      ...generalDetails,
      [key]: newValue,
    });

  const onPlatformChange = (newValue: string) => {
    // update platform
    onGeneralDetailsChange("platform", newValue);
    // Also clear the previous API configurations on platform change.
    pipe(
      defaultApiDetailsMap,
      R.lookup(newValue),
      O.map(setApiDetails),
      O.getOrElseW(() => {
        appErrorHandler(`Unsupported platform ${newValue}`);
      }),
    );
  };

  const onApiDetailsChange = (key: string, newValue: unknown) =>
    setApiDetails({
      ...apiDetails,
      [key]: newValue,
    });

  const generalDetailsRenderOptions = generateGeneralDetails(
    generalDetails,
    onGeneralDetailsChange,
    showValidationErrors,
  );

  const apiDetailsRenderOptions = generateApiDetails(
    apiDetails,
    onApiDetailsChange,
    showValidationErrors,
  );

  const handleOnSave = async () => {
    setShowValidationErrors(true);

    const validateEachField = (): TE.TaskEither<string, void> =>
      pipe(
        checkValidations(
          generalDetailsRenderOptions,
          R.fromEntries(Object.entries(generalDetails)),
        ) &&
          checkValidations(
            apiDetailsRenderOptions,
            R.fromEntries(Object.entries(apiDetails)),
          )
          ? TE.right(undefined)
          : TE.left(checkForm),
      );

    const validateStructure = (): TE.TaskEither<
      string,
      OEQ.Oidc.IdentityProvider
    > =>
      pipe(
        currentOidcValue,
        E.fromPredicate(
          OEQ.Codec.Oidc.GenericIdentityProviderCodec.is,
          () => `Validation for the structure of OIDC configuration failed.`,
        ),
        TE.fromEither,
      );

    const submit = (
      oidcValue: OEQ.Oidc.IdentityProvider,
    ): TE.TaskEither<string, void> =>
      TE.tryCatch(() => updateOidcSettings(oidcValue), String);

    await pipe(
      validateEachField(),
      TE.chain(validateStructure),
      TE.chain(submit),
      TE.match(appErrorHandler, () => {
        // Reset the initial values since user has saved the settings.
        setInitialIdpDetails(currentOidcValue);
        setShowSnackBar(true);
      }),
    )();
  };

  return (
    <SettingPageTemplate
      onSave={handleOnSave}
      preventNavigation={configurationChanged}
      saveButtonDisabled={!configurationChanged}
      snackBarOnClose={() => setShowSnackBar(false)}
      snackbarOpen={showSnackBar}
    >
      <Card>
        <CardContent>
          {/* General details section. */}
          <Grid>
            <GeneralDetailsSection
              title={generalDetailsTitle}
              fields={generalDetailsRenderOptions}
            />
          </Grid>

          <Divider variant="middle" />

          {/* reuse GeneralDetailsSection to display the Platform selector and the API details section. */}
          <Grid mt={2}>
            <GeneralDetailsSection
              title={apiDetailsTitle}
              desc={apiDetailsDesc}
              fields={{
                ...generatePlatform(generalDetails.platform, onPlatformChange),
                ...apiDetailsRenderOptions,
              }}
            />
          </Grid>

          <Divider variant="middle" />

          {/* Role mappings section. */}
          <Grid mt={2}>
            <SettingsList subHeading={roleMappingsTitle}>
              <SelectRoleControl
                ariaLabel={`${editLabel} ${defaultRoleTitle}`}
                primaryText={defaultRoleTitle}
                secondaryText={defaultRoleDesc}
                value={defaultRoles}
                onChange={setDefaultRoles}
                roleListProvider={searchRoleProvider}
              />
              {defaultRolesWarnings && (
                <SettingsListAlert
                  severity="warning"
                  messages={defaultRolesWarnings}
                />
              )}

              <SettingsListControl
                primaryText={roleClaimTitle}
                secondaryText={roleClaimDesc}
                control={textFiledComponent(
                  roleClaimTitle,
                  generalDetails.roleConfig?.roleClaim,
                  false,
                  true,
                  (value) =>
                    setGeneralDetails({
                      ...generalDetails,
                      roleConfig: {
                        roleClaim: value,
                        customRoles:
                          generalDetails.roleConfig?.customRoles ?? new Map(),
                      },
                    }),
                  showValidationErrors,
                )}
              />

              {generalDetails.roleConfig?.roleClaim && (
                <>
                  <CustomRolesMappingControl
                    initialRoleMappings={customRoles}
                    onChange={setCustomRoles}
                    searchRoleProvider={searchRoleProvider}
                    strings={customRoleDialogStrings}
                  />
                  {customRolesWarnings && (
                    <SettingsListAlert
                      severity="warning"
                      messages={customRolesWarnings}
                    />
                  )}
                </>
              )}
            </SettingsList>
          </Grid>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <SettingsList subHeading={oeqDetailsTitle}>
            <ListItem>
              <ListItemText>{oeqDetailsDesc}</ListItemText>
            </ListItem>

            <SettingsListConfiguration
              title={redirectTitle}
              value={redirectUrl}
            />
          </SettingsList>
        </CardContent>
      </Card>
    </SettingPageTemplate>
  );
};

export default OidcSettings;
