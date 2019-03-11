package com.tle.webtests.remotetest.contribute.controls.attachments;

import static org.testng.Assert.assertTrue;

import com.tle.common.Pair;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FlickrUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.FlickrUniversalControlType.SearchType;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test Reference: http://time/DTEC/test/editTest.aspx?testId=15123 Date control assumed tested
 * elsewhere. TODO flickr userid search, add to collection, edit display name
 *
 * @author larry
 */
@TestInstitution("flickrindepth")
public class FlickrFunctionTest extends AbstractCleanupAutoTest {
  /**
   * Contribution wizard for the collection assumed to have only 1 wizard page. Flickr attachment
   * control presumed to be mandatory, hence we can save and publish if we have added a flickr
   * attachment, otherwise only save to draft (or complete wizard / cancel)
   */
  @Test
  public void basicSearchAndAddFlickrPhoto() {
    // Arirang is a photographer's fantasy. There's hundreds of thousands of
    // pix of this on Flickr,
    // and hundreds with this full suite of keywords in their flickr-tag
    // set.
    // Photos matching any tag, (ie Korea not north, Pyongyang without
    // Arirang) are in the millions.
    String searchKeywords = "Arirang North Korea DPRK Pyongyang";
    FlickrUniversalControlType flickrControl = searchPage();
    ItemListPage searchResultsPage =
        flickrControl.performSearch(searchKeywords, SearchType.GENERAL_ANY_TAGS);
    int howManyAnyTags = searchResultsPage.getTotalAvailable();
    assertTrue(
        howManyAnyTags > 1000,
        " We're relying on a large (1000+) returned result from '"
            + searchKeywords
            + "' but there's only "
            + howManyAnyTags);
    searchResultsPage = flickrControl.performSearch(searchKeywords, SearchType.GENERAL);

    int howManyTextAndTags = searchResultsPage.getTotalAvailable();
    searchResultsPage = flickrControl.performSearch(searchKeywords, SearchType.GENERAL_ALL_TAGS);
    int howManyAllTags = searchResultsPage.getTotalAvailable();
    // For any reasonably large-scope search (and Arirang is such) then we
    // would expect the
    // search on any tags to bring back the largest result set, followed
    // by general search, text and tags included, with the least for
    // all-tags
    // {{howManyAnyTags > howManyTextAndTags}}? In practise this is probably
    // true, but not infallibly.
    assertTrue(howManyTextAndTags > howManyAllTags);
    assertTrue(howManyAnyTags > howManyAllTags);

    flickrControl.close();
  }

  /**
   * Make use of the licence filter and ensure returned results conform to expectations. It would be
   * nice if they did, but we cannot rely entirely on Flickr's result set. Cases are observed where
   * an "unrestricted" licenced item is returned within a filtered licence query, which is something
   * EQUELLA cannot be liable for. For a basic sanity check, we expect the majority of returned
   * items to conform to the licence restriction in the filter.
   */
  @Test
  public void creativeCommonsFilterTest() {
    String searchKeywords = "Arirang North Korea DPRK Pyongyang";
    FlickrUniversalControlType flickrControl = searchPage();
    // To get a reliable sample, we use the widest search possible -
    // ANY_TAGS
    ItemListPage searchResultsPage =
        flickrControl.performSearch(searchKeywords, SearchType.GENERAL_ANY_TAGS);
    int howManyAnyTags = searchResultsPage.getTotalAvailable();
    assertTrue(
        howManyAnyTags > 1000,
        " We're relying on a large (1000+) returned result from '"
            + searchKeywords
            + "' but there's only "
            + howManyAnyTags);

    int sumLicencesAllOptions = 0;
    List<Pair<String, String>> licencesTextAltPairs = flickrControl.getLicenceOptions();
    for (Pair<String, String> textAltPair : licencesTextAltPairs) {
      // Search on one licence at a time
      String[] licencesToSearchOn = new String[1];
      licencesToSearchOn[0] = textAltPair.getFirst();
      String expectedLicenceText = textAltPair.getSecond();
      searchResultsPage = flickrControl.useLicencesToSearch(licencesToSearchOn);
      int howManyThisOption = searchResultsPage.getTotalAvailable();
      sumLicencesAllOptions += howManyThisOption;
      // unrestricted is a tight filter, so even on a s large a result set
      // as these search terms returns,
      // we may still only get a small number (in the hundreds)
      assertTrue(howManyAnyTags >= howManyThisOption);
      // On this page?
      int failedElements = 0, asExpectedElements = 0;
      int sampleSize = searchResultsPage.getResults().size();
      for (int ordinal = 1; ordinal <= sampleSize; ++ordinal) {
        ItemSearchResult aVisible = searchResultsPage.getResult(ordinal);
        String licenceVal = aVisible.getDetailText("Licen", 4);

        if (licenceVal.equals(expectedLicenceText)) {
          asExpectedElements++;
        } else {
          failedElements++;
        }
      }
      assertTrue(
          asExpectedElements > failedElements,
          "Expected at least a majority of licence texts to match expected value '"
              + expectedLicenceText
              + "', but only "
              + asExpectedElements
              + " out of "
              + sampleSize
              + " do so.");

      // Clear filters afterwards
      searchResultsPage = flickrControl.resetFilters();
    }
    assertTrue(
        howManyAnyTags >= sumLicencesAllOptions,
        "Expected unchecked total("
            + howManyAnyTags
            + ") to be greater than or possibly equal to sum of single options ("
            + sumLicencesAllOptions
            + ")");
    flickrControl.close();
  }

  /** Search a couple of the institutions */
  @Test
  public void flickrInstitutionsTest() {
    String searchKeywords = "Photograph"; // pretty reliable as a common
    // string in the text
    FlickrUniversalControlType flickrControl = searchPage();
    // To get a reliable sample, we use the widest search possible -
    // ANY_TAGS
    ItemListPage searchResultsPage =
        flickrControl.performSearch(searchKeywords, SearchType.GENERAL);
    int howManyAnyTags = searchResultsPage.getTotalAvailable();
    assertTrue(
        howManyAnyTags > 1000,
        " We're relying on a large (1000+) returned result from '"
            + searchKeywords
            + "' but there's only "
            + howManyAnyTags);

    List<String> availableInstitutions = flickrControl.getInstitutionNames();
    assertTrue(availableInstitutions.size() > 3, "Expected at least 3 institution options");
    // we can expect the first option to be the default [Any], so try
    // a couple of searches past 1, so the next inst after the first slot
    // ...
    searchResultsPage = flickrControl.useInstitution(availableInstitutions.get(1));
    int howManyThisInstitution = searchResultsPage.getTotalAvailable();
    // ... and the last institution
    searchResultsPage =
        flickrControl.useInstitution(availableInstitutions.get(availableInstitutions.size() - 1));
    int howManyThatInstitution = searchResultsPage.getTotalAvailable();
    // we're counting on at least one of these having a value
    assertTrue(
        howManyThisInstitution > 0 || howManyThatInstitution > 0,
        "Expected at least one of our institutions ("
            + availableInstitutions.get(1)
            + ", "
            + availableInstitutions.get(availableInstitutions.size() - 1)
            + ") to return a non-zero result");
    flickrControl.close();
  }

  /** http://dev.equella.com/issues/7288 */
  @Test
  public void testNoSelections() {
    FlickrUniversalControlType flickrControl = searchPage();
    flickrControl.performSearch("funny cat", SearchType.GENERAL);
    Assert.assertFalse(flickrControl.canAdd(), "Able to add with no selections");

    UniversalControl close = flickrControl.close();
    close.getPage().cancel(new ContributePage(context));
  }

  private FlickrUniversalControlType searchPage() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizard = contributePage.openWizard(GENERIC_TESTING_COLLECTION);
    wizard.editbox(1, context.getFullName(" first flickr item"));
    wizard.editbox(2, this.getClass().getSimpleName() + " goes agathering");
    UniversalControl control = wizard.universalControl(3);
    return control.addDefaultResource(new FlickrUniversalControlType(control));
  }
}
