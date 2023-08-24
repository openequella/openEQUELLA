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
import "@testing-library/jest-dom/extend-expect";
import {
  queryIpInput,
  queryNetmaskInput,
  renderIPV4CIDRInput,
  typeInIpInput,
  typeInNetmaskInput,
} from "./IPv4CIDRInputTestHelper";

describe("<IPV4CIDRInput />", () => {
  it.each([
    [
      "should be able to automatically change the focus on next ip input if users complete the current input",
      0,
      "123",
      1,
    ],
    [
      "should be able to focus on next input if users press `enter`",
      1,
      "{enter}",
      2,
    ],
    [
      "should be able to focus on next input if users press `.` (period) after valid value",
      1,
      "1[Period]",
      2,
    ],
    [
      "should be able to focus on next input if users press `.` (NumpadDecimal) after valid value",
      1,
      "1[NumpadDecimal]",
      2,
    ],
    [
      "should be able to change to previous input if users press backspace",
      1,
      "{backspace}",
      0,
    ],
  ])("%s", async (_, inputIndex, typeText, expectedInputIndex) => {
    const { container } = renderIPV4CIDRInput();

    await typeInIpInput(container, typeText, inputIndex);

    const nextInput = queryIpInput(container, expectedInputIndex);
    expect(nextInput).toHaveFocus();
  });

  it("should be able to automatically change the focus on netmask input if use complete the final ip input", async () => {
    const { container } = renderIPV4CIDRInput();

    await typeInIpInput(container, "123", 3);

    expect(queryNetmaskInput(container)).toHaveFocus();
  });

  it("should be able to return the result if user complete all inputs", async () => {
    const onChange = jest.fn();
    const expectedResult = "192.168.1.1/24";
    const { container } = renderIPV4CIDRInput(onChange);

    await typeInIpInput(container, "192", 0);
    await typeInIpInput(container, "168", 1);
    await typeInIpInput(container, "1", 2);
    await typeInIpInput(container, "1", 3);

    await typeInNetmaskInput(container, "24");

    const result = onChange.mock.lastCall[0];
    expect(result).toEqual(expectedResult);
  });

  it("should still be able to return the result if netmask is empty", async () => {
    const onChange = jest.fn();
    const expectedResult = "193.169.2.2/32";
    const { container } = renderIPV4CIDRInput(onChange);

    await typeInIpInput(container, "193", 0);
    await typeInIpInput(container, "169", 1);
    await typeInIpInput(container, "2", 2);
    await typeInIpInput(container, "2", 3);

    const result = onChange.mock.lastCall[0];
    expect(result).toEqual(expectedResult);
  });
});
