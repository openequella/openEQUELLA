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
import { queryByText } from "@testing-library/react";

/**
 * Helper function to find MUI buttons with React Testing Library.
 *
 * @param container A root element to start the search from
 * @param text The text to identify the button by.
 */
export const queryMuiButtonByText = (container: HTMLElement, text: string) =>
  queryByText(
    container,
    (content: string, element: Element | null | undefined) =>
      content === text && (element?.parentElement?.matches("button") ?? false)
  )?.parentElement ?? null;

/**
 * Similar to queryMuiButtonByText, but throws an error if the MUI Button is not found.
 *
 * @param container A root element to start the search from.
 * @param text The text to identify the button by.
 */
export const getMuiButtonByText = (container: HTMLElement, text: string) => {
  const button = queryMuiButtonByText(container, text);
  if (!button) {
    throw new Error("Failed to find MUI Button.");
  }
  return button;
};

/**
 * Helper function to find MUI TextField with React Testing Library.
 *
 * @param container A root element to start the search from.
 * @param labelText The text describing the TextField.
 */
export const queryMuiTextField = (container: HTMLElement, labelText: string) =>
  queryByText(container, labelText)?.parentElement?.querySelector("input");

/**
 * Similar to queryMuiTextField, but throws an error if the MUI TextField is not found.
 *
 * @param container A root element to start the search from.
 * @param labelText The text describing the TextField.
 */
export const getMuiTextField = (container: HTMLElement, labelText: string) => {
  const textField = queryMuiTextField(container, labelText);
  if (!textField) {
    throw new Error("Failed to find MUI TextField.");
  }
  return textField;
};
