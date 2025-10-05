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
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import * as NEA from "fp-ts/NonEmptyArray";
import { flow } from "fp-ts/function";
import * as React from "react";
import { useCallback } from "react";
import {
  getTwoColumnWidths,
  portletFilterByColumn,
  TwoColumnLayout,
} from "./PortletHelper";

export interface PortletContainerProps {
  /**
   * A list of viewable portlets to be displayed within the container.
   */
  portlets: NEA.NonEmptyArray<OEQ.Dashboard.BasicPortlet>;
  /**
   * The layout to be used for arranging the portlets.
   */
  layout?: OEQ.Dashboard.DashboardLayout;
}

/**
 * Component responsible for rendering a list of portlets in a specified layout. Defaults to
 * "SingleColumn" if not configured.
 */
export const PortletContainer = ({
  portlets,
  layout = "SingleColumn",
}: PortletContainerProps) => {
  const getPortletsForColumn: (
    col: OEQ.Dashboard.PortletColumn,
  ) => OEQ.Dashboard.BasicPortlet[] = useCallback(
    // Due to eslint limitation, we have to pass an inline function to useCallback here.
    (col) => portletFilterByColumn(portlets)(col),
    [portlets],
  );

  const renderPortlets: (
    portletList: OEQ.Dashboard.BasicPortlet[],
  ) => React.JSX.Element[] = flow(
    A.map(({ commonDetails }) => (
      // todo: Replace this placeholder with actual portlet components
      <Grid
        size={12}
        id={`portlet-${commonDetails.uuid}`}
        key={commonDetails.uuid}
        sx={{
          border: "1px dashed grey",
          padding: 2,
          textAlign: "center",
          minHeight: "100px",
        }}
      >
        {commonDetails.name}
      </Grid>
    )),
  );

  const renderLayout = () => {
    // Renders a single column by appending the right column's portlets after the left column's.
    const renderSingleColumn = () => (
      <Grid
        container
        size={12}
        spacing={2}
        id="portlet-container-single-column"
      >
        {renderPortlets([
          ...getPortletsForColumn(0),
          ...getPortletsForColumn(1),
        ])}
      </Grid>
    );

    // Renders two columns on medium screens and up, based on the specified two-column layout.
    // On extra-small and small screens, the two columns stack vertically.
    const renderTwoColumns = (twoColumnLayout: TwoColumnLayout) => {
      const [leftColWidth, rightColWidth] = getTwoColumnWidths(twoColumnLayout);
      return (
        <>
          <Grid
            container
            size={{ xs: 12, md: leftColWidth }}
            spacing={2}
            id="portlet-container-left-column"
          >
            {renderPortlets(getPortletsForColumn(0))}
          </Grid>
          <Grid
            container
            size={{ xs: 12, md: rightColWidth }}
            spacing={2}
            id="portlet-container-right-column"
          >
            {renderPortlets(getPortletsForColumn(1))}
          </Grid>
        </>
      );
    };

    return layout === "SingleColumn"
      ? renderSingleColumn()
      : renderTwoColumns(layout);
  };

  return (
    <Grid
      container
      spacing={2}
      id="dashboard-portlet-container"
      alignItems="flex-start"
    >
      {renderLayout()}
    </Grid>
  );
};
