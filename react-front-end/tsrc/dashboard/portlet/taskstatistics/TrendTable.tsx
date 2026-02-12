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
import {
  Link as MuiLink,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { useHistory } from "react-router";

import { languageStrings } from "../../../util/langstrings";
import { buildTaskOnClickHandler } from "../PortletTasksHelper";
import { classes } from "./TaskStatisticsContent";

const strings = {
  ...languageStrings.dashboard.portlets.taskStatistics,
};

interface TrendTableProps {
  /**
   * The list of task trend details to be displayed in the table.
   */
  trends: OEQ.Workflow.TaskTrendDetails[];
}

/**
 * Component for displaying a table of task trends, showing the task name, current waiting count, and trend.
 */
export const TrendTable = ({ trends }: TrendTableProps) => {
  const history = useHistory();

  const taskOnClick = buildTaskOnClickHandler(history, "ptspr.showTaskFilter");

  const renderTrendTableRow = ({
    taskId,
    name,
    waiting,
    trend,
  }: OEQ.Workflow.TaskTrendDetails) => (
    <TableRow key={taskId} className={classes.tableRow}>
      <TableCell>
        <MuiLink
          component="button"
          onClick={taskOnClick(taskId)}
          className={classes.tableLink}
          underline="hover"
        >
          {name}
        </MuiLink>
      </TableCell>
      <TableCell align="right">{waiting}</TableCell>
      <TableCell align="right">{trend > 0 ? `+${trend}` : trend}</TableCell>
    </TableRow>
  );

  return (
    <TableContainer classes={{ root: classes.table }}>
      <Table stickyHeader size="small" aria-label={strings.table.label}>
        <TableHead>
          <TableRow>
            <TableCell>{strings.table.colTask}</TableCell>
            <TableCell align="right">{strings.table.colWaiting}</TableCell>
            <TableCell align="right">{strings.table.colTrend}</TableCell>
          </TableRow>
        </TableHead>

        <TableBody>{pipe(trends, A.map(renderTrendTableRow))}</TableBody>
      </Table>
    </TableContainer>
  );
};
