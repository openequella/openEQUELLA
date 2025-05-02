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
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";

/**
 * A cut down version of the server side interface `com.tle.common.scripting.types.XmlScriptType`
 * which is used to pass state of Items and Advanced Searches into scripts. It is recommended to
 * also review the server side interface.
 *
 * Note that all XPath parameters are not `true` XPaths and are limited to simple node/attribute
 * selection and node indexes. E.g. /xml/test/node[2]/@attribute is as complex as it
 * will get.
 */
export interface XmlScriptType {
  /**
   * Find out if any value of the nodes with a given XPath match a certain value.
   *
   * @param xpath The XPath to the node(s)
   * @param value The value to check for
   * @return true if the value is found
   */
  contains: (xpath: string, value: string) => boolean;
  /**
   * Get the text value of a node using an XPath like syntax. If there is more than one node with
   * that XPath, it will return the value in the first one. If the node cannot be found, a blank
   * string will be returned. You can use exists(String) to determine if a node exists.
   *
   * @param xpath The XPath to get the value from
   * @return The value from the XML document
   */
  get: (xpath: string) => string;
  /**
   * Returns all node text values for a given XPath.
   *
   * @param xpath The XPath to the node(s)
   * @return An array of all the text values of the matching nodes
   */
  getAll: (xpath: string) => ReadonlyArray<string>;
}

/**
 * Referenced by the `user` variable in script. Represents the currently logged in user. Based on
 * server side interface `com.tle.common.scripting.objects.UserScriptObject`.
 */
export interface UserScriptObject {
  getEmail: () => string | undefined;
  getFirstName: () => string;
  getID: () => string;
  getLastName: () => string;
  getUsername: () => string;
  /**
   * Determines if the logged in user has the role with the UUID of `roleUniqueID`
   *
   * @param roleUniqueID The unique id (UUID) of the role to look for
   * @return Has the role?
   */
  hasRole: (roleUniqueID: string) => boolean;
}

/**
 * The various objects etc that are in scope for a script.
 */
export interface ScriptContext {
  xml: XmlScriptType;
  user: UserScriptObject;
}

/**
 * A Visibility Script is a script which queries the current context (represented by an
 * `ScriptContext`) and returns true or false to indicate whether a control should be displayed.
 * The result is captured in an `Either` which if there is an issue parsing or evaluating the script
 * then the left will contain the `Error`.
 */
type VisibilityScript = (context: ScriptContext) => E.Either<Error, boolean>;

/**
 * Given a visibility script (`string`) return a function which can accept details of the current
 * context in the form of a `ScriptContext`. Visibility Scripts are expected to return a boolean
 * result, but we wrap it in an Either to catch any errors in the parsing or evaluation of the
 * provided script.
 *
 * @param script A visibility script provided by the server, which expects a `xml` object to be in
 *               scope - and possibly others (like `user`).
 */
export const buildVisibilityScript =
  (script: string): VisibilityScript =>
  (context: ScriptContext): E.Either<Error, boolean> => {
    // DANGER: Conversion of string into a function which will take a single ScriptContext argument
    // (`scriptContext`) ready for calling.
    const toFunction = (s: string) =>
      pipe(
        // Yes, we know this is dangerous, but this is a key feature...

        new Function(
          "scriptContext",
          `"use strict"; const {xml, user} = scriptContext; ${s}`,
        ),
        // A `Function` returns `any`, and we want more than just compile time forcing a return
        // of boolean, so with `!!` we force a runtime return value of boolean.
        (f) =>
          (context: ScriptContext): boolean =>
            !!f(context),
      );

    // Rather than just execute the script and hope someone else deals with any issues, we know
    // this could be bad, so let's wrap the result in an Either.
    return E.tryCatch(
      () => pipe(context, toFunction(script)),
      (e) => new Error(`Failed to execute visibility script: ${e}`),
    );
  };
