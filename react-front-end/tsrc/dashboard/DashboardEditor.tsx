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
  Grid,
  Tab,
  Tabs,
  Drawer,
  Typography,
  Skeleton,
} from "@mui/material";
import { pipe } from "fp-ts/function";
import * as React from "react";
import KeyboardArrowRightIcon from "@mui/icons-material/KeyboardArrowRight";
import { TabContentSkeletonTestId } from "../../__tests__/tsrc/dashboard/DashboardEditorTestHelper";
import { TooltipIconButton } from "../components/TooltipIconButton";
import { languageStrings } from "../util/langstrings";
import { simpleMatch } from "../util/match";

const {
  title,
  alertInfo,
  dashboardLayout: dashLayoutLabel,
  createPortlet: createPortletLabel,
  restorePortlet: restorePortletLabel,
} = languageStrings.dashboard.editor;
const { close } = languageStrings.common.action;

export interface DashboardEditorProps {
  /**
   * Function to control the open/closed state of the editor.
   */
  setOpenDashboardEditor: (open: boolean) => void;
}

/**
 * Renders a Drawer component from the right side of the screen which is used
 * for editing the Dashboard. It includes tabs for managing dashboard layout, creating,
 * and restoring portlets.
 */
export const DashboardEditor = ({
  setOpenDashboardEditor,
}: DashboardEditorProps) => {
  const [activeTab, setActiveTab] = React.useState(0);
  const [isLoading] = React.useState(true);

  const handleTabChange = (_: React.ChangeEvent<object>, newValue: number) =>
    setActiveTab(newValue);

  const tabContent = pipe(
    activeTab,
    simpleMatch({
      0: () => <Box id="dashboard-layout-content">{/* TODO: OEQ-2688 */}</Box>,
      1: () => <Box id="create-portlet-content">{/* TODO: OEQ-2690 */}</Box>,
      2: () => <Box id="restore-portlet-content">{/* TODO: OEQ-2690 */}</Box>,
      _: () => <Alert severity="error">Unknown tab state!</Alert>,
    }),
  );

  const tabContentSkeleton = (
    <Skeleton
      variant="rectangular"
      width="100%"
      height={400}
      data-testid={TabContentSkeletonTestId}
    />
  );

  return (
    <Drawer
      open
      anchor="right"
      onClose={() => setOpenDashboardEditor(false)}
      slotProps={{
        paper: {
          sx: { width: { xs: "100%", sm: "60%", md: "40%", lg: "30%" }, p: 2 },
        },
      }}
    >
      <Grid container spacing={2}>
        <Grid container alignItems="center" size={12}>
          <Grid size="grow">
            <Typography variant="h5">{title}</Typography>
          </Grid>
          <Grid size="auto">
            <TooltipIconButton
              title={close}
              onClick={() => setOpenDashboardEditor(false)}
              aria-label={close}
            >
              <KeyboardArrowRightIcon />
            </TooltipIconButton>
          </Grid>
        </Grid>
        <Grid>
          <Alert severity="info">{alertInfo}</Alert>
        </Grid>
        <Grid>
          <Tabs
            onChange={handleTabChange}
            variant="fullWidth"
            value={activeTab}
          >
            <Tab label={dashLayoutLabel} />
            <Tab label={createPortletLabel} />
            <Tab label={restorePortletLabel} />
          </Tabs>
        </Grid>
        <Grid size={12}>{isLoading ? tabContentSkeleton : tabContent}</Grid>
      </Grid>
    </Drawer>
  );
};
