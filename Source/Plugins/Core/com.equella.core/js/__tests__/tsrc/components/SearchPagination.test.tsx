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
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { SearchPagination } from "../../../tsrc/search/components/SearchPagination";
import { queryPaginatorControls } from "./SearchPaginationTestHelper";

describe("<SearchPagination/>", () => {
  const mockOnPageChange = jest.fn();
  const mockOnRowsPerPageChange = jest.fn();
  const searchPagination = (
    resultCount: number,
    currentPage: number,
    rowsPerPage: number
  ) =>
    render(
      <SearchPagination
        count={resultCount}
        currentPage={currentPage}
        rowsPerPage={rowsPerPage}
        onPageChange={mockOnPageChange}
        onRowsPerPageChange={mockOnRowsPerPageChange}
      />
    );

  it("Goes back to the first page of results when First Page Button is clicked", () => {
    // Start on a page other than the first, to ensure the first page button is active
    const { container } = searchPagination(30, 1, 10);
    const { getFirstPageButton } = queryPaginatorControls(container);

    userEvent.click(getFirstPageButton());
    expect(mockOnPageChange).toHaveBeenCalledWith(0);
  });

  it("Goes back to the previous page of results when Previous Page Button is clicked", () => {
    // Start on a page other than the first, to ensure the previous page button is active
    const { container } = searchPagination(20, 1, 10);
    const { getPreviousPageButton } = queryPaginatorControls(container);

    userEvent.click(getPreviousPageButton());
    expect(mockOnPageChange).toHaveBeenCalledWith(0);
  });

  it("Goes to the next page of results when Next Page Button is clicked", () => {
    // Start on a page other than the last, to ensure the next page button is active
    const { container } = searchPagination(20, 0, 10);
    const { getNextPageButton } = queryPaginatorControls(container);

    userEvent.click(getNextPageButton());
    expect(mockOnPageChange).toHaveBeenCalledWith(1);
  });

  it("Goes to the last page of results when Last Page Button is clicked", () => {
    // Start on a page other than the last, to ensure the last page button is active
    const { container } = searchPagination(30, 0, 10);
    const { getLastPageButton } = queryPaginatorControls(container);

    userEvent.click(getLastPageButton());
    // should now be on last page
    expect(mockOnPageChange).toHaveBeenCalledWith(2);
  });

  it("Disables all buttons when there are no results", () => {
    const { container } = searchPagination(0, 0, 10);
    const {
      getFirstPageButton,
      getPreviousPageButton,
      getNextPageButton,
      getLastPageButton,
    } = queryPaginatorControls(container);

    expect(getFirstPageButton()).toBeDisabled();
    expect(getPreviousPageButton()).toBeDisabled();
    expect(getNextPageButton()).toBeDisabled();
    expect(getLastPageButton()).toBeDisabled();
  });

  it("Disables FirstPage and PreviousPage buttons on first page of results", () => {
    const { container } = searchPagination(20, 0, 10);
    const {
      getFirstPageButton,
      getPreviousPageButton,
      getNextPageButton,
      getLastPageButton,
    } = queryPaginatorControls(container);

    expect(getFirstPageButton()).toBeDisabled();
    expect(getPreviousPageButton()).toBeDisabled();

    expect(getNextPageButton()).not.toBeDisabled();
    expect(getLastPageButton()).not.toBeDisabled();
  });

  it("Disables LastPage and NextPage buttons on last page of results", () => {
    const { container } = searchPagination(20, 1, 10);
    const {
      getFirstPageButton,
      getPreviousPageButton,
      getNextPageButton,
      getLastPageButton,
    } = queryPaginatorControls(container);

    expect(getFirstPageButton()).not.toBeDisabled();
    expect(getPreviousPageButton()).not.toBeDisabled();

    expect(getNextPageButton()).toBeDisabled();
    expect(getLastPageButton()).toBeDisabled();
  });

  it("Triggers the page count callback when the user changes the number of rows per page", () => {
    const { container } = searchPagination(100, 0, 10);
    const {
      getItemsPerPageOption,
      getItemsPerPageSelect,
    } = queryPaginatorControls(container);

    userEvent.click(getItemsPerPageSelect());

    const itemsPerPageDesired = 25;
    userEvent.click(getItemsPerPageOption(itemsPerPageDesired));

    expect(mockOnRowsPerPageChange).toHaveBeenLastCalledWith(
      itemsPerPageDesired
    );
  });
});
