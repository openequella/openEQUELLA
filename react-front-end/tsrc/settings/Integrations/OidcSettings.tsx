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
import { Card, CardContent } from "@mui/material";
import { useEffect, useState } from "react";
import * as React from "react";
import SettingPageTemplate from "../../components/SettingPageTemplate";
import { routes } from "../../mainui/routes";
import { templateDefaults, TemplateUpdateProps } from "../../mainui/Template";
import { languageStrings } from "../../util/langstrings";

const {
  name,
  oeqDetails: { title: oeqDetailsTitle },
  idpProvider: { title: idpProviderTitle },
} = languageStrings.settings.integration.oidc;

const OidcSettings = ({ updateTemplate }: TemplateUpdateProps) => {
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  const [changesUnsaved] = useState(false);
  const handleOnSave = () => {};

  return (
    <SettingPageTemplate
      onSave={handleOnSave}
      preventNavigation={false}
      saveButtonDisabled={!changesUnsaved}
      snackBarOnClose={() => {}}
      snackbarOpen={false}
    >
      <Card>
        <CardContent>{idpProviderTitle}</CardContent>
      </Card>

      <Card>
        <CardContent>{oeqDetailsTitle}</CardContent>
      </Card>
    </SettingPageTemplate>
  );
};

export default OidcSettings;
