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
import { screen } from "@testing-library/react";

const createGetQuery = (
  elementName: string,
  fn: () => HTMLElement | null
): (() => HTMLElement) => () => {
  const e = fn();
  if (!e) {
    throw new Error(`Failed to get element: ${elementName}`);
  }

  return e;
};

/**
 * Contains all the simple functions for querying the common components of the pagination controls.
 * Following React Testing Library conventions, all queryXyz functions return either the element
 * if found or null.
 */
interface PaginatorBaseQueries {
  /**
   * Query the text caption for the control (e.g. "Items per page"), which then also has an #id which
   * is the aria-labelledby to link to the pop up list for the MUI Select. If not found, will return
   * null.
   */
  queryCaption: () => HTMLElement | null;
  queryFirstPageButton: () => HTMLElement | null;
  queryItemsPerPageOption: (pageItems: number) => HTMLElement | null;
  queryItemsPerPageSelect: () => HTMLElement | null;
  queryLastPageButton: () => HTMLElement | null;
  queryNextPageButton: () => HTMLElement | null;
  queryPageCount: () => HTMLElement | null;
  queryPreviousPageButton: () => HTMLElement | null;
}

/**
 * Builds on `PaginatorBaseQueries` to provide a collection of get queries to access the common
 * components of pagination controls. These follow the React Testing Library convention of either
 * returning the element if found, or otherwise throwing an error.
 */
interface PaginatorQueries extends PaginatorBaseQueries {
  /**
   * Get the text caption for the control (e.g. "Items per page"), which then also has an #id which
   * is the aria-labelledby to link to the pop up list for the MUI Select. If not found, will throw.
   */
  getCaption: () => HTMLElement;
  getFirstPageButton: () => HTMLElement;
  getItemsPerPageOption: (pageItems: number) => HTMLElement;
  getItemsPerPageSelect: () => HTMLElement;
  getLastPageButton: () => HTMLElement;
  getNextPageButton: () => HTMLElement;
  getPageCount: () => HTMLElement;
  getPreviousPageButton: () => HTMLElement;
}

export const queryPaginatorControls = (
  component: Element
): PaginatorQueries => {
  const _queryCaption = () =>
    component.querySelector<HTMLElement>("p.MuiTablePagination-caption");
  // screen.getByText() is used here as the options are actually added as a child to <body> -
  // so well outside the `component`.
  const _queryItemsPerPageOption = (pageItems: number, labelledById: string) =>
    screen.queryByText(`${pageItems}`, {
      selector: `ul[aria-labelledby="${labelledById}"] > li[role="option"]`,
    });

  const baseQueries = {
    queryCaption: _queryCaption,
    queryFirstPageButton: () =>
      component.querySelector<HTMLElement>("#firstPageButton"),
    queryPreviousPageButton: () =>
      component.querySelector<HTMLElement>("#previousPageButton"),
    queryNextPageButton: () =>
      component.querySelector<HTMLElement>("#nextPageButton"),
    queryLastPageButton: () =>
      component.querySelector<HTMLElement>("#lastPageButton"),
    queryPageCount: () =>
      component.querySelector<HTMLElement>(
        ".MuiTablePagination-toolbar p:nth-of-type(2)"
      ),
    queryItemsPerPageOption: (pageItems: number) => {
      const c = _queryCaption();
      if (!c) {
        // The contract for queryXyz is either return the item or null - so if we can't find a
        // dependent we best just return null
        return null;
      }
      return _queryItemsPerPageOption(pageItems, c.id);
    },
    queryItemsPerPageSelect: () =>
      component.querySelector<HTMLElement>(
        'div[role="button"].MuiTablePagination-select'
      ),
  };

  const _getCaption = createGetQuery("Caption", baseQueries.queryCaption);

  const _getItemsPerPageOption = (pageItems: number): HTMLElement => {
    // Note here we pass in the _getCaption so that it can throw an error if that fails.
    // This is different from above, because that was a queryXyz and this is a getXyz
    const e = _queryItemsPerPageOption(pageItems, _getCaption().id);

    if (!e) {
      throw new Error(
        `Failed to get element: Items per page option (${pageItems})`
      );
    }

    return e;
  };

  return {
    ...baseQueries,
    getCaption: _getCaption,
    getFirstPageButton: createGetQuery(
      "First page button",
      baseQueries.queryFirstPageButton
    ),
    getPreviousPageButton: createGetQuery(
      "Previous page button",
      baseQueries.queryPreviousPageButton
    ),
    getNextPageButton: createGetQuery(
      "Next page button",
      baseQueries.queryNextPageButton
    ),
    getLastPageButton: createGetQuery(
      "Last page button",
      baseQueries.queryLastPageButton
    ),
    getPageCount: createGetQuery("Page count", baseQueries.queryPageCount),
    getItemsPerPageOption: _getItemsPerPageOption,
    getItemsPerPageSelect: createGetQuery(
      "Items per page select",
      baseQueries.queryItemsPerPageSelect
    ),
  };
};
