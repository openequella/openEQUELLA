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
import { OutlinedInput } from "@mui/material";
import * as React from "react";
import { WizardControlBasicProps } from "./WizardHelper";
import { WizardLabel } from "./WizardLabel";

export interface WizardEditBoxProps extends WizardControlBasicProps {
  /**
   * If greater than 1 creates a multi-line edit box with the specified number of rows.
   */
  rows: number;
  /**
   * The current value of the control.
   */
  value?: string;
  /**
   * On change handler.
   */
  onChange: (_: string) => void;
}

/**
 * Basic wizard 'EditBox' control.
 */
export const WizardEditBox = ({
  id,
  label,
  description,
  mandatory,
  rows,
  value,
  onChange,
}: WizardEditBoxProps): React.JSX.Element => (
  <>
    <WizardLabel
      mandatory={mandatory}
      label={label}
      description={description}
      labelFor={id}
    />
    <OutlinedInput
      id={id}
      fullWidth
      multiline={rows > 1}
      rows={rows}
      value={value ?? ""}
      onChange={({ target }: React.ChangeEvent<HTMLInputElement>) =>
        onChange(target.value)
      }
      aria-required={mandatory}
    />
  </>
);
