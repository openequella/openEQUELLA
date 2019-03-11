package com.tle.webtests.pageobject.integration.canvas.course;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public abstract class AbstractCanvasCoursePage<T extends AbstractCanvasCoursePage<T>>
    extends AbstractCanvasPage<T> {
  // Canvas course pages all share a common side menu
  @FindBy(xpath = "id('section-tabs')/li/a[text()='Settings']")
  private WebElement settingsLink;

  @FindBy(xpath = "id('section-tabs')/li/a[text()='Modules']")
  private WebElement modulesLink;

  @FindBy(xpath = "id('section-tabs')/li/a[text()='Pages']")
  private WebElement wikiLink;

  public AbstractCanvasCoursePage(PageContext context, By loadedBy) {
    super(context, loadedBy);
  }

  public CanvasSettingsPage settings() {
    settingsLink.click();
    return new CanvasSettingsPage(context).get();
  }

  public CanvasModulePage modules() {
    modulesLink.click();
    return new CanvasModulePage(context).get();
  }

  public CanvasWikiPage wiki() {
    wikiLink.click();
    return new CanvasWikiPage(context).get();
  }
}
