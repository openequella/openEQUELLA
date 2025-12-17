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
import KeyboardArrowRightIcon from "@mui/icons-material/KeyboardArrowRight";
import { Alert, Box, Drawer, Grid, Tab, Tabs, Typography } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { TooltipIconButton } from "../components/TooltipIconButton";
import { languageStrings } from "../util/langstrings";
import { simpleMatch } from "../util/match";
import { PortletCreationList } from "./components/PortletCreationList";
import { DashboardLayout } from "./editor/DashboardLayout";
import { RestorePortletsTab } from "./editor/RestorePortletsTab";

const {
  title,
  alertInfo,
  dashboardLayout: { title: dashLayoutLabel },
  createPortlet: { title: createPortletLabel },
  restorePortlet: { title: restorePortletLabel },
} = languageStrings.dashboard.editor;
const { close } = languageStrings.common.action;

export type ClosedPortletsProvider = () => Promise<
  OEQ.Dashboard.PortletClosed[]
>;

export interface DashboardEditorProps {
  /**
   * Function to close the editor.
   */
  onClose: () => void;
  /**
   * A list of portlet types which the current user can create.
   */
  creatablePortletTypes: OEQ.Dashboard.PortletCreatable[];
  /** Optional provider for closed portlets - primarily used for testing/storybook. */
  closedPortletsProvider?: ClosedPortletsProvider;
}

/**
 * Renders a Drawer component from the right side of the screen which is used
 * for editing the Dashboard. It includes tabs for managing dashboard layout, creating,
 * and restoring portlets.
 */
export const DashboardEditor = ({
  onClose,
  creatablePortletTypes,
  closedPortletsProvider,
}: DashboardEditorProps) => {
  const [activeTab, setActiveTab] = React.useState(0);

  const handleTabChange = (_: React.ChangeEvent<object>, newValue: number) =>
    setActiveTab(newValue);

  const tabContent = pipe(
    activeTab,
    simpleMatch({
      0: () => (
        <Box id="dashboard-layout-content">
          <DashboardLayout />
        </Box>
      ),
      1: () => (
        <Box id="create-portlet-content">
          <PortletCreationList creatablePortletTypes={creatablePortletTypes} />
        </Box>
      ),
      2: () => (
        <Box id="restore-portlet-content">
          <RestorePortletsTab closedPortletsProvider={closedPortletsProvider} />
        </Box>
      ),
      _: () => <Alert severity="error">Unknown tab state!</Alert>,
    }),
  );

  return (
    <Drawer
      open
      anchor="right"
      onClose={onClose}
      slotProps={{
        paper: {
          sx: { width: { xs: "100%", sm: "60%", md: "40%", lg: "30%" }, p: 2 },
        },
      }}
    >
      <Grid container spacing={2} direction="column">
        <Grid container alignItems="center">
          <Grid size="grow">
            <Typography variant="h5">{title}</Typography>
          </Grid>
          <Grid size="auto">
            <TooltipIconButton
              title={close}
              onClick={onClose}
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
        <Grid>{tabContent}</Grid>
      </Grid>
    </Drawer>
  );
};
