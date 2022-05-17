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
import type { DeprecatedThemeOptions } from "@mui/material";
import { createTheme, adaptV4Theme } from "@mui/material/styles";
import { getRenderData } from "../AppConfig";
import * as OEQ from "@openequella/rest-api-client";

declare const themeSettings: OEQ.Theme.ThemeSettings;

const standardThemeSettings = (): DeprecatedThemeOptions =>
  ({
    palette: {
      primary: {
        main: themeSettings.primaryColor,
      },
      secondary: {
        main: themeSettings.secondaryColor,
      },
      background: {
        default: themeSettings.backgroundColor,
        paper: themeSettings.paperColor,
      },
      text: {
        primary: themeSettings.primaryTextColor,
        secondary: themeSettings.menuTextColor,
      },
      menu: {
        text: themeSettings.menuItemTextColor,
        icon: themeSettings.menuItemIconColor,
        background: themeSettings.menuItemColor,
      },
    },
    typography: {
      useNextVariants: true,
      fontSize: themeSettings.fontSize,
    },
    // MUI 5 has changed the values of these breakpoints. To avoid any UI inconsistency,
    // explicitly define these breakpoints with the same values of MUI 4.
    breakpoints: {
      values: {
        xs: 0,
        sm: 600,
        md: 960,
        lg: 1280,
        xl: 1920,
      },
    },
  } as DeprecatedThemeOptions);

const renderData = getRenderData();

const autoTestOptions: DeprecatedThemeOptions =
  typeof renderData == "object" && renderData.autotestMode
    ? {
        transitions: {
          create: () => "none",
        },
      }
    : {};

export const getOeqTheme = () =>
  //todo: drop adaptV4Theme
  createTheme(
    adaptV4Theme({
      ...standardThemeSettings(),
      ...autoTestOptions,
    })
  );
