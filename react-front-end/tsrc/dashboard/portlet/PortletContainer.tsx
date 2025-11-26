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
import { pipe } from "fp-ts/function";
import * as NEA from "fp-ts/NonEmptyArray";
import * as React from "react";
import { useCallback } from "react";
import { PortletDropZoneGrid } from "./PortletDropZoneGrid";
import {
  getTwoColumnWidths,
  portletFilterByColumn,
  renderPortlet,
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
  /**
   * The UUID of a portlet that has just been restored. Used to trigger a highlight action.
   */
  restoredPortletId?: string;
}

/**
 * Component responsible for rendering a list of portlets in a specified layout. Defaults to
 * "SingleColumn" if not configured.
 */
export const PortletContainer = ({
  portlets,
  layout = "SingleColumn",
  restoredPortletId,
}: PortletContainerProps) => {
  const getPortletsForColumn: (
    col: OEQ.Dashboard.PortletColumn,
  ) => OEQ.Dashboard.BasicPortlet[] = useCallback(
    // Due to eslint limitation, we have to pass an inline function to useCallback here.
    (col) => portletFilterByColumn(col)(portlets),
    [portlets],
  );

  const renderPortlets =
    (column: OEQ.Dashboard.PortletColumn) =>
    (portletList: OEQ.Dashboard.BasicPortlet[]): React.JSX.Element[] =>
      pipe(
        portletList,
        A.mapWithIndex((index, portlet) => (
          <Grid
            size={12}
            id={`portlet-${portlet.commonDetails.uuid}`}
            key={portlet.commonDetails.uuid}
          >
            {renderPortlet(
              portlet,
              {
                column,
                order: index,
              },
              // Highlight the portlet if its UUID matches the restored portlet's ID.
              restoredPortletId === portlet.commonDetails.uuid,
            )}
          </Grid>
        )),
      );

  const renderLayout = () => {
    // Renders a single column by appending the right column's portlets after the left column's.
    const renderSingleColumn = () => {
      const portlets = [...getPortletsForColumn(0), ...getPortletsForColumn(1)];
      return (
        <PortletDropZoneGrid
          container
          size={12}
          spacing={2}
          id="portlet-container-single-column"
          column={0}
          count={portlets.length}
        >
          {pipe(portlets, renderPortlets(0))}
        </PortletDropZoneGrid>
      );
    };

    // Renders two columns on medium screens and up, based on the specified two-column layout.
    // On extra-small and small screens, the two columns stack vertically.
    const renderTwoColumns = (twoColumnLayout: TwoColumnLayout) => {
      const [leftColWidth, rightColWidth] = getTwoColumnWidths(twoColumnLayout);
      const firstColumnPortlets = getPortletsForColumn(0);
      const secondColumnPortlets = getPortletsForColumn(1);

      return (
        <>
          <PortletDropZoneGrid
            container
            size={{ xs: 12, md: leftColWidth }}
            spacing={2}
            id="portlet-container-left-column"
            column={0}
            count={firstColumnPortlets.length}
          >
            {pipe(getPortletsForColumn(0), renderPortlets(0))}
          </PortletDropZoneGrid>

          <PortletDropZoneGrid
            container
            size={{ xs: 12, md: rightColWidth }}
            spacing={2}
            id="portlet-container-right-column"
            column={1}
            count={secondColumnPortlets.length}
          >
            {pipe(secondColumnPortlets, renderPortlets(1))}
          </PortletDropZoneGrid>
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
      // Make sure the grid have a specific value of height and the child grid(column) can also take the full height even it's empty.
      sx={{ height: "100%" }}
      // Make sure each column stretches to the same height.
      alignItems="stretch"
      id="dashboard-portlet-container"
    >
      {renderLayout()}
    </Grid>
  );
};
