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
import FileCopyIcon from "@mui/icons-material/FileCopy";
import * as React from "react";
import { ListItem, ListItemSecondaryAction, ListItemText } from "@mui/material";
import { languageStrings } from "../util/langstrings";
import { TooltipIconButton } from "./TooltipIconButton";

const { copy: copyLabel } = languageStrings.common.action;

export interface SettingsListConfigurationProps {
  /** Short description of the configuration. */
  title: string;
  /** The configuration for users to copy. */
  value: string;
}

/**
 * This component is used to define a row where an OEQ configuration is displayed for copy.
 */
const SettingsListConfiguration = ({
  title,
  value,
}: SettingsListConfigurationProps) => (
  <ListItem>
    <ListItemText primary={title} secondary={value} />
    <ListItemSecondaryAction>
      <TooltipIconButton
        edge="end"
        onClick={() => navigator.clipboard.writeText(value)}
        title={copyLabel}
      >
        <FileCopyIcon />
      </TooltipIconButton>
    </ListItemSecondaryAction>
  </ListItem>
);

export default SettingsListConfiguration;
