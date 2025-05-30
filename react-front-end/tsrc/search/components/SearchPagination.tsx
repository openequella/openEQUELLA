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
import { createTheme, styled } from "@mui/material/styles";
import { Grid, IconButton, TablePagination } from "@mui/material";
import FirstPage from "@mui/icons-material/FirstPage";
import KeyboardArrowLeft from "@mui/icons-material/KeyboardArrowLeft";
import KeyboardArrowRight from "@mui/icons-material/KeyboardArrowRight";
import LastPage from "@mui/icons-material/LastPage";
import type { TablePaginationProps } from "@mui/material/TablePagination/TablePagination";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";

const StyledTablePagination = styled(TablePagination)<TablePaginationProps>({
  "& .MuiTablePagination-toolbar": {
    display: "flex",
    justifyContent: "center",
  },
  "& .MuiTablePagination-spacer": {
    display: "none",
  },
});

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
    <StyledTablePagination
      component="div"
      count={count}
      page={currentPage}
      onPageChange={(_, page: number) => onPageChange(page)}
      rowsPerPageOptions={[10, 25, 50]}
      labelRowsPerPage={paginationStrings.itemsPerPage}
      rowsPerPage={rowsPerPage}
      onRowsPerPageChange={(event) =>
        onRowsPerPageChange(parseInt(event.target.value))
      }
      ActionsComponent={PaginationActions}
    />
  );

  /**
   * Provides pagination navigation controls.
   * @return {ReactElement} IconButtons for navigating to First, Previous, Next, and Last Page of results
   */
  function PaginationActions() {
    const theme = createTheme();
    const numberOfPages = Math.ceil(count / rowsPerPage);
    const lastPage = Math.max(0, numberOfPages - 1);

    const isFirstPage = currentPage === 0;
    const isLastPage = currentPage >= numberOfPages - 1;

    return (
      <Grid
        container
        direction="row"
        justifyContent="center"
        wrap="nowrap"
        style={{ marginLeft: theme.spacing(2) }}
      >
        <Grid>
          <IconButton
            onClick={() => onPageChange(0)}
            disabled={isFirstPage}
            aria-label={paginationStrings.firstPageButton}
            id="firstPageButton"
            size="large"
          >
            <FirstPage />
          </IconButton>
        </Grid>
        <Grid>
          <IconButton
            onClick={() => onPageChange(currentPage - 1)}
            aria-label={paginationStrings.previousPageButton}
            disabled={isFirstPage}
            id="previousPageButton"
            size="large"
          >
            <KeyboardArrowLeft />
          </IconButton>
        </Grid>
        <Grid>
          <IconButton
            onClick={() => onPageChange(currentPage + 1)}
            aria-label={paginationStrings.nextPageButton}
            disabled={isLastPage}
            id="nextPageButton"
            size="large"
          >
            <KeyboardArrowRight />
          </IconButton>
        </Grid>
        <Grid>
          <IconButton
            onClick={() => onPageChange(lastPage)}
            aria-label={paginationStrings.lastPageButton}
            disabled={isLastPage}
            id="lastPageButton"
            size="large"
          >
            <LastPage />
          </IconButton>
        </Grid>
      </Grid>
    );
  }
};
