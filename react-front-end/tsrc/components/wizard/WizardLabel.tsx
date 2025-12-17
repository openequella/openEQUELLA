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
import { Typography } from "@mui/material";
import * as React from "react";

export interface WizardLabelProps {
  /**
   * DOM id
   */
  id?: string;
  /**
   * The label to display for the control.
   */
  label?: string;
  /**
   * A description to display alongside the control to assist users.
   */
  description?: string;
  /**
   * Indicate that this control is 'mandatory' to the user. Only visible if the field has a `label`.
   */
  mandatory?: boolean;
  /**
   * The DOM id of the input control this is labelling.
   */
  labelFor?: string;
}

/**
 * Standardises the display of the header information for each Wizard control.
 */
export const WizardLabel = ({
  id,
  label,
  description,
  mandatory = false,
  labelFor,
}: WizardLabelProps): React.JSX.Element => (
  <label id={id} htmlFor={labelFor}>
    {label && (
      <Typography variant="h6" gutterBottom>
        {label}
        {mandatory ? " *" : ""}
      </Typography>
    )}
    {description && (
      <Typography variant="subtitle1" gutterBottom>
        {description}
      </Typography>
    )}
  </label>
);
