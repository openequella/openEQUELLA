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
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { createPlatform } from "../../../../modules/Lti13PlatformsModule";

import { languageStrings } from "../../../../util/langstrings";
import ConfigureLti13Platform, {
  ConfigureLti13PlatformProps,
} from "./ConfigureLti13Platform";

const { name: createPageName } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage;

export interface CreateLti13PlatformProps
  extends Omit<
    ConfigureLti13PlatformProps,
    "pageName" | "configurePlatformProvider"
  > {
  /**
   * Function to create platform.
   */
  createPlatformProvider?: (
    platform: OEQ.LtiPlatform.LtiPlatform,
  ) => Promise<void>;
}

/**
 * The component is responsible for rendering the page for creating new LTI 1.3 platform configurations.
 * Upon form submission, if all validations are passed, the settings will be submitted.
 */
const CreateLti13Platform = ({
  createPlatformProvider = createPlatform,
  ...configureLti13PlatformProps
}: CreateLti13PlatformProps) => (
  <ConfigureLti13Platform
    {...configureLti13PlatformProps}
    pageName={createPageName}
    configurePlatformProvider={createPlatformProvider}
  />
);

export default CreateLti13Platform;
