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

interface LegacyFormProps {
  state: { [key: string]: string[] };
  children: ReactNode;
}

export function LegacyForm({ children, state }: LegacyFormProps) {
  return (
    <form name="eqForm" id="eqpageForm" onSubmit={(e) => e.preventDefault()}>
      <div style={{ display: "none" }} className="_hiddenstate">
        {Object.keys(state).map((k, i) => {
          return (
            <Fragment key={i}>
              {state[k].map((v, i) => (
                <input key={i} type="hidden" name={k} value={v} />
              ))}
            </Fragment>
          );
        })}
      </div>
      {children}
    </form>
  );
}
