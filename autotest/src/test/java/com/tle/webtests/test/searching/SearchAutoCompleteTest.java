package com.tle.webtests.test.searching;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.AutoCompleteOptions;
import com.tle.webtests.pageobject.searching.QuerySection;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import java.text.ParseException;
import java.util.List;
import org.openqa.selenium.Keys;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class SearchAutoCompleteTest extends AbstractCleanupAutoTest {

  private static final String QUERY_NOAUTOCOMPLETE = "X";

  private static final String SHORT_ITEM = "drm allowing composition - package";
  private static final String SHORT_ITEM_ESCAPED = "drm allowing composition \\- package";
  private static final String LONG_ITEM =
      "long words - pneumonoultramicroscopicsilicovolcanoconiosis"
          + " supercalifragilisticexpialidocious pseudopseudohypoparathyroidism";

  private static final String STEMMING_QUERY = "searchstemming";
  private static final String STEMMING_RESULT = STEMMING_QUERY + " - walk";

  private static final String FILTER_QUERY = "searchautocompletefilter";
  private static final String FILTER_DATE_RESULT = FILTER_QUERY + " - datefiltertest";
  private static final String FILTER_SUSPENDED_RESULT = FILTER_QUERY + " - suspended";

  @Override
  protected boolean isCleanupItems() {
    return false;
  }

  @Test
  public void testAutoCompleteKeyActions() {
    // Navigate to search page
    SearchPage sp = new SearchPage(context).load();

    // Set sort for predictable order
    sp.setSort("name");

    // Set the query to partial short item name
    String partialQuery = SHORT_ITEM.substring(0, 25);

    // Check prompt text
    QuerySection qs = new QuerySection(context).get();
    AutoCompleteOptions ac = qs.autoCompleteOptions(partialQuery, SHORT_ITEM);

    assertEquals(ac.getPromptText(), SHORT_ITEM);
    assertListEquals(
        ac.getAutoCompleteOptions(),
        ImmutableList.of(
            SHORT_ITEM,
            "link to drm allowing composition - package",
            "link to summary of drm allowing composition - package",
            "summary drm - link to drm allowing composition - package"));

    // Complete with Tab
    ac.complete(Keys.TAB);
    assertEquals(qs.getQueryText(), SHORT_ITEM_ESCAPED);

    // Do search and set to partial
    sp.search();
    ac = qs.autoCompleteOptions(partialQuery, SHORT_ITEM);

    // Check prompt text
    assertEquals(ac.getPromptText(), SHORT_ITEM);

    // Complete with Right Arrow
    ac.complete(Keys.ARROW_RIGHT);
    assertEquals(qs.getQueryText(), SHORT_ITEM_ESCAPED);

    // Up/Down Arrows
    sp.search();
    ac = qs.autoCompleteOptions(STEMMING_QUERY, STEMMING_RESULT);

    List<String> opts = ac.getAutoCompleteOptions();
    assertKeyboardNav(qs, Keys.ARROW_DOWN, opts);

    sp.search();
    ac = qs.autoCompleteOptions(STEMMING_QUERY, STEMMING_RESULT);

    assertKeyboardNav(qs, Keys.ARROW_UP, Lists.reverse(opts));
    // Set the query to partial long item name
    // Super flakey...
    partialQuery = LONG_ITEM.substring(0, 52);
    ac = qs.autoCompleteOptions(partialQuery, LONG_ITEM);

    // Check there is no prompt text when name is longer than queryfield
    // The width of queryfield is fixed as 682 in old UI
    final int PROMPT_WIDTH = 682;
    if (ac.getPromptField().getSize().width <= PROMPT_WIDTH) {
      assertEquals(ac.getPromptText(), "");
    } else {
      // New UI has enough spaces for this long string so we should expect the whole string
      // displayed
      assertEquals(ac.getPromptText(), LONG_ITEM);
    }

    // Check single term
    qs.setQuery(QUERY_NOAUTOCOMPLETE);
    ac.waitUntilDisappeared();

    ac = qs.autoCompleteOptions(SHORT_ITEM + " " + "compos", "composers");
    assertListEquals(ac.getAutoCompleteOptions(), ImmutableList.of("composers"));
  }

  @Test
  public void testFilteringOnAutoComplete() throws ParseException {
    logon("AutoTest", "automated");

    SearchPage sp = new SearchPage(context).load();
    QuerySection qs = new QuerySection(context).get();
    AutoCompleteOptions autoCompleteOptions =
        qs.autoCompleteOptions(FILTER_QUERY, FILTER_DATE_RESULT);
    assertListEquals(
        autoCompleteOptions.getAutoCompleteOptions(), ImmutableList.of(FILTER_DATE_RESULT));

    qs.setQuery(QUERY_NOAUTOCOMPLETE);
    autoCompleteOptions.waitUntilDisappeared();

    // Set sort for predictable order
    sp.setSort("name");

    sp.setIncludeNonLive(true);
    autoCompleteOptions = qs.autoCompleteOptions(FILTER_QUERY, FILTER_DATE_RESULT);
    assertListEquals(
        autoCompleteOptions.getAutoCompleteOptions(),
        ImmutableList.of(FILTER_DATE_RESULT, FILTER_SUSPENDED_RESULT));
  }

  private void assertKeyboardNav(QuerySection qs, Keys key, List<String> opts) {
    assertTrue(opts.size() == 4, "Expecting 4 options. Found: " + opts.size());

    for (String option : opts) {
      qs.typeKeys(key);
      String escapedOption = option.replaceAll("-", "\\\\-");
      assertEquals(qs.getQueryText(), escapedOption);
    }
  }
}
