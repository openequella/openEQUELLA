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
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { absurd, pipe } from "fp-ts/function";
import * as N from "fp-ts/number";
import * as ORD from "fp-ts/Ord";
import * as S from "fp-ts/string";
import * as O from "fp-ts/Option";
import * as React from "react";
import { PortletPosition } from "../../modules/DashboardModule";
import { PortletBrowse } from "./PortletBrowse";
import { PortletFavourites } from "./PortletFavourites";
import { PortletFormattedText } from "./PortletFormattedText";
import { PortletMyResources } from "./PortletMyResources";
import { PortletQuickSearch } from "./PortletQuickSearch";
import { PortletRecentContributions } from "./PortletRecentContributions";
import { PortletScripted } from "./PortletScripted";
import { PortletTasks } from "./PortletTasks";
import { PortletTaskStatistics } from "./PortletTaskStatistics";
import { PortletUnsupported } from "./PortletUnsupported";

/**
 * Type definition for Supported two-column layouts.
 */
export type TwoColumnLayout = Exclude<
  OEQ.Dashboard.DashboardLayout,
  "SingleColumn"
>;

type TwoColumnWidths = [number, number];

/**
 * Returns the widths of two columns, according to the provided two-column layout. The result is a tuple
 * of two numbers, representing the width of the left and right columns, respectively.
 * These widths are based on MUI's 12-column grid system (e.g., [6, 6] means two columns of equal width).
 */
export const getTwoColumnWidths = (
  layout: TwoColumnLayout,
): TwoColumnWidths => {
  switch (layout) {
    case "TwoEqualColumns":
      return [6, 6];
    case "TwoColumnsRatio1to2":
      return [4, 8];
    case "TwoColumnsRatio2to1":
      return [8, 4];
    default:
      return absurd(layout);
  }
};

const ordByOrder: ORD.Ord<OEQ.Dashboard.BasicPortlet> = pipe(
  N.Ord,
  ORD.contramap((p: OEQ.Dashboard.BasicPortlet) => p.commonDetails.order),
);

const ordByName: ORD.Ord<OEQ.Dashboard.BasicPortlet> = pipe(
  S.Ord,
  ORD.contramap((p: OEQ.Dashboard.BasicPortlet) => p.commonDetails.name),
);

/**
 * Returns a function that filters and sorts the supplied portlets for the specified column.
 */
export const portletFilterByColumn =
  (col: OEQ.Dashboard.PortletColumn) =>
  (portlets: OEQ.Dashboard.BasicPortlet[]): OEQ.Dashboard.BasicPortlet[] =>
    pipe(
      portlets,
      A.filter((p) => p.commonDetails.column === col),
      A.sortBy([ordByOrder, ordByName]),
    );

/**
 * Props common to all portlet components.
 */
export interface PortletBasicProps {
  /**
   * Configuration details of the portlet.
   */
  cfg: OEQ.Dashboard.BasicPortlet;
  /**
   * The actual position of the portlet in the page which is used
   * for drag and drop operations.
   */
  position: PortletPosition;
}

/**
 * Given a portlet, returns the appropriate component to render it.
 *
 * @param portlet The portlet to be rendered.
 * @param position The actual position of the portlet in the page.
 */
export const renderPortlet = (
  portlet: OEQ.Dashboard.BasicPortlet,
  position: PortletPosition,
): React.JSX.Element => {
  const { portletType } = portlet;
  const basicProps: PortletBasicProps = { cfg: portlet, position };

  // TODO: Update portlet component when they are implemented.
  switch (portletType) {
    case "search":
      return <PortletQuickSearch {...basicProps} />;
    case "browse":
      return <PortletBrowse {...basicProps} />;
    case "favourites":
      return <PortletFavourites {...basicProps} />;
    case "freemarker":
      return <PortletScripted {...basicProps} />;
    case "html":
      return (
        <PortletFormattedText
          cfg={portlet as OEQ.Dashboard.FormattedTextPortlet}
          position={basicProps.position}
        />
      );
    case "myresources":
      return <PortletMyResources {...basicProps} />;
    case "recent":
      return (
        <PortletRecentContributions
          cfg={portlet as OEQ.Dashboard.RecentContributionsPortlet}
          position={basicProps.position}
        />
      );
    case "tasks":
      return <PortletTasks {...basicProps} />;
    case "taskstatistics":
      return <PortletTaskStatistics {...basicProps} />;
    default:
      return absurd(portletType);
  }
};

/**
 * A type guard to check if a given dashboard layout is a two-column layout.
 *
 * @param layout The dashboard layout to check.
 * @returns `true` if the layout is a two-column layout.
 */
export const isTwoColumnLayout = (
  layout?: OEQ.Dashboard.DashboardLayout,
): layout is TwoColumnLayout =>
  pipe(
    O.fromNullable(layout),
    O.exists((l) => l !== "SingleColumn"),
  );

/**
 * Returns true if the given portlet is positioned in the second column.
 *
 * @param portlet The portlet whose column value is to be checked.
 * @returns Whether the portlet resides in the second column.
 */
export const isSecondColumnPortlet = (
  portlet: OEQ.Dashboard.BasicPortlet,
): boolean => portlet.commonDetails.column === 1;
