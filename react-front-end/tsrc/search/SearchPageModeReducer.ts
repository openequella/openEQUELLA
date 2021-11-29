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

export type State =
  | {
      mode: "normal";
    }
  | {
      mode: "advSearch";
      definition: OEQ.AdvancedSearch.AdvancedSearchDefinition;
      isAdvSearchPanelOpen: boolean;
      queryValues: FieldValueMap;
      // set true to keep search panel open, ignore hide panel action
      isKeepAdvSearchPanelOpen: boolean;
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
      isKeepAdvSearchPanelOpen: boolean;
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
  if (action === "hide" && state.isKeepAdvSearchPanelOpen) {
    return state;
  }
  return {
    ...state,
    isAdvSearchPanelOpen:
      action === "toggle" ? !state.isAdvSearchPanelOpen : false,
  };
};

const setQueryValues: (_: {
  state: State;
  values: FieldValueMap;
  isKeepAdvSearchPanelOpen: boolean;
}) => State = flow(
  isAdvancedSearchMode(
    "Attempted to set advanced search query values, when _not_ in Advanced Search mode!"
  ),
  E.matchW(
    (e) => {
      throw e;
    },
    ({ state, values, isKeepAdvSearchPanelOpen }) => ({
      ...state,
      queryValues: values,
      isKeepAdvSearchPanelOpen: isKeepAdvSearchPanelOpen,
    })
  )
);

export const searchPageModeReducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "useNormal":
      return { mode: "normal" };
    case "initialiseAdvSearch":
      const { selectedAdvSearch: definition } = action;
      return {
        mode: "advSearch",
        definition,
        isAdvSearchPanelOpen: false,
        queryValues: action.initialQueryValues,
        isKeepAdvSearchPanelOpen: false,
      };
    case "toggleAdvSearchPanel":
      return toggleOrHidePanel(state, "toggle");
    case "hideAdvSearchPanel":
      return toggleOrHidePanel(state, "hide");
    case "setQueryValues":
      return setQueryValues({
        state,
        values: action.values,
        isKeepAdvSearchPanelOpen: action.isKeepAdvSearchPanelOpen,
      });
    default:
      return absurd(action);
  }
};
