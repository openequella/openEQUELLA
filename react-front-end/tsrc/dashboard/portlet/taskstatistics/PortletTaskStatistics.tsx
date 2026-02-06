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
  MenuItem,
  Select,
  ToggleButton,
  ToggleButtonGroup,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import { absurd } from "fp-ts/lib/function";
import * as O from "fp-ts/Option";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useEffect, useState } from "react";
import { TaskStatisticsPortlet } from "../../../../../oeq-ts-rest-api/src/Dashboard";
import {
  isManageWorkflowACLGranted,
  isViewManagementPageACLGranted,
} from "../../../modules/SecurityModule";
import {
  getAllWorkflowsTrends,
  getManageableWorkflows,
  getWorkflowStatistics,
} from "../../../modules/WorkflowModule";
import { languageStrings } from "../../../util/langstrings";
import { simpleMatch } from "../../../util/match";
import { DraggablePortlet } from "../../components/DraggablePortlet";
import { PortletSearchResultNoneFound } from "../../components/PortletSearchResultNoneFound";
import { logWarn, PortletBasicProps } from "../PortletHelper";
import { ItemCount } from "./ItemCount";
import { classes, TaskStatisticsContent } from "./TaskStatisticsContent";
import { TaskStatisticsContentSkeleton } from "./TaskStatisticsContentSkeleton";
import { TrendTable } from "./TrendTable";

const strings = {
  ...languageStrings.dashboard.portlets.taskStatistics,
};

export interface PortletTaskStatisticsProps extends PortletBasicProps {
  /**
   * Configuration details of the portlet.
   */
  cfg: TaskStatisticsPortlet;

  // Data providers (for testing/DI)
  /**
   * A provider function to fetch manageable workflows. Primarily for testing.
   */
  getManageableWorkflowsProvider?: typeof getManageableWorkflows;
  /**
   * A provider function to fetch all workflow trends. Primarily for testing.
   */
  getAllWorkflowTrendsProvider?: typeof getAllWorkflowsTrends;
  /**
   * A provider function to fetch specific workflow statistics. Primarily for testing.
   */
  getWorkflowStatisticsProvider?: typeof getWorkflowStatistics;

  // Permission providers (for testing/DI)
  /**
   * A provider function to check manage workflow ACL permission. Primarily for testing.
   */
  isManageWorkflowACLGrantedProvider?: typeof isManageWorkflowACLGranted;
  /**
   * A provider function to check view management page ACL permission. Primarily for testing.
   */
  isViewManagementPageACLGrantedProvider?: typeof isViewManagementPageACLGranted;
}

// ID for the workflow selection label.
const WORKFLOW_LABEL_ID = "workflow-label";
// Workflow selection value for all workflows.
const WITHIN_ALL_WORKFLOWS = "all";

/**
 * Variant of {@link OEQ.Workflow.WorkflowStatistics} used for UI normalization:
 * The portlet loads trends from two different endpoints that return slightly different payloads.
 * To keep the UI rendering logic consistent, it normalizes both responses into the same
 * shape and make `itemCount` optional for endpoints where it is not provided.
 */
type WorkflowStatisticsView = Omit<
  OEQ.Workflow.WorkflowStatistics,
  "itemCount"
> & { itemCount?: number };

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
      results: WorkflowStatisticsView;
    }
  | { state: "noResults" };

/**
 * Portlet component that displays task statistics.
 */
export const PortletTaskStatistics = ({
  cfg,
  getManageableWorkflowsProvider = getManageableWorkflows,
  getAllWorkflowTrendsProvider = getAllWorkflowsTrends,
  getWorkflowStatisticsProvider = getWorkflowStatistics,
  isManageWorkflowACLGrantedProvider = isManageWorkflowACLGranted,
  isViewManagementPageACLGrantedProvider = isViewManagementPageACLGranted,
  ...restProps
}: PortletTaskStatisticsProps) => {
  const [loadingState, setLoadingState] = React.useState<
    "loading" | "loaded" | "noPermission"
  >("loading");

  // By default, select `within all workflows`.
  const [selectedWorkflow, setSelectedWorkflow] =
    useState<string>(WITHIN_ALL_WORKFLOWS);
  const [selectedTrend, setSelectedTrend] = useState<OEQ.Task.Trend>(cfg.trend);

  const [workflowOptions, setWorkflowOptions] = useState<
    OEQ.Workflow.WorkflowSummary[]
  >([]);

  const [hasViewManagementPageAcl, setHasViewManagementPageAcl] =
    useState<boolean>(false);
  const [workflowStatistics, setWorkflowStatistics] =
    useState<WorkflowStatisticsState>({
      state: "initial",
    });

  useEffect(() => {
    const onFail = (e: string) => {
      logWarn(strings.failedToFetchWorkflowOptions, e);
      setWorkflowOptions([]);
    };

    const fetchWorkflowOptions = pipe(
      TE.tryCatch(() => getManageableWorkflowsProvider(), String),
      TE.match(onFail, setWorkflowOptions),
      T.tapIO(() => () => setLoadingState("loaded")),
    );

    pipe(
      isManageWorkflowACLGrantedProvider,
      TE.matchW(
        (_) => setLoadingState("noPermission"),
        () => fetchWorkflowOptions(),
      ),
    )();
  }, [isManageWorkflowACLGrantedProvider, getManageableWorkflowsProvider]);

  useEffect(() => {
    if (loadingState !== "loaded") {
      return;
    }

    pipe(
      isViewManagementPageACLGrantedProvider,
      TE.match(
        (_) => setHasViewManagementPageAcl(false),
        setHasViewManagementPageAcl,
      ),
    )();
  }, [isViewManagementPageACLGrantedProvider, loadingState]);

  useEffect(() => {
    if (loadingState !== "loaded") {
      return;
    }

    const toStatisticsView = (
      trends: OEQ.Workflow.TaskTrendDetails[],
    ): WorkflowStatisticsView => ({
      taskTrends: trends,
    });

    const fetchStatistics = (): Promise<WorkflowStatisticsView> =>
      selectedWorkflow === WITHIN_ALL_WORKFLOWS
        ? getAllWorkflowTrendsProvider(selectedTrend).then(toStatisticsView)
        : getWorkflowStatisticsProvider(selectedWorkflow, selectedTrend);

    setWorkflowStatistics({ state: "fetching" });

    pipe(
      TE.tryCatch(fetchStatistics, String),
      TE.match(
        (e) => {
          logWarn(strings.failedToFetchStatistics, e);
          setWorkflowStatistics({ state: "noResults" });
        },
        (statistics) => {
          const state: WorkflowStatisticsState = A.isEmpty(
            statistics.taskTrends,
          )
            ? { state: "noResults" }
            : { state: "success", results: statistics };
          setWorkflowStatistics(state);
        },
      ),
    )();
  }, [
    selectedTrend,
    selectedWorkflow,
    getAllWorkflowTrendsProvider,
    getWorkflowStatisticsProvider,
    loadingState,
  ]);

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
        <MenuItem key={WITHIN_ALL_WORKFLOWS} value={WITHIN_ALL_WORKFLOWS}>
          {strings.workflow.allWorkflows}
        </MenuItem>
        {pipe(
          workflowOptions,
          A.map((w) => (
            <MenuItem key={w.uuid} value={w.uuid}>
              {w.name}
            </MenuItem>
          )),
        )}
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

  const renderStatisticsResults = (): React.ReactNode => {
    const { state } = workflowStatistics;
    switch (state) {
      case "initial":
        return null;
      case "fetching":
        return <TaskStatisticsContentSkeleton />;
      case "noResults":
        return (
          <PortletSearchResultNoneFound noneFoundMessage={strings.noResult} />
        );
      case "success": {
        const { taskTrends, itemCount } = workflowStatistics.results;
        const hasItems = itemCount !== undefined && itemCount > 0;
        return (
          <>
            <TrendTable trends={taskTrends} />
            {hasItems && (
              <ItemCount
                workflow={selectedWorkflow}
                itemCount={itemCount}
                isClickable={hasViewManagementPageAcl}
              />
            )}
          </>
        );
      }
      default:
        return absurd(state);
    }
  };

  const portletContent = pipe(
    loadingState,
    simpleMatch({
      noPermission: () => (
        <Alert severity="error">{strings.noPermission}</Alert>
      ),
      loaded: () => (
        <TaskStatisticsContent>
          <Box className={classes.options}>
            {workflowSelector}
            {trendToggleButtons}
          </Box>

          <Divider className={classes.divider} />

          {renderStatisticsResults()}
        </TaskStatisticsContent>
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
