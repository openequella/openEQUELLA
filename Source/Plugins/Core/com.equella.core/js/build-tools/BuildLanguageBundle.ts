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
import { LanguageStrings, languageStrings } from "../tsrc/util/langstrings";

type LanguageStringKey = keyof typeof languageStrings;

const buildLanguageBundle = () => {
  const languageBundle: LanguageStrings[] = [];
  (Object.keys(languageStrings) as Array<LanguageStringKey>).forEach((key) => {
    languageBundle.push(...generateLanguageBundle(key, languageStrings[key]));
  });
  // Need to spread the array otherwise the object structure is wrong.
  return JSON.stringify(Object.assign({}, ...languageBundle));
};

/**
 * Generate a list of key/value pairs for one specific language string key.
 * @param key One language string's key which will be concatenated with its parent key
 * by a '.' if the value has nested objects
 * @param value One language string's value which can be a string or an object
 */
const generateLanguageBundle = (
  key: string,
  value: LanguageStrings | string
): LanguageStrings[] => {
  const output: LanguageStrings[] = [];
  if (typeof value === "string") {
    output.push({ [key]: value });
  } else if (typeof value === "object") {
    for (const subKey in value) {
      if (value.hasOwnProperty(subKey)) {
        const nestedStrings = generateLanguageBundle(
          `${key}.${subKey}`,
          value[subKey]
        );
        output.push(...nestedStrings);
      }
    }
  } else {
    throw new TypeError("Unrecognised language string type");
  }

  return output;
};
// The output of this log is the input of 'jsbundle.json'.
console.log(buildLanguageBundle());
