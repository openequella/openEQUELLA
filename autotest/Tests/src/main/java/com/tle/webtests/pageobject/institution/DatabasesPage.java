package com.tle.webtests.pageobject.institution;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DatabasesPage extends InstitutionTab<DatabasesPage> {
  @FindBy(id = "isdt_add")
  private WebElement addButton;

  @FindBy(id = "isdt_migrateSelectedButton")
  private WebElement migrateSelectedButton;

  @FindBy(id = "isdt_table")
  private WebElement tableElement;

  public DatabasesPage(PageContext context) {
    super(context, "Databases", "Databases");
  }

  @Override
  protected WebElement findLoadedElement() {
    return tableElement;
  }

  @Override
  protected void loadUrl() {
    get("institutions.do", "is.admin", "true", "istabs.tab", "isdt");
  }

  public boolean containsDatabase(String databaseName) {
    return isPresent(getRowSelector(databaseName));
  }

  private By getRowSelector(String databaseName) {
    return By.xpath(".//tr[./td[1]/span[text()=" + quoteXPath(databaseName) + "]]");
  }

  public DatabaseEditDialog addSchema() {
    addButton.click();
    return new DatabaseEditDialog(this).get();
  }

  public DatabaseRow getDatabaseRow(String name) {
    return new DatabaseRow(context, tableElement.findElement(getRowSelector(name)));
  }

  public void migrateAll() {
    migrateSelectedButton.click();
    acceptConfirmation();
    get();
  }
}
