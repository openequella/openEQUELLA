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
import { FormControl, MenuItem, Select } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";
import { WizardControlBasicProps } from "./WizardHelper";
import { WizardLabel } from "./WizardLabel";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";

export interface WizardListBoxProps extends WizardControlBasicProps {
  /**
   * The list of ListBox options.
   */
  options: OEQ.WizardCommonTypes.WizardControlOption[];
  /**
   * The currently selected option.
   */
  value?: string;
  /**
   * Handler for selecting an option.
   */
  onSelect: (selectedValues: string) => void;
}

const { allOptions: allOptionsString } = languageStrings.wizard.options;

export const WizardListBox = ({
  id,
  mandatory,
  description,
  label,
  value,
  onSelect,
  options,
}: WizardListBoxProps) => {
  const buildOptions = (options: OEQ.WizardCommonTypes.WizardControlOption[]) =>
    pipe(
      options,
      A.prepend<OEQ.WizardCommonTypes.WizardControlOption>({
        text: allOptionsString,
        value: "",
      }),
      A.map(({ text, value }) => (
        <MenuItem key={value} value={value}>
          {text}
        </MenuItem>
      ))
    );
  return (
    <>
      <WizardLabel
        mandatory={mandatory}
        label={label}
        description={description}
        labelFor={id}
      />
      <FormControl fullWidth>
        <Select
          id={`${label}-select`}
          value={value ?? ""}
          variant="outlined"
          onChange={({ target: { value } }) => onSelect(value)}
          inputProps={{ id }}
          displayEmpty
        >
          {buildOptions(options)}
        </Select>
      </FormControl>
    </>
  );
};
