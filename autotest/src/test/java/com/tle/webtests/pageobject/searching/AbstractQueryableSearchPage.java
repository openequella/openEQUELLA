package com.tle.webtests.pageobject.searching;

import com.google.common.base.Function;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import org.openqa.selenium.WebDriver;

public abstract class AbstractQueryableSearchPage<
        T extends AbstractQueryableSearchPage<T, RL, SR>,
        RL extends AbstractResultList<RL, SR>,
        SR extends SearchResult<SR>>
    extends AbstractSearchPage<T, RL, SR> {

  protected AbstractQuerySection<?> querySection;

  public AbstractQueryableSearchPage(PageContext context) {
    this(context, -1);
  }

  public AbstractQueryableSearchPage(PageContext context, int timeout) {
    super(context, timeout);
    querySection = createQuerySection();
  }

  protected AbstractQuerySection<?> createQuerySection() {
    return new QuerySection(context);
  }

  public RL search(PrefixedName query) {
    return search(query.toString());
  }

  @Override
  public RL search(String query) {
    return querySection.get().search(query, resultsPageObject.getUpdateWaiter());
  }

  @Override
  public RL exactQuery(String query) {
    return exactQuery(query, 0);
  }

  // If an institution import is occurring at the same time as a bulk
  // operation (eg clone), there can be a small delay before it will be
  // indexed
  public RL exactQuery(String query, int minExpected) {
    int MAX_TRIES = 2;
    RL basicQuery = search('"' + query + '"');
    int tries = 0;
    while (tries < MAX_TRIES && basicQuery.getResults().size() < minExpected) {
      tries++;
      basicQuery = search('"' + query + '"');
    }
    return basicQuery;
  }

  // If a previously indexed result is expected to disappear, then use this
  public RL exactQueryExpectNothing(PrefixedName query) {
    int MAX_TRIES = 2;
    RL basicQuery = search('"' + query.toString() + '"');
    int tries = 0;
    while (tries < MAX_TRIES && basicQuery.getResults().size() > 0) {
      tries++;
      sleepyTime(1000);
      basicQuery = search('"' + query.toString() + '"');
    }
    return basicQuery;
  }

  public T setQuery(PrefixedName query) {
    return setQuery(query.toString());
  }

  public T setQuery(String query) {
    querySection.get().setQuery(query);
    return actualPage();
  }

  public String getQuery() {
    return querySection.get().getQueryText();
  }

  public RL waitForResult(final String title, final int index) {
    waiter.until(
        new Function<WebDriver, Object>() {
          @Override
          public Boolean apply(WebDriver arg0) {
            try {
              return exactQuery(title).doesResultExist(title, index);
            } catch (Exception e) {
              return Boolean.FALSE;
            }
          }
        });

    return exactQuery(title);
  }
}
