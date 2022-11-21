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
import { Boolean } from "runtypes";
import {
  readDataFromStorage,
  saveDataToStorage,
} from "../../../tsrc/modules/BrowserStorageModule";

const mockGetItem = jest.spyOn(Storage.prototype, "getItem");
const mockSetItem = jest
  .spyOn(Storage.prototype, "setItem")
  .mockImplementation(jest.fn);
const booleanValidator = Boolean.guard;
const mockConsoleError = jest.spyOn(console, "error");
const KEY = "test";

describe("readDataFromLocalStorage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("returns undefined when failed to parse value", () => {
    mockGetItem.mockReturnValueOnce('{"a":}');
    expect(readDataFromStorage(KEY, booleanValidator)).toBeUndefined();
    expect(mockConsoleError).toHaveBeenCalledTimes(1);
  });

  it("returns undefined when data format doesn't match", () => {
    mockGetItem.mockReturnValueOnce('{"a": 1}');
    expect(readDataFromStorage(KEY, booleanValidator)).toBeUndefined();
    expect(mockConsoleError).toHaveBeenCalledTimes(1);
  });

  it("supports reading boolean data", () => {
    mockGetItem.mockReturnValueOnce("true");
    expect(readDataFromStorage(KEY, booleanValidator)).toBe(true);
  });
});

describe("saveDataToLocalStorage", function () {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("outputs errors when fails to stringify the value", () => {
    saveDataToStorage(KEY, undefined);
    expect(mockConsoleError).toHaveBeenCalledTimes(1);
    expect(mockSetItem).not.toHaveBeenCalled();
  });

  it("supports saving boolean value", () => {
    saveDataToStorage(KEY, false);
    expect(mockConsoleError).toHaveBeenCalledTimes(0);
    expect(mockSetItem).toHaveBeenCalledTimes(1);
  });

  it("supports value transformation", () => {
    saveDataToStorage(KEY, "world", (value: string) => `hello ${value}`);
    expect(mockSetItem.mock.calls[0][1]).toBe('hello "world"');
  });
});
