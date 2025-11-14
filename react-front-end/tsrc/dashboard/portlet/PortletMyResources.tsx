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
import { Theme } from "@mui/material/styles";
import * as React from "react";
import { LegacyPortlet } from "./LegacyPortlet";
import type { PortletBasicProps } from "./PortletHelper";

// Generates the styles that use a ::before pseudo-element to override the legacy icon with the specified Unicode icon.
const overwriteLegacyIcon = (
  theme: Theme,
  icon: string,
  color = "secondary.main",
) => ({
  "&::before": {
    content: `"\\${icon}"`,
    fontFamily: "Material Icons",
    // The default material UI icon size.
    fontSize: "24px",
    color: color,
    paddingRight: theme.spacing(1),
    // Make it align with the text.
    verticalAlign: "sub",
  },
});

/**
 * Portlet component that displays the user's resources by different categories.
 */
export const PortletMyResources = (props: PortletBasicProps) => (
  <LegacyPortlet
    customStyles={(theme) => ({
      "& .alt-links a.folder-full": {
        // Folder icon.
        ...overwriteLegacyIcon(theme, "e2c7"),
      },
      "& .alt-links a.folder": {
        // Folder open icon.
        ...overwriteLegacyIcon(theme, "e2c8"),
      },
      "& .alt-links a.document": {
        // Article icon.
        ...overwriteLegacyIcon(theme, "ef42"),
      },
    })}
    {...props}
  />
);
