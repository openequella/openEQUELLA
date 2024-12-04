package com.tle.webtests.test.searching;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.google.common.io.Closeables;
import com.tle.webtests.framework.SoapHelper;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.framework.soap.SoapService50;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.searching.SearchScreenOptions;
import com.tle.webtests.pageobject.searching.SearchSettingsPage;
import com.tle.webtests.pageobject.searching.SearchSettingsPage.Order;
import com.tle.webtests.pageobject.searching.ShareSearchQuerySection;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("flakey")
public class SearchSettingsTest extends AbstractCleanupAutoTest {
  private SoapService50 soapService;
  private SoapHelper soapHelper;

  @Override
  protected boolean isCleanupItems() {
    return false;
  }

  @DataProvider(name = "filters", parallel = false)
  public Object[][] filters() {
    return new Object[][] {
      {"JPEG/JPG", "image/jpeg", 1},
      {"PNG", "image/png", 2},
      {"BMP", "image/bmp", 3},
      {"GIF", "image/gif", 4},
      {"TIFF", "image/tiff", 5}
    };
  }

  // Test the sort order options
  @Test
  public void testChangeResultOrder() {
    SettingsPage sp = new SettingsPage(context).load();

    // Load SearchSettings Page by clicking settings link
    SearchSettingsPage searchSettingsPage = sp.searchSettings();
    Order sortOption = SearchSettingsPage.Order.rank;
    searchSettingsPage.setOrder(sortOption).save();

    logon("AutoTest", "automated");

    SearchPage searchPage = new SearchPage(context).load();

    assertTrue(searchPage.ensureSortSelected(sortOption.name()));

    sortOption = Order.datemodified;
    searchSettingsPage = new SettingsPage(context).load().searchSettings();
    searchSettingsPage.setOrder(sortOption).save();

    logon("AutoTest", "automated");

    searchPage = new SearchPage(context).load();

    assertTrue(searchPage.ensureSortSelected(sortOption.name()));

    sortOption = SearchSettingsPage.Order.name;
    searchSettingsPage = new SettingsPage(context).load().searchSettings();
    searchSettingsPage.setOrder(sortOption).save();

    logon("AutoTest", "automated");

    searchPage = new SearchPage(context).load();

    assertTrue(searchPage.ensureSortSelected(sortOption.name()));

    sortOption = SearchSettingsPage.Order.rating;
    searchSettingsPage = new SettingsPage(context).load().searchSettings().get();

    searchSettingsPage.setOrder(sortOption).save();

    logon("AutoTest", "automated");

    searchPage = new SearchPage(context).load();

    assertTrue(searchPage.ensureSortSelected(sortOption.name()));
  }

  // Test the show non live options
  @Test
  public void testShowNonLive() {
    logon("AutoTest", "automated");
    SearchSettingsPage ssp = new SettingsPage(context).load().searchSettings().load();
    ssp.includeNonLive(true).save();

    SearchPage searchPage = new SearchPage(context).load();
    SearchScreenOptions sso = searchPage.openScreenOptions();
    assertTrue(sso.hasNonLiveOption());
    sso.setNonLiveOption(true);
    final String ITEM_NAME = "SearchSettings - Draft Item";
    ItemListPage results = searchPage.search('"' + ITEM_NAME + '"');
    assertTrue(results.doesResultExist(ITEM_NAME, 1));

    ssp = new SettingsPage(context).load().searchSettings();
    ssp.includeNonLive(false).save();

    sso = new SearchPage(context).load().openScreenOptions();
    assertTrue(!(sso.hasNonLiveOption()));

    results = SearchPage.searchExact(context, "SearchSettings - Draft Item");
    assertTrue(!results.doesResultExist("SearchSettings - Draft Item", 1));
  }

  @Test
  public void testDisableGallery() {
    // Disable Images Gallery view
    logon("AutoTest", "automated");
    new SettingsPage(context).load().searchSettings().load().setDisableImageGallery(true).save();

    // Go to search page, test that gallery is disabled
    SearchPage searchPage = new SearchPage(context).load();
    assertFalse(searchPage.isImagesLinkAvailable());

    // Now reenable Images Gallery
    new SettingsPage(context).load().searchSettings().load().setDisableImageGallery(false).save();

    // Go to search page, test that gallery is enabled
    searchPage = new SearchPage(context).load();
    assertTrue(searchPage.isImagesLinkAvailable());
  }

  // Test Authenticated feeds
  @Test
  public void testGenerateAuthenticatedFeeds() throws Exception {
    logon("AutoTest", "automated");
    final String searchTerm = "Relevance";
    final String authString = "auth=basic";

    // Login
    soapService.login("AutoTest", "automated");

    // Do a search and get RSS url
    SearchPage searchPage = new SearchPage(context).load();
    searchPage.search(searchTerm);
    ShareSearchQuerySection ssDialog = searchPage.shareSearch();
    String rssUrl = ssDialog.getRssUrl();
    String atomUrl = ssDialog.getAtomUrl();

    assertTrue(!rssUrl.contains(authString));
    assertTrue(!atomUrl.contains(authString));

    // Check results
    Credentials basicCreds = new UsernamePasswordCredentials("AutoTest", "automated");
    assertTrue(checkRssResponse(getResponse(soapService, rssUrl, null), true));
    assertTrue(checkAtomResponse(getResponse(soapService, atomUrl, null), true));

    // Enable authenticated results
    SearchSettingsPage ssp = new SettingsPage(context).load().searchSettings();
    ssp.setGenerateAuthFeeds(true).save();

    // Do a search and get RSS url
    searchPage = new SearchPage(context).load();
    searchPage.search(searchTerm);
    ssDialog = searchPage.shareSearch();
    rssUrl = ssDialog.getRssUrl();
    atomUrl = ssDialog.getAtomUrl();
    assertTrue(rssUrl.contains(authString));
    assertTrue(atomUrl.contains(authString));

    // Check results
    assertTrue(checkRssResponse(getResponse(soapService, rssUrl, basicCreds), false));
    assertTrue(checkAtomResponse(getResponse(soapService, atomUrl, basicCreds), false));
  }

  private boolean checkRssResponse(PropBagEx response, boolean single) {
    final String itemRandom = "SearchSettings - Random Item";
    final String itemRelevance = "SearchSettings - Relevance";

    if (single) {
      String title = response.getNode("channel/item/title");
      return title.equals(itemRandom);
    } else {
      PropBagIterator iter = response.iterator("channel/item");
      List<String> itemTitles = new ArrayList<String>();
      for (PropBagEx item : iter) {
        itemTitles.add(item.getNode("title"));
      }
      return itemTitles.contains(itemRandom) && itemTitles.contains(itemRelevance);
    }
  }

  private boolean checkAtomResponse(PropBagEx response, boolean single) {
    final String itemRandom = "SearchSettings - Random Item";
    final String itemRelevance = "SearchSettings - Relevance";

    if (single) {
      String title = response.getNode("entry/title");
      return title.equals(itemRandom);
    } else {
      PropBagIterator iter = response.iterator("entry");
      List<String> itemTitles = new ArrayList<String>();
      for (PropBagEx item : iter) {
        itemTitles.add(item.getNode("title"));
      }
      return itemTitles.contains(itemRandom) && itemTitles.contains(itemRelevance);
    }
  }

  private PropBagEx getResponse(SoapService50 soapService, String uri, Credentials creds)
      throws Exception {
    HttpGet get = new HttpGet(uri);
    if (creds != null) {
      get.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false));
    }

    HttpClient client = new DefaultHttpClient();
    InputStream in = client.execute(get).getEntity().getContent();
    try {
      return new PropBagEx(in);
    } finally {
      Closeables.closeQuietly(in);
    }
  }

  @BeforeClass
  public void setupSoapService() throws MalformedURLException {
    soapHelper = new SoapHelper(context);
    soapService =
        soapHelper.createSoap(
            SoapService50.class,
            "services/SoapService50",
            "http://soap.remoting.web.tle.com",
            null);
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    logon("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();

    // Load SearchSettings Page by clicking settings link
    SearchSettingsPage searchSettingsPage = sp.searchSettings();
    Order sortOption = SearchSettingsPage.Order.rank;
    searchSettingsPage.setOrder(sortOption).save();

    super.cleanupAfterClass();
  }
}
