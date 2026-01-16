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
import * as React from "react";
import { ReactNode, Fragment } from "react";
import type { StateData } from "../modules/LegacyContentModule";

/**
 * ID of the main OEQ form in the Legacy UI.
 */
export const legacyFormId = "eqpageForm";

/**
 * Attempt to retrieve a legacy form by ID.
 *
 * WARNING:It's strongly discouraged to directly access DOM using `document.querySelector` in React,
 * and it should be ideally done by using useRef. However, considering the complexity of Legacy content
 * UI structure, we keep this approach for the time being.
 *
 * @param id - ID of the form to retrieve. Defaults to "eqpageForm".
 */
export const getEqPageForm = (
  id: string = legacyFormId,
): HTMLFormElement | null => document.querySelector<HTMLFormElement>(`#${id}`);

interface LegacyFormProps {
  formId: string;
  state: StateData;
  children: ReactNode;
}

/**
 * Render the Legacy form in New UI. Before 25.2, there should be only one form in the whole Legacy
 * content UI structure. However, since 25.2, this is no longer the case as we need to support displaying
 * multiple portlets in the new Dashboard using the Legacy content approach. This means there can be
 * multiple instances of this component in the new Dashboard, and therefore an additional prop for the unique
 * form ID is mandatory.
 */
export const LegacyForm = ({ children, state, formId }: LegacyFormProps) => (
  <form name="eqForm" id={formId} onSubmit={(e) => e.preventDefault()}>
    <div style={{ display: "none" }} className="_hiddenstate">
      {Object.keys(state).map((k, i) => (
        <Fragment key={i}>
          {state[k].map((v, i) => (
            <input key={i} type="hidden" name={k} value={v} />
          ))}
        </Fragment>
      ))}
    </div>
    {children}
  </form>
);
