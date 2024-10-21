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
import { useEffect, useState } from "react";
import * as React from "react";
import GeneralDetailsSection from "../../components/GeneralDetailsSection";
import SettingPageTemplate from "../../components/SettingPageTemplate";
import { routes } from "../../mainui/routes";
import { templateDefaults, TemplateUpdateProps } from "../../mainui/Template";
import { languageStrings } from "../../util/langstrings";
import * as OEQ from "@openequella/rest-api-client";
import {
  defaultIdpGeneralDetails,
  generalDetailsRenderOptions,
} from "./Oidc/OidcSettingsHelper";

const {
  name,
  oeqDetails: { title: oeqDetailsTitle },
  generalDetails: { title: generalDetailsTitle },
} = languageStrings.settings.integration.oidc;

const OidcSettings = ({ updateTemplate }: TemplateUpdateProps) => {
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  const handleOnSave = () => {};

  // Given that the save button on the creation page of OEQ has always been available in present UI,
  // set the disableSaveButton `false` here.
  const [saveButtonDisabled] = useState(false);
  const [showSnackBar, setShowSnackBar] = useState(false);
  const [preventNavigation] = useState(true);

  const [showValidationErrors] = useState(false);

  const [idpDetails, setIdpDetails] = React.useState<OEQ.Oidc.IdentityProvider>(
    defaultIdpGeneralDetails,
  );

  // Update the corresponding value in generalDetailsRenderOptions based on the provided key.
  const onIdpGeneralDetailsChange = (key: string, newValue: unknown) =>
    setIdpDetails({
      ...idpDetails,
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
          <Grid>
            <GeneralDetailsSection
              title={generalDetailsTitle}
              fields={generalDetailsRenderOptions(
                idpDetails,
                onIdpGeneralDetailsChange,
                showValidationErrors,
              )}
            />
          </Grid>

          <Divider variant="middle" />

          <Grid mt={2}>Role mappings</Grid>
        </CardContent>
      </Card>

      <Card>
        <CardContent>{oeqDetailsTitle}</CardContent>
      </Card>
    </SettingPageTemplate>
  );
};

export default OidcSettings;
