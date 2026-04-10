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
import CloseIcon from "@mui/icons-material/Close";
import {
  Button,
  Card,
  CardActions,
  CardContent,
  CardHeader,
  Grid,
  Typography,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { constFalse, flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as SET from "fp-ts/Set";
import * as React from "react";
import LoadingCircle from "../../components/LoadingCircle";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import * as WizardHelper from "../../components/wizard/WizardHelper";
import {
  buildVisibilityScriptContext,
  eqFullTargetAndControlType,
  FieldValueMap,
  WizardErrorContext,
} from "../../components/wizard/WizardHelper";
import { AppContext } from "../../mainui/App";
import { guestUser } from "../../modules/UserModule";
import { languageStrings } from "../../util/langstrings";
import {
  AdvancedSearchPageContext,
  generateAdvancedSearchCriteria,
} from "../AdvancedSearchHelper";
import { SearchContext } from "../SearchPageHelper";

const { title: defaultTitle, duplicateTargetWarning } =
  languageStrings.searchpage.AdvancedSearchPanel;

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
}

/**
 * This component displays all the Wizard Controls of an Advanced search and allows users
 * to update the value of each control. User can then do a search with the configured Advanced
 * search criteria or clear the criteria.
 */
export const AdvancedSearchPanel = ({
  wizardControls,
  values,
  title,
}: AdvancedSearchPanelProps) => {
  const { updateFieldValueMap, openAdvancedSearchPanel, definitionRetrieved } =
    React.useContext(AdvancedSearchPageContext);
  const { search, searchState, searchPageErrorHandler } =
    React.useContext(SearchContext);
  const currentUser = React.useContext(AppContext).currentUser ?? guestUser;

  const [currentValues, setCurrentValues] =
    React.useState<WizardHelper.FieldValueMap>(values);

  const duplicateTarget: boolean = React.useMemo(
    () =>
      pipe(
        wizardControls,
        A.filter(OEQ.WizardControl.isWizardBasicControl),
        A.map(({ controlType, targetNodes }) =>
          targetNodes.map(({ fullTarget }) => ({ controlType, fullTarget })),
        ),
        A.flatten,
        (xs) => {
          const set = SET.fromArray(eqFullTargetAndControlType)(xs);
          return SET.size(set) < A.size(xs);
        },
      ),
    [wizardControls],
  );

  // Keep the values in state (CurrentValues) in sync with those passed in
  // by props (values). Key when the clear button is triggered.
  React.useEffect(() => {
    setCurrentValues(values);
  }, [values]);

  const hasRequiredFields: boolean = pipe(
    wizardControls,
    A.exists(
      flow(
        O.fromPredicate(OEQ.WizardControl.isWizardBasicControl),
        O.map((c) => c.mandatory),
        O.getOrElse(constFalse),
      ),
    ),
  );

  const handleSubmitAdvancedSearch = async (
    advFieldValue: FieldValueMap,
    openPanel = true,
  ) => {
    updateFieldValueMap(advFieldValue);
    openAdvancedSearchPanel(openPanel);
    search({
      ...searchState.options,
      advancedSearchCriteria: generateAdvancedSearchCriteria(advFieldValue),
      advFieldValue,
      currentPage: 0,
    });
  };

  const onChangeHandler = React.useCallback(
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
            { target, value }: WizardHelper.FieldValue,
          ) => pipe(valueMap, WizardHelper.fieldValueMapInsert(target, value)),
        ),
        setCurrentValues,
      );
    },
    [currentValues, setCurrentValues],
  );

  const idPrefix = "advanced-search-panel";
  return (
    <Card id={idPrefix}>
      <CardHeader
        title={title ?? defaultTitle}
        action={
          <TooltipIconButton
            title={languageStrings.common.action.close}
            onClick={() => openAdvancedSearchPanel(false)}
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
        {definitionRetrieved ? (
          <Grid
            id="advanced-search-form"
            container
            direction="column"
            spacing={2}
          >
            <WizardErrorContext.Provider
              value={{ handleError: searchPageErrorHandler }}
            >
              {WizardHelper.render(
                wizardControls,
                currentValues,
                onChangeHandler,
                buildVisibilityScriptContext(currentValues, currentUser),
              ).map((e) => (
                // width is a tricky way to fix additional whitespace issue caused by user selector
                <Grid key={e.props.id} style={{ width: "100%" }}>
                  {e}
                </Grid>
              ))}
            </WizardErrorContext.Provider>
            {hasRequiredFields && (
              <Grid>
                <Typography variant="caption" color="textSecondary">
                  {languageStrings.common.required}
                </Typography>
              </Grid>
            )}
          </Grid>
        ) : (
          <LoadingCircle />
        )}
      </CardContent>
      <CardActions>
        <Button
          id={`${idPrefix}-searchBtn`}
          onClick={() => handleSubmitAdvancedSearch(currentValues, false)}
          color="primary"
        >
          {languageStrings.common.action.search}
        </Button>
        <Button
          id={`${idPrefix}-clearBtn`}
          onClick={() => handleSubmitAdvancedSearch(new Map())}
          color="secondary"
        >
          {languageStrings.common.action.clear}
        </Button>
      </CardActions>
    </Card>
  );
};
