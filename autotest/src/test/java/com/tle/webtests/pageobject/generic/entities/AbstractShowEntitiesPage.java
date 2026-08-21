package com.tle.webtests.pageobject.generic.entities;

import com.tle.webtests.framework.Assert;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.util.List;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractShowEntitiesPage<THIS extends AbstractShowEntitiesPage<THIS>>
    extends AbstractPage<THIS> {
  protected WebElement getAddLink() {
    return findWithId(getSectionId(), "_add");
  }

  @FindBy(id = "entities")
  private WebElement tableElem;

  protected AbstractShowEntitiesPage(PageContext context) {
    super(context);
    loadedBy = getLoadedBy();
  }

  protected abstract String getSectionId();

  protected abstract String getH2Title();

  protected abstract String getEmptyText();

  protected By getLoadedBy() {
    return By.xpath("id('entities')/h2[normalize-space(text())=" + quoteXPath(getH2Title()) + "]");
  }

  public int getEntityCount() {
    List<WebElement> elements = tableElem.findElements(By.xpath(".//tr"));
    if (elements.size() == 1) {
      try {
        elements
            .get(0)
            .findElement(
                By.xpath("./td[normalize-space(text())=" + quoteXPath(getEmptyText()) + "]"));
        return 0;
      } catch (NoSuchElementException nse) {
        return 1;
      }
    }
    return elements.size();
  }

  public boolean entityExists(PrefixedName name) {
    return entityExists(name.toString());
  }

  protected boolean entityExists(String name) {
    return isPresent(tableElem, getXpath(name));
  }

  public boolean actionExists(PrefixedName entityName, String action) {
    return isPresent(getEntityRow(entityName.toString()), getActionXPath(action));
  }

  private WebElement getEntityRow(String entityName) {
    return tableElem.findElement(getXpath(entityName));
  }

  public THIS deleteEntity(PrefixedName name) {
    return deleteEntity(name, null);
  }

  public void deleteEntityFail(PrefixedName name, String alertText) {
    WebElement row = getEntityRow(name.toString());
    WebElement element = row.findElement(getActionXPath("Delete"));
    element.click();
    if (context.getTestConfig().isAlertSupported()) {
      Alert alert = waiter.until(ExpectedConditions.alertIsPresent());
      if (alertText != null) {
        Assert.assertEquals(alert.getText(), alertText);
      }
      alert.dismiss();
    }
  }

  public THIS deleteEntity(PrefixedName name, String alertText) {
    return deleteEntityRow(getEntityRow(name.toString()), alertText);
  }

  private THIS deleteEntityRow(WebElement row, String alertText) {
    WebElement element = row.findElement(getActionXPath("Delete"));
    WaitingPageObject<THIS> deleteWaiter = removalWaiter(row);
    element.click();
    if (context.getTestConfig().isAlertSupported()) {
      Alert alert = waiter.until(ExpectedConditions.alertIsPresent());
      if (alertText != null) {
        Assert.assertEquals(alert.getText(), alertText);
      }
      alert.accept();
    }
    return deleteWaiter.get();
  }

  protected <S extends AbstractShowEntitiesPage<S>, T extends AbstractEditEntityPage<T, S>>
      T createEntity(T editor) {
    getAddLink().click();
    editor.setCreating(true);
    return editor.get();
  }

  protected <S extends AbstractShowEntitiesPage<S>, T extends AbstractEditEntityPage<T, S>>
      T editEntity(T editor, PrefixedName entityName) {
    return editEntity(editor, entityName.toString());
  }

  /**
   * Don't use this in general. Sometimes it's needed though
   *
   * @param editor
   * @param entityName
   * @return
   */
  protected <S extends AbstractShowEntitiesPage<S>, T extends AbstractEditEntityPage<T, S>>
      T editEntity(T editor, String entityName) {
    WebElement row = getEntityRow(entityName);
    WebElement element = row.findElement(getActionXPath("Edit"));
    element.click();
    return editor.get();
  }

  protected <S extends AbstractShowEntitiesPage<S>, T extends AbstractEditEntityPage<T, S>>
      T cloneEntity(T editor, PrefixedName entityName) {
    WebElement row = getEntityRow(entityName.toString());
    WebElement element = row.findElement(getActionXPath("Clone"));
    editor.setCreating(true);
    element.click();
    return editor.get();
  }

  protected THIS clickActionUpdate(String name, String action) {
    WebElement row = getEntityRow(name);
    WaitingPageObject<THIS> waiter = updateWaiter(row);
    row.findElement(getActionXPath(action)).click();
    return waiter.get();
  }

  protected By getActionXPath(String action) {
    return By.xpath("td[@class='actions']/a[text()=" + quoteXPath(action) + "]");
  }

  protected By getXpath(String name) {
    return By.xpath(
        ".//tr[td[@class='name' and normalize-space(text())=" + quoteXPath(name) + "]]");
  }

  protected By getDisabledXpath(String name) {
    return By.xpath(
        ".//tr[td[@class='name' and normalize-space(text())="
            + quoteXPath(name)
            + "] and td[@class='actions']/a[text()='Enable']]");
  }

  public boolean isEntityDisabled(PrefixedName name) {
    return actionExists(name, "Enable");
  }

  public boolean isDeletable(PrefixedName name) {
    return actionExists(name, "Delete");
  }

  public THIS disableEntity(PrefixedName name) {
    return clickActionUpdate(name.toString(), "Disable");
  }

  public THIS enableEntity(PrefixedName name) {
    return clickActionUpdate(name.toString(), "Enable");
  }

  public void deleteAllNamed(List<PrefixedName> names) {
    deleteAllNamed(names.toArray(new PrefixedName[names.size()]));
  }

  public void deleteAllNamed(PrefixedName... names) {
    for (PrefixedName name : names) {
      while (entityExists(name)) {
        if (!isEntityDisabled(name)) {
          disableEntity(name);
        }
        deleteEntity(name);
      }
    }
  }
}
