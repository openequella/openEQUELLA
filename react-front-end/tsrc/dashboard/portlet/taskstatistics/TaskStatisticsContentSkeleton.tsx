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
  Box,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import { pipe } from "fp-ts/lib/function";
import * as NEA from "fp-ts/NonEmptyArray";
import { languageStrings } from "../../../util/langstrings";
import { classes } from "./TaskStatisticsContent";

const { contentSkeletonLabel } =
  languageStrings.dashboard.portlets.taskStatistics;

export const TaskStatisticsContentSkeleton = () => (
  <Box aria-busy aria-label={contentSkeletonLabel}>
    <TableContainer classes={{ root: classes.table }}>
      <Table stickyHeader size="small">
        <TableHead>
          <TableRow>
            <TableCell>
              <Skeleton variant="text" />
            </TableCell>
            <TableCell align="right">
              <Skeleton variant="text" />
            </TableCell>
            <TableCell align="right">
              <Skeleton variant="text" />
            </TableCell>
          </TableRow>
        </TableHead>

        <TableBody>
          {pipe(
            NEA.range(0, 2),
            NEA.map((index) => (
              <TableRow key={index} className={classes.tableRow}>
                <TableCell>
                  <Skeleton variant="text" />
                </TableCell>
                <TableCell align="right">
                  <Skeleton variant="text" width={40} sx={{ ml: "auto" }} />
                </TableCell>
                <TableCell align="right">
                  <Skeleton variant="text" width={40} sx={{ ml: "auto" }} />
                </TableCell>
              </TableRow>
            )),
          )}
        </TableBody>
      </Table>
    </TableContainer>

    <Skeleton variant="text" height={24} />
  </Box>
);
