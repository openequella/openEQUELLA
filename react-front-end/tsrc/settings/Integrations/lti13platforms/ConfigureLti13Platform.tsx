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
import * as A from "fp-ts/Array";
import * as E from "fp-ts/Either";
import { flow, pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as RS from "fp-ts/ReadonlySet";
import * as R from "fp-ts/Record";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useContext, useState } from "react";
import { useHistory } from "react-router-dom";
import SettingPageTemplate from "../../../components/SettingPageTemplate";
import { AppContext } from "../../../mainui/App";
import { routes } from "../../../mainui/routes";
import {
  templateDefaults,
  TemplateUpdateProps,
} from "../../../mainui/Template";
import { defaultACLEntityResolvers } from "../../../modules/ACLExpressionModule";
import { ACLRecipientTypes } from "../../../modules/ACLRecipientModule";
import { groupIds } from "../../../modules/GroupModule";
import { roleIds } from "../../../modules/RoleModule";
import AccessControlSection, {
  checkAclExpressionLength,
  UnknownUserHandlingData,
} from "./AccessControlSection";
import GeneralDetailsSection, {
  generalDetailsDefaultRenderOption,
  GeneralDetailsSectionRenderOptions,
} from "./GeneralDetailsSection";
import RoleMappingsSection, {
  RoleMappingWarnings,
} from "./RoleMappingsSection";
import { GroupWarning } from "./UnknownUserHandlingControl";
import { UsableByControlProps } from "./UsableByControl";

/**
 * Warning messages for group and role selector related controls in ConfigureLti13Platform component.
 */
export type WarningMessages = RoleMappingWarnings & GroupWarning;

/**
 * Contains value of states used in component `ConfigureLti13Platform`
 * serves to display data and allow users to modify it through the UI.
 * The end result forms an LTI platform object that is then submitted to the backend.
 */
export interface ConfigurePlatformValue {
  /**
   * Render options for each text input in general details sections.
   */
  generalDetailsRenderOptions: GeneralDetailsSectionRenderOptions;
  /**
   * The ACL Expression to control access from this platform
   */
  aclExpression: string;
  /**
   * A list of roles to be assigned to a LTI instructor role
   */
  instructorRoles: ReadonlySet<OEQ.UserQuery.RoleDetails>;
  /**
   * A list of roles to be assigned to a LTI role that is neither the instructor or in the list of custom roles
   */
  unknownRoles: ReadonlySet<OEQ.UserQuery.RoleDetails>;
  /**
   * Mappings from LTI roles to OEQ roles
   */
  customRoles: Map<string, Set<OEQ.UserQuery.RoleDetails>>;
  /**
   * The UnknownUserHandling option and list of groups to be added to the user object If the unknown user handling is CREATE
   */
  unknownUserHandlingData: UnknownUserHandlingData;
  /**
   * `true` if the platform is enabled
   */
  enabled: boolean;
}

export const defaultConfigurePlatformValue: ConfigurePlatformValue = {
  generalDetailsRenderOptions: generalDetailsDefaultRenderOption,
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
   * Show warning message for group and role selector related controls
   * if the IDs of group/role details fetched form server
   * doesn't match with the initial group/groups IDs.
   *
   * For example:
   * suppose users select Role A and Role B for UnknownRoles. Later, if Role A gets deleted,
   * its ID will still be stored in the platform.
   * When the Edit page tries to get Role A and B, the server will only return Role B.
   * Consequently, a warning message will be displayed stating that Role A is missing.
   */
  warningMessages?: WarningMessages;
  /**
   * KeyRotationSection for edit platform page.
   */
  KeyRotationSection?: React.ReactNode;
}

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
  warningMessages,
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

  const [generalDetailsRenderOptions, setGeneralDetailsRenderOptions] =
    React.useState<GeneralDetailsSectionRenderOptions>(
      value.generalDetailsRenderOptions,
    );

  const [aclExpression, setAclExpression] = useState(value.aclExpression);

  const [selectedUnknownUserHandling, setSelectedUnknownUserHandling] =
    useState<UnknownUserHandlingData>(value.unknownUserHandlingData);

  const [selectedUnknownRoles, setSelectedUnknownRoles] = useState<
    ReadonlySet<OEQ.UserQuery.RoleDetails>
  >(value.unknownRoles);

  const [selectedInstructorRoles, setSelectedInstructorRoles] = useState<
    ReadonlySet<OEQ.UserQuery.RoleDetails>
  >(value.instructorRoles);

  const [selectedCustomRolesMapping, setSelectedCustomRolesMapping] = useState<
    Map<string, Set<OEQ.UserQuery.RoleDetails>>
  >(value.customRoles);

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

    const generalDetailsValue = pipe(
      generalDetailsRenderOptions,
      R.map((r) => r.value),
    );

    // generate request data to create/update platform
    const platformValue: OEQ.LtiPlatform.LtiPlatform = {
      ...generalDetailsValue,
      unknownUserHandling: selectedUnknownUserHandling.selection,
      allowExpression: aclExpression,
      instructorRoles: pipe(roleIds(selectedInstructorRoles), RS.toSet),
      unknownRoles: pipe(roleIds(selectedUnknownRoles), RS.toSet),
      customRoles: pipe(
        selectedCustomRolesMapping,
        M.map(flow(RS.fromSet, roleIds, RS.toSet)),
      ),
      unknownUserDefaultGroups: pipe(
        groupIds(selectedUnknownUserHandling.groups),
        RS.toSet,
      ),
      enabled: value.enabled,
    };

    const configurePlatformTask: TE.TaskEither<string, void> = TE.tryCatch(
      () => configurePlatformProvider(platformValue),
      String,
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
    generalDetailsRenderOptions,
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

  // check if all input value are valid
  const checkGeneralDetailsValidation = (): boolean =>
    pipe(
      generalDetailsRenderOptions,
      R.toEntries,
      A.every(([_, data]) =>
        data.validate ? data.validate(data.value) : true,
      ),
    );

  const handleSubmit = async () => {
    // once user try to submit the value, the validation error can be displayed.
    setShowValidationErrors(true);
    if (
      checkGeneralDetailsValidation() &&
      checkAclExpressionLength(aclExpression)
    ) {
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
          <Grid mt={2}>
            <GeneralDetailsSection
              showValidationErrors={showValidationErrors}
              value={generalDetailsRenderOptions}
              onChange={setGeneralDetailsRenderOptions}
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
              warningMessageForGroups={warningMessages?.warningMessageForGroups}
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
              searchRoleProvider={searchRoleProvider}
              warningMessages={warningMessages}
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
