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
import DropIndicator from "@atlaskit/pragmatic-drag-and-drop-react-drop-indicator/box";
import { combine } from "@atlaskit/pragmatic-drag-and-drop/combine";
import type {
  DropTargetGetFeedbackArgs,
  DropTargetLocalizedData,
} from "@atlaskit/pragmatic-drag-and-drop/dist/types/internal-types";
import type {
  ElementDragType,
  BaseEventPayload,
} from "@atlaskit/pragmatic-drag-and-drop/types";
import { Box, useTheme } from "@mui/material";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { useEffect, useRef, useState } from "react";
import * as React from "react";
import {
  PortletPosition,
  PortletPositionCodec,
} from "../../modules/DashboardModule";
import PortletItem, { PortletItemProps } from "./PortletItem";
import {
  draggable,
  dropTargetForElements,
} from "@atlaskit/pragmatic-drag-and-drop/element/adapter";
import * as t from "io-ts";
import {
  attachClosestEdge,
  type Edge,
  extractClosestEdge,
} from "@atlaskit/pragmatic-drag-and-drop-hitbox/closest-edge";
import * as OEQ from "@openequella/rest-api-client";

export interface DraggablePortletProps extends PortletItemProps {
  /**
   * The actual position of the portlet in the page which is used for drag and drop operations.
   */
  position: PortletPosition;
}

/**
 * Codec for the data attached to the draggable portlet.
 */
export const DndPortletDataCodec = t.type({
  portlet: OEQ.Codec.Dashboard.BasicPortletCodec,
  position: PortletPositionCodec,
});

/**
 * Data attached to the draggable portlet.
 */
export type DndPortletData = t.TypeOf<typeof DndPortletDataCodec>;

/**
 * It wraps the {@link PortletItem} with drag-and-drop capabilities.
 */
export const DraggablePortlet = ({
  portlet,
  position,
  ...restProps
}: DraggablePortletProps) => {
  const theme = useTheme();
  const ref = useRef(null);
  // State to indicate whether the portlet is being dragged.
  const [dragging, setDragging] = useState<boolean>(false);
  // State to indicate the closest edge during a drag operation.
  const [closestEdge, setClosestEdge] = useState<Edge | null>(null);

  useEffect(() => {
    const dndPortletData: DndPortletData = { portlet, position };

    const showEdgeIndicator = (
      args: BaseEventPayload<ElementDragType> & DropTargetLocalizedData,
    ) => setClosestEdge(extractClosestEdge(args.self.data));
    const hideEdgeIndicator = () => setClosestEdge(null);

    // Prepare the dnd portlet payload with the closest edge information.
    const payload = ({
      element,
      input,
    }: DropTargetGetFeedbackArgs<ElementDragType>) => {
      return attachClosestEdge(dndPortletData, {
        element,
        input,
        allowedEdges: ["top", "bottom"],
      });
    };

    return pipe(
      ref.current,
      O.fromNullable,
      O.map((element) =>
        combine(
          draggable({
            element,
            onDragStart: () => setDragging(true),
            onDrop: () => setDragging(false),
            getInitialData: () => dndPortletData,
          }),
          dropTargetForElements({
            element,
            // Prevent dropping onto itself.
            canDrop: ({ source }) => source.element !== element,
            getData: payload,
            onDragStart: showEdgeIndicator,
            onDragEnter: showEdgeIndicator,
            onDrag: showEdgeIndicator,
            onDragLeave: hideEdgeIndicator,
            onDrop: hideEdgeIndicator,
          }),
        ),
      ),
      O.toUndefined,
    );
  }, [portlet, position]);

  return (
    <Box
      sx={[
        // `position:relative` is required for displaying the DropIndicator.
        { position: "relative", cursor: "grab" },
        // Reduce opacity when the portlet is being dragged.
        // 0.4 is the value provided by the official example.
        dragging ? { opacity: 0.4 } : null,
      ]}
      ref={ref}
    >
      <PortletItem portlet={portlet} {...restProps} />
      {closestEdge && (
        // Show drop indicator at the middle of the gap between each portlet.
        <DropIndicator edge={closestEdge} gap={theme.spacing(2)} />
      )}
    </Box>
  );
};
