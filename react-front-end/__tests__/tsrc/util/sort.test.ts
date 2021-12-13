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
import { sortArrayOfObjects } from "../../../tsrc/util/sort";

describe("sortArrayOfObjects", () => {
  const original_array = [
    { name: "zip" },
    { name: "apple" },
    { name: "Banana" },
  ];

  it("sort array of objects by name in alphabetical order", () => {
    expect(sortArrayOfObjects(original_array, "name")).toEqual([
      { name: "apple" },
      { name: "Banana" },
      { name: "zip" },
    ]);
  });

  it("sort array of objects by name in alphabetical descending order", () => {
    expect(sortArrayOfObjects(original_array, "name", true)).toEqual([
      { name: "zip" },
      { name: "Banana" },
      { name: "apple" },
    ]);
  });

  it("sort array of objects by name in alphabetical order with case sensitive", () => {
    expect(sortArrayOfObjects(original_array, "name", false, true)).toEqual([
      { name: "Banana" },
      { name: "apple" },
      { name: "zip" },
    ]);
  });

  it("sort array of objects by name in alphabetical descending order with case sensitive", () => {
    expect(sortArrayOfObjects(original_array, "name", true, true)).toEqual([
      { name: "zip" },
      { name: "apple" },
      { name: "Banana" },
    ]);
  });
});
