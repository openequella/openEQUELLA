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
import { pipe } from "fp-ts/function";
import { useContext, useEffect, useState } from "react";
import * as React from "react";
import { getBaseUrl } from "../../../AppConfig";
import { CustomRolesMapping } from "../../../components/CustomRoleHelper";
import CustomRolesMappingControl from "../../../components/CustomRolesMappingControl";
import GeneralDetailsSection, {
  textFiledComponent,
} from "../../../components/GeneralDetailsSection";
import SettingPageTemplate from "../../../components/SettingPageTemplate";
import SettingsList from "../../../components/SettingsList";
import SettingsListControl from "../../../components/SettingsListControl";
import SettingsListConfiguration from "../../../components/SettingsListConfiguration";
import SettingsListWarning from "../../../components/SettingsListWarning";
import { AppContext } from "../../../mainui/App";
import { routes } from "../../../mainui/routes";
import {
  templateDefaults,
  TemplateUpdateProps,
} from "../../../mainui/Template";
import { languageStrings } from "../../../util/langstrings";
import * as OEQ from "@openequella/rest-api-client";
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
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  const { appErrorHandler } = useContext(AppContext);
  const handleOnSave = () => {};

  // Given that the save button on the creation page of OEQ has always been available in present UI,
  // set the disableSaveButton `false` here.
  const [saveButtonDisabled] = useState(false);
  const [showSnackBar, setShowSnackBar] = useState(false);
  const [preventNavigation] = useState(true);

  const [generalDetails, setGeneralDetails] =
    useState<OEQ.Oidc.IdentityProvider>(defaultGeneralDetails);

  const [apiDetails, setApiDetails] = useState<ApiDetails>(
    defaultGenericApiDetails,
  );

  const [showValidationErrors] = useState(false);

  // Warning messages if the oeq roles can't be found in the server.
  const [
    { defaultRoles: defaultRolesWarnings, customRoles: customRolesWarnings },
  ] = useState<RoleWarningMessages>({
    defaultRoles: undefined,
    customRoles: undefined,
  });
  const [defaultRoles, setDefaultRoles] = useState<ReadonlySet<RoleDetails>>(
    new Set(),
  );
  const [customRoles, setCustomRoles] = useState<CustomRolesMapping>(new Map());

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

  return (
    <SettingPageTemplate
      onSave={handleOnSave}
      preventNavigation={preventNavigation}
      saveButtonDisabled={saveButtonDisabled}
      snackBarOnClose={() => setShowSnackBar(false)}
      snackbarOpen={showSnackBar}
    >
      <Card>
        <CardContent>
          {/* General details section. */}
          <Grid>
            <GeneralDetailsSection
              title={generalDetailsTitle}
              fields={generateGeneralDetails(
                generalDetails,
                onGeneralDetailsChange,
                showValidationErrors,
              )}
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
                ...generateApiDetails(
                  apiDetails,
                  onApiDetailsChange,
                  showValidationErrors,
                ),
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
                <SettingsListWarning messages={defaultRolesWarnings} />
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
                    <SettingsListWarning messages={customRolesWarnings} />
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
