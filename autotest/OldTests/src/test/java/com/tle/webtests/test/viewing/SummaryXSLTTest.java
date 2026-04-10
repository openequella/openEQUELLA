package com.tle.webtests.test.viewing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.IntegrationTesterPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.test.AbstractIntegrationTest;
import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class SummaryXSLTTest extends AbstractIntegrationTest {

  private static final String ITEM_NAME = "SummaryXSLTTest";
  private static final String IMS_ITEM_NAME = "SummaryXSLTTest - IMS Package";
  private static final String COLLECTION_NAME = "Summary XSLT Collection";
  private static final String ITEMDIR = "items/268fbf77-ffdd-4731-a474-77c63c6aea62/1/";

  @Test
  public void viewItemWithXSLT() throws MalformedURLException {
    logon("AutoTest", "automated");
    new SearchPage(context).load().exactQuery(ITEM_NAME).viewFromTitle(ITEM_NAME);
    WebDriver driver = context.getDriver();
    // Assert xslt specific parts
    WebElement xmldoc = driver.findElement(By.tagName("xml"));
    assertEquals(xmldoc.findElement(By.xpath("./item/name")).getText(), ITEM_NAME);
    String itemdir = xmldoc.findElement(By.xpath("./itemdir")).getText();
    assertEquals(
        new URL(context.getBaseUrl() + ITEMDIR), new URL(new URL(context.getBaseUrl()), itemdir));
    xmldoc.findElement(By.xpath("./template"));
    assertEquals(xmldoc.findElement(By.xpath("./collection")).getText(), COLLECTION_NAME);
  }

  @Test
  public void viewIMSItemWithXSLT() throws MalformedURLException {
    IntegrationTesterPage tester = new IntegrationTesterPage(context, "token", "token").load();
    tester.select("selectOrAdd", "AutoTest", "", "");
    SelectionSession selection = new SelectionSession(context).get();
    selection.homeExactSearch(IMS_ITEM_NAME).viewFromTitle(IMS_ITEM_NAME);
    WebDriver driver = context.getDriver();
    // Assert xslt specific parts
    WebElement xmldoc = driver.findElement(By.tagName("xml"));
    assertEquals(xmldoc.findElement(By.xpath("./item/name")).getText(), IMS_ITEM_NAME);
    assertEquals(xmldoc.findElement(By.xpath("./viewims")).getText(), "viewscorm.jsp");
    assertEquals(
        xmldoc.findElement(By.xpath("./imsdir")).getText(),
        "IMS%20-%20Air%20pressure%20particle%20model.zip/");
    WebElement selectionNode = xmldoc.findElement(By.tagName("selection"));
    assertFunction(selectionNode, "selectAttachmentFunction");
    assertFunction(selectionNode, "selectPathFunction");
    assertFunction(selectionNode, "selectItemFunction");
  }

  @Override
  protected boolean isCleanupItems() {
    return false;
  }

  private void assertFunction(WebElement selectionNode, String func) {
    String functionName = selectionNode.findElement(By.xpath("./" + func)).getText();
    JavascriptExecutor exec = ((JavascriptExecutor) context.getDriver());
    assertTrue((Boolean) exec.executeScript("return jQuery.isFunction(" + functionName + ");"));
  }
}
