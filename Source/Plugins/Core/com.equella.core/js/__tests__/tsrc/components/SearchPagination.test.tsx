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
import * as React from "react";
import { mount, ReactWrapper } from "enzyme";
import { SearchPagination } from "../../../tsrc/search/components/SearchPagination";

describe("<SearchPagination/>", () => {
  const mockOnPageChange = jest.fn();
  const mockOnRowsPerPageChange = jest.fn();
  const searchPagination = (
    resultCount: number,
    currentPage: number,
    rowsPerPage: number
  ) =>
    mount(
      <SearchPagination
        count={resultCount}
        currentPage={currentPage}
        rowsPerPage={rowsPerPage}
        onPageChange={mockOnPageChange}
        onRowsPerPageChange={mockOnRowsPerPageChange}
      />
    );

  const firstPageButton = (component: ReactWrapper) => {
    return component.find("#firstPageButton button");
  };
  const previousPageButton = (component: ReactWrapper) => {
    return component.find("#previousPageButton button");
  };
  const nextPageButton = (component: ReactWrapper) => {
    return component.find("#nextPageButton button");
  };
  const lastPageButton = (component: ReactWrapper) => {
    return component.find("#lastPageButton button");
  };

  it("Goes back to the first page of results when First Page Button is clicked", () => {
    //currently on page 3 of 3
    const lastPage = searchPagination(30, 1, 10);

    firstPageButton(lastPage).simulate("click");
    expect(mockOnPageChange).toHaveBeenCalledWith(0);
  });

  it("Goes back to the previous page of results when Previous Page Button is clicked", () => {
    //currently on page 2 of 2
    const lastPages = searchPagination(20, 1, 10);

    previousPageButton(lastPages).simulate("click");
    expect(mockOnPageChange).toHaveBeenCalledWith(0);
  });

  it("Goes to the next page of results when Next Page Button is clicked", () => {
    //currently on page 1 of 2
    const firstPage = searchPagination(20, 0, 10);

    nextPageButton(firstPage).simulate("click");
    expect(mockOnPageChange).toHaveBeenCalledWith(1);
  });

  it("Goes to the last page of results when Last Page Button is clicked", () => {
    //currently on page 1 of 3
    const firstPage = searchPagination(30, 0, 10);

    lastPageButton(firstPage).simulate("click");
    //should now be on last page
    expect(mockOnPageChange).toHaveBeenCalledWith(2);
  });

  it("Disables all buttons when there are no results", () => {
    const noResults = searchPagination(0, 0, 10);

    expect(firstPageButton(noResults).prop("disabled")).toBeTruthy();
    expect(previousPageButton(noResults).prop("disabled")).toBeTruthy();

    expect(nextPageButton(noResults).prop("disabled")).toBeTruthy();
    expect(lastPageButton(noResults).prop("disabled")).toBeTruthy();
  });

  it("Disables FirstPage and PreviousPage buttons on first page of results", () => {
    const firstPage = searchPagination(20, 0, 10);

    expect(firstPageButton(firstPage).prop("disabled")).toBeTruthy();
    expect(previousPageButton(firstPage).prop("disabled")).toBeTruthy();

    expect(nextPageButton(firstPage).prop("disabled")).toBeFalsy();
    expect(lastPageButton(firstPage).prop("disabled")).toBeFalsy();
  });

  it("Disables LastPage and NextPage buttons on last page of results", () => {
    const lastPage = searchPagination(20, 1, 10);

    expect(firstPageButton(lastPage).prop("disabled")).toBeFalsy();
    expect(previousPageButton(lastPage).prop("disabled")).toBeFalsy();

    expect(nextPageButton(lastPage).prop("disabled")).toBeTruthy();
    expect(lastPageButton(lastPage).prop("disabled")).toBeTruthy();
  });
});
