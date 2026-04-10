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
import AllInclusiveIcon from "@mui/icons-material/AllInclusive";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import { Badge, Grid, Paper } from "@mui/material";
import { styled } from "@mui/material/styles";
import { pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import * as React from "react";
import { useState } from "react";
import { languageStrings } from "../util/langstrings";
import { OrdAsIs } from "../util/Ord";
import { CheckboxList } from "./CheckboxList";
import { TooltipIconButton } from "./TooltipIconButton";

const strings = languageStrings.shuffleBox;

const PREFIX = "ShuffleBox";
const classes = {
  checkboxListPaper: `${PREFIX}-checkboxListPaper`,
};

const StyledGrid = styled(Grid)({
  [`& .${classes.checkboxListPaper}`]: {
    height: 250,
    overflow: "auto",
  },
});

const RightSideBadge = styled(Badge)({
  "& .MuiBadge-badge": {
    top: 17,
  },
});

const LeftSideBadge = styled(Badge)({
  "& .MuiBadge-badge": {
    right: 35,
    top: 17,
  },
});

const AddAllIcon = (): React.JSX.Element => (
  <LeftSideBadge badgeContent={<AllInclusiveIcon fontSize="small" />}>
    <ChevronRightIcon fontSize="large" />
  </LeftSideBadge>
);

const RemoveAllIcon = (): React.JSX.Element => (
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
  values: Set<string>;
  /**
   * Handler for selecting an option.
   */
  onSelect: (selectedValues: ReadonlySet<string>) => void;
}

export const ShuffleBox = ({
  id = "shufflebox",
  options,
  onSelect,
  values,
}: ShuffleBoxProps): React.JSX.Element => {
  const [checkedChoices, setCheckedChoices] = useState<ReadonlySet<string>>(
    RSET.empty,
  );
  const [checkedSelections, setCheckedSelections] = useState<
    ReadonlySet<string>
  >(RSET.empty);

  const handleAddAll = () => {
    setCheckedChoices(RSET.empty);
    pipe(
      options,
      M.keys<string>(OrdAsIs),
      RSET.fromReadonlyArray(S.Eq),
      onSelect,
    );
  };

  const handleRemoveAll = () => {
    setCheckedSelections(RSET.empty);
    onSelect(RSET.empty);
  };

  const handleAddSelected = () => {
    const newValues: ReadonlySet<string> = pipe(
      values,
      RSET.union(S.Eq)(checkedChoices),
    );
    setCheckedChoices(RSET.empty);
    onSelect(newValues);
  };

  const handleRemoveSelected = () => {
    const newValues: ReadonlySet<string> = pipe(
      values,
      RSET.difference(S.Eq)(checkedSelections),
    );
    setCheckedSelections(RSET.empty);
    onSelect(newValues);
  };

  // Split the options into those already chosen, and those remaining
  const [choices, selections]: [Map<string, string>, Map<string, string>] =
    pipe(
      options,
      M.partitionWithIndex((k) => pipe(values, RSET.elem(S.Eq)(k))),
      ({ left, right }) => [left, right],
    );

  const buttons: [string, () => void, React.JSX.Element][] = [
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
    <StyledGrid id={id} container spacing={2}>
      <Grid size="grow">
        <Paper
          className={classes.checkboxListPaper}
          variant="outlined"
          aria-label={strings.availableOptions}
        >
          <CheckboxList
            id={`${id}-options`}
            options={choices}
            checked={checkedChoices}
            onChange={setCheckedChoices}
          />
        </Paper>
      </Grid>
      <Grid size={1}>
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
      <Grid size="grow">
        <Paper
          className={classes.checkboxListPaper}
          variant="outlined"
          aria-label={strings.currentSelections}
        >
          <CheckboxList
            id={`${id}-selections`}
            options={selections}
            checked={checkedSelections}
            onChange={setCheckedSelections}
          />
        </Paper>
      </Grid>
    </StyledGrid>
  );
};
