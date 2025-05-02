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
import { flow, identity, pipe } from "fp-ts/function";
import * as T from "fp-ts/Task";
import { useContext, useEffect, useState, useReducer } from "react";
import * as React from "react";
import CustomRolesMappingControl from "../../../components/CustomRolesMappingControl";
import GeneralDetailsSection, {
  checkValidations,
  plainTextFiled,
} from "../../../components/GeneralDetailsSection";
import SettingPageTemplate from "../../../components/SettingPageTemplate";
import SettingsList from "../../../components/SettingsList";
import SettingsListControl from "../../../components/SettingsListControl";
import SpinnerOverlay from "../../../components/SpinnerOverlay";
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
import SelectRoleControl from "../../../components/SelectRoleControl";
import { isNonEmptyString } from "../../../util/validation";
import {
  generateGeneralDetails,
  generateApiDetails,
  generatePlatform,
  oeqDetailsList,
  defaultApiDetails,
} from "./OidcSettingsHelper";
import * as O from "fp-ts/Option";
import * as R from "fp-ts/Record";
import { initialState, reducer } from "./OidcSettingsReducer";

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

export type OidcSettingsProps = TemplateUpdateProps;

const OidcSettings = ({ updateTemplate }: OidcSettingsProps) => {
  const { appErrorHandler } = useContext(AppContext);
  const [showSnackBar, setShowSnackBar] = useState(false);
  const [showValidationErrors, setShowValidationErrors] = useState(false);

  const [state, dispatch] = useReducer(reducer, initialState);
  const { status, config, initialConfig, serverHasConfig, isConfigChanged } =
    state;

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  // Fetch OIDC settings from the server.
  useEffect(() => {
    if (status !== "initialising") return;

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
        (e) => {
          appErrorHandler(e);
          dispatch({ type: "init-complete", serverHasConfig: false });
        },
        flow(O.toUndefined, (idp) => {
          dispatch({
            type: "init-complete",
            initialConfig: idp,
            serverHasConfig: idp !== undefined,
          });
        }),
      ),
    )();
  }, [appErrorHandler, status]);

  // Update the corresponding value in idpConfigurations state based on the provided key.
  const onConfigChange = (key: string, newValue: unknown) =>
    dispatch({
      type: "configure",
      config: {
        ...config,
        [key]: newValue,
      },
    });

  const onPlatformChange = (newValue: string) =>
    pipe(
      newValue,
      O.fromPredicate(OEQ.Codec.Oidc.IdentityProviderPlatformCodec.is),
      O.fold(
        () => appErrorHandler(`Unsupported platform ${newValue}`),
        (platform) => {
          // If user switch back to the initial platform, show the initial API details.
          const apiDetails: OEQ.Oidc.RestApiDetails =
            platform === initialConfig.platform
              ? {
                  apiUrl: initialConfig.apiUrl,
                  apiClientId: initialConfig.apiClientId,
                }
              : defaultApiDetails;
          dispatch({
            type: "configure",
            config: {
              ...config,
              platform,
              ...apiDetails,
            },
          });
        },
      ),
    );

  const generalDetailsRenderOptions = generateGeneralDetails(
    config,
    onConfigChange,
    showValidationErrors,
    serverHasConfig,
  );

  const apiDetailsRenderOptions = generateApiDetails(
    config,
    onConfigChange,
    showValidationErrors,
    // OKTA platform doesn't have client secret field which means there is no secret configuration in the server.
    serverHasConfig && initialConfig.platform !== "OKTA",
  );

  useEffect(() => {
    if (status !== "saving") return;

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

    pipe(
      validateEachField(),
      TE.chain(validateStructure),
      TE.chain(submit),
      TE.match(
        (e) => {
          appErrorHandler(e);
          dispatch({
            type: "configure",
            config,
          });
        },
        () => {
          // Reset the initial values since user has saved the settings.
          dispatch({
            type: "reset",
          });
          setShowSnackBar(true);
        },
      ),
    )();
  }, [
    apiDetailsRenderOptions,
    appErrorHandler,
    config,
    generalDetailsRenderOptions,
    status,
  ]);

  return (
    <SettingPageTemplate
      onSave={() => dispatch({ type: "submit" })}
      preventNavigation={isConfigChanged}
      saveButtonDisabled={
        status === "saving" ||
        status === "initialising" ||
        (status === "configuring" && !isConfigChanged)
      }
      snackBarOnClose={() => setShowSnackBar(false)}
      snackbarOpen={showSnackBar}
    >
      <Card sx={{ position: "relative" }}>
        {/*Display a loading spinner when the page is initialising.*/}
        {(state.status === "initialising" || status === "saving") && (
          <SpinnerOverlay />
        )}
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
                  onChange: (value) => {
                    const roleConfig = isNonEmptyString(value)
                      ? {
                          roleClaim: value,
                          customRoles:
                            config.roleConfig?.customRoles ?? new Map(),
                        }
                      : undefined;

                    dispatch({
                      type: "configure",
                      config: {
                        ...config,
                        roleConfig,
                      },
                    });
                  },
                  showValidationErrors,
                })}
              />

              {pipe(
                config.roleConfig?.roleClaim,
                O.fromPredicate(isNonEmptyString),
                O.map((roleClaim) => (
                  <CustomRolesMappingControl
                    initialMappings={
                      config.roleConfig?.customRoles ?? new Map()
                    }
                    onChange={(value) =>
                      dispatch({
                        type: "configure",
                        config: {
                          ...config,
                          roleConfig: {
                            roleClaim,
                            customRoles: value,
                          },
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
