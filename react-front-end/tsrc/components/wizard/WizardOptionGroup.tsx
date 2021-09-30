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
import { Theme } from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import * as OEQ from "@openequella/rest-api-client/";
import * as React from "react";
import { WizardControlBasicProps } from "./WizardHelper";
import { WizardLabel } from "./WizardLabel";
import { range } from "lodash";

const useStyles = makeStyles<Theme, { optionWidth: number }>({
  optionRow: {
    flexDirection: "row",
    display: "flex",
    alignItems: "center",
  },
  optionColumn: {
    width: ({ optionWidth }) => `${optionWidth}%`,
  },
});

export interface WizardCheckBoxGroupTemplateProps
  extends WizardControlBasicProps {
  /**
   * The list of CheckBox options.
   */
  options: OEQ.WizardCommonTypes.WizardControlOption[];
  /**
   * The number of options displayed in one row.
   */
  columns: number;
  /**
   * Function to build a JSX.Element for one option.
   */
  buildOption: (
    option: OEQ.WizardCommonTypes.WizardControlOption
  ) => JSX.Element;
}

/**
 * This component provides the basic structure of CheckBox Group type components
 * such as RadioButton Group.
 */
export const WizardOptionGroup = ({
  id,
  label,
  description,
  mandatory,
  columns,
  options,
  buildOption,
}: WizardCheckBoxGroupTemplateProps) => {
  const columnNumber = columns > 0 ? columns : 1;
  const rowNumber = Math.ceil(options.length / columnNumber);
  const classes = useStyles({ optionWidth: Math.round(100 / columnNumber) });

  // Function to allocate options to each row, depending on the number of column.
  const getOptionsForRow = (
    rowIndex: number
  ): OEQ.WizardCommonTypes.WizardControlOption[] =>
    options.filter(
      (_, optionIndex) =>
        rowIndex * columnNumber <= optionIndex &&
        optionIndex < columnNumber * (rowIndex + 1)
    );

  return (
    <>
      <WizardLabel
        mandatory={mandatory}
        label={label}
        description={description}
      />
      {range(0, rowNumber).map((rowIndex) => (
        <div className={classes.optionRow} key={`${id}-${rowIndex}`}>
          {getOptionsForRow(rowIndex).map((option, optionIndex) => (
            <div
              className={classes.optionColumn}
              key={`${id}-${rowIndex}-${optionIndex}`}
            >
              {buildOption(option)}
            </div>
          ))}
        </div>
      ))}
    </>
  );
};
