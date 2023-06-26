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
import { createPlatforms } from "../../../modules/Lti13PlatformsModule";
import SettingPageTemplate from "../../../components/SettingPageTemplate";
import { AppContext } from "../../../mainui/App";
import { routes } from "../../../mainui/routes";
import {
  templateDefaults,
  TemplateUpdateProps,
} from "../../../mainui/Template";
import { ACLRecipientTypes } from "../../../modules/ACLRecipientModule";
import { groupIds } from "../../../modules/GroupModule";
import { roleIds } from "../../../modules/RoleModule";
import { languageStrings } from "../../../util/langstrings";
import AccessControlSection, {
  UnknownUserHandlingData,
} from "./AccessControlSection";
import GeneralDetailsSection, {
  generalDetailsDefaultRenderOption,
  GeneralDetailsSectionRenderOptions,
} from "./GeneralDetailsSection";
import RoleMappingsSection from "./RoleMappingsSection";
import { UsableByControlProps } from "./UsableByControl";

const { name: pageName } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage;

export interface CreateLti13PlatformProps
  extends Pick<
      UsableByControlProps,
      | "searchUserProvider"
      | "searchGroupProvider"
      | "searchRoleProvider"
      | "aclEntityResolversProvider"
    >,
    TemplateUpdateProps {
  /**
   * Function to create platform.
   */
  createPlatformsProvider?: (
    platform: OEQ.LtiPlatform.LtiPlatform
  ) => Promise<void>;
}

/**
 * The component is responsible for rendering the page for creating new LTI 1.3 platform configurations.
 * Upon form submission, if all validations are passed, the settings will be submitted.
 */
const CreateLti13Platform = ({
  updateTemplate,
  searchUserProvider,
  searchGroupProvider,
  searchRoleProvider,
  aclEntityResolversProvider,
  createPlatformsProvider = createPlatforms,
}: CreateLti13PlatformProps) => {
  const { appErrorHandler } = useContext(AppContext);
  const history = useHistory();

  const [showSnackBar, setShowSnackBar] = useState<boolean>(false);
  const [preventNavigation, setPreventNavigation] = useState(true);
  const [showValidationErrors, setShowValidationErrors] = React.useState(false);
  const [generalDetailsRenderOptions, setGeneralDetailsRenderOptions] =
    React.useState<GeneralDetailsSectionRenderOptions>(
      generalDetailsDefaultRenderOption
    );

  const [aclExpression, setAclExpression] = useState(
    ACLRecipientTypes.Everyone
  );

  const [selectedUnknownUserHandling, setSelectedUnknownUserHandling] =
    useState<UnknownUserHandlingData>({
      selection: "ERROR",
      groups: RS.empty,
    });

  const [selectedUnknownRoles, setSelectedUnknownRoles] = useState<
    ReadonlySet<OEQ.UserQuery.RoleDetails>
  >(RS.empty);

  const [selectedInstructorRoles, setSelectedInstructorRoles] = useState<
    ReadonlySet<OEQ.UserQuery.RoleDetails>
  >(RS.empty);

  const [selectedCustomRolesMapping, setSelectedCustomRolesMapping] = useState<
    Map<string, Set<OEQ.UserQuery.RoleDetails>>
  >(new Map());

  React.useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(pageName)(tp),
      backRoute: routes.Lti13PlatformsSettings.path,
    }));
  }, [updateTemplate]);

  // check if all input value are valid
  const checkValidation = (): boolean =>
    pipe(
      generalDetailsRenderOptions,
      R.toEntries,
      A.every(([_, data]) => (data.validate ? data.validate(data.value) : true))
    );

  const handleSubmit = async () => {
    // once uses try to submit the value, the validation error can be displayed.
    setShowValidationErrors(true);
    if (!checkValidation()) {
      return;
    }

    const generalDetailsValue = pipe(
      generalDetailsRenderOptions,
      R.map((r) => r.value)
    );

    // generate request data to create platform
    const platformValue: OEQ.LtiPlatform.LtiPlatform = {
      ...generalDetailsValue,
      unknownUserHandling: selectedUnknownUserHandling.selection,
      allowExpression: aclExpression,
      instructorRoles: pipe(roleIds(selectedInstructorRoles), RS.toSet),
      unknownRoles: pipe(roleIds(selectedUnknownRoles), RS.toSet),
      customRoles: pipe(
        selectedCustomRolesMapping,
        M.map(flow(RS.fromSet, roleIds, RS.toSet))
      ),
      unknownUserDefaultGroups: pipe(
        groupIds(selectedUnknownUserHandling.groups),
        RS.toSet
      ),
      enabled: true,
    };

    const createPlatformTask: TE.TaskEither<string, void> = TE.tryCatch(
      () => createPlatformsProvider(platformValue),
      (e) => `${e}`
    );

    setPreventNavigation(false);

    pipe(
      await createPlatformTask(),
      E.match(
        (e) => {
          setPreventNavigation(true);
          appErrorHandler(e);
        },
        () => {
          setShowSnackBar(true);
          history.push(routes.Lti13PlatformsSettings.path);
        }
      )
    );
  };

  return (
    <SettingPageTemplate
      onSave={handleSubmit}
      // Given that the save button on the creation page of OEQ has always been available in present UI,
      // keep the disableSaveButton `false` here.
      saveButtonDisabled={false}
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
            />
          </Grid>
        </CardContent>
      </Card>
    </SettingPageTemplate>
  );
};

export default CreateLti13Platform;
