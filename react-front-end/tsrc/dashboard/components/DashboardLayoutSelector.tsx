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
import { Button, ButtonGroup, Tooltip } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import { SvgIconProps } from "@mui/material";
import { languageStrings } from "../../util/langstrings";
import { SingleColumnIcon } from "../../icons/SingleColumnIcon";
import { TwoColumnsEqualIcon } from "../../icons/TwoColumnsEqualIcon";
import { TwoColumnsRatio1to2Icon } from "../../icons/TwoColumnsRatio1to2Icon";
import { TwoColumnsRatio2to1Icon } from "../../icons/TwoColumnsRatio2to1Icon";

const { dashboardLayout: dashboardLayoutStrings } =
  languageStrings.dashboard.editor;

export interface DashboardLayoutSelectorProps {
  /**
   * Currently selected layout.
   */
  value?: OEQ.Dashboard.DashboardLayout;
  /**
   * Handler invoked when user selects a new layout.
   */
  onChange: (layout: OEQ.Dashboard.DashboardLayout) => void;
}

/**
 * A list of available dashboard layout options, including their text, value, and corresponding icon.
 * This is used to build the layout selector UI.
 */
const layoutOptions: {
  text: string;
  value: OEQ.Dashboard.DashboardLayout;
  icon: (props: SvgIconProps) => React.JSX.Element;
}[] = [
  {
    text: dashboardLayoutStrings.singleColumn,
    value: "SingleColumn",
    icon: SingleColumnIcon,
  },
  {
    text: dashboardLayoutStrings.twoColumnsEqual,
    value: "TwoEqualColumns",
    icon: TwoColumnsEqualIcon,
  },
  {
    text: dashboardLayoutStrings.twoColumnsRatio1to2,
    value: "TwoColumnsRatio1to2",
    icon: TwoColumnsRatio1to2Icon,
  },
  {
    text: dashboardLayoutStrings.twoColumnsRatio2to1,
    value: "TwoColumnsRatio2to1",
    icon: TwoColumnsRatio2to1Icon,
  },
];

/**
 * A group of buttons allowing the user to choose a dashboard layout.
 */
export const DashboardLayoutSelector = ({
  value,
  onChange,
}: DashboardLayoutSelectorProps) => {
  const buttons = layoutOptions.map(
    ({ text, value: layoutValue, icon: Icon }) => {
      const selected = layoutValue === value;
      return (
        <Tooltip title={text} key={layoutValue}>
          <Button
            variant={selected ? "contained" : "outlined"}
            onClick={() => onChange(layoutValue)}
            aria-checked={selected}
            aria-label={text}
          >
            <Icon />
          </Button>
        </Tooltip>
      );
    },
  );

  return <ButtonGroup color="secondary">{buttons}</ButtonGroup>;
};
