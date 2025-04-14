package com.tle.webtests.test.searching;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import java.util.List;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class SearchEnhancementsTest extends AbstractCleanupAutoTest {
  @Override
  protected boolean isCleanupItems() {
    return false;
  }

  // Check stemming is working correctly
  @Test
  public void testStemming() {
    // Expected results
    final String STEMMING_ITEM_PREFIX = "SearchStemming - ";
    final List<String> queries = Lists.newArrayList("Walking", "Walked", "Walks", "Walk");

    List<String> expected = Lists.newArrayList();
    for (String query : queries) {
      expected.add(STEMMING_ITEM_PREFIX + query);
    }

    // Search for each term
    SearchPage sp = new SearchPage(context).load();

    for (String query : queries) {
      sp.exactQuery(query);

      // Ensure that all 4 results exist for each search
      checkResults(sp.results(), expected, null);
    }
  }

  @Test
  public void testHyphen() {
    // Basic Hyphen tests
    List<String> expected =
        Lists.newArrayList(
            "SearchSettings - Image 1 - JPEG/JPG",
            "SearchSettings - Image 2 - PNG",
            "SearchSettings - Image 3 - BMP",
            "SearchSettings - Image 4 - GIF");
    List<String> notExpected = Lists.newArrayList("SearchSettings - Image 5 - TIFF");
    SearchPage sp = new SearchPage(context).load();

    // Check basic query (no quotes) returns result with hyphen
    sp.search(expected.get(0));
    assertTrue(sp.results().doesResultExist(expected.get(0), 1));
    sp.search("SearchSettings - - Image 2 - - - PNG");
    assertTrue(sp.results().doesResultExist(expected.get(1), 1));
    sp.search("SearchSettings -- Image 3 -- BMP");
    assertTrue(sp.results().doesResultExist(expected.get(2), 1));
    sp.search("SearchSettings-- Image 4-- GIF");
    assertTrue(sp.results().doesResultExist(expected.get(3), 1));

    // Check prohibiting basic
    sp.search("SearchSettings - Image -TIFF");
    assertTrue(sp.results().getResults().size() == 4);
    checkResults(sp.results(), expected, notExpected);
    // Check prohibiting exact
    sp.search("\"SearchSettings - Image\" -TIFF");
    assertTrue(sp.results().getResults().size() == 4);
    checkResults(sp.results(), expected, notExpected);

    // Check prohibiting phrases
    expected =
        Lists.newArrayList(
            "Summary DRM - Link to DRM Allowing Composition - Package",
            "Summary DRM - Link to DRM Show On Summary and Allowing Composition - Package");
    notExpected =
        Lists.newArrayList(
            "Summary DRM - Link to DRM Require Composition Acceptance - Package",
            "Summary DRM - Link to DRM Show On Summary and Require Composition Acceptance -"
                + " Package");

    sp.search("\"Summary DRM - Link to DRM\" -\"Require Composition Acceptance\"");
    checkResults(sp.results(), expected, notExpected);
  }

  @Test
  public void testExclamation() {
    // Basic Exclamation tests
    List<String> expected =
        Lists.newArrayList(
            "ExclamationTest ! Cat!", "ExclamationTest ! Dog!", "ExclamationTest ! Rat!");
    List<String> notExpected = Lists.newArrayList("ExclamationTest ! Crazy! Horse!");

    SearchPage sp = new SearchPage(context).load();
    sp.search(expected.get(0));
    assertTrue(sp.results().doesResultExist(expected.get(0), 1));
    sp.search("ExclamationTest !!! !! Dog!");
    assertTrue(sp.results().doesResultExist(expected.get(1), 1));
    sp.search("ExclamationTest Rat!!!");
    assertTrue(sp.results().doesResultExist(expected.get(2), 1));

    // Check prohibiting basic
    sp.search("ExclamationTest ! !Crazy");
    assertEquals(sp.results().getResults().size(), 3, "Unexpected result count");
    checkResults(sp.results(), expected, notExpected);
    // Check prohibiting exact
    sp.search("\"ExclamationTest\" !Horse!");
    assertEquals(sp.results().getResults().size(), 3, "Unexpected result count");
    checkResults(sp.results(), expected, notExpected);

    // Check prohibiting phrases using exclamation
    expected =
        Lists.newArrayList(
            "Summary DRM - Link to DRM Allowing Composition - Package",
            "Summary DRM - Link to DRM Show On Summary and Allowing Composition - Package");
    notExpected =
        Lists.newArrayList(
            "Summary DRM - Link to DRM Require Composition Acceptance - Package",
            "Summary DRM - Link to DRM Show On Summary and Require Composition Acceptance -"
                + " Package");

    sp.search("\"Summary DRM - Link to DRM\" !\"Require Composition Acceptance\"");
    checkResults(sp.results(), expected, notExpected);
  }

  private void checkResults(ItemListPage results, List<String> expected, List<String> notExpected) {
    for (String name : expected) {
      assertTrue(results.doesResultExist(name), "Expecting '" + name);
    }
    if (!Check.isEmpty(notExpected)) {
      for (String name : notExpected) {
        assertFalse(results.doesResultExist(name, 1), "Not expecting '" + name);
      }
    }
  }
}
