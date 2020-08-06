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
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

@TestInstitution("asc")
public class AdvancedScriptControlTests extends AbstractCleanupTest {

  public static final String NAME_PACKAGE = "Zou ba! Visiting China: Is this your first visit?";
  private String expectedString;
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
    assertEquals(
        getAscMessage().getText().trim(), "I told you to upload and XML document, so do it!");

    wizard.editbox(1, itemName);
    wizard.addSingleFile(3, Attachments.get("complex.xml"));

    // assert text == 'tns:addUserToGroupResponse'
    assertEquals(getAscMessage().getText().trim(), "tns:addUserToGroupResponse");

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
    // @formatter:on

    for (int i = 0; i < 4; i++) {
      // press the button to populate XML
      clickAscInput(populateButtons[i], wizard);
      By expectedElement = By.xpath("//span[normalize-space(.)='" + expectedOriginals[i] + "']");
      wizard.getWaiter().until(ExpectedConditions.visibilityOfElementLocated(expectedElement));
      assertEquals(getAscMessage().getText().trim(), expectedOriginals[i]);

      // press the button to kill a subtree
      clickAscInput(deleteButtons[i], wizard);
      expectedElement = By.xpath("//span[normalize-space(.)='" + expectedModifed[i] + "']");
      wizard.getWaiter().until(ExpectedConditions.visibilityOfElementLocated(expectedElement));
      assertEquals(getAscMessage().getText().trim(), expectedModifed[i]);
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
    assertEquals(getAscMessage().getText().trim(), "A");

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
   * Can use POST for information passing
   * http://dtec.equella.com/DTEC/test/editTest.aspx?testId=14515
   *
   * @throws MalformedURLException
   */
  @Test(enabled = false)
  public void testRedirectionServlet() throws MalformedURLException {
    String itemName = context.getFullName("dtec14515");
    WizardPageTab wizard =
        new ContributePage(context)
            .load()
            .openWizard("dtec14515 Can use POST for information passing");
    wizard.editbox(1, itemName);

    String echoServerUrl = context.getTestConfig().getEchoServerUrl() + "/index.do";
    context.getDriver().findElement(By.id("submitto")).sendKeys(echoServerUrl);
    final String toEcho = "Echo This!";
    context.getDriver().findElement(By.id("query")).sendKeys(toEcho);

    clickAscInput("Do it", wizard);

    wizard
        .getWaiter()
        .until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath(
                    "//div[@id='results' and text()=" + AbstractPage.quoteXPath(toEcho) + "]")));

    wizard.save().publish();
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
    wizard.editbox(1, itemName);
    expectedString = "b.txt";
    clickAscButtonAndWait("Create text file", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString, "ASC Message was wrong");
    SummaryPage item = wizard.save().publish();
    assertTrue(item.attachments().attachmentExists("autotest text file"));
    wizard = item.edit();
    expectedString = "text file succesfully edited";
    clickAscButtonAndWait("Edit text file", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString, "ASC Message was wrong");
    item = wizard.saveNoConfirm();
    assertTrue(item.attachments().attachmentExists("autotest text file"));

    // binary creation (image)
    wizard = item.edit();
    expectedString = "Binary attachment created!";
    clickAscButtonAndWait("Create binary attachment", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString, "ASC Message was wrong");
    item = wizard.saveNoConfirm();
    assertTrue(item.attachments().attachmentExists("EQUELLA Logo"));

    // resize image
    wizard = item.edit();
    expectedString = "Width: 140 | Height: 350";
    clickAscButtonAndWait("Get Image Size", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString, "ASC Message was wrong");
    expectedString = "Width: 70 | Height: 175";
    clickAscButtonAndWait("Resize Image", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString, "ASC Message was wrong");

    // html creation + single attachment deletion
    expectedString = "I am a\nhtml\nattachment";
    clickAscButtonAndWait("Create html attachment", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    item = wizard.saveNoConfirm();
    assertTrue(item.attachments().attachmentExists("html attachment"));
    wizard = item.edit();
    expectedString = "html attachment deleted";
    clickAscButtonAndWait("Remove html attachment", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    item = wizard.saveNoConfirm();
    assertFalse(item.attachments().attachmentExists("html attachment"));

    // equella resource attachment
    wizard = item.edit();
    expectedString = "Resource Attachment";
    clickAscButtonAndWait("Create resource attachment", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    item = wizard.saveNoConfirm();
    assertTrue(item.attachments().attachmentExists("Equella resource"));

    // custom attachment
    wizard = item.edit();
    expectedString = "custom attachment added";
    clickAscButtonAndWait("Create custom attachment", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);

    // custom atachment details
    clickAscButtonAndWait("Get custom details", wizard, "www.google.com", ASC_MESSAGE_DIV_ID);
    String details = getAscMessage().getText();
    assertTrue(details.contains("0")); // size
    assertTrue(details.contains("link")); // Custom Type
    assertTrue(details.contains("custom property here")); // Cust property
    assertTrue(details.contains("CUSTOM")); // Type
    assertTrue(details.contains("www.google.com")); // URL

    // get Mime details for a jpeg
    expectedString =
        "Type: image/jpeg\nDescription: Image\nFile Extensions: jfif\njif\njpe\njpeg\njpg";
    clickAscButtonAndWait("Get Mime Details", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);

    // delete all attachments
    expectedString = "all attachments deleted";
    clickAscButtonAndWait("Delete all attachments", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
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
    expectedString = "Wed Oct 25 00:00:00";
    clickAscButtonAndWait("Parse Date", wizard, expectedString, "dateResult");
    assertEquals(
        removeTimeZone(getDivMessageForId("dateResult")), removeTimeZone(parsedDate.toString()));
    // Facet Count
    ascEditbox(3, "facetquery", "Facet");
    expectedString = "Apples : 2\nPears : 1";
    clickAscButtonAndWait("Facet Count", wizard, expectedString, "facetResults");
    assertEquals(getDivMessageForId("facetResults"), expectedString);
    // Query Count
    ascEditbox(3, "querycount", "Apples");
    clickAscButtonAndWait("Query Count", wizard, "2", "queryCountResult");
    assertEquals(getDivMessageForId("queryCountResult"), "2");
    // URL For item
    ascEditbox(3, "item", "Facet 2");
    clickAscButtonAndWait("Get URL", wizard, itemUrl, "itemURLResult");
    assertEquals(getDivMessageForId("itemURLResult"), itemUrl);
    assertEquals(Integer.parseInt(getDivMessageForId("responseCode")), 200);
    assertFalse(Boolean.valueOf(getDivMessageForId("isResponseError")));
    assertEquals(getDivMessageForId("responseContentType"), "text/html;charset=UTF-8");
    // get collection details
    expectedString =
        "Name: Utils script object collection\nDescription: Collection for testing the util scripting object";
    clickAscButtonAndWait("Get collection details", wizard, expectedString, "collectionDetails");
    assertEquals(getDivMessageForId("collectionDetails"), expectedString);
    // create xml document
    expectedString = "Text field empty";
    clickAscButtonAndWait("Xml from string", wizard, expectedString, "xml");
    assertEquals(getDivMessageForId("xml"), expectedString);
    ascEditbox(3, "xmlstring", "<leaf>wooooo</leaf>");

    expectedString = "Xml document created from wooooo";
    clickAscButtonAndWait("Xml from string", wizard, expectedString, "xml");
    assertEquals(getDivMessageForId("xml"), expectedString);

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
    expectedString = "equellaLogo.gif";
    clickAscButtonAndWait("Create binary file", wizard, expectedString, "stagingFiles");
    assertEquals(getDivMessageForId("stagingFiles"), expectedString);
    // Create text file
    expectedString = "autotest.txt";
    clickAscButtonAndWait("Create text file", wizard, expectedString, "stagingFiles");
    assertEqualsNoOrder(
        getDivMessageForId("stagingFiles").split("\n"),
        new String[] {"autotest.txt", "equellaLogo.gif"});
    // fileHandle get details
    ascSelectDropdown("fileDetails", "autotest.txt");
    clickAscButton("Get File Details", wizard);
    // TODO: check file details accuracy
    // Copy
    clickAscButtonAndWait("Copy all files", wizard, "autotest.txt", "stagingFiles");
    assertEqualsNoOrder(
        getDivMessageForId("stagingFiles").split("\n"),
        new String[] {
          "autotest.txt", "Copy of autotest.txt", "Copy of equellaLogo.gif", "equellaLogo.gif"
        });

    // Delete
    ascSelectDropdown("delFileList", "Copy of autotest.txt");
    clickAscButton("Delete file", wizard);
    ascSelectDropdown("delFileList", "Copy of equellaLogo.gif");
    clickAscButtonAndWait("Delete file", wizard, "autotest.txt", "stagingFiles");
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
    wizard.editbox(1, itemName);
    // get users details
    expectedString = "AutoTest";
    clickAscButtonAndWait("Get user details", wizard, expectedString, "ascMessage");
    String details = getAscMessage().getText();
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
    expectedString = "Yes";
    clickAscButtonAndWait("in group", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    ascSelectDropdown("groups", "group 2");
    expectedString = "No";
    clickAscButtonAndWait("in group", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);

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
    // Delete Children + Root node
    clickAscButton("deleteChildren", wizard);
    // add child to node to be deleted
    ascEditbox(4, "nodeName", "child");
    ascSelectDropdown("addList", NAME_PACKAGE);
    clickAscButton("addNode", wizard);
    clickAscButton("deleteNode", wizard);
    wizard
        .getWaiter()
        .until(ExpectedConditions.textToBe(By.id(STRUCTURE_DIV_ID), "Split View Allowed: No"));
    assertFalse(getDivMessageForId(STRUCTURE_DIV_ID).contains(NAME_PACKAGE));
    assertFalse(getDivMessageForId(STRUCTURE_DIV_ID).contains("child"));
    // Initialise Structure
    clickAscButtonAndWait("initialise", wizard, NAME_PACKAGE, STRUCTURE_DIV_ID);
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
    ascEditbox(4, "nodeName", "base");
    expectedString = "base";
    clickAscButtonAndWait("addNode", wizard, expectedString, STRUCTURE_DIV_ID);
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains(expectedString));
    ascEditbox(4, "nodeName", "child 1");
    ascSelectDropdown("addList", "base");
    expectedString = "child 1";
    clickAscButtonAndWait("addNode", wizard, expectedString, STRUCTURE_DIV_ID);
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("child 1"));
    ascEditbox(4, "nodeName", "child 2");
    ascSelectDropdown("addList", "base");
    expectedString = "child 2";
    clickAscButtonAndWait("addNode", wizard, expectedString, STRUCTURE_DIV_ID);
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("base"));
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("child 1"));
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("child 2"));
    // Add 2 tabs to child
    ascSelectDropdown("allNodes", "child 1");
    ascSelectDropdown("attachments", NAME_PACKAGE);
    ascEditbox(4, "tabName", "data");
    clickAscButton("createTab", wizard);
    ascSelectDropdown("allNodes", "child 1");
    ascSelectDropdown("attachments", "index.html");
    ascEditbox(4, "tabName", "index");
    clickAscButton("createTab", wizard);
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
    clickAscButtonAndWait("splitSwitch", wizard, "Yes", STRUCTURE_DIV_ID);
    assertTrue(getDivMessageForId(STRUCTURE_DIV_ID).contains("Yes"));
    clickAscButtonAndWait("splitSwitch", wizard, "No", STRUCTURE_DIV_ID);
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

    wizard.editbox(1, itemName);
    expectedString = "Resource Attachment";
    clickAscButtonAndWait("Get item", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    clickAscButtonAndWait("Get latest version item", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    clickAscButtonAndWait("Get live item", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    clickAscButtonAndWait("Get item xml", wizard, "true", ASC_MESSAGE_DIV_ID);
    assertTrue(Boolean.valueOf(getAscMessage().getText()));
    expectedString = "live";
    clickAscButtonAndWait("Get item status", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    expectedString = "Basic Items";
    clickAscButtonAndWait("Get item collection", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    expectedString = "Attachment to be added through scripting";
    clickAscButtonAndWait("Get item Description", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    expectedString = "adfcaf58-241b-4eca-9740-6a26d1c3dd58";
    clickAscButtonAndWait("Get owner", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    expectedString = "ad2c30da-2b1c-4427-b21c-45ef5bd09f11";
    clickAscButtonAndWait("Add shared owner", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
    clickAscButtonAndWait("Remove shared owner", wizard, "true", ASC_MESSAGE_DIV_ID);
    assertTrue(Boolean.valueOf(getAscMessage().getText()));
    expectedString = "ad2c30da-2b1c-4427-b21c-45ef5bd09f11";
    clickAscButtonAndWait("Set owner", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);
  }

  @Test
  public void metadataScriptObject() {
    final String attName = "derpy";
    final String itemName = context.getFullName("metadata script oject item");
    final String ASC_MESSAGE_DIV_ID = "ascMessage";
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Metadata script object collection");

    wizard.editbox(1, itemName);
    UniversalControl control = wizard.universalControl(3);
    FileUniversalControlType fc = control.addDefaultResource(new FileUniversalControlType(control));
    fc.uploadFile(Attachments.get("fireworks.dng"));
    control.editResource(fc.fileEditor(), "fireworks.dng").setDisplayName(attName).save();

    String expectedString = "Successfully retrieved Metadata for attachment";
    clickAscButtonAndWait(
        "Get metadata for attachment", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);

    expectedString = "Successfully retrieved Metadata for file";
    clickAscButtonAndWait("Get metadata for file", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);

    expectedString = "[MakerNotes, Composite, File, XMP, EXIF]";
    clickAscButtonAndWait("Get types available", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);

    getAscInput(By.id("alltype")).sendKeys("EXIF");
    expectedString = "124, Artist: Adam Croser";
    clickAscButtonAndWait("Get all for type", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);

    getAscInput(By.id("firstkey")).sendKeys("LensID");
    expectedString = "LensID: AF-S Zoom-Nikkor 24-70mm f/2.8G ED";
    clickAscButtonAndWait("Get first for key", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);

    getAscInput(By.id("spectype")).sendKeys("XMP");
    getAscInput(By.id("speckey")).sendKeys("LensID");
    expectedString = "XMP:LensID: 147";
    clickAscButtonAndWait("Get specific key", wizard, expectedString, ASC_MESSAGE_DIV_ID);
    assertEquals(getAscMessage().getText(), expectedString);

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

  private <T extends PageObject> T clickAscButtonAndWait(
      String text, WaitingPageObject<T> returnTo, String expectedString, String divId) {
    getAscButton(text).click();
    By expectedElement = By.xpath("//div[@id='" + divId + "']");
    returnTo
        .getWaiter()
        .until(ExpectedConditions.textToBePresentInElementLocated(expectedElement, expectedString));
    return returnTo.get();
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

  // same as above but with <button> not <input>
  private WebElement getAscButton(String value) {
    return context.getDriver().findElement(By.xpath("//button[@value='" + value + "']"));
  }

  private List<WebElement> getDivsByPrefix(String prefix) {
    return context.getDriver().findElements(By.xpath("//div[starts-with(@id,'" + prefix + "')]"));
  }

  /**
   * If a wizard only happens to have one ASC on it, you should include a DIV with id='ascMessage'
   * to retrieve any output values.
   *
   * @return
   */
  private WebElement getAscMessage() {
    By ascMessageXpath = By.xpath("//div[@id='ascMessage']/span");
    WebDriverWait wait = new WebDriverWait(context.getDriver(), 30);
    wait.until(ExpectedConditions.visibilityOfElementLocated(ascMessageXpath));
    return context.getDriver().findElement(ascMessageXpath);
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
    WebElement field = context.getDriver().findElement(By.name("c" + ctrlNum + suffix));
    WebDriverWait wait = new WebDriverWait(context.getDriver(), 30);
    wait.until(ExpectedConditions.visibilityOf(field));
    field.clear();
    field.sendKeys(text);
  }

  /**
   * Takes the id of the select element and the value of the option you want to select
   *
   * @param id
   * @param text
   */
  private void ascSelectDropdown(String id, String optText) {
    WebDriverWait wait = new WebDriverWait(context.getDriver(), 30);
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
}
