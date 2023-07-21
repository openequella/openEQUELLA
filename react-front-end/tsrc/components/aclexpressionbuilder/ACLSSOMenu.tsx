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
import { Alert, FormControl, MenuItem, Select } from "@mui/material";
import * as S from "fp-ts/string";
import * as NA from "fp-ts/NonEmptyArray";
import { flow, pipe } from "fp-ts/function";
import * as E from "fp-ts/Either";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useEffect, useState } from "react";
import { getTokens } from "../../modules/UserModule";
import { languageStrings } from "../../util/langstrings";
import { pfTernary } from "../../util/pointfree";
import LoadingCircle from "../LoadingCircle";

const { ssoTokensFailed, ssoTokensNotFound } =
  languageStrings.aclExpressionBuilder.errors;

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
  const [ssoTokens, setSSOTokens] = useState<NA.NonEmptyArray<string>>();
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    const task = pipe(
      TE.tryCatch(
        () => getSSOTokens(),
        (err) => `${ssoTokensFailed}: ${err}`
      ),
      TE.chain(
        flow(
          NA.fromArray,
          TE.fromOption(() => ssoTokensNotFound)
        )
      )
    );

    (async () =>
      pipe(
        await task(),
        E.match(setErrorMessage, (tokens) => {
          setSSOTokens(tokens);
          // set default sso token
          pipe(tokens, NA.head, (token) => onChange(token));
        })
      ))();
  }, [getSSOTokens, onChange]);

  return ssoTokens ? (
    <FormControl>
      <Select
        id="sso-select"
        defaultValue={value ?? NA.head(ssoTokens)}
        onChange={(event) => onChange(event.target.value)}
      >
        {ssoTokens.map((token: string) => (
          <MenuItem key={token} value={token}>
            {token}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  ) : (
    pipe(
      errorMessage,
      pfTernary(
        S.isEmpty,
        () => <LoadingCircle />,
        () => <Alert severity="warning">{errorMessage}</Alert>
      )
    )
  );
};

export default ACLSSOMenu;
