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
import { alpha } from "@mui/material";
import { Theme } from "@mui/material/styles";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as t from "io-ts";
import * as React from "react";
import { useEffect, useRef, useState } from "react";
import Grid, { type GridProps } from "@mui/material/Grid";
import { dropTargetForElements } from "@atlaskit/pragmatic-drag-and-drop/element/adapter";
import * as OEQ from "@openequella/rest-api-client";

export interface PortletDropZoneGridProps extends GridProps {
  /**
   * Index of the column this drop zone represents
   */
  column: OEQ.Dashboard.PortletColumn;
  /**
   * Number of portlets in the column.
   */
  count: number;
}

/**
 * Codec for the data attached to the portlet column.
 */
export const DndColumnDataCodec = t.type({
  /**
   * Index of the column being dragged or dropped onto.
   */
  column: OEQ.Codec.Dashboard.PortletColumnCodec,
  /**
   * Number of portlets in the column.
   */
  count: t.number,
});

/**
 * Type representing the data attached to the portlet column.
 */
export type DndColumnData = t.TypeOf<typeof DndColumnDataCodec>;

/**
 * A Grid component that acts as a drop zone for {@link DraggablePortlet}.
 * It highlights itself when a portlet is dragged over it.
 */
export const PortletDropZoneGrid: React.FC<PortletDropZoneGridProps> = ({
  column,
  count,
  ...restGridProps
}) => {
  const ref = useRef(null);
  const [isDraggedOver, setIsDraggedOver] = useState(false);

  useEffect(
    () =>
      pipe(
        ref.current,
        O.fromNullable,
        O.map((element) =>
          dropTargetForElements({
            element,
            getData: (): DndColumnData => ({ column, count }),
            onDragStart: () => setIsDraggedOver(true),
            onDragEnter: () => setIsDraggedOver(true),
            onDragLeave: () => setIsDraggedOver(false),
            onDrop: () => setIsDraggedOver(false),
          }),
        ),
        O.toUndefined,
      ),
    [column, count],
  );

  // Style applied when a portlet is dragged over the drop zone.
  // Alpha values are recommended by AI.
  const highlightStyle = (theme: Theme) => ({
    boxShadow: `0 0 ${theme.spacing(1)} ${alpha(theme.palette.primary.main, 0.3)}`,
    backgroundColor: alpha(theme.palette.primary.main, 0.05),
  });

  return (
    <Grid
      ref={ref}
      {...restGridProps}
      sx={[
        {
          // Ensure the drop zone always fills the height with the container.
          minHeight: "100%",
          // Ensure portlet starts from top of the drop zone.
          alignContent: "flex-start",
        },
        (theme) => (isDraggedOver ? highlightStyle(theme) : null),
      ]}
    />
  );
};
