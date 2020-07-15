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
import { TablePagination } from "@material-ui/core";
import { languageStrings } from "../../util/langstrings";

export interface SearchPaginationProps {
  count: number;
  currentPage: number;
  rowsPerPage: number;
  onPageChange: (currentPage: number) => void;
  onRowsPerPageChange: (rowsPerPage: number) => void;
}
export const SearchPagination = ({
  count,
  currentPage,
  rowsPerPage,
  onPageChange,
  onRowsPerPageChange,
}: SearchPaginationProps) => {
  const paginationStrings = languageStrings.searchpage.pagination;
  return (
    <TablePagination
      component="div"
      count={count}
      page={currentPage}
      onChangePage={(_, page: number) => onPageChange(page)}
      rowsPerPageOptions={[10, 25, 50]}
      labelRowsPerPage={paginationStrings.itemsPerPage}
      rowsPerPage={rowsPerPage}
      onChangeRowsPerPage={(event) =>
        onRowsPerPageChange(parseInt(event.target.value))
      }
    />
  );
};
