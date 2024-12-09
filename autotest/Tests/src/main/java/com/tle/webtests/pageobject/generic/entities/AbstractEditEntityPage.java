package com.tle.webtests.pageobject.generic.entities;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.component.MultiLingualEditbox;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public abstract class AbstractEditEntityPage<
        THIS extends AbstractEditEntityPage<THIS, SHOWLISTPAGE>,
        SHOWLISTPAGE extends AbstractShowEntitiesPage<SHOWLISTPAGE>>
    extends AbstractPage<THIS> {
  protected WebElement getNameField() {
    return findWithId(getEditorSectionId(), "_t");
  }

  private WebElement getDescriptionField() {
    return findWithId(getEditorSectionId(), "_d");
  }

  protected By getSaveButtonBy() {
    return byPrefixId(getContributeSectionId(), "_sv");
  }

  protected WebElement getSaveButton() {
    return driver.findElement(getSaveButtonBy());
  }

  protected WebElement getCancelButton() {
    return findWithId(getContributeSectionId(), "_cl");
  }

  private SHOWLISTPAGE listPage;
  private boolean creating;

  protected AbstractEditEntityPage(SHOWLISTPAGE listPage) {
    super(listPage.getContext());
    this.listPage = listPage;
    setupLoadedBy();
  }

  private void setupLoadedBy() {
    final String entityName = getEntityName();
    loadedBy = By.xpath("//h2[text()='" + getTitle(creating) + entityName + "']");
  }

  protected String getTitle(boolean create) {
    return (create ? "Create new " : "Edit ");
  }

  @SuppressWarnings("unchecked")
  public THIS setCreating(boolean creating) {
    this.creating = creating;
    setupLoadedBy();
    return (THIS) this;
  }

  protected abstract String getEntityName();

  protected abstract String getContributeSectionId();

  protected abstract String getEditorSectionId();

  protected String invalidMessage(WebElement elem) {
    try {
      WebElement invalid =
          elem.findElement(By.xpath("..")).findElement(By.cssSelector(".ctrlinvalidmessage"));
      return invalid.getText();
    } catch (NoSuchElementException nse) {
      return null;
    }
  }

  protected boolean isInvalid(WebElement elem) {
    return invalidMessage(elem) != null;
  }

  // Public

  // TODO: use the WebElement
  public boolean isNameInvalid() {
    try {
      driver.findElement(
          By.xpath("//div[@id = '" + getEditorSectionId() + "_t']/following-sibling::p"));
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  public SHOWLISTPAGE save() {
    getSaveButton().click();
    return listPage.get();
  }

  public THIS saveWithErrors() {
    getSaveButton().click();
    return visibilityWaiter(driver, By.className("ctrlinvalidmessage")).get();
  }

  public SHOWLISTPAGE cancel() {
    getCancelButton().click();
    return listPage.get();
  }

  public THIS setName(PrefixedName name) {
    return setName(name == null ? "" : name.toString());
  }

  /**
   * Avoid using this directly
   *
   * @param name
   * @return
   */
  @SuppressWarnings("unchecked")
  protected THIS setName(String name) {
    MultiLingualEditbox nameBox = new MultiLingualEditbox(context, getNameField());
    if (name == null) {
      nameBox.setCurrentString("");
    } else {
      nameBox.setCurrentString(name);
    }
    return (THIS) this;
  }

  public String getName() {
    return new MultiLingualEditbox(context, getNameField()).getCurrentString();
  }

  public THIS setDescription(PrefixedName description) {
    return setDescription(description.toString());
  }

  @SuppressWarnings("unchecked")
  public THIS setDescription(String description) {

    MultiLingualEditbox descBox = new MultiLingualEditbox(context, getDescriptionField(), true);
    descBox.setCurrentString(description);
    return (THIS) this;
  }

  public String getDescription() {

    return new MultiLingualEditbox(context, getDescriptionField(), true).getCurrentString();
  }

  protected SHOWLISTPAGE getShowListPage() {
    return listPage;
  }
}
