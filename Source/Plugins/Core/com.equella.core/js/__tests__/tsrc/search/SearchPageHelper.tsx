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
 * Helper function to assist in finding the Refine Search panel.
 * @param container The root container where <RefineSearchPanel/> exists
 */
export const queryRefineSearchPanel = (
  container: Element
): HTMLDivElement | null =>
  container.querySelector<HTMLDivElement>("#refine-panel");

/**
 * Similar to queryRefineSearchPanel but throws an error if RefineSearchPanel is not found.
 * @param container The root container where <RefineSearchPanel/> exists
 */
export const getRefineSearchPanel = (container: Element): HTMLDivElement => {
  const refineSearchPanel = queryRefineSearchPanel(container);
  if (!refineSearchPanel) {
    throw new Error("Unable to find refine search panel");
  }

  return refineSearchPanel;
};

/**
 * Similar to queryRefineSearchComponent but throws an error if the component is not found.
 *
 * @see queryRefineSearchComponent
 * @param container The root container to start the search from
 * @param componentSuffix Typically the `idSuffix` provided in `SearchPage.tsx`
 */
export const getRefineSearchComponent = (
  container: Element,
  componentSuffix: string
) => {
  const e = queryRefineSearchComponent(container, componentSuffix);
  if (!e) {
    throw new Error(`Failed to find ${componentSuffix}`);
  }

  return e as HTMLElement;
};

/**
 * Helper function to find individual Refine Search components based on the their `idSuffix`,
 * or return null if the component is not found.
 *
 * @param container The root container to start the search from
 * @param componentSuffix Typically the `idSuffix` provided in `SearchPage.tsx`
 */
export const queryRefineSearchComponent = (
  container: Element,
  componentSuffix: string
): HTMLElement | null => {
  const id = `#RefineSearchPanel-${componentSuffix}`;
  return container.querySelector(id);
};

/**
 * Helper function to assist in finding the Owner selector
 *
 * @param container a root container within which <OwnerSelector/> exists
 */
export const queryOwnerSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "OwnerSelector");

/**
 * Helper function to assist in finding the Date Range selector
 *
 * @param container a root container within which <DateRangeSelector/> exists
 */
export const queryDateRangeSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "DateRangeSelector");

/**
 * Helper function to assist in finding the Search Attachments selector
 *
 * @param container a root container within which <SearchAttachmentsSelector/> exists
 */
export const querySearchAttachmentsSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "SearchAttachmentsSelector");

/**
 * Helper function to assist in finding the Collection selector
 *
 * @param container a root container within which <CollectionSelector/> exists
 */
export const queryCollectionSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "CollectionSelector");

/**
 * Helper function to assist in finding the Status selector
 *
 * @param container a root container within which <StatusSelector/> exists
 */
export const queryStatusSelector = (container: HTMLElement) =>
  queryRefineSearchComponent(container, "StatusSelector");
