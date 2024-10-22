package com.tle.webtests.test.webservices.soap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.webtests.framework.SoapHelper;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.framework.soap.SoapService51;
import com.tle.webtests.pageobject.hierarchy.TopicPage;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.binding.soap.SoapFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import testng.annotation.OldUIOnly;

@TestInstitution("fiveo")
public class Soap51Test extends AbstractCleanupTest {
  private static final String UTF8 = "UTF-8"; // $NON-NLS-1$

  private SoapService51 soapService;

  private static final String COLLECTION_UUID = "287fd8d3-0d58-4ee8-87b0-85b161e46dfa";
  private static final String COLLECTION_MODERATE_UUID = "1eff9569-451b-4836-829e-2b27bf3641de";

  @BeforeClass
  public void setUp() throws Exception {
    SoapHelper soapHelper = new SoapHelper(context);
    soapService =
        soapHelper.createSoap(
            SoapService51.class,
            "services/SoapService51",
            "http://soap.remoting.web.tle.com",
            null);
    soapService.login("AutoTest", "automated");

    setDeleteCredentials("AutoTest", "automated");
  }

  @BeforeMethod
  public void loginSoap() throws Exception {
    soapService.login("AutoTest", "automated");
  }

  @Test
  public void testLogins() throws Exception {
    PropBagEx user = new PropBagEx(soapService.login("AutoTest", "automated"));
    Assert.assertEquals("Auto", user.getNode("/firstName"));
    String userId = user.getNode("/uuid");
    soapService.logout();
    soapService.getContributableCollections();

    soapService.login("NoSearchCreateUser", "``````");
    checkCollections(true, true);
    soapService.logout();
    checkCollections(false, true);

    soapService.loginWithToken(generateToken("NoSearchCreateUser", "token", "token"));
    checkCollections(true, true);
    soapService.logout();

    soapService.login("AutoTest", "automated");
    user = new PropBagEx(soapService.getUser(userId));
    Assert.assertEquals("Auto", user.getNode("/firstName"));

    soapService.keepAlive();
  }

  @Test(
      expectedExceptions = Exception.class,
      expectedExceptionsMessageRegExp = "com.tle.exceptions.badusername")
  public void badUsername() throws Exception {
    soapService.loginWithToken(generateToken("NoUser", "token", "token"));
  }

  @Test(
      expectedExceptions = Exception.class,
      expectedExceptionsMessageRegExp = "com.tle.exceptions.secret")
  public void wrongSecret() throws Exception {
    soapService.loginWithToken(generateToken("NoSearchCreateUser", "token", "token1"));
  }

  @Test
  public void simplePurgeTest() throws Exception {
    String fullName = context.getFullName("purge test");
    search(fullName, false, 0);
    PropBagEx item = new PropBagEx(soapService.newItem(COLLECTION_UUID));
    item.setNode("/item/name", fullName);
    item.setNode("/item/description", "A description");
    item = new PropBagEx(soapService.saveItem(item.toString(), true));

    String uuid = item.getNode("/item/@id");

    soapService.deleteItem(uuid, 1);
    soapService.deleteItem(uuid, 1);

    search(fullName, false, 0);
  }

  @Test(
      expectedExceptions = Exception.class,
      expectedExceptionsMessageRegExp = "Length must be less than or equal to 50")
  public void fastSearchTest() throws Exception {
    String fullName = context.getFullName("fast search");

    for (int i = 0; i < 10; i++) {
      PropBagEx item = new PropBagEx(soapService.newItem(COLLECTION_UUID));
      item.setNode("/item/name", fullName + " " + i);
      item.setNode("/item/description", "A description");
      item = new PropBagEx(soapService.saveItem(item.toString(), true));
    }

    PropBagEx basic =
        new PropBagEx(
            soapService.searchItemsFast(
                "\"" + fullName + "\"",
                null,
                null,
                false,
                0,
                false,
                0,
                -1,
                new String[] {"basic"}));
    Assert.assertTrue(basic.nodeExists("@available"));
    Assert.assertTrue(basic.nodeExists("result/@uuid"));
    Assert.assertTrue(basic.nodeExists("result/@version"));
    Assert.assertTrue(basic.nodeExists("result/name"));
    Assert.assertTrue(basic.nodeExists("result/description"));

    PropBagEx metadata =
        new PropBagEx(
            soapService.searchItemsFast(
                "\"" + fullName + "\"",
                null,
                null,
                false,
                0,
                false,
                0,
                10,
                new String[] {"metadata"}));

    Assert.assertTrue(metadata.nodeExists("@available"));
    Assert.assertTrue(metadata.nodeExists("result/metadata"));
    Assert.assertTrue(metadata.nodeExists("result/metadata/xml"));

    soapService.searchItems("\"" + fullName + "\"", null, null, false, 0, false, 0, -1);

    ArrayList<Double> fastTimes = Lists.newArrayList();
    ArrayList<Double> slowTimes = Lists.newArrayList();

    for (int i = 0; i < 10; i++) {
      double startTime = System.nanoTime();
      double endTime;
      soapService.searchItemsFast(
          "\"" + fullName + "\"", null, null, false, 0, false, 0, -1, new String[] {"basic"});
      endTime = System.nanoTime();
      fastTimes.add(endTime - startTime);

      startTime = System.nanoTime();
      soapService.searchItems("\"" + fullName + "\"", null, null, false, 0, false, 0, -1);
      endTime = System.nanoTime();
      slowTimes.add(endTime - startTime);
    }

    Collections.sort(fastTimes);
    Collections.sort(slowTimes);

    double medianTimeFast = median(fastTimes.toArray(new Double[fastTimes.size()]));
    double medianTimeSlow = median(slowTimes.toArray(new Double[slowTimes.size()]));

    Assert.assertTrue(
        medianTimeFast < medianTimeSlow, "Fast search was slower than normal search!");

    soapService.searchItemsFast(
        "\"" + fullName + "\"", null, null, false, 0, false, 0, 51, new String[] {"basic"});

    PropBagEx noResults =
        new PropBagEx(
            soapService.searchItemsFast(
                "dasgafhasdashafaghrajhadhjadh",
                null,
                null,
                false,
                0,
                false,
                0,
                -1,
                new String[] {"basic"}));

    Assert.assertEquals(noResults.getIntNode("@available"), 0);

    PropBagEx noCat =
        new PropBagEx(
            soapService.searchItemsFast(
                "\"" + fullName + "\"", null, null, false, 0, false, 0, 10, null));

    Assert.assertFalse(noCat.nodeExists("result/name"));
    Assert.assertFalse(noCat.nodeExists("result/metadata"));
  }

  public static double median(Double[] m) {
    int middle = m.length / 2;
    if (m.length % 2 == 1) {
      return m[middle];
    } else {
      return (m[middle - 1] + m[middle]) / 2.0;
    }
  }

  @Test
  public void itemTests() throws Exception {
    String fullName = context.getFullName("New soap test item");
    PropBagEx item = new PropBagEx(soapService.newItem(COLLECTION_UUID));
    item.setNode("/item/name", fullName);
    item.setNode("/item/description", "A description");
    item = new PropBagEx(soapService.saveItem(item.toString(), true));

    String uuid = item.getNode("/item/@id");

    search(fullName, true, 1);

    Assert.assertTrue(soapService.itemExists(uuid, 1), "Item does not exist and should");
    soapService.archiveItem(uuid, 1);

    search(fullName, true, 0);

    item = new PropBagEx(soapService.newVersionItem(uuid, 1, true));
    item.setNode("/item/name", fullName + " v2");
    item = new PropBagEx(soapService.saveItem(item.toString(), true));
    search(fullName, false, 2);

    item = new PropBagEx(soapService.getItem(uuid, 2, ""));
    Assert.assertEquals(fullName + " v2", item.getNode("/item/name"));

    item = new PropBagEx(soapService.newVersionItem(uuid, 2, true));
    item.setNode("/item/name", fullName + " v3");
    item.setNode("/item/description", "");
    item = new PropBagEx(soapService.saveItem(item.toString(), true));
    search(fullName, false, 3);

    int count =
        soapService.queryCount(
            new String[] {COLLECTION_UUID}, "/xml/item/name = '" + fullName + "'");

    Assert.assertEquals(count, 1);

    int[] queryCounts =
        soapService.queryCounts(
            new String[] {COLLECTION_UUID},
            new String[] {
              "/xml/item/name = '" + fullName + "'",
              "/xml/item/name = '" + fullName + " v2'",
              "/xml/item/name = '" + fullName + " v4'"
            });

    Assert.assertEquals(1, queryCounts[0]);
    Assert.assertEquals(1, queryCounts[1]);
    Assert.assertFalse(1 == queryCounts[2]);

    PropBagEx facetCount =
        new PropBagEx(
            soapService.facetCount(
                "\"" + fullName + "\"",
                null,
                null,
                new String[] {"/xml/item/name", "/xml/item/description", "/xml/item/doesntexist"}));

    Assert.assertEquals(facetCount.nodeCount("facet"), 3);
    PropBagIterator iterator = facetCount.iterator("facet");
    while (iterator.hasNext()) {
      PropBagEx facet = iterator.next();

      if (facet.getNode("@xpath").equals("/xml/item/name")) {
        Assert.assertEquals(facet.nodeCount("value"), 3);
        Assert.assertEquals(facet.getIntNode("value/@count"), 1);
      } else if (facet.getNode("@xpath").equals("/xml/item/description")) {
        Assert.assertEquals(facet.nodeCount("value"), 1);
        Assert.assertEquals(facet.getIntNode("value/@count"), 2);
      } else {
        Assert.assertFalse(facet.nodeExists("value"));
      }
    }

    item = new PropBagEx(soapService.cloneItem(uuid, 1, false));
    item.setNode("/item/@itemdefid", COLLECTION_MODERATE_UUID);
    item.setNode("/item/name", fullName);
    item = new PropBagEx(soapService.saveItem(item.toString(), true));

    count =
        soapService.queryCount(
            new String[] {COLLECTION_UUID}, "/xml/item/name = '" + fullName + "'");
    Assert.assertFalse(count == 2);

    count = soapService.queryCount(new String[] {}, "/xml/item/name = '" + fullName + "'");
    Assert.assertEquals(count, 2);

    search(fullName, false, 1, new String[] {COLLECTION_MODERATE_UUID});

    soapService.deleteItem(item.getNode("/item/@id"), 1);
    soapService.deleteItem(item.getNode("/item/@id"), 1);

    Assert.assertFalse(soapService.itemExists(item.getNode("/item/@id"), 1));

    item = new PropBagEx(soapService.cloneItem(uuid, 1, false));
    item.setNode("/item/@itemdefid", COLLECTION_MODERATE_UUID);
    item.setNode("/item/name", fullName);
    soapService.cancelItemEdit(item.getNode("/item/@id"), item.getIntNode("/item/@version"));

    search(fullName, false, 0, new String[] {COLLECTION_MODERATE_UUID});
  }

  @Test
  public void moderationAndTasks() throws Exception {
    String fullName = context.getFullName("Moderate me");
    PropBagEx item = new PropBagEx(soapService.newItem(COLLECTION_MODERATE_UUID));
    item.setNode("/item/name", fullName);
    item.setNode("/item/description", "A description");
    item = new PropBagEx(soapService.saveItem(item.toString(), true));
    String[] filterNames = soapService.getTaskFilterNames();
    // This could change as we add new stuff, so just make sure its not
    // empty
    Assert.assertTrue(filterNames.length > 1, "Should be at least 2 filter names");
    PropBagEx tasksCount = new PropBagEx(soapService.getTaskFilterCounts(false));
    Assert.assertEquals(filterNames.length, tasksCount.nodeCount("filter"));

    PropBagEx tasks = new PropBagEx(soapService.getTaskList("taskall", 0, 1));
    PropBagEx task = tasks.getSubtree("task");
    String itemUuid = task.getNode("itemUuid");
    int version = task.getIntNode("itemVersion");
    String taskUUid = task.getNode("taskUuid");

    search(fullName, true, 0, new String[] {COLLECTION_MODERATE_UUID});
    soapService.acceptTask(itemUuid, version, taskUUid, true);
    search(fullName, true, 1, new String[] {COLLECTION_MODERATE_UUID});

    String fullName2 = context.getFullName("Reject me");
    item = new PropBagEx(soapService.newItem(COLLECTION_MODERATE_UUID));
    item.setNode("/item/name", fullName2);
    item.setNode("/item/description", "A description");
    item = new PropBagEx(soapService.saveItem(item.toString(), true));

    search(fullName2, true, 0, new String[] {COLLECTION_MODERATE_UUID});
    tasks = new PropBagEx(soapService.getTaskList("taskall", 0, 1));
    task = tasks.getSubtree("task");
    itemUuid = task.getNode("itemUuid");
    version = task.getIntNode("itemVersion");
    taskUUid = task.getNode("taskUuid");

    Assert.assertEquals(
        new PropBagEx(soapService.getTaskList("noterejected", 0, 10)).nodeCount("task"), 0);
    soapService.rejectTask(itemUuid, version, taskUUid, "Rejected", "", true);
    Assert.assertEquals(
        new PropBagEx(soapService.getTaskList("noterejected", 0, 10)).nodeCount("task"), 1);
  }

  @Test
  public void testSoapComments() throws Exception {
    String fullName = context.getFullName("Comment on me");
    String commentText = "This is a comment";

    PropBagEx item = new PropBagEx(soapService.newItem(COLLECTION_UUID));
    item.setNode("/item/name", fullName);
    item.setNode("/item/description", "A description");
    item = new PropBagEx(soapService.saveItem(item.toString(), true));
    String uuid = item.getNode("/item/@id");

    PropBagEx comment =
        new PropBagEx(soapService.getComments(uuid, 1, 0, 1, 10)).getSubtree("comment");
    Assert.assertNull(comment);

    soapService.addComment(uuid, 1, commentText, 5, false);

    comment = new PropBagEx(soapService.getComments(uuid, 1, 0, 1, 10)).getSubtree("comment");
    Assert.assertEquals(commentText, comment.getNode("text"));

    comment = new PropBagEx(soapService.getComment(uuid, 1, comment.getNode("uuid")));
    Assert.assertEquals(commentText, comment.getNode("text"));

    logon("AutoTest", "automated");
    Assert.assertTrue(
        new SearchPage(context)
            .load()
            .exactQuery(fullName)
            .getResultForTitle(fullName, 1)
            .viewSummary()
            .commentsSection()
            .containsComment(commentText));

    soapService.deleteComment(uuid, 1, comment.getNode("uuid"));

    comment = new PropBagEx(soapService.getComments(uuid, 1, 0, 1, 10)).getSubtree("comment");
    Assert.assertNull(comment);

    PropBagThoroughIterator comments =
        new PropBagEx(soapService.getComments(uuid, 1, 0, 1, 100)).iterateAll("comment");
    while (comments.hasNext()) {
      PropBagEx c = comments.next();
      soapService.deleteComment(uuid, 1, c.getNode("uuid"));
    }

    String text = commentText + " v2";
    soapService.addComment(uuid, 1, text, 0, false);
    comment = new PropBagEx(soapService.getComments(uuid, 1, 0, 1, 1)).getSubtree("comment");
    Assert.assertEquals(comment.getNode("text"), text);
    Assert.assertFalse(comment.nodeExists("rating"));
    Assert.assertFalse(Check.isEmpty(comment.getNode("owner")));
    soapService.deleteComment(uuid, 1, comment.getNode("uuid"));

    text = commentText + " v3";
    soapService.addComment(uuid, 1, text, 0, true);
    comment = new PropBagEx(soapService.getComments(uuid, 1, 0, 1, 1)).getSubtree("comment");
    Assert.assertEquals(comment.getNode("text"), text);
    Assert.assertFalse(comment.nodeExists("rating"));
    Assert.assertTrue(Check.isEmpty(comment.getNode("owner")));
    soapService.deleteComment(uuid, 1, comment.getNode("uuid"));

    text = "";
    soapService.addComment(uuid, 1, text, 0, true);
    comment = new PropBagEx(soapService.getComments(uuid, 1, 0, 1, 1)).getSubtree("comment");
    Assert.assertFalse(comment.nodeExists("text"));
    Assert.assertFalse(comment.nodeExists("rating"));
    Assert.assertTrue(Check.isEmpty(comment.getNode("owner")));
    soapService.deleteComment(uuid, 1, comment.getNode("uuid"));
  }

  @Test
  public void itemFileTest() throws Exception {
    String fullName = context.getFullName("attachment test");
    String filename = "google.zip";
    String imageFilename = "avatar.png";
    String image2Filename = "shopimage.jpeg";
    String imageAttachmentUuid = UUID.randomUUID().toString();
    String image2AttachmentUuid = UUID.randomUUID().toString();

    PropBagEx item = new PropBagEx(soapService.newItem(COLLECTION_UUID));
    item.setNode("/item/name", fullName);
    item.setNode("/item/description", "A description");
    item.setNode("/item/thumbnail", "default");

    PropBagEx attachmentsNode = item.aquireSubtree("/item/attachments");
    PropBagEx attachmentNode = attachmentsNode.newSubtree("attachment");
    attachmentNode.setNode("uuid", UUID.randomUUID().toString());
    attachmentNode.setNode("@type", "local");
    attachmentNode.setNode("file", filename);
    attachmentNode.setNode("description", "Google Zip");

    attachmentNode = attachmentsNode.newSubtree("attachment");
    attachmentNode.setNode("uuid", imageAttachmentUuid);
    attachmentNode.setNode("@type", "local");
    attachmentNode.setNode("file", imageFilename);
    attachmentNode.setNode("thumbnail", "suppress");
    attachmentNode.setNode("description", "Image");

    attachmentNode = attachmentsNode.newSubtree("attachment");
    attachmentNode.setNode("uuid", image2AttachmentUuid);
    attachmentNode.setNode("@type", "local");
    attachmentNode.setNode("file", image2Filename);
    // attachmentNode.setNode("thumbnail", "suppress");
    attachmentNode.setNode("description", "Image2");

    Base64 b64 = new Base64(-1);
    String base64Data = b64.encodeToString(getAttachmentData(filename));
    String stagingUuid = item.getNode("/item/staging");
    soapService.uploadFile(stagingUuid, filename, base64Data, false);
    soapService.uploadFile(stagingUuid, "_IMS/test", base64Data, false);

    base64Data = b64.encodeToString(getAttachmentData(imageFilename));
    soapService.uploadFile(stagingUuid, imageFilename, base64Data, false);

    base64Data = b64.encodeToString(getAttachmentData(image2Filename));
    soapService.uploadFile(stagingUuid, image2Filename, base64Data, false);

    item = new PropBagEx(soapService.saveItem(item.toString(), true));

    // if you wish to break here, your item should have the shop thumbnail in gallery etc.  Go on.
    // Go and look.
    // The avatar thumbnail should be suppressed
    String uuid = item.getNode("/item/@id");

    String[] filenames = soapService.getItemFilenames(uuid, 1, "", false);
    Assert.assertEquals(filenames.length, 3);
    Assert.assertEquals(filenames[0], imageFilename);
    Assert.assertEquals(filenames[1], filename);
    Assert.assertEquals(filenames[2], image2Filename);

    // MAY include thumbs files (both sizes) for image2, _IMS folder and actual attached files
    filenames = soapService.getItemFilenames(uuid, 1, "", true);
    List<String> nonThumbs = removeThumbFiles(filenames);
    Assert.assertEquals(nonThumbs.size(), 4);

    item = new PropBagEx(soapService.editItem(uuid, 1, true));
    Assert.assertEquals(item.getNode("/item/thumbnail"), "default");
    // unsuppress the first image attachment
    attachmentsNode = item.getSubtree("/item/attachments");
    for (PropBagEx attachment : attachmentsNode.iterateAll("attachment")) {
      if (attachment.getNode("uuid").equals(imageAttachmentUuid)) {
        attachment.deleteNode("thumbnail");
        break;
      }
    }
    item.setNode("/item/thumbnail", "custom:" + imageAttachmentUuid);

    soapService.uploadFile(stagingUuid, filename, base64Data, true);
    soapService.unzipFile(stagingUuid, filename, "");
    item = new PropBagEx(soapService.saveItem(item.toString(), true));

    // if you wish to break here, your item should have the avatar thumbnail (specifically, not via
    // default)
    Assert.assertEquals(item.getNode("/item/thumbnail"), "custom:" + imageAttachmentUuid);

    filenames = soapService.getItemFilenames(uuid, 1, "", false);
    Assert.assertEquals(filenames.length, 3);

    item = new PropBagEx(soapService.editItem(uuid, 1, true));
    Assert.assertEquals(item.getNode("/item/thumbnail"), "custom:" + imageAttachmentUuid);
    stagingUuid = item.getNode("/item/staging");
    soapService.deleteFile(stagingUuid, filename);
    soapService.cancelItemEdit(uuid, 1);

    filenames = soapService.getItemFilenames(uuid, 1, "", false);
    Assert.assertEquals(filenames.length, 3);

    item = new PropBagEx(soapService.editItem(uuid, 1, true));
    stagingUuid = item.getNode("/item/staging");
    soapService.deleteFile(stagingUuid, filename);
    soapService.saveItem(item.toString(), true);

    filenames = soapService.getItemFilenames(uuid, 1, "", false);
    Assert.assertEquals(filenames.length, 2);

    item = new PropBagEx(soapService.editItem(uuid, 1, true));
    stagingUuid = item.getNode("/item/staging");
    soapService.uploadFile(stagingUuid, "_test", base64Data, false);
    soapService.saveItem(item.toString(), true);

    filenames = soapService.getItemFilenames(uuid, 1, "", false);
    // the 2 image files and the _test filename (it's not a system folder)
    Assert.assertEquals(filenames.length, 3);
    // includes thumbs folders (both sizes) for both images
    filenames = soapService.getItemFilenames(uuid, 1, "", true);
    nonThumbs = removeThumbFiles(filenames);
    Assert.assertEquals(nonThumbs.size(), 4);
  }

  @Test
  public void userAndGroupsTest() throws Exception {
    String groupId1 = "32cd6389-a2e2-4f7d-8f4b-309b93645c46";
    String groupId2 = "005acb90-541d-11e0-b8af-0800200c9a66";
    String userId = "1b0a6aee-2aa2-4fa0-ae1c-47210a59bd19";
    String groupName1 = "Group 1";
    String groupName2 = "Group 2";
    String username = "AUser";

    Assert.assertFalse(soapService.groupExists(groupId1));
    Assert.assertEquals(null, soapService.getGroupUuidForName(groupName1));

    soapService.addGroup(groupId1, groupName1);
    Assert.assertTrue(soapService.groupExists(groupId1));
    Assert.assertEquals(groupId1, soapService.getGroupUuidForName(groupName1));

    soapService.addGroup(groupId2, groupName2);

    Assert.assertFalse(soapService.userExists(userId));
    soapService.addUser(userId, username, "passowrd", "A", "User1", "");
    Assert.assertTrue(soapService.userExists(userId));

    Assert.assertEquals("User1", new PropBagEx(soapService.getUser(userId)).getNode("/lastName"));
    soapService.editUser(userId, username, null, "A", "User", "");
    Assert.assertEquals("User", new PropBagEx(soapService.getUser(userId)).getNode("/lastName"));

    Assert.assertFalse(soapService.isUserInGroup(userId, groupId1));
    soapService.addUserToGroup(userId, groupId1);
    soapService.addUserToGroup(userId, groupId2);
    Assert.assertTrue(soapService.isUserInGroup(userId, groupId1));
    Assert.assertTrue(soapService.isUserInGroup(userId, groupId2));

    soapService.removeAllUsersFromGroup(groupId1);
    Assert.assertFalse(soapService.isUserInGroup(userId, groupId1));
    Assert.assertTrue(soapService.isUserInGroup(userId, groupId2));

    soapService.addUserToGroup(userId, groupId1);
    Assert.assertTrue(soapService.isUserInGroup(userId, groupId1));
    soapService.removeUserFromAllGroups(userId);
    Assert.assertFalse(soapService.isUserInGroup(userId, groupId1));
    Assert.assertFalse(soapService.isUserInGroup(userId, groupId2));

    soapService.addUserToGroup(userId, groupId1);
    soapService.addUserToGroup(userId, groupId2);
    Assert.assertTrue(soapService.isUserInGroup(userId, groupId1));
    Assert.assertTrue(soapService.isUserInGroup(userId, groupId2));

    soapService.removeUserFromGroup(userId, groupId1);
    Assert.assertFalse(soapService.isUserInGroup(userId, groupId1));
    Assert.assertTrue(soapService.isUserInGroup(userId, groupId2));

    soapService.addUserToGroup(userId, groupId1);
    Assert.assertTrue(soapService.isUserInGroup(userId, groupId1));

    soapService.removeAllUsersFromGroup(groupId1);
    Assert.assertFalse(soapService.isUserInGroup(userId, groupId1));
    Assert.assertTrue(soapService.isUserInGroup(userId, groupId2));

    soapService.deleteUser(userId);
    Assert.assertFalse(soapService.userExists(userId));

    soapService.deleteGroup(groupId1);
    soapService.deleteGroup(groupId2);
    Assert.assertFalse(soapService.groupExists(groupId1));
    Assert.assertFalse(soapService.groupExists(groupId2));
  }

  @Test
  public void nullUuid() throws Exception {
    try {
      soapService.editItem(null, 1, false);
      throw new Error("Should not get here");
    } catch (SoapFault fault) {
      Assert.assertEquals(fault.getReason(), "itemUuid cannot be null");
    }
  }

  @Test
  public void itemLocked() throws Exception {
    String fullName = context.getFullName("Blank uuid");
    PropBagEx item = new PropBagEx(soapService.newItem(COLLECTION_UUID));
    item.setNode("/item/name", fullName);
    item.setNode("/item/description", "A description");
    item.setNode("/item/@id", "");
    item = new PropBagEx(soapService.saveItem(item.toString(), true));
    String uuid = item.getNode("/item/@id");
    assertTrue(!Check.isEmpty(uuid));

    int version = item.getIntNode("/item/@version");
    Assert.assertEquals(1, version);

    item = new PropBagEx(soapService.editItem(uuid, 1, false));
    try {
      item = new PropBagEx(soapService.editItem(uuid, 1, false));
      throw new Error("Should not get here");
    } catch (SoapFault fault) {
      Assert.assertEquals(fault.getReason(), "Item is already locked");
    }
  }

  @Test(
      expectedExceptions = Exception.class,
      expectedExceptionsMessageRegExp = "Error parsing where clause: Match Failed: Expected '/xml'")
  public void invalidWhere() throws Exception {
    soapService.queryCount(null, "Invalid where count");
  }

  @Test
  public void emptyCounts() throws Exception {
    Assert.assertEquals(0, soapService.queryCounts(null, null).length);
    Assert.assertEquals("<facets/>", soapService.facetCount(null, null, null, null));
  }

  @Test
  public void collectionAndSchemaTest() throws Exception {
    String fullName = context.getFullName("An Item");
    PropBagEx item = new PropBagEx(soapService.newItem(COLLECTION_UUID));
    item.setNode("/item/name", fullName);
    item.setNode("/item/description", "A description");
    item = new PropBagEx(soapService.saveItem(item.toString(), true));

    PropBagEx collection = new PropBagEx(soapService.getCollection(COLLECTION_UUID));
    PropBagEx schema = new PropBagEx(soapService.getSchema(collection.getNode("/schemaUuid")));

    Assert.assertEquals(schema.getNode("name"), "Basic Schema");
    Assert.assertEquals(collection.getNode("name"), "SOAP and Harvesting");
  }

  @Test
  public void draftTest() throws Exception {
    String fullName = context.getFullName("An draft item");
    PropBagEx item = new PropBagEx(soapService.newItem(COLLECTION_UUID));
    item.setNode("/item/name", fullName);
    item.setNode("/item/description", "A description");
    item = new PropBagEx(soapService.saveItem(item.toString(), false));
    String uuid = item.getNode("/item/@id");
    search(fullName, true, 0);

    item = new PropBagEx(soapService.editItem(uuid, 1, false));
    item = new PropBagEx(soapService.saveItem(item.toString(), false));
    search(fullName, true, 0);
    search(fullName, false, 1);

    item = new PropBagEx(soapService.editItem(uuid, 1, false));
    item = new PropBagEx(soapService.saveItem(item.toString(), true));
    search(fullName, true, 1, new String[] {});
    soapService.deleteItem(uuid, 1);
    soapService.deleteItem(uuid, 1);
  }

  @Test
  public void modifyOwnerTest() throws Exception {
    String autoTextUuid = "adfcaf58-241b-4eca-9740-6a26d1c3dd58";
    String otherUuid = "f4c788d1-6e97-5247-e06c-3d4982b9469a";

    String fullName = context.getFullName("An item");
    PropBagEx item = new PropBagEx(soapService.newItem(COLLECTION_UUID));
    item.setNode("/item/name", fullName);
    item.setNode("/item/description", "A description");
    item = new PropBagEx(soapService.saveItem(item.toString(), true));
    String uuid = item.getNode("/item/@id");
    search(fullName, true, 1);

    logon("AutoTest", "automated");
    checkOwners(fullName, true, false);

    soapService.setOwner(uuid, 1, otherUuid);
    checkOwners(fullName, false, true);

    soapService.addSharedOwner(uuid, 1, autoTextUuid);
    checkOwners(fullName, true, true);

    soapService.removeSharedOwner(uuid, 1, autoTextUuid);
    checkOwners(fullName, false, true);

    soapService.deleteItem(uuid, 1);
    soapService.deleteItem(uuid, 1);
  }

  private void checkOwners(String name, boolean autoTest, boolean other) {
    assertEquals(
        new SearchPage(context)
            .load()
            .setOwnerFilter("AutoTest")
            .exactQuery(name)
            .isResultsAvailable(),
        autoTest);

    assertEquals(
        new SearchPage(context)
            .load()
            .setOwnerFilter("NoSearchCreateUser")
            .exactQuery(name)
            .isResultsAvailable(),
        other);
  }

  private void checkCollections(boolean cont, boolean search) throws Exception {
    assertEquals(
        cont, new PropBagEx(soapService.getContributableCollections()).nodeExists("/itemdef"));
    assertEquals(
        search, new PropBagEx(soapService.getSearchableCollections()).nodeExists("/itemdef"));
  }

  private void search(String fullName, boolean live, int count, String[] col) throws Exception {
    PropBagEx results =
        new PropBagEx(
            soapService.searchItems("\"" + fullName + "\"", col, null, live, 0, false, 0, 10));

    Assert.assertEquals(
        results.getIntNode("@count"),
        count,
        live ? "Result count for live status is wrong" : "Result count for any status is wrong");
  }

  private void search(String fullName, boolean live, int count) throws Exception {
    search(fullName, live, count, null);
  }

  public static String generateToken(
      String username, String sharedSecretId, String sharedSecretValue) {
    final String secretId = (sharedSecretId == null ? "" : sharedSecretId); // $NON-NLS-1$

    String time = Long.toString(System.currentTimeMillis());
    String toMd5 = username + secretId + time + sharedSecretValue;

    StringBuilder b = new StringBuilder();

    b.append(username);
    b.append(':');
    b.append(secretId);
    b.append(':');
    b.append(time);
    b.append(':');
    b.append(new String(Base64.encodeBase64(getMd5Bytes(toMd5))));

    return b.toString();
  }

  private static byte[] getMd5Bytes(String str) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5"); // $NON-NLS-1$
      digest.update(str.getBytes(UTF8));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return digest.digest();
  }

  private byte[] getAttachmentData(String filename) {
    InputStream attachmentStream = null;
    ByteArrayOutputStream data = null;

    try {
      attachmentStream = new FileInputStream(new File(Attachments.get(filename).toURI()));
      data = new ByteArrayOutputStream();
      byte buffer[] = new byte[4096];
      for (int bytes = attachmentStream.read(buffer, 0, buffer.length);
          bytes != -1;
          bytes = attachmentStream.read(buffer, 0, buffer.length)) {
        data.write(buffer, 0, bytes);
      }
      return data.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (attachmentStream != null) {
          attachmentStream.close();
        }
        if (data != null) {
          data.close();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  @OldUIOnly
  public void hierarchyTest() throws Exception {
    String aTopicUUID = "e8c49738-7609-0079-e354-67b2e4e6b54c";
    String aTopicName = "A Topic";
    String aTopicChildUUID = "e3988e94-7e76-ee78-4b46-086ba1dda897";
    String aTopicChildName = "Child";

    Map<String, String> rootTopics = new LinkedHashMap<String, String>();
    rootTopics.put("0e3c503b-0fac-419b-b0ca-eef61dec2adc", "Topic A");
    rootTopics.put(aTopicUUID, aTopicName);
    rootTopics.put("cd42ec56-d893-a81d-ba59-00742414ff3d", "No Results");
    rootTopics.put("d62bbe4e-84d9-06f2-62e1-31ddc28a1ee6", "Power Search");
    rootTopics.put("36b3f358-f229-0558-bfaf-59db6294978b", "Results not shown");
    rootTopics.put("33ecf7c7-24a6-f01d-d83e-38e9782c3803", "Some Children Hidden");

    String newTopicUUID = "";
    String newTopicName = "New SOAP Topic";

    // List root topics
    PropBagEx topicXml = new PropBagEx(soapService.listTopics(""));
    PropBagIterator topicIterator = topicXml.iterator("topic");
    for (Entry<String, String> topic : rootTopics.entrySet()) {
      checkTopic(topicIterator.next(), topic.getValue(), topic.getKey());
    }

    // List child topics of "A Topic"
    PropBagIterator childTopicIterator =
        new PropBagEx(soapService.listTopics(aTopicUUID)).iterator("topic");
    checkTopic(childTopicIterator.next(), aTopicChildName, aTopicChildUUID);

    // Get root topic
    PropBagEx aTopicXml = new PropBagEx(soapService.getTopic(aTopicUUID));
    checkTopic(aTopicXml, aTopicName, aTopicUUID);

    // Get child topic
    checkTopic(
        new PropBagEx(soapService.getTopic(aTopicChildUUID)), aTopicChildName, aTopicChildUUID);

    // Create root topic at top of list
    PropBagEx newTopicXml = new PropBagEx(aTopicXml.toString());
    newTopicXml.setNode("@uuid", "");
    newTopicXml.setNode("name", newTopicName);
    newTopicXml.setNode("constraints/freetext", "relevance");
    newTopicUUID = soapService.createTopic("", newTopicXml.toString(), -1);

    logon("AutoTest", "automated");

    MenuSection menuSection = new MenuSection(context);
    assertTrue(menuSection.hasMenuOption(newTopicName));
    newTopicXml = new PropBagEx(soapService.getTopic(newTopicUUID));
    checkTopic(newTopicXml, newTopicName, newTopicUUID);

    // Create child topic x2
    String newFirstChildName = "ChildOne";
    String newSecondChildName = "ChildTwo";

    PropBagEx newFirstChildXml = new PropBagEx(newTopicXml.toString());
    PropBagEx newSecondChildXml = new PropBagEx(newTopicXml.toString());

    newFirstChildXml.setNode("name", newFirstChildName);
    newSecondChildXml.setNode("name", newSecondChildName);

    // Test that -1 appends children
    String newFirstChildUUID =
        soapService.createTopic(newTopicUUID, newFirstChildXml.toString(), -1);
    // Test that an index greater than the number of siblings appends too.
    String newSecondChildUUID =
        soapService.createTopic(newTopicUUID, newSecondChildXml.toString(), Integer.MAX_VALUE);

    TopicPage newTopicPage = menuSection.clickTopic(newTopicName);
    assertTrue(newTopicPage.hasSubTopic(newFirstChildName));
    assertTrue(newTopicPage.hasSubTopic(newSecondChildName));

    // Edit root topic
    newTopicName = newTopicName + " EDITED";
    newTopicXml.setNode("name", newTopicName);
    soapService.editTopic(newTopicUUID, newTopicXml.toString());
    logon("AutoTest", "automated"); // Refresh topics
    assertTrue(menuSection.hasMenuOption(newTopicName));

    // Edit child topic
    String firstChildEdited = "Child111";
    newFirstChildXml.setNode("name", firstChildEdited);
    soapService.editTopic(newFirstChildUUID, newFirstChildXml.toString());
    String secondChildEdited = "Child222";
    newSecondChildXml.setNode("name", secondChildEdited);
    soapService.editTopic(newSecondChildUUID, newSecondChildXml.toString());

    newTopicPage = menuSection.clickTopic(newTopicName);
    assertTrue(newTopicPage.hasSubTopic(firstChildEdited));
    assertTrue(newTopicPage.hasSubTopic(secondChildEdited));

    // Move root topic
    soapService.moveTopic(newTopicUUID, "", 1);
    logon("AutoTest", "automated"); // Refresh topics
    assertTrue(menuSection.hasHierarchyTopic(newTopicName, 2));

    soapService.moveTopic(newTopicUUID, "", 3);
    logon("AutoTest", "automated"); // Refresh topics
    assertTrue(menuSection.hasHierarchyTopic(newTopicName, 4));

    // Move child topics using MIN/MAX
    soapService.moveTopic(newFirstChildUUID, newTopicUUID, Integer.MAX_VALUE);
    logon("AutoTest", "automated"); // Refresh topics
    newTopicPage = menuSection.clickTopic(newTopicName);
    assertTrue(newTopicPage.hasSubTopic(firstChildEdited, 2));
    assertTrue(newTopicPage.hasSubTopic(secondChildEdited, 1));

    soapService.moveTopic(newFirstChildUUID, newTopicUUID, Integer.MIN_VALUE);
    logon("AutoTest", "automated"); // Refresh topics
    newTopicPage = menuSection.clickTopic(newTopicName);
    assertTrue(newTopicPage.hasSubTopic(firstChildEdited, 1));
    assertTrue(newTopicPage.hasSubTopic(secondChildEdited, 2));

    // Delete child topic one
    soapService.deleteTopic(newFirstChildUUID);
    logon("AutoTest", "automated"); // Refresh topics
    newTopicPage = menuSection.clickTopic(newTopicName);
    assertFalse(newTopicPage.hasSubTopic(firstChildEdited));

    // Delete root topic with remaining child topic
    soapService.deleteTopic(newTopicUUID);
    logon("AutoTest", "automated");
    assertFalse(menuSection.get().hasMenuOption(newTopicName));
  }

  private void checkTopic(PropBagEx xml, String name, String uuid) {
    String actualName = xml.getNode("name");
    String actualUuid = xml.getNode("@uuid");

    String expectedTopic = uuid + " - " + name;
    String actualTopic = actualUuid + " - " + actualName;
    assertEquals(actualTopic, expectedTopic, "Topic does not match requested topic.");
  }

  private List<String> removeThumbFiles(String[] filenames) {
    final List<String> result = new ArrayList<>();
    for (String filename : filenames) {
      if (!filename.startsWith("_THUMBS")) {
        result.add(filename);
      }
    }
    return result;
  }
}
