package com.tle.webtests.pageobject.wizard;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.component.ShuffleBox;
import com.tle.webtests.pageobject.wizard.controls.CalendarControl;
import com.tle.webtests.pageobject.wizard.controls.EditBoxControl;
import com.tle.webtests.pageobject.wizard.controls.GroupControl;
import java.util.Date;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class DRMAccessWizardPage extends AbstractPage<DRMAccessWizardPage> {
  private final WizardPageTab page;
  private final int usersAndGroups;
  private final int networks;
  private final int licenseCountSelect;
  private final int licenseCountTree;
  private final int dateRangeSelect;
  private final int dateRangeTree;
  private final int eduSelect;
  private final int attribSelect;
  private final int termSelect;
  private final int termTree;

  public DRMAccessWizardPage(PageContext context, WizardPageTab page) {
    super(context);
    this.page = page;
    int offset = 1;
    int treeOffset = 2;
    usersAndGroups = offset++;
    networks = offset++;
    licenseCountSelect = offset++;
    licenseCountTree = treeOffset++;
    offset++;
    dateRangeSelect = offset++;
    dateRangeTree = treeOffset++;
    offset++;
    eduSelect = offset++;
    attribSelect = offset++;
    termSelect = offset++;
    termTree = treeOffset++;
    offset++;
  }

  @Override
  public void checkLoaded() throws Error {
    page.checkLoaded();
  }

  public void selectUserId(String user) {
    ShuffleBox shuffle = page.shuffleBox(usersAndGroups);
    shuffle.moveRightByValue("u" + user);
  }

  public void selectNetwork(String networkName) {
    ShuffleBox shuffle = page.shuffleBox(networks);
    shuffle.moveRightByText(networkName);
  }

  public void setLicenseCount(int count) {
    enableLicenseCount(true).getGroupItem(0, 0).editbox(1, Integer.toString(count));
  }

  public GroupControl enableLicenseCount(boolean enable) {
    GroupControl group = page.group(licenseCountSelect, licenseCountTree);
    group.setGroupEnabled("true", enable);
    return group;
  }

  public boolean isLicenseCountEnabled() {
    GroupControl group = page.group(licenseCountSelect, licenseCountTree);
    EditBoxControl count = group.getGroupItem(0, 0).editbox(1);
    return group.isEnabled() && count.isEnabled();
  }

  public void setDateRange(Date start, Date end) {
    CalendarControl range = enableDateRange(true).getGroupItem(0, 0).calendar(1);
    range.setDateRange(start, end);
  }

  public GroupControl enableDateRange(boolean enable) {
    GroupControl group = page.group(dateRangeSelect, dateRangeTree);
    group.setGroupEnabled("true", enable);
    return group;
  }

  public boolean isDateRangeEnabled() {
    GroupControl group = page.group(dateRangeSelect, dateRangeTree);
    CalendarControl range = group.getGroupItem(0, 0).calendar(1);
    return group.isEnabled() && !range.isRangeDisabled();
  }

  public void setEducationSector(boolean yes) {
    page.setCheck(eduSelect, "sectors:educational", yes);
  }

  public void setRequireAttribution(boolean yes) {
    page.setCheck(attribSelect, "true", yes);
  }

  public GroupControl enableRequireTermsAcceptance(boolean enable) {
    GroupControl group = page.group(termSelect, termTree);
    group.setGroupEnabled("true", enable);
    return group;
  }

  public boolean isRequireTermsAcceptanceEnabled() {
    GroupControl group = page.group(termSelect, termTree);
    WebElement terms = group.getGroupItem(0, 0).textarea(1);
    return group.isEnabled() && terms.isEnabled();
  }

  public void setAcceptanceTerms(String terms) {
    GroupControl group = enableRequireTermsAcceptance(true);
    boolean empty = Check.isEmpty(terms);

    if (!empty) {
      group.getGroupItem(0, 0).editbox(1, terms);
    }
  }

  public boolean hasError(String error) {
    return isPresent(
        By.xpath("//p[@class='ctrlinvalidmessage' and text()=" + quoteXPath(error) + "]"));
  }
}
