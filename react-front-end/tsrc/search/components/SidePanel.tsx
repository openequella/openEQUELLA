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
import { Grid } from "@mui/material";
import * as React from "react";
import { CategorySelectorProps } from "./CategorySelector";

import { ClassificationsPanel } from "./ClassificationsPanel";
import { RefinePanelProps, RefineSearchPanel } from "./RefineSearchPanel";

interface SidePanelProps {
  /**
   * Props passed to Refine Search panel
   */
  refinePanelProps: RefinePanelProps;
  /**
   * Props passed to Classifications Panel, if undefined the panel is not displayed.
   */
  classificationsPanelProps?: CategorySelectorProps;
}

/**
 * Right-hand side panel which includes Refine Search panel and Classifications panel.
 */
export const SidePanel = ({
  refinePanelProps,
  classificationsPanelProps,
}: SidePanelProps) => (
  <Grid container direction="column" spacing={2}>
    <Grid id="refine-panel">
      <RefineSearchPanel {...refinePanelProps} />
    </Grid>
    {classificationsPanelProps?.classifications.some(
      (c) => c.categories.length > 0,
    ) && (
      <Grid id="classification-panel">
        <ClassificationsPanel {...classificationsPanelProps} />
      </Grid>
    )}
  </Grid>
);
