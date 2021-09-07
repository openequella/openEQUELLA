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
  CardActions,
  CardContent,
  CardHeader,
  IconButton,
  Typography,
} from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import React from "react";
import { languageStrings } from "../../util/langstrings";

export interface AdvancedSearchPanelProps {
  /**
   * JSON string defining the advanced search wizard.
   */
  wizardDefinition: string;

  /**
   * The values of current Advanced Search criteria.
   * TODO: Type still to be defined in later stories. See AdvancedSearchCriteria in design.
   */
  values?: Map<string, string | number>;

  /**
   * When the user submits the advanced search criteria to trigger an additional search, this
   * is called with the updated values.
   * TODO: Type still to be defined in later stories. See AdvancedSearchCriteria in design.
   */
  onSubmit: (updatedValues?: Map<string, string | number>) => void;

  /**
   * Handler for when user selects to close the panel.
   */
  onClose: () => void;
}

export const AdvancedSearchPanel = ({
  wizardDefinition,
  values,
  onClose,
  onSubmit,
}: AdvancedSearchPanelProps) => (
  <Card>
    <CardHeader
      title={languageStrings.searchpage.AdvancedSearchPanel.title}
      action={
        <TooltipIconButton
          title={languageStrings.common.action.close}
          onClick={onClose}
        >
          <CloseIcon />
        </TooltipIconButton>
      }
    />
    <CardContent>
      <Typography>TODO: AdvancedSearchForm will be here</Typography>
      <code>{wizardDefinition}</code>
    </CardContent>
    <CardActions>
      <Button onClick={() => onSubmit(values)} color="primary">
        {languageStrings.common.action.search}
      </Button>
    </CardActions>
  </Card>
);
