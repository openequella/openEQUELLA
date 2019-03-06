package com.tle.resttests.test.item;

import static com.tle.resttests.util.RestTestConstants.USERID_AUTOTEST;
import static com.tle.resttests.util.RestTestConstants.USERID_RESTNOPRIV;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Pair;
import com.tle.json.entity.ItemId;
import com.tle.resttests.AbstractItemApiTest;

public class ItemApiViewTest extends AbstractItemApiTest
{
	private static final String DRMITEM_ID = "4de9dc1e-9740-4d6a-b848-cf5712882ce6";
	private static final String ITEM_UUID = "2f6e9be8-897d-45f1-98ea-7aa31b449c0e";
	private static final String COLLECTION_ID = "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c";

	private static final ItemId ITEM_ID = new ItemId(ITEM_UUID, 1);

	private static final String OAUTH_CLIENT_ID = "IAVTClient";
	private static final String OAUTH_NOPRIVS_CLIENT_ID = "IAVTNoPrivsClient";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, USERID_AUTOTEST));
		clients.add(new Pair<String, String>(OAUTH_NOPRIVS_CLIENT_ID, USERID_RESTNOPRIV));
	}

	@Test
	public void testGetWithAllCategories() throws Exception
	{
		assertAll(getItem(ITEM_ID, "basic,detail,metadata,attachment,navigation", getToken()));
	}

	@Test
	public void testGetDefault() throws Exception
	{
		assertAll(getItem(ITEM_ID, null, getToken()));
	}

	// TODO: Not supported by CreateFromScratchTest
	// @Test
	public void testHistory() throws Exception
	{
		assertHistory(getItemSubResource(ITEM_UUID, 1, "history", getToken()));
	}

	// TODO: Not supported by CreateFromScratchTest
	// @Test
	public void testComments() throws Exception
	{
		assertComments(getItemSubResource(ITEM_UUID, 1, "comment", getToken()));
	}

	@Test
	public void testFileList() throws IOException
	{
		// EQUELLA has 2 thumbs for avatar, which pushes the list out (IF they
		// even exist, which they probaby won't yet)
		// Also we don't do a crappy case sensitive text comparison like EPS, so
		// the order will be different
		final boolean equella = isEquella();
		final int conditionsIndex = equella ? 6 : 0;
		final int navTopIndex = equella ? 14 : 8;
		final int avatarIndex = equella ? 4 : 18;
		final int resttodoIndex = equella ? 5 : 19;
		final int size = equella ? 19 : 20;

		final ArrayNode fileList = (ArrayNode) getItemSubResource(ITEM_UUID, 1, "file", getToken()).withArray("files");
		final List<ObjectNode> fileNodes = new ArrayList<>();
		for( JsonNode node : fileList )
		{
			final ObjectNode file = (ObjectNode) node;
			final String filename = file.get("name").asText();
			if( equella && !filename.startsWith("_THUMBS") )
			{
				fileNodes.add(file);
			}
		}

		assertFile(fileNodes.get(conditionsIndex), "The vile vendor go figure.zip/ConditionsOfUse.html", 12926,
			"\"9f8e81b1ef4c8e0b5bdc3438cd50cf0e\"");
		assertFile(fileNodes.get(navTopIndex), "The vile vendor go figure.zip/images/nav_top.png", 331,
			"\"f691a28829c39b90bc0eca49f5fef98e\"");
		assertFile(fileNodes.get(avatarIndex), "avatar.png", 12627, "\"5a4e69eeae86aa557c4a27d52257b757\"");
		assertFile(fileNodes.get(resttodoIndex), "resttodo.zip/resttodo.txt", 890,
			"\"faf2927956642c51240ad0591089de9b\"");
		assertEquals(fileNodes.size(), size);
	}

	private void assertFile(JsonNode file, String path, long length, String etag)
	{
		assertEquals(file.get("name").asText(), path);
		assertEquals(file.get("size").asLong(), length);
		assertEquals(file.get("etag").asText(), etag);
	}

	@Test
	public void testOnlyBasic() throws Exception
	{
		ObjectNode tree = getItem(ITEM_ID, "basic", getToken());
		asserter.assertBasic(tree, "ItemApiViewTest - All attachments", null);
		assertNulls(tree, "metadata", "attachments", "status", "createdDate", "modifiedDate", "owner", "collection");
	}

	@Test
	public void testNoPrivs() throws Exception
	{
		final String token = requestToken(OAUTH_NOPRIVS_CLIENT_ID);
		ObjectNode tree = getItem(ITEM_ID, "basic", token);
		asserter.assertBasic(tree, "ItemApiViewTest - All attachments", null);
		assertNulls(tree, "metadata", "attachments", "status", "createdDate", "modifiedDate", "owner", "collection");
	}

	@Test
	public void testAllVersions() throws Exception
	{
		ArrayNode tree = getItems(DRMITEM_ID, "", "basic,detail", getToken());
		assertNameVersionStatus(tree.get(0), "ItemApiViewTest - DRM and versioning", 1, "archived");
		assertNameVersionStatus(tree.get(1), "ItemApiViewTest - DRM and versioning v2", 2, "live");
		assertNameVersionStatus(tree.get(2), "ItemApiViewTest - DRM and versioning v3", 3, "draft");
	}

	@Test
	public void testLatest() throws Exception
	{
		String token = getToken();
		JsonNode tree = getItem(getItemUri(DRMITEM_ID, "latest"), "basic,detail", token);
		assertNameVersionStatus(tree, "ItemApiViewTest - DRM and versioning v3", 3, "draft");
		tree = getItem(getItemUri(DRMITEM_ID, "latestlive"), "basic,detail", token);
		assertNameVersionStatus(tree, "ItemApiViewTest - DRM and versioning v2", 2, "live");
	}

	@Test
	public void testDRM() throws Exception
	{
		final String token = getToken();
		JsonNode tree = getItems(DRMITEM_ID, "", "drm", token);
		JsonNode drmNode = tree.get(0).get("drm").get("options");
		assertStdDRM(drmNode);
		drmNode = tree.get(1).get("drm").get("options");
		assertStdDRM(drmNode);
		assertEquals(drmNode.get("termsOfAgreement").textValue(), "Verifiable use statement");
		drmNode = tree.get(2).get("drm").get("options");
		assertStdDRM(drmNode);
		assertEquals(drmNode.get("termsOfAgreement").textValue(), "Verifiable use statement");
	}

	@Test
	public void testSCORM() throws Exception
	{
		final String token = getToken();
		JsonNode tree = getItem("672c5ae2-df5c-4538-89fc-1cb9f2d86699", 1, "basic,attachment", token);
		JsonNode attachmentsNode = tree.get("attachments");
		JsonNode attachNode = attachmentsNode.get(0);
		assertEquals(attachmentsNode.size(), 1);
		asserter.assertAttachmentBasics(attachNode, new ItemId("672c5ae2-df5c-4538-89fc-1cb9f2d86699", 1), "scorm",
			"5f2639a3-0bcb-40eb-a668-345efcda04ce", "scormpackage.zip");

		assertEquals(attachNode.get("packageFile").asText(), "scormpackage.zip");
		assertEquals(attachNode.get("size").asInt(), 172045);
		assertEquals(attachNode.get("scormVersion").asText(), "1.2");
	}

	private void assertStdDRM(JsonNode drmNode)
	{
		assertEquals(drmNode.get("drmPageUuid").textValue(), "8a1d54b7-3f82-42e5-b5ad-1895149fe1a3");
		assertEquals(drmNode.get("hideLicencesFromOwner").booleanValue(), false);
		assertEquals(drmNode.get("showLicenceCount").booleanValue(), false);
		assertEquals(drmNode.get("allowSummary").booleanValue(), false);
		assertEquals(drmNode.get("ownerMustAccept").booleanValue(), false);
		assertEquals(drmNode.get("studentsMustAcceptIfInCompilation").booleanValue(), true);
		assertEquals(drmNode.get("previewAllowed").booleanValue(), false);
		assertEquals(drmNode.get("attributionOfOwnership").booleanValue(), false);
		assertEquals(drmNode.get("enforceAttribution").booleanValue(), true);
		JsonNode owners = drmNode.get("contentOwners");
		JsonNode owner1 = owners.get(0);
		assertEquals(owner1.get("userId").textValue(), USERID_AUTOTEST);
		assertEquals(owner1.get("name").textValue(), "Auto Test [AutoTest]");
		assertEquals(owner1.get("email").textValue(), "auto@test.com");
		assertEquals(owner1.get("owner").booleanValue(), true);
		JsonNode usages = drmNode.get("usages");
		assertEquals(usages.get(0).textValue(), "DISPLAY");
		assertEquals(usages.get(1).textValue(), "EXECUTE");
		assertEquals(usages.get(2).textValue(), "PLAY");
		assertEquals(usages.get(3).textValue(), "PRINT");
		assertEquals(drmNode.get("requireAcceptanceFrom").textValue(), "* ");
		JsonNode restrictNode = drmNode.get("restriction");
		assertEquals(restrictNode.get("educationalSector").booleanValue(), false);
		assertEquals(restrictNode.get("maximumUsage").intValue(), 0);
	}

	private void assertComments(JsonNode comments)
	{
		asserter.assertComment(comments.get(0), "4ffdbed8-d286-4a10-a7b3-c771696005fb", 5, USERID_AUTOTEST, false,
			"Average rating", 1330060433936L);
		asserter.assertComment(comments.get(1), "ddd11c14-4c5d-4d7f-81e3-3de7f2730f10", 0, null, true,
			"Anonymous comment", 1330060309824L);
		asserter.assertComment(comments.get(2), "5e7c910e-0da3-47d5-a79f-75c164a0c1e7", 3, USERID_AUTOTEST, false,
			"I'm adding a comment", 1330060203140L);
	}

	private void assertAttachments(JsonNode tree)
	{
		JsonNode attachments = tree.get("attachments");
		assertFileAttachment(attachments.get(0));
		assertResourceAttachment(attachments.get(1));
		assertUrlAttachment(attachments.get(2), ITEM_ID);
		assertWebPageAttachment(attachments.get(3));
		assertGoogleBookAttachment(attachments.get(4));
		assertYoutubeAttachment(attachments.get(5));
		assertFlickrAttachment(attachments.get(6));
		assertITunesU(attachments.get(7));
		assertPackageResource(attachments.get(8));
		assertPackage(attachments.get(9));
		assertZip(attachments.get(11));
		assertEquals(attachments.size(), 12);
	}

	private void assertAll(ObjectNode tree)
	{
		asserter.assertBasic(tree, "ItemApiViewTest - All attachments", null);
		asserter.assertDetails(tree, "live", 5.0, 1330041445615L, "2012-03-04T18:03:24.769-06:00", USERID_AUTOTEST,
			COLLECTION_ID);
		assertMetadata(tree, "item/name", "ItemApiViewTest - All attachments");
		assertAttachments(tree);
		assertNavigation(tree);
		assertLinks(tree, ITEM_ID);
	}

	private void assertHistory(JsonNode history)
	{
		asserter.assertHistory(history.get(0), USERID_AUTOTEST, 1330041445613L, "contributed", "draft");
		asserter.assertHistory(history.get(1), USERID_AUTOTEST, 1330041445613L, "edit", "draft");
		asserter.assertHistory(history.get(2), USERID_AUTOTEST, 1330041445613L, "statechange", "live");
		asserter.assertHistory(history.get(3), USERID_AUTOTEST, 1330041528580L, "edit", "live");
		asserter.assertHistory(history.get(4), USERID_AUTOTEST, 1330061150626L, "edit", "live");
		asserter.assertHistory(history.get(5), USERID_AUTOTEST, 1330061273107L, "edit", "live");
		asserter.assertHistory(history.get(6), USERID_AUTOTEST, "2012-03-04T18:03:24.769-06:00", "edit", "live");
		assertEquals(history.size(), 7);
	}

	private void assertNavigation(JsonNode tree)
	{
		JsonNode navigationNode = tree.get("navigation");
		JsonNode rootNodes = navigationNode.get("nodes");
		JsonNode firstNode = asserter.assertNavNode(rootNodes, 0, "49ffd9d1-cc47-4fce-ae49-897ca0a54024", "avatar.png",
			"Tab 1", "63862d54-1b6d-4dce-9a79-44b3a8c9e107", "");
		JsonNode bookNode = asserter.assertNavNode(firstNode, 0, "44d6eaa6-1a1a-4cff-94d9-31e8bb871d73", "Book!",
			"Tab 1", "b24aa5fa-de50-4021-b7c4-ac4d3a918129", "");

		asserter.assertNavNode(bookNode, 0, "fadf767b-9715-4e33-80fb-ac49bce869ae", "Web Page", "Tab 1",
			"78a2de74-96de-48de-8865-22856c22ae49", "");

		JsonNode c64Node = asserter.assertNavNode(rootNodes, 1, "0cc07c84-fba7-491f-970b-2abb623ac962", "Gold C64",
			"Tab 1", "221b8a48-3a35-4d29-ad3a-0f04a1536b3b", "");
		asserter.assertNavNode(c64Node, 0, "4961186a-7bb1-46ca-a0b9-a92ec7cd5777", "Google", "Tab 1",
			"32a79ea6-8b67-4b38-af85-341b2d512f09", "");

		JsonNode resNode = asserter.assertNavNode(rootNodes, 2, "f074109b-e0fa-4404-9296-848a12034ff5",
			"SearchStemming - Walking", new String[][]{{"Tab 1", "32f45d7c-6df7-44d8-926a-3d6f841c2009", ""},
					{"Tab 2", "63862d54-1b6d-4dce-9a79-44b3a8c9e107", ""}});

		JsonNode youNode = asserter.assertNavNode(resNode, 0, "4ef8419a-1d76-478e-b58c-80022fedf4ce",
			"Test Your Awareness: Do The Test", "Tab 1", "dd26b4ac-9592-4062-bd15-cb9691bfe9a3", "");

		asserter.assertNavNode(youNode, 0, "529882e2-16ce-46c3-bd87-b66f4365c49f", "SGA Roundup 01-31-11", "Tab 1",
			"20196bbc-7f5f-44a4-980d-55dab4e6eec9", "");

		JsonNode imsNode = asserter.assertNavNode(rootNodes, 3, "89b8bbd5-8d67-4ae5-a531-b4b8e2957cf2",
			"The vile vendor: go figure", new String[][]{});
		asserter.assertNavNode(imsNode, 0, "6f5a50eb-3c28-4dcb-bf99-615b6715ea37", "Start: The vile vendor: go figure",
			"Auto Created By IMS Viewer", "6553c4bf-38f1-43f5-a58a-91ddfa4ff30f", "");
		asserter.assertNavNode(rootNodes, 4, "4441d5fe-9941-412f-9674-3f860a44fb5b", "index.html", "Tab 1",
			"bb4f8b1d-8760-4082-a4bc-c3cea9ad0ce7", "");
	}

	private void assertWebPageAttachment(JsonNode webAttachment)
	{
		asserter.assertAttachmentBasics(webAttachment, ITEM_ID, "htmlpage", "78a2de74-96de-48de-8865-22856c22ae49",
			"Web Page");
		assertEquals(webAttachment.get("filename").textValue(),
			"_mypages/78a2de74-96de-48de-8865-22856c22ae49/page.html");
		assertEquals(webAttachment.get("size").intValue(), 28);
	}

	private void assertFileAttachment(JsonNode fileAttachment)
	{
		asserter.assertAttachmentBasics(fileAttachment, ITEM_ID, "file", "63862d54-1b6d-4dce-9a79-44b3a8c9e107",
			"avatar.png");
		assertEquals(fileAttachment.path("viewer").textValue(), "livNavTreeViewer");
		assertEquals(fileAttachment.path("filename").textValue(), "avatar.png");
		assertEquals(fileAttachment.path("size").intValue(), 12627);
		assertEquals(fileAttachment.path("md5").textValue(), "5a4e69eeae86aa557c4a27d52257b757");
		if( !isEquella() )
		{
			assertEquals(fileAttachment.path("thumbFilename").textValue(), "_THUMBS/avatar.png.jpeg");
		}
		assertEquals(fileAttachment.path("conversion").booleanValue(), false);
		JsonNode linksNode = fileAttachment.get("links");

		asserter.assertLink(linksNode, "view",
			getExpectedViewUrl(ITEM_ID, "/file/avatar.png", "63862d54-1b6d-4dce-9a79-44b3a8c9e107"));

		asserter.assertLink(linksNode, "thumbnail",
			getExpectedThumbUrl(ITEM_ID, "63862d54-1b6d-4dce-9a79-44b3a8c9e107"));
	}

	// EQUELLA view link is the web UI
	protected String getExpectedViewUrl(ItemId itemId, String filePath, String attachmentUuid)
	{
		if( isEquella() )
		{
			return context.getBaseUrl() + "items/" + itemId + "/?attachment.uuid=" + attachmentUuid;
		}
		return context.getBaseUrl() + "api/item/" + itemId + filePath;
	}

	// EQUELLA thumbs are not at api/item/xxx/thumb URLs but at thumbs/xx
	// URLs
	protected String getExpectedThumbUrl(ItemId itemId, String attachmentUuid)
	{
		if( isEquella() )
		{
			return context.getBaseUrl() + "thumbs/" + itemId + "/" + attachmentUuid;
		}
		return context.getBaseUrl() + "api/item/" + itemId + "/thumb/" + attachmentUuid;
	}

	private void assertResourceAttachment(JsonNode resAttachment)
	{
		asserter.assertAttachmentBasics(resAttachment, ITEM_ID, "linked-resource",
			"32f45d7c-6df7-44d8-926a-3d6f841c2009", "SearchStemming - Walking");
		assertEquals(resAttachment.get("itemUuid").textValue(), "072c40d8-c8a8-412d-8ad2-3ef188ea016d");
		assertEquals(resAttachment.get("itemVersion").intValue(), 1);
		assertEquals(resAttachment.get("resourceType").textValue(), "p");
		assertEquals(resAttachment.get("resourcePath").textValue(), "");
	}

	private void assertZip(JsonNode zipAttachment)
	{
		asserter.assertAttachmentBasics(zipAttachment, ITEM_ID, "zip", "4398bcda-4127-41d7-ab42-d2f0f8b9b100",
			"resttodo.zip");
		assertEquals(zipAttachment.get("folder").textValue(), "_zips/resttodo.zip");
		assertEquals(zipAttachment.get("mapped").asBoolean(), false);
	}

	private void assertGoogleBookAttachment(JsonNode bookAttachment)
	{
		asserter.assertAttachmentBasics(bookAttachment, ITEM_ID, "googlebook", "b24aa5fa-de50-4021-b7c4-ac4d3a918129",
			"Book!");
		assertEquals(bookAttachment.get("bookId").textValue(),
			"http://www.google.com/books/feeds/volumes/NBMDtWq1MnUC");
		assertEquals(bookAttachment.get("viewUrl").textValue(),
			"http://books.google.com/books?id=NBMDtWq1MnUC&ie=ISO-8859-1&source=gbs_gdata");
		assertEquals(bookAttachment.get("thumbUrl").textValue(),
			"http://bks6.books.google.com/books?id=NBMDtWq1MnUC&printsec=frontcover&img=1&zoom=5&edge=curl&source=gbs_gdata");
		assertEquals(bookAttachment.get("publishedDate").textValue(), "2001-09-17");
		assertEquals(bookAttachment.get("pages").textValue(), "44");
	}

	private void assertYoutubeAttachment(JsonNode youtubeAttachment)
	{
		asserter.assertAttachmentBasics(youtubeAttachment, ITEM_ID, "youtube", "dd26b4ac-9592-4062-bd15-cb9691bfe9a3",
			"Test Your Awareness: Do The Test");
		assertEquals(youtubeAttachment.get("videoId").textValue(), "Ahg6qcgoay4");
		assertEquals(youtubeAttachment.get("uploader").textValue(), "dothetest");
		asserter.assertDate(youtubeAttachment.get("uploadedDate"), 1205170217000L);
		asserter.assertViewerAndPreview(youtubeAttachment, null, false);
		assertEquals(youtubeAttachment.get("viewUrl").textValue(),
			"http://www.youtube.com/v/Ahg6qcgoay4?version=3&f=videos&c=EQUELLA&app=youtube_gdata");
		assertEquals(youtubeAttachment.get("thumbUrl").textValue(), "http://i.ytimg.com/vi/Ahg6qcgoay4/default.jpg");
		assertEquals(youtubeAttachment.get("tags").textValue(),
			"test, awareness, basketball, visual, do, the, attention, span, bike, ad, cycling, TFL, safety, moonwalking, bear");
		assertEquals(youtubeAttachment.get("duration").textValue(), "PT1M9S");
	}

	private void assertFlickrAttachment(JsonNode flickrAttachment)
	{
		asserter.assertAttachmentBasics(flickrAttachment, ITEM_ID, "flickr", "221b8a48-3a35-4d29-ad3a-0f04a1536b3b",
			"Gold C64");
		assertEquals(flickrAttachment.get("author").textValue(), "FRaNKy--");
		asserter.assertDate(flickrAttachment.get("datePosted"), 1161629176000L);
		asserter.assertDate(flickrAttachment.get("dateTaken"), 1161395412000L);

		assertEquals(flickrAttachment.get("licenseName").textValue(), "All Rights Reserved");
		assertEquals(flickrAttachment.get("viewUrl").textValue(), "http://flickr.com/photos/19017538@N00/277527331");
		assertEquals(flickrAttachment.get("thumbUrl").textValue(),
			"http://farm1.staticflickr.com/88/277527331_ab0fa4348e_t.jpg");

		assertEquals(flickrAttachment.get("licenseCode").textValue(), "not-for-display");
		assertEquals(flickrAttachment.get("licenseKey").textValue(), "0");
		assertEquals(flickrAttachment.get("photoId").textValue(), "277527331");
		assertEquals(flickrAttachment.get("mediumUrl").textValue(),
			"http://farm1.staticflickr.com/88/277527331_ab0fa4348e.jpg");
	}

	private void assertITunesU(JsonNode itunesAttachment)
	{
		asserter.assertAttachmentBasics(itunesAttachment, ITEM_ID, "itunesu", "20196bbc-7f5f-44a4-980d-55dab4e6eec9",
			"SGA Roundup 01-31-11");
		assertEquals(itunesAttachment.get("playUrl").textValue(),
			"http://deimos.apple.com/WebObjects/Core.woa/Browsev2/utm.edu.6530144102");
		assertEquals(itunesAttachment.get("trackName").textValue(), "SGA Roundup 01-31-11");
	}

	private void assertPackageResource(JsonNode pkgresAttachment)
	{
		asserter.assertAttachmentBasics(pkgresAttachment, ITEM_ID, "package-res",
			"bb4f8b1d-8760-4082-a4bc-c3cea9ad0ce7", "index.html");
		assertEquals(pkgresAttachment.get("filename").textValue(), "The vile vendor go figure.zip/index.html");
		assertEquals(pkgresAttachment.get("size").intValue(), 0);
	}

	private void assertPackage(JsonNode pkgAttachment)
	{
		asserter.assertAttachmentBasics(pkgAttachment, ITEM_ID, "package", "034dfcb7-b66a-45bf-98c8-03da9011c74d",
			"The vile vendor: go figure");
		assertEquals(pkgAttachment.get("size").intValue(), 21718);
		assertEquals(pkgAttachment.get("packageFile").textValue(), "The vile vendor go figure.zip");
	}
}
