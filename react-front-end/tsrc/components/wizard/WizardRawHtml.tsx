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
import { flow, pipe } from "fp-ts/function";
import * as React from "react";
import HTMLReactParser from "html-react-parser";
import {
  FieldValueMap,
  valuesByNode,
  WizardControlBasicProps,
} from "./WizardHelper";
import * as A from "fp-ts/Array";
import * as O from "fp-ts/Option";
import * as M from "fp-ts/Map";
import * as S from "fp-ts/string";

export interface WizardRawHtmlProps extends WizardControlBasicProps {
  /**
   * The map which contains all the control targets and values of a Wizard definition.
   */
  fieldValueMap?: FieldValueMap;
}

export const WizardRawHtml = ({
  id,
  description,
  fieldValueMap = new Map(),
}: WizardRawHtmlProps) => {
  const pathAndValues = valuesByNode(fieldValueMap);
  // Retrieve the metadata for a given path. One schemaNode in theory can be set in different controls,
  // but here we only use the one found first, and return the concatenated values.
  const getMetadata = (path: string): string | undefined =>
    pipe(
      pathAndValues,
      M.lookup(S.Eq)(path),
      O.map((controlValue) => controlValue.join()),
      O.toUndefined,
    );

  const resolveXpath = (description: string): string => {
    // Regex used to extract the metadata xpath from the control's description.
    // The description may have 0 or more xpaths and each xpath is wrapped by a pair of curly braces.
    // For example, a description may look like `name: {/xml/item/name}, age: {/xml/item/age}`.
    // And the matching result is [{/xml/item/name}, {/xml/item/age}].
    const xpathPattern = /({(\/.+?)})+/g;

    return pipe(
      description.match(xpathPattern),
      O.fromNullable,
      O.map(
        flow(
          A.reduce(description, (d, matched) => {
            const xpath = matched.substring(1).slice(0, -1); // Remove the curly braces.
            const value = getMetadata(xpath);
            return value ? d.replace(matched, value) : d;
          }),
        ),
      ),
      O.getOrElse(() => description),
    );
  };

  return description ? (
    <div id={id}>{HTMLReactParser(resolveXpath(description))}</div>
  ) : (
    <div />
  );
};
