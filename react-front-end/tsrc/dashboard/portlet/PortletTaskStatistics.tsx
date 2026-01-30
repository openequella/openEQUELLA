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
  Alert,
  Box,
  Divider,
  FormControl,
  InputLabel,
  Link as MuiLink,
  MenuItem,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  ToggleButton,
  ToggleButtonGroup,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useEffect, useState } from "react";
import { useHistory } from "react-router";
import { TaskStatisticsPortlet } from "../../../../oeq-ts-rest-api/src/Dashboard";
import { isManageWorkflowACLGranted } from "../../modules/SecurityModule";
import {
  getAllWorkflowsTrends,
  getWorkflowTrends,
} from "../../modules/WorkflowModule";
import { languageStrings } from "../../util/langstrings";
import { simpleMatch } from "../../util/match";
import { DraggablePortlet } from "../components/DraggablePortlet";
import { PortletSearchResultNoneFound } from "../components/PortletSearchResultNoneFound";
import { PortletBasicProps } from "./PortletHelper";
import { buildTaskOnClickHandler } from "./PortletTasksHelper";
import {
  classes,
  PortletTaskStatisticsContent,
} from "./PortletTaskStatisticsContent";
import { PortletTaskStatisticsContentSkeleton } from "./PortletTaskStatisticsContentSkeleton";

const strings = {
  ...languageStrings.dashboard.portlets.taskStatistics,
};

export interface PortletTaskStatisticsProps extends PortletBasicProps {
  /**
   * Configuration details of the portlet.
   */
  cfg: TaskStatisticsPortlet;
  /**
   * A provider function to fetch all workflow trends. Primarily for testing.
   */
  getAllWorkflowTrendsProvider?: typeof getAllWorkflowsTrends;
  /**
   * A provider function to fetch specific workflow trends. Primarily for testing.
   */
  getWorkflowTrendsProvider?: typeof getWorkflowTrends;
  /**
   * A provider function to check manage workflow ACL permission. Primarily for testing.
   */
  isManageWorkflowACLGrantedProvider?: typeof isManageWorkflowACLGranted;
}

// ID for the workflow selection label.
const WORKFLOW_LABEL_ID = "workflow-label";
// Workflow selection value for all workflows.
const WITHIN_ALL_WORKFLOWS = "all";

/**
 * Represents the lifecycle of fetching workflow statistics (task trends) data.
 * - initial: not requested yet.
 * - fetching: request in progress.
 * - success: request succeeded with results.
 * - noResults: request succeeded but returned an empty list.
 */
type WorkflowStatisticsState =
  | { state: "initial" }
  | { state: "fetching" }
  | {
      state: "success";
      results: OEQ.Workflow.TaskTrendDetails[];
    }
  | { state: "noResults" };

/**
 * Portlet component that displays task statistics.
 */
export const PortletTaskStatistics = ({
  cfg,
  getAllWorkflowTrendsProvider = getAllWorkflowsTrends,
  getWorkflowTrendsProvider = getWorkflowTrends,
  isManageWorkflowACLGrantedProvider = isManageWorkflowACLGranted,
  ...restProps
}: PortletTaskStatisticsProps) => {
  const history = useHistory();

  const [loadingState, setLoadingState] = React.useState<
    "loading" | "loaded" | "noPermission"
  >("loading");

  // By default, select `within all workflows`.
  const [selectedWorkflow, setSelectedWorkflow] =
    useState<string>(WITHIN_ALL_WORKFLOWS);
  const [selectedTrend, setSelectedTrend] = useState<OEQ.Task.Trend>(cfg.trend);

  const [workflowStatistics, setWorkflowStatistics] =
    React.useState<WorkflowStatisticsState>({
      state: "initial",
    });

  useEffect(() => {
    pipe(
      isManageWorkflowACLGrantedProvider,
      TE.match(
        (_) => setLoadingState("noPermission"),
        (_) => setLoadingState("loaded"),
      ),
    )();
  }, [isManageWorkflowACLGrantedProvider]);

  useEffect(() => {
    if (loadingState !== "loaded") {
      return;
    }

    const fetchTrends = (): Promise<OEQ.Workflow.TaskTrendDetails[]> =>
      selectedWorkflow === WITHIN_ALL_WORKFLOWS
        ? getAllWorkflowTrendsProvider(selectedTrend)
        : getWorkflowTrendsProvider(selectedWorkflow, selectedTrend);

    setWorkflowStatistics({ state: "fetching" });

    pipe(
      TE.tryCatch(fetchTrends, String),
      TE.match(
        (e) => {
          console.warn(`${strings.failedToFetchTrends} [${e}]`);
          setWorkflowStatistics({ state: "noResults" });
        },
        (statistics) =>
          pipe(
            statistics,
            O.fromPredicate(A.isEmpty),
            O.match<OEQ.Workflow.TaskTrendDetails[], WorkflowStatisticsState>(
              () => ({ state: "success", results: statistics }),
              () => ({ state: "noResults" }),
            ),
            setWorkflowStatistics,
          ),
      ),
    )();
  }, [
    selectedTrend,
    selectedWorkflow,
    getAllWorkflowTrendsProvider,
    getWorkflowTrendsProvider,
    loadingState,
  ]);

  const taskOnClick = buildTaskOnClickHandler(history, "ptspr.showTaskFilter");

  const handleTrendChange = (
    _: React.MouseEvent<HTMLElement>,
    newTrend: OEQ.Task.Trend,
  ) => pipe(newTrend, O.fromNullable, O.map(setSelectedTrend));

  const workflowSelector = (
    <FormControl size="small">
      <InputLabel id={WORKFLOW_LABEL_ID}>{strings.workflow.label}</InputLabel>
      <Select
        labelId={WORKFLOW_LABEL_ID}
        value={selectedWorkflow}
        label={strings.workflow.label}
        onChange={(e) => setSelectedWorkflow(e.target.value)}
      >
        <MenuItem value="all">{strings.workflow.allWorkflows}</MenuItem>
        {/*TODO: OEQ-2702 render other workflow options fetched from API*/}
      </Select>
    </FormControl>
  );

  const trendToggleButtons = (
    <ToggleButtonGroup
      size="small"
      value={selectedTrend}
      exclusive
      onChange={handleTrendChange}
      aria-label={strings.trend.label}
    >
      <ToggleButton value="WEEK" aria-label={strings.trend.weekly}>
        {strings.trend.weekly}
      </ToggleButton>
      <ToggleButton value="MONTH" aria-label={strings.trend.monthly}>
        {strings.trend.monthly}
      </ToggleButton>
    </ToggleButtonGroup>
  );

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

  const renderTrendTable = (statistics: OEQ.Workflow.TaskTrendDetails[]) => (
    <TableContainer classes={{ root: classes.table }}>
      <Table stickyHeader size="small" aria-label={strings.table.label}>
        <TableHead>
          <TableRow>
            <TableCell>{strings.table.colTask}</TableCell>
            <TableCell align="right">{strings.table.colWaiting}</TableCell>
            <TableCell align="right">{strings.table.colTrend}</TableCell>
          </TableRow>
        </TableHead>

        <TableBody>{pipe(statistics, A.map(renderTrendTableRow))}</TableBody>
      </Table>
    </TableContainer>
  );

  const renderStatisticsResults = () => {
    switch (workflowStatistics.state) {
      case "initial":
        return null;
      case "fetching":
        return <PortletTaskStatisticsContentSkeleton />;
      case "noResults":
        return (
          <PortletSearchResultNoneFound noneFoundMessage={strings.noResult} />
        );
      case "success":
        return (
          <>
            {renderTrendTable(workflowStatistics.results)}
            {/*TODO: OEQ-2890 show item count for workflow, and show link if user has view management page permission.*/}
          </>
        );
    }
  };

  const portletContent = pipe(
    loadingState,
    simpleMatch({
      noPermission: () => (
        <Alert severity="error">{strings.noPermission}</Alert>
      ),
      loaded: () => (
        <PortletTaskStatisticsContent>
          <Box className={classes.options}>
            {workflowSelector}
            {trendToggleButtons}
          </Box>

          <Divider className={classes.divider} />

          {renderStatisticsResults()}
        </PortletTaskStatisticsContent>
      ),
      // In theory this state shouldn't be seen because we show a loading state in the DraggablePortlet
      _: () => <div>Loading...</div>,
    }),
  );

  return (
    <DraggablePortlet
      portlet={cfg}
      isLoading={loadingState === "loading"}
      {...restProps}
    >
      {portletContent}
    </DraggablePortlet>
  );
};
