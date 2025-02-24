package com.tle.core.dao.helpers;

import java.util.Optional;

public class Pagination {
  private Integer offset;
  private Integer limit;

  private Pagination() {}

  public Optional<Integer> getOffset() {
    return Optional.ofNullable(offset);
  }

  public Optional<Integer> getLimit() {
    return Optional.ofNullable(limit);
  }

  public static Pagination of(Integer offset, Integer limit) {
    Pagination paginationDetails = new Pagination();
    paginationDetails.offset = offset;
    paginationDetails.limit = limit;

    return paginationDetails;
  }

  /**
   * Supports the legacy method of specifying no limits on offset and limit by using negative
   * values.
   *
   * @param offset The offset (-1 for no offset)
   * @param limit The limit (-1 for no limit)
   * @return The pagination details
   */
  public static Pagination ofLegacy(int offset, int limit) {
    return of(convertLegacy(offset), convertLegacy(limit));
  }

  private static Integer convertLegacy(int value) {
    return value < 0 ? null : value;
  }
}
