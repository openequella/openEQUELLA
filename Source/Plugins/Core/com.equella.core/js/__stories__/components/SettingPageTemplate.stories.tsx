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
import * as React from "react";
import { action } from "@storybook/addon-actions";
import SettingPageTemplate from "../../tsrc/components/SettingPageTemplate";
import { Card } from "@material-ui/core";
import { boolean } from "@storybook/addon-knobs";

export default {
  title: "SettingPageTemplate",
  component: SettingPageTemplate,
};

const children = (
  <Card>
    <h3>SettingPageTemplate Story</h3>
  </Card>
);

export const saveButtonEnabled = () => (
  <SettingPageTemplate
    onSave={action("save")}
    saveButtonDisabled={boolean("disable Save butto", false)}
    snackbarOpen={boolean("open snack bar", false)}
    preventNavigation={boolean("prevent navigation", true)}
    snackBarOnClose={action("Close snackbar")}
  >
    {children}
  </SettingPageTemplate>
);
