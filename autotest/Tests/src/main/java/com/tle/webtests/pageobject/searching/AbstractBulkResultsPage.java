package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;

public abstract class AbstractBulkResultsPage<
        T extends AbstractBulkResultsPage<T, RL, SR>,
        RL extends AbstractResultList<RL, SR>,
        SR extends SearchResult<SR>>
    extends AbstractQueryableSearchPage<T, RL, SR> {
  private final BulkSection bulkSection;

  public AbstractBulkResultsPage(PageContext context) {
    super(context);
    bulkSection = new BulkSection(this);
  }

  public BulkSection bulk() {
    return bulkSection.get();
  }
}
