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
import { Card, CardContent, Divider, Grid } from "@mui/material";
import { constVoid, flow, identity, pipe } from "fp-ts/function";
import * as T from "fp-ts/Task";
import { isEqual } from "lodash";
import { useContext, useEffect, useState } from "react";
import * as React from "react";
import CustomRolesMappingControl from "../../../components/CustomRolesMappingControl";
import GeneralDetailsSection, {
  checkValidations,
  plainTextFiled,
} from "../../../components/GeneralDetailsSection";
import SettingPageTemplate from "../../../components/SettingPageTemplate";
import SettingsList from "../../../components/SettingsList";
import SettingsListControl from "../../../components/SettingsListControl";
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
import { languageStrings } from "../../../util/langstrings";
import * as OEQ from "@openequella/rest-api-client";
import * as TE from "../../../util/TaskEither.extended";
import * as E from "fp-ts/Either";
import * as S from "fp-ts/string";
import SelectRoleControl from "../../../components/SelectRoleControl";
import {
  defaultConfig,
  generateGeneralDetails,
  generateApiDetails,
  generatePlatform,
  oeqDetailsList,
  defaultApiDetails,
} from "./OidcSettingsHelper";
import * as O from "fp-ts/Option";
import * as R from "fp-ts/Record";
import * as A from "fp-ts/Array";

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
} = languageStrings.settings.integration.oidc;
const { edit: editLabel } = languageStrings.common.action;
const { checkForm } = languageStrings.common.result;

// Compare the initial and current details to see if the configuration has changed.
// In order to handle the secret field,
// It will consider the configuration is not changed if the current value is an empty string and the initial value is missing.
const hasConfigurationChanged = (
  initialDetails: OEQ.Oidc.IdentityProvider,
  currentDetails: OEQ.Oidc.IdentityProvider,
): boolean => {
  // Helper function to check if a field has changed compared to the initial value.
  const hasKeyChanged = (key: string, currentValue: unknown): boolean =>
    pipe(
      Object.entries(initialDetails),
      R.fromEntries,
      R.lookup(key),
      O.fold(
        // If the key is missing in initialDetails, consider it changed if currentValue is a non-empty string.
        // For secret values which are absent in the initial details, if the current value is a non-empty string, it's considered changed;
        // For other values do a normal comparison.
        // Because there is a case user might input some value and then delete it, then the value will become empty string.
        () => S.isString(currentValue) && !S.isEmpty(currentValue),
        // If the key is present in initialDetails, check for equality
        (initialValue) => !isEqual(initialValue, currentValue),
      ),
    );

  return pipe(
    Object.entries(currentDetails),
    // Check if any key-value pair in currentDetails indicates a change
    A.some(([key, currentValue]) => hasKeyChanged(key, currentValue)),
  );
};

export interface OidcSettingsProps extends TemplateUpdateProps {}

const OidcSettings = ({ updateTemplate }: OidcSettingsProps) => {
  const { appErrorHandler } = useContext(AppContext);
  const [showSnackBar, setShowSnackBar] = useState(false);
  const [showValidationErrors, setShowValidationErrors] = useState(false);

  // States for values displayed in different sections.
  const [config, setConfig] =
    useState<OEQ.Oidc.IdentityProvider>(defaultConfig);

  // Initial configuration retrieved from server, but before the config is returned, use the defaults values of "config".
  const [initialConfig, setInitialConfig] =
    useState<OEQ.Oidc.IdentityProvider>(config);

  // Flag to indicate if there is an existing configuration in server.
  const [serverHasConfiguration, setServerHasConfiguration] = useState(false);

  const configurationChanged = hasConfigurationChanged(initialConfig, config);

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
            setServerHasConfiguration(true);
            setInitialConfig(idp);
            setConfig(idp);
          }),
        ),
      ),
    )();
  }, [appErrorHandler]);

  // Update the corresponding value in idpConfigurations state based on the provided key.
  const onConfigChange = (key: string, newValue: unknown) =>
    setConfig({
      ...config,
      [key]: newValue,
    });

  const onPlatformChange = (newValue: string) => {
    if (!OEQ.Codec.Oidc.IdentityProviderPlatformCodec.is(newValue)) {
      throw new Error(`Unsupported platform ${newValue}`);
    }

    // Update platform and reset the API details.
    setConfig({
      ...config,
      platform: newValue,
      ...defaultApiDetails,
    });
  };

  const generalDetailsRenderOptions = generateGeneralDetails(
    config,
    onConfigChange,
    showValidationErrors,
    serverHasConfiguration,
  );

  const apiDetailsRenderOptions = generateApiDetails(
    config,
    onConfigChange,
    showValidationErrors,
    // OKTA platform doesn't have client secret field which means there is no secret configuration in the server.
    serverHasConfiguration && initialConfig.platform !== "OKTA",
  );

  const handleOnSave = async () => {
    setShowValidationErrors(true);

    const validateEachField = (): TE.TaskEither<string, void> =>
      pipe(
        checkValidations(
          generalDetailsRenderOptions,
          R.fromEntries(Object.entries(config)),
        ) &&
          checkValidations(
            apiDetailsRenderOptions,
            R.fromEntries(Object.entries(config)),
          )
          ? TE.right(undefined)
          : TE.left(checkForm),
      );

    const validateStructure = (): TE.TaskEither<
      string,
      OEQ.Oidc.IdentityProvider
    > =>
      pipe(
        config,
        E.fromPredicate(
          OEQ.Codec.Oidc.IdentityProviderCodec.is,
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
        setInitialConfig(config);
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
                ...generatePlatform(config.platform, onPlatformChange),
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
                value={config.defaultRoles}
                onChange={(role) => onConfigChange("defaultRoles", role)}
              />

              <SettingsListControl
                primaryText={roleClaimTitle}
                secondaryText={roleClaimDesc}
                control={plainTextFiled({
                  name: roleClaimTitle,
                  value: config.roleConfig?.roleClaim,
                  disabled: false,
                  required: true,
                  onChange: (value) =>
                    setConfig({
                      ...config,
                      roleConfig: {
                        roleClaim: value,
                        customRoles:
                          config.roleConfig?.customRoles ?? new Map(),
                      },
                    }),
                  showValidationErrors,
                })}
              />

              {pipe(
                O.fromNullable(config.roleConfig?.roleClaim),
                O.map((roleClaim) => (
                  <CustomRolesMappingControl
                    initialMappings={
                      config.roleConfig?.customRoles ?? new Map()
                    }
                    onChange={(value) =>
                      setConfig({
                        ...config,
                        roleConfig: {
                          roleClaim,
                          customRoles: value,
                        },
                      })
                    }
                    strings={customRoleDialogStrings}
                  />
                )),
                O.toUndefined,
              )}
            </SettingsList>
          </Grid>
        </CardContent>
      </Card>

      <Card>
        <CardContent>{oeqDetailsList}</CardContent>
      </Card>
    </SettingPageTemplate>
  );
};

export default OidcSettings;
