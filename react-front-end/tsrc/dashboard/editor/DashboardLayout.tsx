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
import { useContext } from "react";
import * as React from "react";
import { Alert, Box, Typography } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import { DashboardPageContext } from "../DashboardPageContext";
import { DashboardLayoutSelector } from "../components/DashboardLayoutSelector";
import { languageStrings } from "../../util/langstrings";

const { dashboardLayout: strings } = languageStrings.dashboard.editor;

/**
 * This component provides the UI for a user to select a new layout for their dashboard.
 */
export const DashboardLayout = () => {
  const { dashboardDetails } = useContext(DashboardPageContext);

  const [activeLayout, setActiveLayout] = React.useState<
    OEQ.Dashboard.DashboardLayout | undefined
  >(dashboardDetails?.layout);

  const handleChange = (layout: OEQ.Dashboard.DashboardLayout) => {
    setActiveLayout(layout);
  };

  return dashboardDetails ? (
    <>
      <Typography variant="body1" mb={3}>
        {strings.chooseLayout}
      </Typography>
      <Box sx={{ display: "flex", justifyContent: "center" }}>
        <DashboardLayoutSelector value={activeLayout} onChange={handleChange} />
      </Box>
    </>
  ) : (
    <Alert severity="error">{strings.alertNoDashboardDetails}</Alert>
  );
};
