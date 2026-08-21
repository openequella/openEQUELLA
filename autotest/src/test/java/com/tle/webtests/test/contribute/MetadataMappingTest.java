package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.ItemXmlPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;
import org.testng.annotations.Test;

@TestInstitution("contribute")
public class MetadataMappingTest extends AbstractCleanupTest {
  private static final String COLLECTION = "Metadata Mapping";
  private static String USERNAME = "AutoTest";
  private static String PASSWORD = "automated";

  public MetadataMappingTest() {
    setDeleteCredentials(USERNAME, PASSWORD);
  }

  @Test
  public void imsMapping() {
    final String text = "Default text for each field";
    final String title = "Zou ba! Visiting China: Is this your first visit?";
    logon(USERNAME, PASSWORD);
    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
    String fullName = context.getFullName("mapping");
    wizard.editbox(1, fullName);
    for (int i = 2; i < 10; i++) {
      wizard.editbox(i, text);
    }
    wizard.shuffleList(10).add(text);
    wizard.shuffleList(12).add(text);

    wizard = wizard.next();
    UniversalControl control = wizard.universalControl(1);
    control
        .addDefaultResource(new FileUniversalControlType(control))
        .uploadPackageOption(Attachments.get("package.zip"))
        .showStructure()
        .save();
    wizard.addSingleFile(1, Attachments.get("meta.html"));

    ItemXmlPage x = wizard.save().publish().itemXml();

    assertNode(x, "Simple", text + " " + title);
    assertNode(x, "SimpleReplace", title);

    assertNode(x, "Compound/version/langstring", text);
    assertNode(x, "Compound/version/langstring", "1.0");
    assertNode(x, "CompoundReplace/version/langstring", "1.0");
    assertNoNode(x, "CompoundReplace/version/langstring", text);

    assertNode(x, "Attribute/entry/langstring", "lang", text);
    assertNode(x, "Attribute/entry/langstring", "lang", "x-none");
    assertNode(x, "AttributeReplace/entry/langstring", "lang", "x-none");
    assertNoNode(x, "AttributeReplace/entry/langstring", "lang", text);

    assertNode(x, "Repeating", text);
    assertNode(x, "Repeating", "Vocabulary");
    assertNode(x, "Repeating", "Travel");

    assertNoNode(x, "RepeatingReplace", text);
    assertNode(x, "RepeatingReplace", "Vocabulary");
    assertNode(x, "RepeatingReplace", "Travel");

    assertNode(x, "HTML", text);
    assertNode(x, "Literal", "Fixed value");
    assertNoNode(x, "Literal", text);
  }

  private void assertNode(ItemXmlPage itemXml, String node, String attribute, String value) {
    assertTrue(itemXml.nodeHasValue("/item/" + node, attribute, value));
  }

  private void assertNode(ItemXmlPage itemXml, String node, String value) {
    assertNode(itemXml, node, "", value);
  }

  private void assertNoNode(ItemXmlPage itemXml, String node, String attribute, String value) {
    assertFalse(itemXml.nodeHasValue("/item/" + node, attribute, value));
  }

  private void assertNoNode(ItemXmlPage itemXml, String node, String value) {
    assertNoNode(itemXml, node, "", value);
  }
}
