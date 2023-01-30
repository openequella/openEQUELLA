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
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import IPV4CIDRInput from "../../../tsrc/components/IPv4CIDRInput";

export const queryIpInput = (container: HTMLElement, index: number) =>
  container.querySelector(`#ip-${index}-input`);

export const queryNetmaskInput = (container: HTMLElement) =>
  container.querySelector(`#netmask-input`);

/**
 * Type ``text` in the specified input.
 *
 * @param container The element which contains the component.
 * @param text The text will be typed in the input.
 * @param index The index refer to different ip input.
 */
export const typeInIpInput = (
  container: HTMLElement,
  text: string,
  index: number
) => {
  const input = queryIpInput(container, index);
  if (!input) {
    throw new Error(`Unable to find ip input ${index}!`);
  }
  userEvent.click(input);
  userEvent.keyboard(`${text}`);
};

/**
 * Type ``text` in the netmask input.
 */
export const typeInNetmaskInput = (container: HTMLElement, text: string) => {
  const input = queryNetmaskInput(container);
  if (!input) {
    throw new Error(`Unable to find netmask input!`);
  }
  userEvent.type(input, `${text}`);
};

export const renderIPV4CIDRInput = (
  onChange: (ipAddress: string) => void = jest.fn()
) => render(<IPV4CIDRInput onChange={onChange} />);
