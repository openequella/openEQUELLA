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
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import * as SET from "fp-ts/Set";
import { constFalse, flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as TE from "fp-ts/TaskEither";
import React, {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import * as WizardHelper from "../../components/wizard/WizardHelper";
import {
  buildVisibilityScriptContext,
  eqFullTargetAndControlType,
  WizardErrorContext,
} from "../../components/wizard/WizardHelper";
import { getCurrentUserDetails, guestUser } from "../../modules/UserModule";
import { languageStrings } from "../../util/langstrings";
import { SearchPageRenderErrorContext } from "../SearchPage";

export interface AdvancedSearchPanelProps {
  /**
   * Title of the Advanced search panel. Defaults to language string `languageStrings.searchpage.AdvancedSearchPanel.title`.
   */
  title?: string;
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

const { title: defaultTitle, duplicateTargetWarning } =
  languageStrings.searchpage.AdvancedSearchPanel;

export const AdvancedSearchPanel = ({
  wizardControls,
  values,
  onClose,
  onSubmit,
  onClear,
  title,
}: AdvancedSearchPanelProps) => {
  const { handleError } = useContext(SearchPageRenderErrorContext);
  const [currentValues, setCurrentValues] =
    useState<WizardHelper.FieldValueMap>(values);
  const [currentUser, setCurrentUser] =
    useState<OEQ.LegacyContent.CurrentUserDetails>(guestUser);

  const duplicateTarget: boolean = useMemo(
    () =>
      pipe(
        wizardControls,
        A.filter(OEQ.WizardControl.isWizardBasicControl),
        A.map(({ controlType, targetNodes }) =>
          targetNodes.map(({ fullTarget }) => ({ controlType, fullTarget }))
        ),
        A.flatten,
        (xs) => {
          const set = SET.fromArray(eqFullTargetAndControlType)(xs);
          return SET.size(set) < A.size(xs);
        }
      ),
    [wizardControls]
  );

  // For visibility scripting we need to have the current user's details
  useEffect(() => {
    const initUser = pipe(
      TE.tryCatch(getCurrentUserDetails, (reason: unknown) =>
        reason instanceof Error
          ? reason
          : new Error("Failed to retrieve current user details: " + reason)
      ),
      TE.match(handleError, setCurrentUser)
    );

    (async () => await initUser())();
  }, [handleError]);

  // Keep the values in state (CurrentValues) in sync with those passed in
  // by props (values). Key when the clear button is triggered.
  useEffect(() => {
    setCurrentValues(values);
  }, [values]);

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
    (updates: WizardHelper.FieldValue[]): void => {
      console.debug("AdvancedSearchPanel : onChangeHandler called.", {
        currentValues,
        updates,
      });
      pipe(
        updates,
        A.reduce(
          currentValues,
          (
            valueMap: WizardHelper.FieldValueMap,
            { target, value }: WizardHelper.FieldValue
          ) => pipe(valueMap, WizardHelper.fieldValueMapInsert(target, value))
        ),
        setCurrentValues
      );
    },
    [currentValues, setCurrentValues]
  );

  const idPrefix = "advanced-search-panel";
  return (
    <Card id={idPrefix}>
      <CardHeader
        title={title ?? defaultTitle}
        action={
          <TooltipIconButton
            title={languageStrings.common.action.close}
            onClick={onClose}
          >
            <CloseIcon />
          </TooltipIconButton>
        }
        subheader={
          duplicateTarget && (
            <Typography color="secondary">{duplicateTargetWarning}</Typography>
          )
        }
      />
      <CardContent>
        <Grid
          id="advanced-search-form"
          container
          direction="column"
          spacing={2}
        >
          <WizardErrorContext.Provider value={{ handleError }}>
            {WizardHelper.render(
              wizardControls,
              currentValues,
              onChangeHandler,
              buildVisibilityScriptContext(currentValues, currentUser)
            ).map((e) => (
              // width is a tricky way to fix additional whitespace issue caused by user selector
              <Grid key={e.props.id} item style={{ width: "100%" }}>
                {e}
              </Grid>
            ))}
          </WizardErrorContext.Provider>
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
