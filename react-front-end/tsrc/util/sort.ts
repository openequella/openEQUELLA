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
/**
 * Function to sort alphabetically an array of objects by some specific key.
 * Base on https://stackoverflow.com/questions/43311121/sort-an-array-of-objects-in-typescript
 *
 * Example:
 * ```
 * const original_array = [{ name: "B" }, { name: "A" }]
 * const ordered_array = sortArrayOfObjects(original_array, "name")
 * console.log(ordered_array)
 * //output
 * [{ name: "A" }, { name: "B" }]
 * ```
 *
 * @param data Array of objects.
 * @param keyToSort Key of the object to sort.
 * @param desc Sort to descending order or not.
 * @param caseSensitive Case sensitive or not.
 */
export const sortArrayOfObjects = <T>(
  data: T[],
  keyToSort: keyof T,
  desc = false,
  caseSensitive = false
): typeof data => {
  const compare = (objectB: T, objectA: T) => {
    const valueB = caseSensitive
      ? objectB[keyToSort]
      : String(objectB[keyToSort]).toLowerCase();
    const valueA = caseSensitive
      ? objectA[keyToSort]
      : String(objectA[keyToSort]).toLowerCase();

    if (valueA === valueB) {
      return 0;
    }

    if (valueB > valueA) {
      return desc ? -1 : 1;
    } else {
      return desc ? 1 : -1;
    }
  };

  return data.slice().sort(compare);
};
