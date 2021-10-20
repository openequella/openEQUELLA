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
import { Badge, Grid, Paper } from "@material-ui/core";
import {
  createStyles,
  makeStyles,
  Theme,
  withStyles,
} from "@material-ui/core/styles";
import AllInclusiveIcon from "@material-ui/icons/AllInclusive";
import ChevronLeftIcon from "@material-ui/icons/ChevronLeft";
import ChevronRightIcon from "@material-ui/icons/ChevronRight";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as S from "fp-ts/string";
import * as React from "react";
import { useState } from "react";
import { languageStrings } from "../util/langstrings";
import { OrdAsIs } from "../util/Ord";
import { CheckboxList } from "./CheckboxList";
import { TooltipIconButton } from "./TooltipIconButton";

const strings = languageStrings.shuffleBox;

const useStyles = makeStyles((_: Theme) => ({
  checkboxListPaper: {
    height: 250,
    overflow: "auto",
  },
}));

const RightSideBadge = withStyles((_: Theme) =>
  createStyles({
    badge: {
      top: 17,
    },
  })
)(Badge);

const LeftSideBadge = withStyles((_: Theme) =>
  createStyles({
    badge: {
      right: 35,
      top: 17,
    },
  })
)(Badge);

const AddAllIcon = (): JSX.Element => (
  <LeftSideBadge badgeContent={<AllInclusiveIcon fontSize="small" />}>
    <ChevronRightIcon fontSize="large" />
  </LeftSideBadge>
);

const RemoveAllIcon = (): JSX.Element => (
  <RightSideBadge badgeContent={<AllInclusiveIcon fontSize="small" />}>
    <ChevronLeftIcon fontSize="large" />
  </RightSideBadge>
);

export interface ShuffleBoxProps {
  /**
   * DOM id.
   */
  id?: string;
  /**
   * The available options. Where the **keys** are ultimately the values used at the program level,
   * and the **values** are used for display purposes.
   */
  options: Map<string, string>;
  /**
   * The currently selected options - an empty array represents none. These should be a subset of
   * the **keys** of `options`.
   */
  values: string[];
  /**
   * Handler for selecting an option.
   */
  onSelect: (selectedValues: string[]) => void;
}

export const ShuffleBox = ({
  id = "shufflebox",
  options,
  onSelect,
  values,
}: ShuffleBoxProps): JSX.Element => {
  const classes = useStyles();

  const [checkedChoices, setCheckedChoices] = useState<string[]>([]);
  const [checkedSelections, setCheckedSelections] = useState<string[]>([]);

  const handleAddAll = () => {
    setCheckedChoices([]);
    pipe(options, M.keys<string>(OrdAsIs), onSelect);
  };

  const handleRemoveAll = () => {
    setCheckedSelections([]);
    onSelect([]);
  };

  const handleAddSelected = () => {
    const newValues: string[] = pipe(values, A.concat(checkedChoices));
    setCheckedChoices([]);
    onSelect(newValues);
  };

  const handleRemoveSelected = () => {
    const newValues: string[] = pipe(
      values,
      A.difference(S.Eq)(checkedSelections)
    );
    setCheckedSelections([]);
    onSelect(newValues);
  };

  // Split the options into those already chosen, and those remaining
  const [choices, selections]: [Map<string, string>, Map<string, string>] =
    pipe(
      options,
      M.partitionWithIndex((k) => values.includes(k)),
      ({ left, right }) => [left, right]
    );

  const buttons: [string, () => void, JSX.Element][] = [
    [strings.addAll, handleAddAll, <AddAllIcon />],
    [
      strings.addSelected,
      handleAddSelected,
      <ChevronRightIcon fontSize="large" />,
    ],
    [
      strings.removeSelected,
      handleRemoveSelected,
      <ChevronLeftIcon fontSize="large" />,
    ],
    [strings.removeAll, handleRemoveAll, <RemoveAllIcon />],
  ];

  return (
    <Grid container spacing={2}>
      <Grid item xs={3}>
        <Paper className={classes.checkboxListPaper} variant="outlined">
          <CheckboxList
            id={`${id}-options`}
            options={choices}
            checked={checkedChoices}
            onChange={setCheckedChoices}
          />
        </Paper>
      </Grid>
      <Grid item xs={1}>
        <Grid container direction="column" alignItems="center">
          {buttons.map(([toolTip, handler, icon], idx) => (
            <TooltipIconButton
              key={`${toolTip} ${idx}`}
              title={toolTip}
              onClick={handler}
            >
              {icon}
            </TooltipIconButton>
          ))}
        </Grid>
      </Grid>
      <Grid item xs={3}>
        <Paper className={classes.checkboxListPaper} variant="outlined">
          <CheckboxList
            id={`${id}-selections`}
            options={selections}
            checked={checkedSelections}
            onChange={setCheckedSelections}
          />
        </Paper>
      </Grid>
    </Grid>
  );
};
