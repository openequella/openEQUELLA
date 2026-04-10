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
import { Checkbox, FormControlLabel } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client/";
import * as React from "react";
import { WizardOptionGroup } from "./WizardOptionGroup";
import { WizardControlBasicProps } from "./WizardHelper";

export interface WizardCheckBoxGroupProps extends WizardControlBasicProps {
  /**
   * The list of CheckBox options.
   */
  options: OEQ.WizardCommonTypes.WizardControlOption[];
  /**
   * The number of options displayed in one row.
   */
  columns: number;
  /**
   * The currently selected options.
   */
  values?: string[];
  /**
   * Handler for selecting an option.
   */
  onSelect: (selectedValues: string[]) => void;
}

export const WizardCheckBoxGroup = (props: WizardCheckBoxGroupProps) => {
  const values = props.values ?? [];
  const buildOption = ({
    text,
    value,
  }: OEQ.WizardCommonTypes.WizardControlOption): React.JSX.Element => (
    <FormControlLabel
      label={text}
      control={
        <Checkbox
          checked={values.some((v) => v === value)}
          value={value}
          onChange={(e) => {
            const selectedValues = e.target.checked
              ? [...values, value]
              : values.filter((v) => v !== value);
            props.onSelect(selectedValues);
          }}
        />
      }
    />
  );

  return <WizardOptionGroup {...props} buildOption={buildOption} />;
};
