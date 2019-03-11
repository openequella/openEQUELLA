package com.tle.webtests.remotetest.integration;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.searching.ManageExternalResourcePage;
import com.tle.webtests.pageobject.viewitem.FindUsesPage;
import com.tle.webtests.pageobject.viewitem.ItemPage;
import com.tle.webtests.pageobject.viewitem.LMSExportPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.test.files.Attachments;

public class ConnectorHelper {
  protected static final String COLLECTION = "Navigation and Attachments";
  public static final String PACKAGE_NAME = "Zou ba! Visiting China: Is this your first visit?";
  private final PrefixedName connectorName;
  private final PageContext context;

  public ConnectorHelper(PageContext context, PrefixedName connectorName) {
    this.context = context;
    this.connectorName = connectorName;
  }

  protected PrefixedName getConnectorName() {
    return connectorName;
  }

  public LMSExportPage selectConnector(LMSExportPage lmsExportPage) {
    return selectConnector(lmsExportPage, getConnectorName());
  }

  public LMSExportPage selectConnector(LMSExportPage lmsExportPage, PrefixedName name) {
    if (!lmsExportPage.singleConnector()) {
      return lmsExportPage.selectConnector(name);
    }
    return lmsExportPage;
  }

  public LMSExportPage selectConnectorError(LMSExportPage lmsExportPage, PrefixedName name) {
    if (!lmsExportPage.singleConnector()) {
      return lmsExportPage.selectConnectorError(name);
    }
    return lmsExportPage;
  }

  public FindUsesPage selectConnector(FindUsesPage findUsesPage) {
    return selectConnector(findUsesPage, getConnectorName());
  }

  protected FindUsesPage selectConnector(FindUsesPage findUsesPage, PrefixedName name) {
    if (!findUsesPage.singleConnector()) {
      return findUsesPage.selectConnector(name);
    }
    return findUsesPage;
  }

  public ManageExternalResourcePage selectConnector(ManageExternalResourcePage external) {
    return selectConnector(external, getConnectorName());
  }

  protected ManageExternalResourcePage selectConnector(
      ManageExternalResourcePage external, PrefixedName name) {
    if (!external.singleConnector()) {
      return external.selectConnector(name);
    }
    return external;
  }

  public SummaryPage createTestItem(String fullName) {
    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
    wizard.editbox(1, fullName);

    wizard.addFile(2, "page.html");
    wizard.addFile(2, "veronicas_wall1.jpg");
    UniversalControl control = wizard.universalControl(2);
    control
        .addResource(new FileUniversalControlType(control))
        .uploadPackageOption(Attachments.get("package.zip"))
        .showStructure()
        .save();

    return wizard.save().publish();
  }

  public LMSExportPage addToCourse(ItemPage<?> item, String course, String section) {
    LMSExportPage lms = item.lmsPage();
    selectConnector(lms);
    lms.showArchived(true);
    lms.clickCourse(course).clickSection(section);
    lms.selectSummary();
    return lms.publish();
  }
}
