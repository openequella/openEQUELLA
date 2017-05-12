package com.tle.webtests.test.payment.storefront;

import static com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront.STORE_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueResourcePage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.pageobject.viewitem.ImagePage;
import com.tle.webtests.test.AbstractSessionTest;

/**
 * @see DTEC: #017996
 * @author Dustin
 */

@TestInstitution("storefront")
public class PreviewItemTest extends AbstractSessionTest
{
	// PreviewAttachmentTest will cover the setting of previews, here we just
	// test whether they work (They've been broken a bazillion times before)
	private static final String ITEM_NAME = "PreviewTest Item";

	private static final PrefixedName CATALOGUE_NAME = new NotPrefixedName("cat1");
	private static final String IMAGE = "11jul.jpg";
	private static final String DOCUMENT = "Pearson Professional Services Asia Pac Org Chart 2012-06-22.pdf";
	private static final String URL = "http://www.google.com";
	private static final String WEB_PAGE = "My page";
	private static final String GOOGLE_BOOK = "Is Google the Next Stage of Evolution of Life on Earth?";
	private static final String YOUTUBE = "The Dark Knight Rises \"13 Minute Preview\" [HD]: Chrisopher Nolan, Christian Bale & More";
	private static final String FLIKR = "Preview.";

	private static final String[] PREVIEWABLES = {DOCUMENT, IMAGE, URL, GOOGLE_BOOK, YOUTUBE, FLIKR};
	private static final String[][] LINK_PREVIEWABLES = {{URL, "google.com"}, {GOOGLE_BOOK, "books.google.com"},
			{YOUTUBE, "youtube.com"}, {FLIKR, "flickr.com"}};

	// Let's just hope these sites don't go away (I think we'll be alright)

	@Test
	public void previewTest()
	{
		logon("autotest", "automated");

		ShopPage shopPage = new ShopPage(context).load();
		BrowseCataloguePage browseCatPage = shopPage.pickStoreSingleCatalogue(STORE_NAME, CATALOGUE_NAME);
		CatalogueResourcePage resourcePage = browseCatPage.search(ITEM_NAME).getResult(1).viewSummary();

		assertFalse(resourcePage.isPreview(WEB_PAGE));
		for( String name : PREVIEWABLES )
		{
			assertTrue(resourcePage.isPreview(name));
		}

		ImagePage image = resourcePage.previewAttachment(IMAGE, new ImagePage(context));
		assertEquals(image.getDimensions().width, 370);
		assertEquals(image.getDimensions().height, 400);

		resourcePage = goBack(resourcePage);
		/*
		 * DownloadFilePage fileDownload = new DownloadFilePage(context,
		 * DOCUMENT, "4bb2ca74ec7231208bc61c98c9f2a9e3");
		 * fileDownload.deleteFile(); resourcePage.previewAttachment(DOCUMENT);
		 * assertTrue(fileDownload.get().fileIsDownloaded());
		 * fileDownload.deleteFile();
		 */

		// Annoyingly I don't think we can test this, because it's not a real
		// attachment with a viewableResource it won't download properly,
		// if you know how to do it be my guest

		for( String[] att : LINK_PREVIEWABLES )
		{
			// Tests that the canonical URL is getting set ok (Otherwise we'd
			// probably only bother doing it once)
			String previewLink = resourcePage.getPreviewLink(att[0]);
			assertTrue(previewLink.contains(att[1]), "URL: " + previewLink + " doesn't contain " + att[1]);
		}
	}
}
