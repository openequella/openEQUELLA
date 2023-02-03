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
import { MenuItem, Select, Typography } from "@mui/material";
import * as React from "react";
import { useEffect, useState } from "react";
import { getTokens } from "../../modules/UserModule";

export interface ACLSSOMenuProps {
  /**
   * Default selected token.
   */
  value?: string;
  /**
   * Fired when the selected token is changed.
   */
  onChange: (token: string) => void;
  /**
   * Function which will provide the list of tokens (shared secrets / SSO).
   */
  getSSOTokens?: () => Promise<string[]>;
}

const ACLSSOMenu = ({
  value,
  onChange,
  getSSOTokens = getTokens,
}: ACLSSOMenuProps) => {
  const [ssoTokens, setSSOTokens] = useState<string[]>([]);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    getSSOTokens()
      .then(setSSOTokens)
      .catch((err) => setErrorMessage(`Failed to get SSO tokens: ${err}`));
  }, [getSSOTokens]);

  return errorMessage ? (
    <Typography color="error">{errorMessage}</Typography>
  ) : (
    <Select
      id="sso-select"
      displayEmpty
      defaultValue={value ?? ""}
      onChange={(event) => onChange(event.target.value)}
    >
      {ssoTokens.map((token: string) => (
        <MenuItem key={token} value={token}>
          {token}
        </MenuItem>
      ))}
    </Select>
  );
};

export default ACLSSOMenu;
