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
import { FormControlLabel, Theme } from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import * as OEQ from "@openequella/rest-api-client/";
import * as React from "react";
import { WizardLabel } from "./WizardLabel";
import { range } from "lodash";
import { Checkbox } from "@material-ui/core";

const useStyles = makeStyles<Theme, { optionWidth: number }>({
  checkBoxGroupRow: {
    flexDirection: "row",
    display: "flex",
    alignItems: "center",
  },
  checkBoxGroupColumn: {
    width: ({ optionWidth }) => `${optionWidth}%`,
  },
});

export interface WizardCheckBoxGroupProps {
  /**
   * DOM id.
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
   * Indicate that this control is 'mandatory' to the user.
   */
  mandatory: boolean;
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

export const WizardCheckBoxGroup = ({
  id,
  label,
  description,
  mandatory,
  columns,
  options,
  values = [],
  onSelect,
}: WizardCheckBoxGroupProps) => {
  const columnNumber = columns > 0 ? columns : 1;
  const rowNumber = Math.ceil(options.length / columnNumber);
  const classes = useStyles({ optionWidth: Math.round(100 / columnNumber) });

  // Allocate options to each row, depending on the number of column.
  const getOptionsForRow = (
    rowIndex: number
  ): OEQ.WizardCommonTypes.WizardControlOption[] =>
    options.filter(
      (_, optionIndex) =>
        rowIndex * columnNumber <= optionIndex &&
        optionIndex < columnNumber * (rowIndex + 1)
    );

  const buildOption = ({
    text,
    value,
  }: OEQ.WizardCommonTypes.WizardControlOption): JSX.Element => (
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
            onSelect(selectedValues);
          }}
        />
      }
    />
  );

  return (
    <>
      <WizardLabel
        mandatory={mandatory}
        label={label}
        description={description}
        labelFor={id}
      />
      <div id={id}>
        {range(0, rowNumber).map((rowIndex) => (
          <div className={classes.checkBoxGroupRow} key={`${id}-${rowIndex}`}>
            {getOptionsForRow(rowIndex).map((option, optionIndex) => (
              <div
                className={classes.checkBoxGroupColumn}
                key={`${id}-${rowIndex}-${optionIndex}`}
              >
                {buildOption(option)}
              </div>
            ))}
          </div>
        ))}
      </div>
    </>
  );
};
