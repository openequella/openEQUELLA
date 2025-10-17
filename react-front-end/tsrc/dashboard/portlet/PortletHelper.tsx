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
import * as NEA from "fp-ts/NonEmptyArray";
import * as N from "fp-ts/number";
import * as ORD from "fp-ts/Ord";
import * as S from "fp-ts/string";
import { PortletFormattedText } from "./PortletFormattedText";
import * as React from "react";
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
  (portlets: NEA.NonEmptyArray<OEQ.Dashboard.BasicPortlet>) =>
  (col: OEQ.Dashboard.PortletColumn): OEQ.Dashboard.BasicPortlet[] =>
    pipe(
      portlets,
      A.filter((p) => p.commonDetails.column === col),
      A.sortBy([ordByOrder, ordByName]),
    );

/**
 * Given a portlet, returns the appropriate component to render it.
 *
 * @param portlet The portlet to be rendered.
 */
export const renderPortlet = (
  portlet: OEQ.Dashboard.BasicPortlet,
): React.JSX.Element => {
  const { portletType } = portlet;

  // TODO: Update portlet component when they are implemented.
  switch (portletType) {
    case "search":
      return <PortletUnsupported cfg={portlet} />;
    case "browse":
      return <PortletUnsupported cfg={portlet} />;
    case "favourites":
      return <PortletUnsupported cfg={portlet} />;
    case "freemarker":
      return <PortletUnsupported cfg={portlet} />;
    case "html":
      return (
        <PortletFormattedText
          cfg={portlet as OEQ.Dashboard.FormattedTextPortlet}
        />
      );
    case "myresources":
      return <PortletUnsupported cfg={portlet} />;
    case "recent":
      return <PortletUnsupported cfg={portlet} />;
    case "tasks":
      return <PortletUnsupported cfg={portlet} />;
    case "taskstatistics":
      return <PortletUnsupported cfg={portlet} />;
    default:
      return absurd(portletType);
  }
};
