package com.tle.webtests.test.contribute.controls.asc;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.DisplayNodesPage;
import com.tle.webtests.pageobject.viewitem.ItemUrlPage;
import com.tle.webtests.pageobject.viewitem.ItemXmlPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ConfirmationDialog;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.SubWizardPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.EditBoxControl;
import com.tle.webtests.pageobject.wizard.controls.RepeaterControl;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.PackageAttachmentEditPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import testng.annotation.RetryTest;

@RetryTest
@TestInstitution("asc")
public class AdvancedScriptControlTests extends AbstractCleanupTest {

  public static final String NAME_PACKAGE = "Zou ba! Visiting China: Is this your first visit?";
  public static final String CHILD_NODE =
      "Start: Zou ba! Visiting China: Is this your second visit?";
  private final String ASC_MESSAGE_DIV_ID = "ascMessage";
  private final String STRUCTURE_DIV_ID = "structure";

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  /**
   * Ability to refer to other controls in the script
   * http://dtec.equella.com/DTEC/test/editTest.aspx?testId=14380 Retrieves other controls and sets
   * them to invalid if their text == 'invalid'
   */
  @Test
  public void testSetInvalidOnOtherControls() {
    String itemName = context.getFullName("dtec14380");
    WizardPageTab wizard =
        new ContributePage(context)
            .load()
            .openWizard("dtec14380 Ability to refer to other controls in the script");

    wizard.editbox(1, itemName);
    wizard.editbox(2, "invalid");
    wizard.editbox(5, "invalid");
    wizard.clickButton("Reload");

    // verify invalid boxes
    EditBoxControl ctl2 = wizard.editbox(2);
    assertEquals(
        ctl2.getInvalidMessage(), "Set invalid by advanced script control! CONTROL BEFORE");
    EditBoxControl ctl5 = wizard.editbox(5);
    assertEquals(ctl5.getInvalidMessage(), "Set invalid by advanced script control! CONTROL AFTER");

    wizard = wizard.save().finishInvalid(wizard);

    // verify invalid boxes
    ctl2 = wizard.editbox(2);
    assertEquals(
        ctl2.getInvalidMessage(), "Set invalid by advanced script control! CONTROL BEFORE");
    ctl5 = wizard.editbox(5);
    assertEquals(ctl5.getInvalidMessage(), "Set invalid by advanced script control! CONTROL AFTER");

    // fix em up, successfully save the item
    wizard.editbox(2, "Not invalid");
    wizard.editbox(5, "Not invalid");

    wizard.clickButton("Reload");

    assertNotEquals(
        wizard.editbox(2).getInvalidMessage(),
        "Set invalid by advanced script control! CONTROL BEFORE");
    assertNotEquals(
        wizard.editbox(5).getInvalidMessage(),
        "Set invalid by advanced script control! CONTROL AFTER");

    ConfirmationDialog conf = wizard.save();

    conf.publish();
  }

  /**
   * Load XML Script Objects from Attachments
   * http://dtec.equella.com/DTEC/test/editTest.aspx?testId=14766 Gets the XML contents of the
   * attachment (if it ends in .XML) and into an XmlScriptType object and selects a specific node
   * that equals "tns:addUserToGroupResponse"
   */
  @Test
  public void testLoadXMLFromAttachment() {
    String itemName = context.getFullName("dtec14766");
    WizardPageTab wizard =
        new ContributePage(context)
            .load()
            .openWizard("dtec14766 Load XML Script Objects from Attachments");

    // assert text == 'I told you to upload and XML document, so do it!'
    assertEquals(getAscMessage().trim(), "I told you to upload and XML document, so do it!");

    wizard.editbox(1, itemName);
    wizard.addSingleFile(3, Attachments.get("complex.xml"));

    // assert text == 'tns:addUserToGroupResponse'
    assertEquals(getAscMessage().trim(), "tns:addUserToGroupResponse");

    wizard.save().publish();
  }

  /**
   * Test deleteSubtree method on XmlScriptType
   * http://dtec.equella.com/DTEC/test/editTest.aspx?testId=15127 Fills the attributes object with
   * some HTML. Deletes a specific subtree and verifies the result.
   */
  @Test
  public void testDeleteSubtreeMethod() {
    String itemName = context.getFullName("dtec15127");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("dtec15127 deleteSubtree method");

    // @formatter:off
    String[] populateButtons =
        new String[] {
          "Populate Leaf", "Populate Complex", "Populate Mixed", "Populate Delete Nothing"
        };
    String[] deleteButtons =
        new String[] {"Delete Leaf", "Delete Complex", "Delete Mixed", "Delete Nothing"};
    String[] expectedOriginals =
        new String[] {
          "<xml><item><name/><description/></item><subtree><a><test>1111</test><test>2222</test><test2>3333</test2></a></subtree></xml>",
          "<xml><item><name/><description/></item><subtree><a><test1>1111<child1>11<grandkid1>111</grandkid1><grandkid2>112</grandkid2><grandkid3>113</grandkid3></child1><child2>12<grandkid1>121</grandkid1><grandkid2>122</grandkid2></child2><child3>13</child3></test1><test>1111</test><test2>3333</test2></a></subtree></xml>",
          "<xml><item><name/><description/></item><subtree><a><test1>1111<child1>11<grandkid1>111</grandkid1><grandkid2>112</grandkid2><grandkid3>113</grandkid3></child1><child2>12<grandkid1>121</grandkid1><grandkid2>122</grandkid2></child2><child3>13</child3></test1><test1>2222</test1><test1>2233</test1><test2>3333</test2></a></subtree></xml>",
          "<xml><item><name/><description/></item><subtree><a><test>1111</test><test>1111</test><test2>3333</test2></a></subtree></xml>"
        };
    String[] expectedModifed =
        new String[] {
          "<xml><item><name/><description/></item><subtree><a><test2>3333</test2></a></subtree></xml>",
          "<xml><item><name/><description/></item><subtree><a><test>1111</test><test2>3333</test2></a></subtree></xml>",
          "<xml><item><name/><description/></item><subtree><a><test2>3333</test2></a></subtree></xml>",
          "<xml><item><name/><description/></item><subtree><a><test>1111</test><test>1111</test><test2>3333</test2></a></subtree></xml>"
        };

    for (int i = 0; i < 4; i++) {
      // press the button to populate XML
      clickAscInput(populateButtons[i], wizard);
      By expectedElement = By.xpath("//span[normalize-space(.)='" + expectedOriginals[i] + "']");
      wizard.getWaiter().until(ExpectedConditions.visibilityOfElementLocated(expectedElement));
      assertEquals(getAscMessage().trim(), expectedOriginals[i]);

      // press the button to kill a subtree
      clickAscInput(deleteButtons[i], wizard);
      expectedElement = By.xpath("//span[normalize-space(.)='" + expectedModifed[i] + "']");
      wizard.getWaiter().until(ExpectedConditions.visibilityOfElementLocated(expectedElement));
      assertEquals(getAscMessage().trim(), expectedModifed[i]);
    }

    wizard.editbox(1, itemName);
    wizard.save().publish();
  }

  /** staging.Move method http://dtec.equella.com/DTEC/test/editTest.aspx?testId=15128 */
  @Test
  public void testStagingMove() {
    String itemName = context.getFullName("dtec15128");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("dtec15128 staging.Move method");

    wizard.editbox(1, itemName);

    // Upload 7 files. Content is not really relevant, as long as they all
    // have diff filenames
    wizard.addFiles(3, true, "A.txt", "B.txt", "C.txt", "D.txt", "E.txt", "F.txt", "G.txt");

    clickAscInput("Shuffle Files", wizard);

    // button script performs the following:
    // file A: leave alone
    // file B: call move on a non existent target folder /A (1 deep)
    // file C: call move on a non existent target folder /X/Y (2 deep)
    // file D: call move on a non existent target folder /X/Y/Z/A
    // file E: call move on a non existent target folder /X/Y/B/C
    // file F: call move on a non existent target folder /X/Y/D/E THEN move
    // again to /X/Y/F/G
    // file G: call move on a non existent target folder /F/G/G/G.txt
    // THEN move again but with different filename /F/G/G/G new.txt
    // file 'H': non-existent file. call move to /A/H.txt Should probably
    // handle it gracefully.

    // view ~ and make sure files are in correct locations
    ItemUrlPage tilde = wizard.save().publish().tilde();

    assertNotNull(tilde.getFileLink("A.txt"));
    assertNotNull(tilde.getFolderLink("A"));
    assertNotNull(tilde.getFolderLink("F"));
    assertNotNull(tilde.getFolderLink("X"));

    tilde = tilde.viewFolder("A");
    assertNotNull(tilde.getFileLink("B.txt"));
    assertNull(tilde.getFileLink("H.txt")); // H doesn't exist

    tilde = tilde.viewFolder("/X");
    assertNotNull(tilde.getFolderLink("Y"));

    tilde = tilde.viewFolder("Y"); // /X/Y
    assertNotNull(tilde.getFileLink("C.txt"));
    assertNotNull(tilde.getFolderLink("Z"));
    assertNotNull(tilde.getFolderLink("B"));
    assertNotNull(tilde.getFolderLink("D"));
    assertNotNull(tilde.getFolderLink("F"));

    tilde = tilde.viewFolder("Z"); // /X/Y/Z
    assertNotNull(tilde.getFolderLink("A"));

    tilde = tilde.viewFolder("A"); // /X/Y/Z/A
    assertNotNull(tilde.getFileLink("D.txt"));

    tilde = tilde.viewFolder("/X/Y/B");
    assertNotNull(tilde.getFolderLink("C"));

    tilde = tilde.viewFolder("C");
    assertNotNull(tilde.getFileLink("E.txt"));

    tilde = tilde.viewFolder("/X/Y/D");
    assertNotNull(tilde.getFolderLink("E"));

    tilde = tilde.viewFolder("E");
    assertNull(tilde.getFileLink("F.txt")); // null! It's been moved again

    tilde = tilde.viewFolder("/X/Y/F");
    assertNotNull(tilde.getFolderLink("G"));

    tilde = tilde.viewFolder("G");
    assertNotNull(tilde.getFileLink("F.txt"));

    tilde = tilde.viewFolder("/F");
    assertNotNull(tilde.getFolderLink("G"));

    tilde = tilde.viewFolder("G");
    assertNotNull(tilde.getFolderLink("G"));

    tilde = tilde.viewFolder("G");
    assertNotNull(tilde.getFileLink("G new.txt"));
    assertNull(tilde.getFileLink("G.txt"));
  }

  /** staging.createFolder method http://dtec.equella.com/DTEC/test/editTest.aspx?testId=15130 */
  @Test
  public void testStagingCreateFolder() {
    String itemName = context.getFullName("dtec15130");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("dtec15130 staging.createFolder method");

    wizard.editbox(1, itemName);

    wizard.addSingleFile(3, "A.txt");

    clickAscInput("Create Folders", wizard);

    // staging.createFolder('folder1');
    // staging.createFolder('test1/test2/test3');
    // staging.createFolder('test1/test2/sub1/subsub1a');
    // staging.createFolder('test2/sub1/subsub1a');
    // move a file into here
    // staging.createFolder('test2/sub1/subsub1a');

    // make sure the file was un-harmed
    assertEquals(getAscMessage().trim(), "A");

    // view ~ and make sure files are in correct locations
    ItemUrlPage tilde = wizard.save().publish().tilde();

    assertNotNull(tilde.getFolderLink("folder1"));
    assertNotNull(tilde.getFolderLink("test1"));
    assertNotNull(tilde.getFolderLink("test2"));

    tilde = tilde.viewFolder("test1");
    assertNotNull(tilde.getFolderLink("test2"));

    tilde = tilde.viewFolder("/test1/test2");
    assertNotNull(tilde.getFolderLink("test3"));
    assertNotNull(tilde.getFolderLink("sub1"));

    tilde = tilde.viewFolder("/test1/test2/sub1");
    assertNotNull(tilde.getFolderLink("subsub1a"));

    tilde = tilde.viewFolder("/test2/sub1");
    assertNotNull(tilde.getFolderLink("subsub1a"));

    tilde = tilde.viewFolder("/test2/sub1/subsub1a");
    assertNotNull(tilde.getFileLink("A.txt"));
  }

  /** staging.createFolder and Move http://dtec.equella.com/DTEC/test/editTest.aspx?testId=15131 */
  @Test
  public void testStagingCreateFolderAndMove() {
    // staging.createFolder('folder1/folder2');
    // staging.move('File1.png','/folder1/folder2/File1.png');

    String itemName = context.getFullName("dtec15131");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("dtec15131 staging.createFolder and Move");

    wizard.editbox(1, itemName);

    wizard.addSingleFile(3, "A.txt");

    clickAscInput("Create Folder And Move", wizard);

    // view ~ and make sure files are in correct locations
    ItemUrlPage tilde = wizard.save().publish().tilde();

    assertNull(tilde.getFileLink("A.txt"));
    assertNotNull(tilde.getFolderLink("folder1"));
    tilde = tilde.viewFolder("folder1");

    assertNotNull(tilde.getFolderLink("folder2"));
    tilde = tilde.viewFolder("folder2");

    assertNotNull(tilde.getFileLink("A.txt"));
  }

  /**
   * Wizard Control - Advanced Scripting - Prohibited Operations
   * http://dtec.equella.com/DTEC/test/editTest.aspx?testId=15097
   */
  @Test
  public void testProhibitedOperations() {
    /*
     * It should not be possible to access any of the following from the
     * on-load / on-submit pane. Java classes such as java.lang.Class The
     * System object eg. System.out.println('whatever') The java object eg.
     * java.lang.Runtime.exec('whatever')
     */
    String itemName = context.getFullName("dtec15097");
    WizardPageTab wizard =
        new ContributePage(context)
            .load()
            .openWizard("dtec15097 Wizard Control - Advanced Scripting - Prohibited Operations");
    wizard.editbox(1, itemName);

    ErrorPage errorPage = clickAscButtonExpectError("Class For Name", wizard);
    assertTrue(
        errorPage.getDetail().contains("Access to Java class \"java.lang.Class\" is prohibited"));
    errorPage.goBack(wizard);
    errorPage = clickAscButtonExpectError("System", wizard);
    assertTrue(errorPage.getDetail().contains("ReferenceError: \"System\" is not defined"));

    errorPage.goBack(wizard);
    errorPage = clickAscButtonExpectError("Runtime", wizard);
    assertTrue(errorPage.getDetail().contains("ReferenceError: \"Runtime\" is not defined"));

    errorPage.goBack(wizard);
    errorPage = clickAscButtonExpectError("Propbag", wizard);
    assertTrue(errorPage.getDetail().contains("ReferenceError: \"Packages\" is not defined"));

    errorPage.goBack(wizard);
    errorPage = clickAscButtonExpectError("Class Loophole", wizard);
    assertTrue(
        errorPage.getDetail().contains("Access to Java class \"java.lang.Class\" is prohibited"));

    errorPage.goBack(wizard);

    wizard.editbox(1, itemName);
    wizard.save().publish();
  }

  /** Expert Scripting http://dtec.equella.com/DTEC/test/editTest.aspx?testId=14637 */
  @Test
  public void testSaveScriptAndNewVersionScript() {
    String itemName = context.getFullName("dtec14637");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("dtec14637 Expert Scripting");
    wizard.editbox(1, itemName);
    SummaryPage summary = wizard.save().publish();
    ItemXmlPage xml = summary.itemXml();
    assertEquals("Save Script Worked", xml.nodeValue("/Elements/Element_1"));
    assertTrue(null == xml.nodeValue("/Elements/Element_2"));

    context.getDriver().navigate().back();
    wizard = summary.get().newVersion();
    wizard.editbox(1, context.getFullName("dtec14637 new version"));

    summary = wizard.save().publish();
    xml = summary.itemXml();

    assertEquals("Save Script Worked", xml.nodeValue("/Elements/Element_1"));
    assertEquals("New Version Script Worked", xml.nodeValue("/Elements/Element_2"));
  }

  /**
   * http://dev.equella.com/issues/6173 Using path overrides in freemarker and onload scripts,
   * nested repeaters
   */
  @Test
  public void testPathOverrides() {
    String itemName = context.getFullName("redmine6173");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("redmine6173 ASC in a repeater");
    wizard.editbox(1, itemName);

    // add 3 top levels, and 3 nested levels in each
    RepeaterControl repeater1 = wizard.repeater(3);

    SubWizardPage groupA = repeater1.add(2, 5);
    groupA.editbox(0).setText("A");

    SubWizardPage repA1 = groupA.repeater(4).add(3, 10);
    repA1.editbox(0).setText("A1");
    SubWizardPage repA2 = groupA.repeater(4).add(4, 14);
    repA2.editbox(0).setText("A2");
    SubWizardPage repA3 = groupA.repeater(4).add(5, 18);
    repA3.editbox(0).setText("A3");

    SubWizardPage groupB = repeater1.add(6, 22);
    groupB.editbox(0).setText("B");

    SubWizardPage repB1 = groupB.repeater(4).add(7, 27);
    repB1.editbox(0).setText("B1");
    SubWizardPage repB2 = groupB.repeater(4).add(8, 31);
    repB2.editbox(0).setText("B2");
    SubWizardPage repB3 = groupB.repeater(4).add(9, 35);
    repB3.editbox(0).setText("B3");

    SubWizardPage groupC = repeater1.add(10, 39);
    groupC.editbox(0).setText("C");

    SubWizardPage repC1 = groupC.repeater(4).add(11, 44);
    repC1.editbox(0).setText("C1");
    SubWizardPage repC2 = groupC.repeater(4).add(12, 48);
    repC2.editbox(0).setText("C2");
    SubWizardPage repC3 = groupC.repeater(4).add(13, 52);
    repC3.editbox(0).setText("C3");

    wizard.clickButton("Reload");

    // check the values
    List<WebElement> load1s = getDivsByPrefix("load1_");
    List<WebElement> load2s = getDivsByPrefix("load2_");
    List<WebElement> submit1s = getDivsByPrefix("submit1_");
    List<WebElement> submit2s = getDivsByPrefix("submit2_");

    assertEquals(load1s.get(0).getText(), "A");
    assertEquals(load2s.get(0).getText(), "A1");
    assertEquals(load2s.get(1).getText(), "A2");
    assertEquals(load2s.get(2).getText(), "A3");
    assertEquals(submit1s.get(0).getText(), "A");
    assertEquals(submit2s.get(0).getText(), "A1");
    assertEquals(submit2s.get(1).getText(), "A2");
    assertEquals(submit2s.get(2).getText(), "A3");

    assertEquals(load1s.get(1).getText(), "B");
    assertEquals(load2s.get(3).getText(), "B1");
    assertEquals(load2s.get(4).getText(), "B2");
    assertEquals(load2s.get(5).getText(), "B3");
    assertEquals(submit1s.get(1).getText(), "B");
    assertEquals(submit2s.get(3).getText(), "B1");
    assertEquals(submit2s.get(4).getText(), "B2");
    assertEquals(submit2s.get(5).getText(), "B3");

    assertEquals(load1s.get(2).getText(), "C");
    assertEquals(load2s.get(6).getText(), "C1");
    assertEquals(load2s.get(7).getText(), "C2");
    assertEquals(load2s.get(8).getText(), "C3");
    assertEquals(submit1s.get(2).getText(), "C");
    assertEquals(submit2s.get(6).getText(), "C1");
    assertEquals(submit2s.get(7).getText(), "C2");
    assertEquals(submit2s.get(8).getText(), "C3");

    wizard.save().publish();
  }

  @Test
  public void testAttachmentScriptingObject() {
    // TODO: check attachment contents
    final String itemName = context.getFullName("attachment sripting oject item");
    // text file creation + edit
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Attachment script object collection");

    var clickButtonAndValidateMessage = buildAscMessageValidator(wizard, ASC_MESSAGE_DIV_ID);

    wizard.editbox(1, itemName);
    clickButtonAndValidateMessage.accept("Create text file", "b.txt");

    SummaryPage item = wizard.save().publish();
    assertTrue(item.attachments().attachmentExists("autotest text file"));
    wizard = item.edit();
    clickButtonAndValidateMessage.accept("Edit text file", "text file succesfully edited");

    item = wizard.saveNoConfirm();
    assertTrue(item.attachments().attachmentExists("autotest text file"));

    // binary creation (image)
    wizard = item.edit();
    clickButtonAndValidateMessage.accept("Create binary attachment", "Binary attachment created!");

    item = wizard.saveNoConfirm();
    assertTrue(item.attachments().attachmentExists("EQUELLA Logo"));

    // resize image
    wizard = item.edit();
    clickButtonAndValidateMessage.accept("Get Image Size", "Width: 140 | Height: 350");
    clickButtonAndValidateMessage.accept("Resize Image", "Width: 70 | Height: 175");

    // html creation + single attachment deletion
    clickButtonAndValidateMessage.accept("Create html attachment", "I am a\nhtml\nattachment");

    item = wizard.saveNoConfirm();
    assertTrue(item.attachments().attachmentExists("html attachment"));
    wizard = item.edit();
    clickButtonAndValidateMessage.accept("Remove html attachment", "html attachment deleted");

    item = wizard.saveNoConfirm();
    assertFalse(item.attachments().attachmentExists("html attachment"));

    // equella resource attachment
    wizard = item.edit();
    clickButtonAndValidateMessage.accept("Create resource attachment", "Resource Attachment");

    item = wizard.saveNoConfirm();
    assertTrue(item.attachments().attachmentExists("Equella resource"));

    // custom attachment
    wizard = item.edit();
    clickButtonAndValidateMessage.accept("Create custom attachment", "custom attachment added");

    // custom atachment details
    WaitForTextInDiv waitForTextInDiv = new WaitForTextInDiv("www.google.com", ASC_MESSAGE_DIV_ID);
    clickAscButtonAndWait("Get custom details", waitForTextInDiv, wizard);
    String details = getAscMessage();
    assertTrue(details.contains("0")); // size
    assertTrue(details.contains("link")); // Custom Type
    assertTrue(details.contains("custom property here")); // Cust property
    assertTrue(details.contains("CUSTOM")); // Type
    assertTrue(details.contains("www.google.com")); // URL

    // get Mime details for a jpeg
    clickButtonAndValidateMessage.accept(
        "Get Mime Details",
        "Type: image/jpeg\nDescription: Image\nFile Extensions: jfif\njif\njpe\njpeg\njpg");

    // delete all attachments
    clickButtonAndValidateMessage.accept("Delete all attachments", "all attachments deleted");

    item = wizard.saveNoConfirm();
    assertFalse(item.hasAttachmentsSection());
  }

  @Test
  public void testUtilsScriptingObject() throws ParseException {
    final String itemName = context.getFullName("utils sripting oject item");
    SummaryPage urlItem = SearchPage.searchAndView(context, "Facet 2");
    String itemUrl = context.getBaseUrl() + "items/" + urlItem.getItemId() + "/";

    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Utils script object collection");
    wizard.editbox(1, itemName);
    // date parsing
    String date = "25/10/89";
    ascEditbox(3, "date", date);
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
    Date parsedDate = sdf.parse(date);

    var waitForTime = new WaitForTextInDiv("Wed Oct 25 00:00:00", "dateResult");
    clickAscButtonAndWait("Parse Date", waitForTime, wizard);
    assertEquals(
        removeTimeZone(getDivMessageForId("dateResult")), removeTimeZone(parsedDate.toString()));

    // Facet Count
    ascEditbox(3, "facetquery", "Facet");
    String applesAndPears = "Apples : 2\nPears : 1";
    var waitForApplesAndPears = new WaitForTextInDiv(applesAndPears, "facetResults");
    clickAscButtonAndWait("Facet Count", waitForApplesAndPears, wizard);
    assertEquals(getDivMessageForId("facetResults"), applesAndPears);

    // Query Count
    ascEditbox(3, "querycount", "Apples");
    var waitFor2QueryCount = new WaitForTextInDiv("2", "queryCountResult");
    clickAscButtonAndWait("Query Count", waitFor2QueryCount, wizard);
    assertEquals(getDivMessageForId("queryCountResult"), "2");

    // URL For item
    ascEditbox(3, "item", "Facet 2");
    var waitForItemUrl = new WaitForTextInDiv(itemUrl, "itemURLResult");
    clickAscButtonAndWait("Get URL", waitForItemUrl, wizard);
    assertEquals(getDivMessageForId("itemURLResult"), itemUrl);
    assertEquals(Integer.parseInt(getDivMessageForId("responseCode")), 200);
    assertFalse(Boolean.valueOf(getDivMessageForId("isResponseError")));
    assertEquals(getDivMessageForId("responseContentType"), "text/html;charset=UTF-8");

    // get collection details
    String nameAndDesc =
        "Name: Utils script object collection\n"
            + "Description: Collection for testing the util scripting object";
    var waitForCollectionDetails = new WaitForTextInDiv(nameAndDesc, "collectionDetails");
    clickAscButtonAndWait("Get collection details", waitForCollectionDetails, wizard);
    assertEquals(getDivMessageForId("collectionDetails"), nameAndDesc);

    // create xml document
    String emptyTextField = "Text field empty";
    var waitForEmptyTextField = new WaitForTextInDiv(emptyTextField, "xml");
    clickAscButtonAndWait("Xml from string", waitForEmptyTextField, wizard);
    assertEquals(getDivMessageForId("xml"), emptyTextField);

    ascEditbox(3, "xmlstring", "<leaf>wooooo</leaf>");

    String xmlCreated = "Xml document created from wooooo";
    var waitForXmlCreated = new WaitForTextInDiv(xmlCreated, "xml");
    clickAscButtonAndWait("Xml from string", waitForXmlCreated, wizard);
    assertEquals(getDivMessageForId("xml"), xmlCreated);

    wizard.save().publish();
  }

  private String removeTimeZone(String date) {
    String[] splits = date.split(" ");
    String newDate = "";
    int count = 1;
    for (String s : splits) {
      if (count != 5) {
        newDate += s;
        if (count != splits.length) {
          newDate += " ";
        }
      }
      count++;
    }
    return newDate;
  }

  @Test
  public void testStagingScriptingObject() {
    final String itemName = context.getFullName("staging scripting object item");

    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Staging script object collection");
    wizard.editbox(1, itemName);

    // Create binary file
    String gif = "equellaLogo.gif";
    var waitForGif = new WaitForTextInDiv(gif, "stagingFiles");
    clickAscButtonAndWait("Create binary file", waitForGif, wizard);
    assertEquals(getDivMessageForId("stagingFiles"), gif);

    // Create text file
    String txt = "autotest.txt";
    var waitForTxt = new WaitForTextInDiv(txt, "stagingFiles");
    clickAscButtonAndWait("Create text file", waitForTxt, wizard);
    assertEqualsNoOrder(
        getDivMessageForId("stagingFiles").split("\n"),
        new String[] {"autotest.txt", "equellaLogo.gif"});

    // fileHandle get details
    ascSelectDropdown("fileDetails", txt);
    var waitForTxtDetails = new WaitForTextInDiv(txt, "details");
    clickAscButtonAndWait("Get File Details", waitForTxtDetails, wizard);

    // TODO: check file details accuracy
    // Copy
    var waitForAutotestTxt = new WaitForTextInDiv("autotest.txt", "stagingFiles");
    clickAscButtonAndWait("Copy all files", waitForAutotestTxt, wizard);
    assertEqualsNoOrder(
        getDivMessageForId("stagingFiles").split("\n"),
        new String[] {
          "autotest.txt", "Copy of autotest.txt", "Copy of equellaLogo.gif", "equellaLogo.gif"
        });

    // Delete
    ascSelectDropdown("delFileList", "Copy of autotest.txt");
    clickAscButton("Delete file", wizard);
    String copyGifName = "Copy of equellaLogo.gif";
    ascSelectDropdown("delFileList", copyGifName);
    var waitForGifDeleted = new WaitForTextInDiv(copyGifName, "stagingFiles", false);
    clickAscButtonAndWait("Delete file", waitForGifDeleted, wizard);
    assertEqualsNoOrder(
        getDivMessageForId("stagingFiles").split("\n"),
        new String[] {"autotest.txt", "equellaLogo.gif"});

    wizard.cancel(new ContributePage(context));
  }

  @Test
  public void testUserScriptingObject() {
    final String itemName = context.getFullName("user scripting object item");

    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("User script object collection");

    var clickButtonAndValidateMessage = buildAscMessageValidator(wizard, ASC_MESSAGE_DIV_ID);

    wizard.editbox(1, itemName);
    // get users details
    var waitForAutoTest = new WaitForTextInDiv("AutoTest", "ascMessage");
    clickAscButtonAndWait("Get user details", waitForAutoTest, wizard);

    String details = getAscMessage();
    assertTrue(details.contains("AutoTest"));
    assertTrue(details.contains("Auto"));
    assertTrue(details.contains("Test"));
    assertTrue(details.contains("junk@autotest.com.au"));
    assertTrue(details.contains("group 1 - 70098d57-fc2a-474f-b1b7-4f26213ad211"));
    assertTrue(details.contains("group 3 child - 24f715db-a580-44d0-bf4a-caa0b5a3eb41"));

    String details1 = getAscMessage1().getText();
    assertTrue(details1.contains("adfcaf58-241b-4eca-9740-6a26d1c3dd58"));
    assertTrue(details1.contains("AutoTest"));
    assertTrue(details1.contains("Auto"));
    assertTrue(details1.contains("Test"));
    assertTrue(details1.contains("junk@autotest.com.au"));

    // check groups
    ascSelectDropdown("groups", "group 3 child");
    clickButtonAndValidateMessage.accept("in group", "Yes");

    ascSelectDropdown("groups", "group 2");
    clickButtonAndValidateMessage.accept("in group", "No");

    wizard.save().publish();
  }

  private WaitingPageObject<UniversalControl> fixme(UniversalControl control) {
    return control.attachNameWaiter("FROG", false);
  }

  @Test
  public void testNavScriptingObject() {
    final String itemName = context.getFullName("nav scipting object item");

    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Navigation script object collection");

    var clickButtonAndWait = buildClickButtonAndWaiter(wizard, STRUCTURE_DIV_ID);

    wizard.editbox(1, itemName);
    // package upload
    UniversalControl universal = wizard.universalControl(3);
    FileUniversalControlType packageUpload =
        universal.addDefaultResource(new FileUniversalControlType(universal));
    packageUpload.uploadFile(Attachments.get("package.zip"), NAME_PACKAGE);
    universal
        .editResource(new PackageAttachmentEditPage(universal), NAME_PACKAGE)
        .showStructure()
        .save();
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains(NAME_PACKAGE));

    // Delete Children.
    var waitForChildrenRemoved = new WaitForTextInDiv(CHILD_NODE, STRUCTURE_DIV_ID, false);
    clickAscButtonAndWait("deleteChildren", waitForChildrenRemoved, wizard);

    // add child to node to be deleted
    ascEditbox(4, "nodeName", "child");
    ascSelectDropdown("addList", NAME_PACKAGE);
    clickButtonAndWait.accept("addNode", NAME_PACKAGE);
    clickAscButton("deleteNode", wizard);

    wizard
        .getWaiter()
        .until(ExpectedConditions.textToBe(By.id(STRUCTURE_DIV_ID), "Split View Allowed: No"));
    assertFalse(getDivMessageForId(STRUCTURE_DIV_ID).contains(NAME_PACKAGE));
    assertFalse(getDivMessageForId(STRUCTURE_DIV_ID).contains("child"));

    // Initialise Structure
    var waitForInitialise = new WaitForTextInDiv(NAME_PACKAGE, STRUCTURE_DIV_ID);
    clickAscButtonAndWait("initialise", waitForInitialise, wizard);
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains(NAME_PACKAGE));
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("index.html"));

    // Delete all
    clickAscButton("deleteAll", wizard);
    wizard
        .getWaiter()
        .until(ExpectedConditions.textToBe(By.id(STRUCTURE_DIV_ID), "Split View Allowed: No"));
    assertFalse(getDivMessageForId(STRUCTURE_DIV_ID).contains(NAME_PACKAGE));
    assertFalse(getDivMessageForId(STRUCTURE_DIV_ID).contains("index.html"));

    // Add root + child
    String base = "base";
    ascEditbox(4, "nodeName", base);
    clickButtonAndWait.accept("addNode", base);
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains(base));

    ascEditbox(4, "nodeName", "child 1");
    ascSelectDropdown("addList", "base");
    clickButtonAndWait.accept("addNode", "child 1");
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("child 1"));

    ascEditbox(4, "nodeName", "child 2");
    ascSelectDropdown("addList", "base");
    clickButtonAndWait.accept("addNode", "child 2");
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("base"));
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("child 1"));
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("child 2"));

    // Add 2 tabs to child
    ascSelectDropdown("allNodes", "child 1");
    ascSelectDropdown("attachments", NAME_PACKAGE);
    ascEditbox(4, "tabName", "data");
    clickAscButton("createTab", wizard);
    // After clicking, the page will be refreshed and the tabName input will be empty.
    waitUntilTabNameInputIsEmpty();

    ascSelectDropdown("allNodes", "child 1");
    ascSelectDropdown("attachments", "index.html");
    ascEditbox(4, "tabName", "index");
    clickAscButton("createTab", wizard);
    // After clicking, the page will be refreshed and the tabName input will be empty.
    waitUntilTabNameInputIsEmpty();

    // TODO: check tabs exist (could do on nav builder page)
    // delete created node
    ascSelectDropdown("delNodeList", "child 2");
    clickAscButton("deleteNode", wizard);
    wizard
        .getWaiter()
        .until(
            ExpectedConditions.textToBe(
                By.id(STRUCTURE_DIV_ID),
                "Split View Allowed: No\n" + "\n" + "base\n" + "  child 1"));
    assertFalse(getDivMessageForId(STRUCTURE_DIV_ID).contains("child 2"));
    // Switch split view
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("No"));

    var waitForYes = new WaitForTextInDiv("Yes", STRUCTURE_DIV_ID);
    clickAscButtonAndWait("splitSwitch", waitForYes, wizard);
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("Yes"));

    var waitForNo = new WaitForTextInDiv("No", STRUCTURE_DIV_ID);
    clickAscButtonAndWait("splitSwitch", waitForNo, wizard);
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("No"));
    wizard = wizard.next();
    wizard.save().publish();
  }

  @Test
  public void testDrmScriptingOject() {
    final String itemName = context.getFullName("drm item");
    Random random = new Random();

    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("DRM script object collection");
    boolean bool1 = random.nextBoolean();
    boolean bool2 = random.nextBoolean();
    boolean bool3 = random.nextBoolean();
    int maxUsage = random.nextInt(100);
    String ToA = "use me liberally";

    wizard.editbox(1, itemName);
    clickAscCheckbox("allowSummary", bool1);
    clickAscCheckbox("attribution", bool2);
    clickAscCheckbox("enforceAttribution", bool3);
    clickAscCheckbox("hideLicences", bool1);
    ascEditbox(3, "maxUsage", String.valueOf(maxUsage));
    clickAscCheckbox("ownerAccept", false);
    clickAscCheckbox("previewAllowed", bool3);
    clickAscCheckbox("sectorRestrict", bool1);
    clickAscCheckbox("licenceCount", bool2);
    clickAscCheckbox("compilationAccept", bool3);
    ascEditbox(3, "ToA", ToA);

    wizard.save().publish();

    assertTrue(getDivMessageForId("allowSummary").contains(String.valueOf(bool1)));
    assertTrue(getDivMessageForId("attribution").contains(String.valueOf(bool2)));
    assertTrue(getDivMessageForId("enforce").contains(String.valueOf(bool3)));
    assertTrue(getDivMessageForId("hide").contains(String.valueOf(bool1)));
    assertTrue(getDivMessageForId("maxUser").contains(String.valueOf(maxUsage)));
    assertTrue(getDivMessageForId("ownerAccept").contains(String.valueOf(false)));
    assertTrue(getDivMessageForId("preview").contains(String.valueOf(bool3)));
    assertTrue(getDivMessageForId("sector").contains(String.valueOf(bool1)));
    assertTrue(getDivMessageForId("licence").contains(String.valueOf(bool2)));
    assertTrue(getDivMessageForId("compilation").contains(String.valueOf(bool3)));
    assertTrue(getDivMessageForId("ToA").contains(ToA));
  }

  @Test
  public void testItemScriptObject() {
    final String itemName = context.getFullName("item script oject item");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Item script object collection");

    var clickButtonAndValidateMessage = buildAscMessageValidator(wizard, ASC_MESSAGE_DIV_ID);

    wizard.editbox(1, itemName);

    String resourceAttachment = "Resource Attachment";
    clickButtonAndValidateMessage.accept("Get item", resourceAttachment);

    clickButtonAndValidateMessage.accept("Get latest version item", resourceAttachment);

    clickButtonAndValidateMessage.accept("Get live item", resourceAttachment);

    var waitForTrue = new WaitForTextInDiv("true", ASC_MESSAGE_DIV_ID);
    clickAscButtonAndWait("Get item xml", waitForTrue, wizard);
    assertTrue(Boolean.valueOf(getAscMessage()));

    clickButtonAndValidateMessage.accept("Get item status", "live");

    clickButtonAndValidateMessage.accept("Get item collection", "Basic Items");

    clickButtonAndValidateMessage.accept(
        "Get item Description", "Attachment to be added through scripting");

    clickButtonAndValidateMessage.accept("Get owner", "adfcaf58-241b-4eca-9740-6a26d1c3dd58");

    clickButtonAndValidateMessage.accept(
        "Add shared owner", "ad2c30da-2b1c-4427-b21c-45ef5bd09f11");

    clickAscButtonAndWait("Remove shared owner", waitForTrue, wizard);
    assertTrue(Boolean.valueOf(getAscMessage()));

    clickButtonAndValidateMessage.accept("Set owner", "ad2c30da-2b1c-4427-b21c-45ef5bd09f11");
  }

  @Test
  public void metadataScriptObject() {
    final String attName = "derpy";
    final String itemName = context.getFullName("metadata script oject item");
    final String ASC_MESSAGE_DIV_ID = "ascMessage";
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Metadata script object collection");

    var clickButtonAndValidateMessage = buildAscMessageValidator(wizard, ASC_MESSAGE_DIV_ID);

    wizard.editbox(1, itemName);
    UniversalControl control = wizard.universalControl(3);
    FileUniversalControlType fc = control.addDefaultResource(new FileUniversalControlType(control));
    fc.uploadFile(Attachments.get("fireworks.dng"));
    control.editResource(fc.fileEditor(), "fireworks.dng").setDisplayName(attName).save();

    clickButtonAndValidateMessage.accept(
        "Get metadata for attachment", "Successfully retrieved Metadata for attachment");

    clickButtonAndValidateMessage.accept(
        "Get metadata for file", "Successfully retrieved Metadata for file");

    clickButtonAndValidateMessage.accept(
        "Get types available", "[MakerNotes, Composite, File, XMP, EXIF]");

    getAscInput(By.id("alltype")).sendKeys("EXIF");

    clickButtonAndValidateMessage.accept("Get all for type", "124, Artist: Adam Croser");

    getAscInput(By.id("firstkey")).sendKeys("LensID");

    clickButtonAndValidateMessage.accept(
        "Get first for key", "LensID: AF-S Zoom-Nikkor 24-70mm f/2.8G ED");

    getAscInput(By.id("spectype")).sendKeys("XMP");
    getAscInput(By.id("speckey")).sendKeys("LensID");

    clickButtonAndValidateMessage.accept("Get specific key", "XMP:LensID: 147");

    // Check saved shiznit
    SummaryPage summary = wizard.save().publish();
    assertEquals(
        summary.getItemDescription(), "2013 New Year's Eve, Brighton Beach, South Australia");
    DisplayNodesPage nodez = summary.displayNodes();
    assertEquals(nodez.getTextByName("Tags"), "Adelaide, beach, fireworks");
    assertEquals(nodez.getTextByName("Camera"), "NIKON D300");
    assertEquals(nodez.getTextByName("Custom"), "[Author, Camera, Camera Lens]");

    // Check attachment data
    String deets = summary.attachments().attachmentDetails(attName);
    assertTrue(deets.contains("Adam Croser"), "Details did not contain: Adam Croser");
    assertTrue(deets.contains("NIKON D300"), "Details did not contain: NIKON D300");
    assertTrue(deets.contains("24-70mm f/2.8"), "Details did not contain: 24-70mm f/2.8");

    // Search for saved shiznit e.g Author, Tags
    SearchPage sp = new SearchPage(context).load();
    sp.search("Adelaide");

    assertTrue(sp.results().doesResultExist(itemName));
  }

  // FIXME: could create a page object that extends WizardTabPage for these
  // advanced script control page methods

  /**
   * Just finds an input with the supplied text. There is no parent context or anything, so make
   * your button values page unique!
   *
   * @param text
   * @return
   */
  private <T extends PageObject> T clickAscInput(String text, WaitingPageObject<T> returnTo) {
    getAscInput(text).click();
    return returnTo.get();
  }

  // same as above but for <button> instead of <input>
  private <T extends PageObject> T clickAscButton(String text, WaitingPageObject<T> returnTo) {
    getAscButton(text).click();
    return returnTo.get();
  }

  // Click the button and wait for the provided condition.
  private <T extends PageObject> void clickAscButtonAndWait(
      String buttonLabel, WaitForTextInDiv waitFor, AbstractPage<T> currentPage) {
    getAscButton(buttonLabel).click();
    // After clicking the page will be reloaded, so make sure the page finished reloading.
    currentPage.get();
    getWaiter().until(waitFor.condition());
    // This extra wait at the end reduces test flakiness,
    // likely due to inconsistencies in the page request response time.
    currentPage.get();
  }

  // Wait for a specific text to be present or not present in a div.
  private record WaitForTextInDiv(String expectedText, String divId, boolean isPresent) {
    // Constructor that would be used most of the time
    public WaitForTextInDiv(String expectedText, String divId) {
      this(expectedText, divId, true);
    }

    public ExpectedCondition<Boolean> condition() {
      By containingDiv = By.xpath("//div[@id='" + divId + "']");
      ExpectedCondition<Boolean> presentCondition =
          ExpectedConditions.textToBePresentInElementLocated(containingDiv, expectedText);

      return isPresent ? presentCondition : ExpectedConditions.not(presentCondition);
    }
  }

  private ErrorPage clickAscButtonExpectError(String text, WizardPageTab wizard) {
    getAscInput(text).click();
    return new ErrorPage(context).get();
  }

  /**
   * Just finds an input with the supplied text. There is no parent context or anything, so make
   * your button values page unique!
   *
   * @param text
   * @return
   */
  private WebElement getAscInput(String text) {
    return context.getDriver().findElement(By.xpath("//input[@value='" + text + "']"));
  }

  private WebElement getAscInput(By by) {
    return context.getDriver().findElement(by);
  }

  private WebDriverWait getWaiter() {
    return new WebDriverWait(context.getDriver(), Duration.ofSeconds(30));
  }

  // same as above but with <button> not <input>
  private WebElement getAscButton(String value) {
    By buttonXpath = By.xpath("//button[@value='" + value + "']");
    return getWaiter().until(ExpectedConditions.elementToBeClickable(buttonXpath));
  }

  private List<WebElement> getDivsByPrefix(String prefix) {
    return context.getDriver().findElements(By.xpath("//div[starts-with(@id,'" + prefix + "')]"));
  }

  /**
   * If a wizard only happens to have one ASC on it, you should include a DIV with id='ascMessage'
   * to retrieve any output values.
   */
  private String getAscMessage() {
    By ascMessageXpath = By.xpath("//div[@id='ascMessage']/span");
    WebDriverWait wait = getWaiter();
    wait.until(ExpectedConditions.visibilityOfElementLocated(ascMessageXpath));
    return wait.until(d -> d.findElement(ascMessageXpath).getText());
  }

  private WebElement getAscMessage1() {
    return context.getDriver().findElement(By.xpath("//div[@id='ascMessage1']/span"));
  }

  /**
   * Same as getAscMessage() except that the div id is variable and the text is returned instead of
   * the element
   */
  private String getDivMessageForId(String id) {
    return context.getDriver().findElement(By.xpath("//div[@id = '" + id + "']")).getText();
  }

  private void ascEditbox(int ctrlNum, String suffix, String text) {
    By locator = By.name("c" + ctrlNum + suffix);
    WebDriverWait wait = new WebDriverWait(context.getDriver(), Duration.ofSeconds(30));
    WebElement field = wait.until(ExpectedConditions.elementToBeClickable(locator));
    field.clear();
    field.sendKeys(text);
  }

  /** Takes the id of the select element and the value of the option you want to select. */
  private void ascSelectDropdown(String id, String optText) {
    WebDriverWait wait = new WebDriverWait(context.getDriver(), Duration.ofSeconds(30));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
    Select dropdown = new Select(context.getDriver().findElement(By.id(id)));
    dropdown.selectByVisibleText(optText);
  }

  private void clickAscCheckbox(String id, boolean enable) {
    WebElement checkbox = context.getDriver().findElement(By.id(id));
    if (checkbox.isSelected() != enable) {
      checkbox.click();
    }
  }

  private void waitUntilTabNameInputIsEmpty() {
    WebDriverWait wait = new WebDriverWait(context.getDriver(), Duration.ofSeconds(5));
    wait.until(ExpectedConditions2.inputValueToBe(By.id("tabName"), ""));
  }

  // It builds a function that will click the button and then wait for the message.
  private BiConsumer<String, String> buildClickButtonAndWaiter(
      WizardPageTab wizard, String buttonDivId) {
    return (buttonLabel, expectedMessage) -> {
      var waitForTextInDiv = new WaitForTextInDiv(expectedMessage, buttonDivId);
      clickAscButtonAndWait(buttonLabel, waitForTextInDiv, wizard);
    };
  }

  // It builds a function that will click the button and then validate the ASC message.
  private BiConsumer<String, String> buildAscMessageValidator(
      WizardPageTab wizard, String buttonDivId) {
    return (buttonLabel, expectedMessage) -> {
      buildClickButtonAndWaiter(wizard, buttonDivId).accept(buttonLabel, expectedMessage);
      assertEquals(getAscMessage(), expectedMessage);
    };
  }
}
