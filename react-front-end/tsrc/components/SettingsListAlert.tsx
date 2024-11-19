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
import { Alert, ListItem, ListItemText } from "@mui/material";
import { AlertColor } from "@mui/material/Alert/Alert";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
import * as React from "react";
import { listItemTextStyle } from "./SettingsListControl";

export interface SettingsListWarningProps {
  /**
   * Whether there is a divider at the bottom of this message.
   */
  divider?: boolean;
  /**
   *  Warning text to appear on the left hand of the row.
   */
  messages: string[];
  /**
   * Type of the alert.
   */
  severity: AlertColor;
}

/**
 * This component is used to define a row and show the warning message inside a SettingsList
 * to be used in the page/settings/* pages.
 * It should be placed within a SettingsList.
 * If there are multiple messages shows them in multiple lines.
 */
const SettingsListAlert = ({
  divider,
  messages,
  severity,
}: SettingsListWarningProps) => {
  const warning = (
    <Alert severity={severity}>
      {/*first message, to align with the warning icon*/}
      {pipe(
        messages,
        A.head,
        O.getOrElse(() => S.empty),
      )}

      {/*show the rest of each message in new line*/}
      {pipe(
        messages,
        A.tail,
        O.map(A.map((m) => <p key={m}>{m}</p>)),
        O.getOrElseW(() => []),
      )}
    </Alert>
  );

  return (
    <ListItem
      alignItems="center"
      sx={{
        justifyContent: "space-between",
        paddingTop: 0,
        paddingBottom: 0,
      }}
      divider={divider}
    >
      {/*match the css with settingsListControl*/}
      <ListItemText primary={warning} sx={listItemTextStyle} />
    </ListItem>
  );
};

export default SettingsListAlert;
