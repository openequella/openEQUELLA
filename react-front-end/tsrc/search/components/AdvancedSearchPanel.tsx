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
  Grid,
  Typography,
} from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { constFalse, flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import React, { useCallback, useEffect, useState } from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import * as WizardHelper from "../../components/wizard/WizardHelper";
import { languageStrings } from "../../util/langstrings";

export interface AdvancedSearchPanelProps {
  /**
   * A list of Wizard controls.
   */
  wizardControls: OEQ.WizardControl.WizardControl[];

  /**
   * The values of current Advanced Search criteria.
   */
  values: WizardHelper.FieldValueMap;

  /**
   * When the user submits the advanced search criteria to trigger an additional search, this
   * is called with the current values.
   */
  onSubmit: (currentValues: WizardHelper.FieldValueMap) => void;

  /**
   * Handler for when user click the clear button.
   */
  onClear: () => void;

  /**
   * Handler for when user selects to close the panel.
   */
  onClose: () => void;
}

export const AdvancedSearchPanel = ({
  wizardControls,
  values,
  onClose,
  onSubmit,
  onClear,
}: AdvancedSearchPanelProps) => {
  const [currentValues, setCurrentValues] =
    useState<WizardHelper.FieldValueMap>(values);

  const hasRequiredFields: boolean = pipe(
    wizardControls,
    A.exists(
      flow(
        O.fromPredicate(OEQ.WizardControl.isWizardBasicControl),
        O.map((c) => c.mandatory),
        O.getOrElse(constFalse)
      )
    )
  );

  const onChangeHandler = useCallback(
    ({ target, value }: WizardHelper.FieldValue): void => {
      console.debug("AdvancedSearchPanel : onChangeHandler called.", {
        currentValues,
        update: {
          target,
          value,
        },
      });
      setCurrentValues(
        pipe(currentValues, WizardHelper.fieldValueMapInsert(target, value))
      );
    },
    [currentValues, setCurrentValues]
  );

  // handle props `value` changing event
  useEffect(() => {
    setCurrentValues(values);
  }, [values]);

  const idPrefix = "advanced-search-panel";
  return (
    <Card id={idPrefix}>
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
        <Grid
          id="advanced-search-form"
          container
          direction="column"
          spacing={2}
        >
          {WizardHelper.render(
            wizardControls,
            currentValues,
            onChangeHandler
          ).map((e) => (
            <Grid key={e.props.id} item container direction="column">
              {e}
            </Grid>
          ))}
          {hasRequiredFields && (
            <Grid item>
              <Typography variant="caption" color="textSecondary">
                {languageStrings.common.required}
              </Typography>
            </Grid>
          )}
        </Grid>
      </CardContent>
      <CardActions>
        <Button
          id={`${idPrefix}-searchBtn`}
          onClick={() => onSubmit(currentValues)}
          color="primary"
        >
          {languageStrings.common.action.search}
        </Button>
        <Button id={`${idPrefix}-clearBtn`} onClick={onClear} color="secondary">
          {languageStrings.common.action.clear}
        </Button>
      </CardActions>
    </Card>
  );
};
