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
import * as OEQ from "@openequella/rest-api-client";
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";
import * as R from "fp-ts/Record";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useContext, useState } from "react";
import { useHistory } from "react-router-dom";
import { CustomRolesMappings } from "../../../../components/CustomRoleHelper";
import SettingPageTemplate from "../../../../components/SettingPageTemplate";
import { AppContext } from "../../../../mainui/App";
import { routes } from "../../../../mainui/routes";
import {
  templateDefaults,
  TemplateUpdateProps,
} from "../../../../mainui/Template";
import { defaultACLEntityResolvers } from "../../../../modules/ACLExpressionModule";
import { ACLRecipientTypes } from "../../../../modules/ACLRecipientModule";
import { languageStrings } from "../../../../util/langstrings";
import { isNonEmptyString, isValidURL } from "../../../../util/validation";
import AccessControlSection, {
  UnknownUserHandlingData,
} from "./AccessControlSection";
import GeneralDetailsSection, {
  checkValidations,
  FieldRenderOptions,
  plainTextFiled,
} from "../../../../components/GeneralDetailsSection";
import { validateUsernameClaim } from "./LtiUsernameClaimParser";
import RoleMappingsSection from "./RoleMappingsSection";
import { UsableByControlProps } from "./UsableByControl";
import * as S from "fp-ts/string";

const {
  name,
  title,
  platformId,
  clientId,
  platformAuthenticationRequestURL,
  platformKeysetURL,
  usernameClaim,
  usernameClaimDesc,
  usernamePrefix,
  usernameSuffix,
  needUrl,
} =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .generalDetails;

export type LtiGeneralDetails = Omit<
  OEQ.LtiPlatform.LtiPlatformBase,
  "kid" | "enabled" | "allowExpression" | "unknownUserHandling"
>;

/**
 * Contains value of states used in component `ConfigureLti13Platform`
 * serves to display data and allow users to modify it through the UI.
 * The end result forms an LTI platform object that is then submitted to the backend.
 */
export interface ConfigurePlatformValue {
  /**
   * An object contains the key and value of general details of the LTI platform.
   */
  generalDetails: LtiGeneralDetails;
  /**
   * The ACL Expression to control access from this platform
   */
  aclExpression: string;
  /**
   * A list of roles to be assigned to a LTI instructor role
   */
  instructorRoles: ReadonlySet<OEQ.Common.UuidString>;
  /**
   * A list of roles to be assigned to a LTI role that is neither the instructor or in the list of custom roles
   */
  unknownRoles: ReadonlySet<OEQ.Common.UuidString>;
  /**
   * Mappings from LTI roles to OEQ roles
   */
  customRoles: CustomRolesMappings;
  /**
   * The UnknownUserHandling option and list of groups to be added to the user object If the unknown user handling is CREATE
   */
  unknownUserHandlingData: UnknownUserHandlingData;
  /**
   * `true` if the platform is enabled
   */
  enabled: boolean;
}

const defaultGeneralDetails = {
  platformId: "",
  name: "",
  clientId: "",
  authUrl: "",
  keysetUrl: "",
  usernameClaim: "",
  usernamePrefix: "",
  usernameSuffix: "",
};

export const defaultConfigurePlatformValue: ConfigurePlatformValue = {
  generalDetails: defaultGeneralDetails,
  aclExpression: ACLRecipientTypes.Everyone,
  instructorRoles: new Set(),
  unknownRoles: new Set(),
  customRoles: new Map(),
  unknownUserHandlingData: {
    selection: "ERROR",
    groups: new Set(),
  },
  enabled: true,
};

export interface ConfigureLti13PlatformProps
  extends Pick<
      UsableByControlProps,
      | "searchUserProvider"
      | "searchGroupProvider"
      | "searchRoleProvider"
      | "aclEntityResolversProvider"
    >,
    TemplateUpdateProps {
  /**
   * The name of the page which show at the top of the settings list section.
   */
  pageName: string;
  /**
   * The initial value for the LTI platform which wait for editing/creating.
   */
  value?: ConfigurePlatformValue;
  /**
   * Function to configure platform, such as CREATE or EDIT.
   */
  configurePlatformProvider: (
    platform: OEQ.LtiPlatform.LtiPlatform,
  ) => Promise<void>;
  /**
   * KeyRotationSection for edit platform page.
   */
  KeyRotationSection?: React.ReactNode;
}

// An empty string should be allowed as users may put some values in and delete them later.
const validateClaim = (claim: unknown) =>
  S.isString(claim) && (S.isEmpty(claim) || validateUsernameClaim(claim));

const validateUrl = (v: unknown) => isNonEmptyString(v) && isValidURL(v);

/**
 * The component is responsible for rendering the page for creating new LTI 1.3 platform configurations.
 * Upon form submission, if all validations are passed, the settings will be submitted.
 */
const ConfigureLti13Platform = ({
  value = defaultConfigurePlatformValue,
  pageName,
  updateTemplate,
  searchUserProvider,
  searchGroupProvider,
  searchRoleProvider,
  configurePlatformProvider,
  aclEntityResolversProvider = defaultACLEntityResolvers,
  KeyRotationSection,
}: ConfigureLti13PlatformProps) => {
  const { appErrorHandler } = useContext(AppContext);
  const history = useHistory();

  // Indicates whether the page is waiting for a save request
  const [saving, setSaving] = useState<boolean>(false);
  // Indicates whether the save request is done ior not
  const [saved, setSaved] = useState(false);

  // Given that the save button on the creation page of OEQ has always been available in present UI,
  // set the disableSaveButton `false` here.
  const [saveButtonDisabled, setSaveButtonDisabled] = useState<boolean>(false);
  const [showSnackBar, setShowSnackBar] = useState<boolean>(false);
  const [preventNavigation, setPreventNavigation] = useState(true);
  const [showValidationErrors, setShowValidationErrors] = React.useState(false);

  // initialize general details value, using the default values and replacing those
  // in the retrieved 'platform' value.
  const [ltiGeneralDetails, setLtiGeneralDetails] =
    React.useState<LtiGeneralDetails>(
      pipe(
        defaultGeneralDetails,
        R.mapWithIndex((key, defaultValue) => {
          return value?.generalDetails[key] ?? defaultValue;
        }),
      ),
    );

  const [aclExpression, setAclExpression] = useState(value.aclExpression);

  const [selectedUnknownUserHandling, setSelectedUnknownUserHandling] =
    useState<UnknownUserHandlingData>(value.unknownUserHandlingData);

  const [selectedUnknownRoles, setSelectedUnknownRoles] = useState<
    ReadonlySet<OEQ.Common.UuidString>
  >(value.unknownRoles);

  const [selectedInstructorRoles, setSelectedInstructorRoles] = useState<
    ReadonlySet<OEQ.Common.UuidString>
  >(value.instructorRoles);

  const [selectedCustomRolesMapping, setSelectedCustomRolesMapping] =
    useState<CustomRolesMappings>(value.customRoles);

  // Update the corresponding value in generalDetailsRenderOptions based on the provided key.
  const onGeneralDetailsChange = (key: string, newValue: unknown) =>
    setLtiGeneralDetails({
      ...ltiGeneralDetails,
      [key]: newValue,
    });

  /**
   * Build the render options for GeneralDetailsSection.
   */
  const ltiGeneralDetailsRenderOptions: Record<string, FieldRenderOptions> = {
    platformId: {
      label: platformId,
      required: true,
      validate: isNonEmptyString,
      component: plainTextFiled({
        name: platformId,
        value: ltiGeneralDetails.platformId,
        // Disable platform ID field if there is a pre-defined value(ID can't be changed in edit page).
        disabled: !S.isEmpty(value?.generalDetails["platformId"] ?? ""),
        required: true,
        onChange: (value) => onGeneralDetailsChange("platformId", value),
        showValidationErrors,
        validate: isNonEmptyString,
      }),
    },
    name: {
      label: name,
      required: true,
      validate: isNonEmptyString,
      component: plainTextFiled({
        name: name,
        value: ltiGeneralDetails.name,
        disabled: false,
        required: true,
        onChange: (value) => onGeneralDetailsChange("name", value),
        showValidationErrors,
        validate: isNonEmptyString,
      }),
    },
    clientId: {
      label: clientId,
      required: true,
      validate: isNonEmptyString,
      component: plainTextFiled({
        name: clientId,
        value: ltiGeneralDetails.clientId,
        disabled: false,
        required: true,
        onChange: (value) => onGeneralDetailsChange("clientId", value),
        showValidationErrors,
        validate: isNonEmptyString,
      }),
    },
    authUrl: {
      label: platformAuthenticationRequestURL,
      desc: needUrl,
      required: true,
      validate: validateUrl,
      component: plainTextFiled({
        name: platformAuthenticationRequestURL,
        value: ltiGeneralDetails.authUrl,
        disabled: false,
        required: true,
        onChange: (value) => onGeneralDetailsChange("authUrl", value),
        showValidationErrors,
        validate: validateUrl,
      }),
    },
    keysetUrl: {
      label: platformKeysetURL,
      desc: needUrl,
      required: true,
      validate: validateUrl,
      component: plainTextFiled({
        name: platformKeysetURL,
        value: ltiGeneralDetails.keysetUrl,
        disabled: false,
        required: true,
        onChange: (value) => onGeneralDetailsChange("keysetUrl", value),
        showValidationErrors,
        validate: validateUrl,
      }),
    },
    usernameClaim: {
      label: usernameClaim,
      desc: usernameClaimDesc,
      required: false,
      validate: validateClaim,
      component: plainTextFiled({
        name: usernameClaim,
        value: ltiGeneralDetails.usernameClaim,
        disabled: false,
        required: true,
        onChange: (value) => onGeneralDetailsChange("usernameClaim", value),
        showValidationErrors,
        validate: validateClaim,
      }),
    },
    usernamePrefix: {
      label: usernamePrefix,
      required: false,
      component: plainTextFiled({
        name: usernamePrefix,
        value: ltiGeneralDetails.usernamePrefix,
        disabled: false,
        required: true,
        onChange: (value) => onGeneralDetailsChange("usernamePrefix", value),
        showValidationErrors,
      }),
    },
    usernameSuffix: {
      label: usernameSuffix,
      required: false,
      component: plainTextFiled({
        name: usernameSuffix,
        value: ltiGeneralDetails.usernameSuffix,
        disabled: false,
        required: true,
        onChange: (value) => onGeneralDetailsChange("usernameSuffix", value),
        showValidationErrors,
      }),
    },
  };

  React.useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(pageName)(tp),
      backRoute: routes.Lti13PlatformsSettings.path,
    }));
  }, [pageName, updateTemplate]);

  // if page is in saving mode, save the platform.
  React.useEffect(() => {
    if (!saving) return;

    setSaveButtonDisabled(true);

    // Build all values for lti platform.
    const ltiPlatformValue = {
      ...ltiGeneralDetails,
      enabled: value.enabled,
      unknownUserHandling: selectedUnknownUserHandling.selection,
      allowExpression: aclExpression,
      instructorRoles: selectedInstructorRoles,
      unknownRoles: selectedUnknownRoles,
      customRoles: selectedCustomRolesMapping,
      unknownUserDefaultGroups: selectedUnknownUserHandling.groups,
    };

    // Verify the base platform value and save it.
    const configurePlatformTask: TE.TaskEither<string, void> = pipe(
      ltiPlatformValue,
      E.fromPredicate(
        OEQ.Codec.LtiPlatform.LtiPlatformCodec.is,
        () => `Mismatched LTI platform value.`,
      ),
      TE.fromEither,
      TE.chain((validValue) =>
        TE.tryCatch(() => configurePlatformProvider(validValue), String),
      ),
    );

    (async () => {
      pipe(
        await configurePlatformTask(),
        E.match(
          (e) => {
            setSaving(false);
            setSaveButtonDisabled(false);
            appErrorHandler(e);
          },
          () => {
            setPreventNavigation(false);
            setShowSnackBar(true);
            setSaved(true);
          },
        ),
      );
    })();
  }, [
    aclExpression,
    appErrorHandler,
    configurePlatformProvider,
    ltiGeneralDetails,
    saving,
    selectedCustomRolesMapping,
    selectedInstructorRoles,
    selectedUnknownRoles,
    selectedUnknownUserHandling.groups,
    selectedUnknownUserHandling.selection,
    value.enabled,
  ]);

  // back to setting page if platform has been saved
  React.useEffect(() => {
    if (!saved) return;
    history.push(routes.Lti13PlatformsSettings.path);
  }, [history, saved]);

  const handleSubmit = async () => {
    // once user try to submit the value, the validation error can be displayed.
    setShowValidationErrors(true);
    if (checkValidations(ltiGeneralDetailsRenderOptions, ltiGeneralDetails)) {
      // change page into saving mode
      setSaving(true);
    } else {
      appErrorHandler("Please check the form for errors.");
    }
  };

  return (
    <SettingPageTemplate
      onSave={handleSubmit}
      onCancel={() => {
        history.push(routes.Lti13PlatformsSettings.path);
      }}
      saveButtonDisabled={saveButtonDisabled}
      snackBarOnClose={() => setShowSnackBar(false)}
      snackbarOpen={showSnackBar}
      preventNavigation={preventNavigation}
    >
      <Card>
        <CardContent>
          <Grid>
            <GeneralDetailsSection
              title={title}
              fields={ltiGeneralDetailsRenderOptions}
            />
          </Grid>

          <Divider variant="middle" />

          <Grid mt={2}>
            <AccessControlSection
              aclExpression={aclExpression}
              setAclExpression={setAclExpression}
              unknownUserHandling={selectedUnknownUserHandling}
              setUnknownUserHandling={setSelectedUnknownUserHandling}
              searchUserProvider={searchUserProvider}
              searchGroupProvider={searchGroupProvider}
              searchRoleProvider={searchRoleProvider}
              aclEntityResolversProvider={aclEntityResolversProvider}
              showValidationErrors={showValidationErrors}
            />
          </Grid>

          <Divider variant="middle" />

          <Grid mt={2}>
            <RoleMappingsSection
              instructorRoles={selectedInstructorRoles}
              setInstructorRoles={setSelectedInstructorRoles}
              customRolesMapping={selectedCustomRolesMapping}
              setCustomRolesMapping={setSelectedCustomRolesMapping}
              unknownRoles={selectedUnknownRoles}
              setUnknownRoles={setSelectedUnknownRoles}
            />
          </Grid>
          {KeyRotationSection && (
            <>
              <Divider variant="middle" />

              <Grid mt={2}>{KeyRotationSection}</Grid>
            </>
          )}
        </CardContent>
      </Card>
    </SettingPageTemplate>
  );
};

export default ConfigureLti13Platform;
