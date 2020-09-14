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
} from "@material-ui/core";
import { FilterList } from "@material-ui/icons";
import * as React from "react";
import { ReactNode } from "react";
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
}

interface RefinePanelProps {
  /**
   * Child components rendered inside this panel.
   */
  controls: RefinePanelControl[];
  /**
   * Handler for when the show more/show less button is clicked.
   */
  handleClick: (expansionState: boolean) => void;
  /**
   * Specifies whether the expansion panel should be expanded or collapsed.
   */
  panelExpanded: boolean;
  /**
   * True if filter icon should be shown next to the 'Show more' button.
   */
  showFilterIcon: boolean;
}

export const RefineSearchPanel = ({
  controls,
  handleClick,
  panelExpanded,
  showFilterIcon,
}: RefinePanelProps) => {
  const { title } = languageStrings.searchpage.refineSearchPanel;

  const { showMore, showLess } = languageStrings.common.action;

  const [alwaysVisibleControl, ...collapsedControls] = controls.filter(
    (c) => !c.disabled
  );

  const collapsibleSection = (controls: RefinePanelControl[]) => {
    return (
      <>
        <Collapse className="collapsibleRefinePanel" in={panelExpanded}>
          <List>{controls.map((control) => renderRefineControl(control))}</List>
        </Collapse>
        <Button
          id="collapsibleRefinePanelButton"
          fullWidth
          onClick={() => handleClick(panelExpanded)}
          endIcon={
            showFilterIcon && !panelExpanded ? (
              <FilterList
                aria-label="collapsibleFiltersSet"
                color="secondary"
              />
            ) : undefined
          }
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
        >
          <Grid item>
            <RefinePanelControlHeading title={control.title} />
          </Grid>
          <Grid item>{control.component}</Grid>
        </Grid>
      </ListItem>
    );
  };
  return (
    <Card>
      <CardContent>
        <Typography variant="h5">{title}</Typography>
        <List>
          {renderRefineControl(alwaysVisibleControl)}
          {collapsibleSection(collapsedControls)}
        </List>
      </CardContent>
    </Card>
  );
};
