package com.tle.webtests.test.contribute.controls;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.dytech.devlib.PropBagEx;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.AbstractWizardControlsTest;
import com.tle.webtests.pageobject.wizard.controls.HTMLEditBoxControl;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class HTMLEditBoxTest extends AbstractWizardControlsTest {
  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  @Test
  public void lazyLoadTest() throws Exception {
    final String CONTENT = "Hello this is some content";
    // NB: do not alter 'wung' (see spellchecker below), so long as 'wung'
    // never becomes a word ...
    final String HTML =
        "<p>'...it wasn't all wung!'</p> <p>- Jolse Maginnis (after being accused of winging his"
            + " SOAP presentation with Clemens)</p>";
    final String DISABLED_HTML =
        "'...it wasn't all wung!'\n"
            + "- Jolse Maginnis (after being accused of winging his SOAP presentation with"
            + " Clemens)";

    // Load contribution wizard
    ContributePage contribute = new ContributePage(context).load();
    WizardPageTab wizard = contribute.openWizard("HTML Edit Box");

    wizard.editbox(1, context.getFullName("Lazy Load"));
    wizard.editbox(2, context.getFullName("Test out the normal/lazy load HTML editor controls"));

    HTMLEditBoxControl lazyHtmlEditBox = wizard.htmlEditBox(4, true);

    // Toggle edit on locked htmlEditBox and check enabled
    lazyHtmlEditBox.toggleEdit(); // Enable
    assertTrue(lazyHtmlEditBox.isEditable());

    lazyHtmlEditBox.toggleEdit(); // Disable
    assertFalse(lazyHtmlEditBox.isEditable());

    lazyHtmlEditBox.toggleEdit(); // Enable
    lazyHtmlEditBox.setHtmlContent(HTML);

    HTMLEditBoxControl htmlEditBox = wizard.htmlEditBox(3, false);
    htmlEditBox.setBodyContent(CONTENT);

    // Toggle edit on lazy htmlEditBox
    lazyHtmlEditBox = wizard.htmlEditBox(4, false);
    lazyHtmlEditBox.toggleEdit(); // Disable
    Assert.assertEquals(lazyHtmlEditBox.getBodyContent(), DISABLED_HTML);

    // wung is not a word
    lazyHtmlEditBox.invokeSpellChecker();
    Assert.assertTrue(
        lazyHtmlEditBox.hasMispeltWords(), "Expected spellchecker to find misspelt words");
    Assert.assertTrue(
        lazyHtmlEditBox.confirmMisspeltWord("wung"), "Expected spellchecker to highlight 'wung'");
    lazyHtmlEditBox.toggleSpellingSuggestionsContextMenu(false);
    lazyHtmlEditBox.toggleSpellingSuggestionsContextMenu(true);

    // Save Item
    SummaryPage summary = wizard.save().publish().get();

    // Check XML
    ItemId itemId = summary.getItemId();
    soap.login("AutoTest", "automated");
    PropBagEx itemXml = new PropBagEx(soap.getItem(itemId.getUuid(), itemId.getVersion(), null));
    assertTrue(checkItemXml(itemXml, CONTENT, HTML));
    soap.logout();

    // Edit item and check content is present and equal
    wizard = summary.edit();
    htmlEditBox = wizard.htmlEditBox(3, false);
    Assert.assertEquals(htmlEditBox.getBodyContent(), CONTENT);
    lazyHtmlEditBox = wizard.htmlEditBox(4, true);
    Assert.assertEquals(lazyHtmlEditBox.getBodyContent(), DISABLED_HTML);
    lazyHtmlEditBox.toggleEdit();
    Assert.assertEquals(lazyHtmlEditBox.getHtmlContent().replace("\n", " "), HTML);
  }

  private boolean checkItemXml(PropBagEx itemXml, String content, String html) {
    // Check /item/html/htmleditbox and /item/html/htmleditboxondemand
    String htmlEditBox = itemXml.getNode("item/controls/html/htmleditbox").trim();
    String lazyHtmlEditBox =
        itemXml.getNode("item/controls/html/htmleditboxondemand").replace("\n", " ").trim();

    return htmlEditBox.equals("<p>" + content + "</p>") && lazyHtmlEditBox.equals(html);
  }

  @Test
  public void itemXmlHtmlTest() throws Exception {
    // Load contribution wizard
    ContributePage contribute = new ContributePage(context).load();
    WizardPageTab wizard = contribute.openWizard("HTML Edit Box");

    wizard.editbox(1, context.getFullName("Item XML"));
    wizard.editbox(2, "HTML is stored correctly in the XML");
    HTMLEditBoxControl htmlEditBox = wizard.htmlEditBox(3, false);
    HTMLEditBoxControl lazyHtmlEditBox = wizard.htmlEditBox(4, true);

    htmlEditBox.setBodyContent("This is some fantastic content. http://www.google.com.au");
    lazyHtmlEditBox.toggleEdit();
    lazyHtmlEditBox.setBodyContent("This is also some fantastic content");

    // Save Item
    SummaryPage summary = wizard.save().publish().get();

    // Check HTML in XML and ensure no <body></body> tags
    ItemId itemId = summary.getItemId();
    soap.login("AutoTest", "automated");
    PropBagEx itemXml = new PropBagEx(soap.getItem(itemId.getUuid(), itemId.getVersion(), null));
    String htmlNode = itemXml.getNode("item/controls/html/htmleditbox");
    assertFalse(htmlNode.contains("<body>") || htmlNode.contains("</body>"));
    htmlNode = itemXml.getNode("item/controls/html/htmleditboxondemand");
    assertFalse(htmlNode.contains("<body>") || htmlNode.contains("</body>"));
    soap.logout();
  }
}
