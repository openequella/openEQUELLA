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
import {
  Button,
  Card,
  CardContent,
  Collapse,
  Grid,
  List,
  ListItem,
  Typography,
  useMediaQuery,
} from "@mui/material";
import FilterList from "@mui/icons-material/FilterList";
import CloseIcon from "@mui/icons-material/Close";
import type { Theme } from "@mui/material/styles";
import * as React from "react";
import { ReactNode } from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { languageStrings } from "../../util/langstrings";
import { RefinePanelControlHeading } from "./RefinePanelControlHeading";

export interface RefinePanelControl {
  /**
   * A suffix to use in the DOM id of this control which will be prefixed with `RefineSearchPanel-`.
   */
  idSuffix: string;
  /**
   * Title of a Refine control.
   */
  title: string;
  /**
   * Refine control.
   */
  component: ReactNode;
  /**
   * True if the control is configured not to be showed.
   */
  disabled?: boolean;
  /**
   * True if the control should be outside the 'show more' section.
   */
  alwaysVisible?: boolean;
}

export interface RefinePanelProps {
  /**
   * Child components rendered inside this panel.
   */
  controls: RefinePanelControl[];
  /**
   * Handler for when the show more/show less button is clicked.
   */
  onChangeExpansion: (expansionState: boolean) => void;
  /**
   * Specifies whether the expansion panel should be expanded or collapsed.
   */
  panelExpanded: boolean;
  /**
   * True if filter icon should be shown next to the 'Show more' button.
   */
  showFilterIcon: boolean;
  /**
   * Function fired to close the panel.
   */
  onClose: () => void;
}

export const RefineSearchPanel = ({
  controls,
  onChangeExpansion,
  panelExpanded,
  showFilterIcon,
  onClose,
}: RefinePanelProps) => {
  const isMdDown = useMediaQuery<Theme>((theme) =>
    theme.breakpoints.down("md"),
  );

  const { title } = languageStrings.searchpage.refineSearchPanel;

  const { showMore, showLess } = languageStrings.common.action;

  const { visible: alwaysVisibleControls, collapsed: collapsedControls } =
    controls
      .filter((c) => !c.disabled)
      .reduce(
        (acc, cur: RefinePanelControl) => {
          (cur.alwaysVisible ? acc.visible : acc.collapsed).push(cur);
          return acc;
        },
        {
          visible: [] as RefinePanelControl[],
          collapsed: [] as RefinePanelControl[],
        },
      );

  const alwaysVisibleSection = (controls: RefinePanelControl[]) =>
    controls.map((control) => renderRefineControl(control));

  const collapsibleSection = (controls: RefinePanelControl[]) => {
    return (
      <>
        <Collapse className="collapsibleRefinePanel" in={panelExpanded}>
          <List>{controls.map((control) => renderRefineControl(control))}</List>
        </Collapse>
        <Button
          id="collapsibleRefinePanelButton"
          fullWidth
          onClick={() => onChangeExpansion(panelExpanded)}
          endIcon={
            showFilterIcon && !panelExpanded ? (
              <FilterList
                aria-label="collapsibleFiltersSet"
                color="secondary"
              />
            ) : undefined
          }
          color="inherit"
        >
          {panelExpanded ? showLess : showMore}
        </Button>
      </>
    );
  };

  const renderRefineControl = (control: RefinePanelControl) => {
    return (
      <ListItem key={control.title}>
        <Grid
          id={`RefineSearchPanel-${control.idSuffix}`}
          container
          direction="column"
          size="grow"
        >
          <Grid>
            <RefinePanelControlHeading title={control.title} />
          </Grid>
          <Grid>{control.component}</Grid>
        </Grid>
      </ListItem>
    );
  };
  return (
    <Card>
      <CardContent>
        <Grid container alignItems="center">
          <Grid size={11}>
            <Typography variant="h5">{title}</Typography>
          </Grid>
          <Grid size={1}>
            {isMdDown && (
              <TooltipIconButton
                title={languageStrings.common.action.close}
                onClick={onClose}
              >
                <CloseIcon />
              </TooltipIconButton>
            )}
          </Grid>
        </Grid>
        <List>
          {alwaysVisibleSection(alwaysVisibleControls)}
          {collapsibleSection(collapsedControls)}
        </List>
      </CardContent>
    </Card>
  );
};
