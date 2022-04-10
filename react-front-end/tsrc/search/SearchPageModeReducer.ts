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
import * as E from "fp-ts/Either";
import { absurd, flow, pipe } from "fp-ts/function";
import type { FieldValueMap } from "../components/wizard/WizardHelper";
import { simpleMatch } from "../util/match";

export type State =
  | {
      mode: "normal";
    }
  | {
      mode: "advSearch";
      definition: OEQ.AdvancedSearch.AdvancedSearchDefinition;
      isAdvSearchPanelOpen: boolean;
      queryValues: FieldValueMap;
      /**
       * Indicates that the next Hide action should be ignored, so as to support
       * initial searches and clearing of advanced searches. Will be reset to
       * false on the next `action === hide`.
       *
       * This is due to interactions with the SearchPageReducer which this
       * reducer now probably needs to be merged with. Before the clear
       * button and the hiding of this pane based on the `search` action
       * in the SearchPageReducer it was independent, but not any-more.
       */
      overrideHide: boolean;
    };

export type Action =
  | {
      type: "useNormal";
    }
  | {
      type: "initialiseAdvSearch";
      selectedAdvSearch: OEQ.AdvancedSearch.AdvancedSearchDefinition;
      initialQueryValues: FieldValueMap;
    }
  | {
      type: "toggleAdvSearchPanel";
    }
  | {
      type: "hideAdvSearchPanel";
    }
  | {
      type: "setQueryValues";
      values: FieldValueMap;
      overrideHide: boolean;
    };

const isAdvancedSearchMode =
  (errorMessage: string) =>
  <A extends { state: State }>(a: A): E.Either<Error, A> =>
    pipe(
      a,
      E.fromPredicate(
        ({ state: { mode } }) => mode === "advSearch",
        () => new Error(errorMessage)
      )
    );

// function to toggle or hide Adv search panel.
const toggleOrHidePanel = (state: State, action: "toggle" | "hide") => {
  if (state.mode !== "advSearch") {
    throw new Error(
      `Request to ${action} Advanced Search Panel when _not_ in Advanced Search mode. Request ignored.`
    );
  }

  return pipe(
    action,
    simpleMatch<State>({
      /**
       * Ignore hide action if overrideHide is 'true',
       * in order to support initial searches and clearing of advanced searches.
       * Reset overrideHide to 'false' finally.
       */
      hide: () => ({
        ...state,
        overrideHide: false,
        isAdvSearchPanelOpen: state.overrideHide
          ? state.isAdvSearchPanelOpen
          : false,
      }),
      toggle: () => ({
        ...state,
        isAdvSearchPanelOpen: !state.isAdvSearchPanelOpen,
      }),
      _: () => {
        throw new TypeError("Unknown action type for toggleOrHidePanel");
      },
    })
  );
};

const setQueryValues: (_: {
  state: State;
  values: FieldValueMap;
  overrideHide: boolean;
}) => State = flow(
  isAdvancedSearchMode(
    "Attempted to set advanced search query values, when _not_ in Advanced Search mode!"
  ),
  E.matchW(
    (e) => {
      throw e;
    },
    ({ state, values, overrideHide }) => ({
      ...state,
      queryValues: values,
      overrideHide: overrideHide,
    })
  )
);

export const searchPageModeReducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "useNormal":
      return { mode: "normal" };
    case "initialiseAdvSearch":
      return pipe(
        action,
        ({ initialQueryValues, selectedAdvSearch: definition }) => ({
          mode: "advSearch",
          definition,
          isAdvSearchPanelOpen: true,
          queryValues: initialQueryValues,
          overrideHide: true,
        })
      );
    case "toggleAdvSearchPanel":
      return toggleOrHidePanel(state, "toggle");
    case "hideAdvSearchPanel":
      return toggleOrHidePanel(state, "hide");
    case "setQueryValues":
      return setQueryValues({
        state,
        values: action.values,
        overrideHide: action.overrideHide,
      });
    default:
      return absurd(action);
  }
};
