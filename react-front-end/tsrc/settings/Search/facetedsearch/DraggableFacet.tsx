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
import { combine } from "@atlaskit/pragmatic-drag-and-drop/combine";
import {
  draggable,
  dropTargetForElements,
} from "@atlaskit/pragmatic-drag-and-drop/element/adapter";
import DropIndicator from "@atlaskit/pragmatic-drag-and-drop-react-drop-indicator/box";
import type { Edge } from "@atlaskit/pragmatic-drag-and-drop-hitbox/types";
import DeleteIcon from "@mui/icons-material/Delete";
import DragIndicatorIcon from "@mui/icons-material/DragIndicator";
import EditIcon from "@mui/icons-material/Edit";
import { ListItem, ListItemIcon, ListItemText } from "@mui/material";
import { styled } from "@mui/material/styles";
import { flow, pipe } from "fp-ts/function";
import * as N from "fp-ts/number";
import * as O from "fp-ts/Option";
import * as R from "fp-ts/Record";
import * as React from "react";
import { useEffect, useRef, useState } from "react";
import { TooltipIconButton } from "../../../components/TooltipIconButton";
import type { FacetedSearchClassificationWithFlags } from "../../../modules/FacetedSearchSettingsModule";
import { languageStrings } from "../../../util/langstrings";

const facetedsearchsettingStrings =
  languageStrings.settings.searching.facetedsearchsetting;

const StyledListItem = styled(ListItem)({
  cursor: "grab",
  "&:active": {
    cursor: "grabbing",
  },
  // Position required as per the documentation of the DropIndicator component.
  // https://atlassian.design/components/pragmatic-drag-and-drop/optional-packages/react-drop-indicator/about#positionrelative-needed
  position: "relative",
});

interface DraggableFacetProps {
  /**
   * Facet to be dragged and dropped
   */
  facet: FacetedSearchClassificationWithFlags;
  /**
   * Index of the provided facet in a facet list
   */
  index: number;
  /**
   * Handler to delete the facet
   */
  onDelete: (facet: FacetedSearchClassificationWithFlags) => void;
  /**
   * Handler to delete the facet
   */
  onEdit: (facet: FacetedSearchClassificationWithFlags) => void;
}

/**
 * Retrieve the index of a facet from the data attached to a dragged or dropped item.
 */
export const getFacetIndex: (
  data: Record<string, unknown>,
) => O.Option<number> = flow(R.lookup("index"), O.filter(N.isNumber));

/**
 * A `ListItem` that supports drag and drop for a configured facet.
 */
export const DraggableFacet = ({
  facet,
  index,
  onDelete,
  onEdit,
}: DraggableFacetProps) => {
  const ref = useRef<HTMLLIElement | null>(null);
  const [closestEdge, setClosestEdge] = useState<Edge | null>(null);

  useEffect(() => {
    const showBottomEdge = () => setClosestEdge("bottom");
    const hideBottomEdge = () => setClosestEdge(null);

    return pipe(
      ref.current,
      O.fromNullable,
      O.map((element) =>
        combine(
          draggable({
            element,
            // This is the data to be used when the facet is dragged, and we are only interested in the index.
            getInitialData: () => ({ index }),
          }),
          dropTargetForElements({
            element,
            canDrop: ({ source }) => source.element !== element,
            // This is the data to be used when another facet is dropped onto this facet.
            getData: () => ({ index }),
            onDragStart: showBottomEdge,
            onDragEnter: showBottomEdge,
            onDragLeave: hideBottomEdge,
            onDrop: hideBottomEdge,
          }),
        ),
      ),
      O.toUndefined,
    );
  }, [facet, ref, index]);

  return (
    <StyledListItem divider ref={ref}>
      <ListItemIcon>
        <TooltipIconButton title={languageStrings.common.action.dragToReorder}>
          <DragIndicatorIcon />
        </TooltipIconButton>
      </ListItemIcon>
      <ListItemText primary={facet.name} />
      <ListItemIcon>
        <TooltipIconButton
          title={facetedsearchsettingStrings.edit}
          color="secondary"
          onClick={() => onEdit(facet)}
        >
          <EditIcon />
        </TooltipIconButton>
      </ListItemIcon>
      <ListItemIcon>
        <TooltipIconButton
          title={facetedsearchsettingStrings.delete}
          color="secondary"
          onClick={() => onDelete(facet)}
        >
          <DeleteIcon />
        </TooltipIconButton>
      </ListItemIcon>
      {closestEdge && <DropIndicator edge={closestEdge} />}
    </StyledListItem>
  );
};
