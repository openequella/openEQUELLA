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
import type { Theme, ThemeOptions } from "@mui/material";
import { createTheme } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import { getRenderData } from "../AppConfig";

declare const themeSettings: OEQ.Theme.ThemeSettings;

/**
 * Extended Theme object for oEQ to capture additional areas of theming - such as the menu.
 */
interface ExtTheme extends Theme {
  menu: {
    background: string;
    text: string;
    icon: string;
  };
}

const standardThemeSettings = (): ThemeOptions => ({
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
  },
  typography: {
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
});

const renderData = getRenderData();

const autoTestOptions: ThemeOptions =
  typeof renderData == "object" && renderData.autotestMode
    ? {
        transitions: {
          create: () => "none",
        },
      }
    : {};

export const getOeqTheme = (): ExtTheme => ({
  ...createTheme({
    ...standardThemeSettings(),
    ...autoTestOptions,
  }),
  menu: {
    text: themeSettings.menuItemTextColor,
    icon: themeSettings.menuItemIconColor,
    background: themeSettings.menuItemColor,
  },
});
