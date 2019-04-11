package com.tle.webtests.pageobject.remoterepo.z3950;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.searching.AbstractQuerySection;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RemoteRepoAdvancedZ3950QuerySection
    extends AbstractQuerySection<RemoteRepoAdvancedZ3950QuerySection> {
  @FindBy(id = "zq_use1")
  private WebElement schemaSelect1;

  @FindBy(id = "searchform-search")
  private WebElement searchButton;

  @FindBy(id = "zq_op2")
  private WebElement operatorSelect1;

  @FindBy(id = "zq_use2")
  private WebElement schemaSelect2;

  @FindBy(id = "zq_term2")
  private WebElement queryField2;

  @FindBy(id = "zq_op3")
  private WebElement operatorSelect2;

  @FindBy(id = "zq_use3")
  private WebElement schemaSelect3;

  @FindBy(id = "zq_term3")
  private WebElement queryField3;

  private EquellaSelect operatorField1;
  private EquellaSelect operatorField2;

  private EquellaSelect schemaField1;
  private EquellaSelect schemaField2;
  private EquellaSelect schemaField3;

  public RemoteRepoAdvancedZ3950QuerySection(PageContext context) {
    super(context);
  }

  @Override
  public void checkLoaded() throws Error {
    super.checkLoaded();

    operatorField1 = new EquellaSelect(context, operatorSelect1);
    operatorField2 = new EquellaSelect(context, operatorSelect2);
    schemaField1 = new EquellaSelect(context, schemaSelect1);
    schemaField2 = new EquellaSelect(context, schemaSelect2);
    schemaField3 = new EquellaSelect(context, schemaSelect3);
  }

  public void setAvancedQuery(
      String s1, String q1, String o1, String s2, String q2, String o2, String s3, String q3) {
    queryField.clear();
    queryField2.clear();
    queryField3.clear();

    schemaField1.selectByVisibleText(s1);
    queryField.sendKeys(q1);

    operatorField1.selectByValue(o1);
    schemaField2.selectByVisibleText(s2);
    queryField2.sendKeys(q2);

    operatorField2.selectByValue(o2);
    schemaField3.selectByVisibleText(s3);
    queryField3.sendKeys(q3);
  }

  public void setOperatorField1(EquellaSelect operatorField1) {
    this.operatorField1 = operatorField1;
  }

  public void setOperatorField2(EquellaSelect operatorField2) {
    this.operatorField2 = operatorField2;
  }

  public void setSchemaField1(EquellaSelect schemaField1) {
    this.schemaField1 = schemaField1;
  }

  public void setSchemaField2(EquellaSelect schemaField2) {
    this.schemaField2 = schemaField2;
  }

  public void setSchemaField3(EquellaSelect schemaField3) {
    this.schemaField3 = schemaField3;
  }

  public WebElement getSearchButton() {
    return searchButton;
  }
}
