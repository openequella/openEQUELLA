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
import * as t from "io-ts";
import { FormControlLabel, Radio, RadioGroup, TextField } from "@mui/material";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";

/**
 * Referrer Type.
 * Contain: Match referrers containing this value.
 * Exact: Only match this exact referrer.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const ReferrerTypesUnion = t.union([t.literal("Contain"), t.literal("Exact")]);
export type ReferrerType = t.TypeOf<typeof ReferrerTypesUnion>;

const {
  aclExpressionBuilder: {
    otherACLDescriptions: {
      exactReferrer: exactReferrerDesc,
      containReferrer: containReferrerDesc,
      referrerLabel,
    },
  },
} = languageStrings;

interface ACLHTTPReferrerInputProps {
  /**
   * Fired when the input or referrer type is changed.
   */
  onChange: (referrer: string) => void;
}

/**
 * HTTPReferrerInput contains two radio options with a textField to receive HTTP referrer input.
 */
const ACLHTTPReferrerInput = ({ onChange }: ACLHTTPReferrerInputProps) => {
  const [referrerType, setReferrerType] =
    React.useState<ReferrerType>("Contain");
  const [referrer, setReferrer] = React.useState("");

  const handleOnChanged = (
    newReferrerType: ReferrerType,
    newReferrer: string,
  ) => {
    const encodedUrl = encodeURIComponent(newReferrer);
    onChange(newReferrerType === "Exact" ? encodedUrl : `*${encodedUrl}*`);
  };

  return (
    <>
      <TextField
        label={referrerLabel}
        autoFocus
        value={referrer}
        onChange={(event) => {
          const value = event.target.value;
          setReferrer(value);
          handleOnChanged(referrerType, value);
        }}
      />
      <RadioGroup
        name="referrer"
        value={referrerType}
        onChange={(event) => {
          const value = event.target.value as ReferrerType;
          setReferrerType(value);
          handleOnChanged(value, referrer);
        }}
      >
        {[
          { value: "Contain", label: containReferrerDesc },
          { value: "Exact", label: exactReferrerDesc },
        ].map(({ value, label }) => (
          <FormControlLabel
            key={value}
            value={value}
            control={<Radio />}
            label={label}
          />
        ))}
      </RadioGroup>
    </>
  );
};

export default ACLHTTPReferrerInput;
