package com.tle.webtests.pageobject.wizard;

import com.tle.common.PathUtils;
import com.tle.webtests.framework.EBy;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.generic.component.MultiLingualEditbox;
import com.tle.webtests.pageobject.generic.component.ShuffleBox;
import com.tle.webtests.pageobject.wizard.controls.AutoCompleteTermControl;
import com.tle.webtests.pageobject.wizard.controls.CalendarControl;
import com.tle.webtests.pageobject.wizard.controls.EditBoxControl;
import com.tle.webtests.pageobject.wizard.controls.EmailSelectorControl;
import com.tle.webtests.pageobject.wizard.controls.GroupControl;
import com.tle.webtests.pageobject.wizard.controls.HTMLEditBoxControl;
import com.tle.webtests.pageobject.wizard.controls.MultiEditboxControl;
import com.tle.webtests.pageobject.wizard.controls.NavigationBuilder;
import com.tle.webtests.pageobject.wizard.controls.PopupTermControl;
import com.tle.webtests.pageobject.wizard.controls.RepeaterControl;
import com.tle.webtests.pageobject.wizard.controls.SelectGroupControl;
import com.tle.webtests.pageobject.wizard.controls.SelectRoleControl;
import com.tle.webtests.pageobject.wizard.controls.SelectUserControl;
import com.tle.webtests.pageobject.wizard.controls.ShuffleGroupControl;
import com.tle.webtests.pageobject.wizard.controls.ShuffleListControl;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.UrlUniversalControlType;
import com.tle.webtests.test.files.Attachments;
import java.io.File;
import java.net.URL;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractWizardControlPage<T extends AbstractWizardControlPage<T>>
    extends AbstractWizardTab<T> {
  protected int pageNum;

  public AbstractWizardControlPage(PageContext context, By loadedBy, int pageNum) {
    super(context, loadedBy);
    this.pageNum = pageNum;
  }

  public int getPageNum() {
    return pageNum;
  }

  protected int getControlNum(int ctrlNum) {
    return ctrlNum;
  }

  public abstract String getControlId(int ctrlNum);

  public final String subComponentId(int ctrlNum, String subName) {
    return getControlId(ctrlNum) + "_" + subName;
  }

  public void editbox(int ctrlNum, PrefixedName text) {
    editbox(ctrlNum, text.toString());
  }

  public void editbox(int ctrlNum, String text) {
    WebElement field =
        waiter.until(
            ExpectedConditions.elementToBeClickable(By.name("c" + getControlNum(ctrlNum))));
    field.clear();
    field.sendKeys(text);
  }

  public void clickButton(String buttonText) {
    WaitingPageObject<T> waiter = getGeneralWaiter();
    WebElement button = driver.findElement(EBy.buttonText(buttonText));
    button.click();
    waiter.get();
  }

  public WebElement getControl(int ctrlNum) {
    return driver.findElement(By.name("c" + getControlNum(ctrlNum)));
  }

  public void addToShuffleList(int ctrlNum, String value) {
    shuffleList(ctrlNum).add(value);
  }

  public ShuffleListControl shuffleList(int ctrlNum) {
    return new ShuffleListControl(context, ctrlNum, this).get();
  }

  public HTMLEditBoxControl htmlEditBox(int ctrlNum) {
    return htmlEditBox(ctrlNum, false);
  }

  public HTMLEditBoxControl htmlEditBox(int ctrlNum, boolean locked) {
    return new HTMLEditBoxControl(context, ctrlNum, this, locked).get();
  }

  public void selectDropDown(int ctrlNum, String name) {
    getDropDown(ctrlNum).selectByVisibleText(name);
  }

  public T selectDropDownReload(int ctrlNum, String name) {
    WaitingPageObject<T> waiter = getGeneralWaiter();
    getDropDown(ctrlNum).selectByVisibleText(name);
    return waiter.get();
  }

  public UniversalControl universalControl(int ctrlnum) {
    return new UniversalControl(context, ctrlnum, this).get();
  }

  public void addSingleFile(int ctrlNum, String file) {
    addSingleFile(ctrlNum, Attachments.get(file));
  }

  public static String filenameFromURL(URL file) {
    String filePath = getPathFromUrl(file);
    return PathUtils.getFilenameFromFilepath(filePath);
  }

  public void addSingleFile(int ctrlNum, URL file) {
    String nameOnly = filenameFromURL(file);
    addSingleFile(ctrlNum, file, nameOnly);
  }

  public void addSingleFile(int ctrlNum, URL file, String filename) {
    UniversalControl universalControl = universalControl(ctrlNum);
    FileUniversalControlType fileControl =
        universalControl.addDefaultResource(new FileUniversalControlType(universalControl));
    fileControl.uploadFile(file, filename);
  }

  public void addFile(int ctrlNum, String file) {
    addFile(ctrlNum, Attachments.get(file));
  }

  public void addFile(int ctrlNum, URL file) {
    addFile(ctrlNum, file, null);
  }

  public void addFile(int ctrlNum, URL file, Boolean preview) {
    UniversalControl universalControl = universalControl(ctrlNum);
    FileUniversalControlType fileControl =
        universalControl.addResource(new FileUniversalControlType(universalControl));

    fileControl.uploadFile(file);
    if (preview != null) {
      throw new Error("This needs to be re-written, doesn't edit the file anymore");
    }
  }

  public void addFile(int ctrlNum, File file) {
    UniversalControl universalControl = universalControl(ctrlNum);
    FileUniversalControlType fileControl =
        universalControl.addResource(new FileUniversalControlType(universalControl));
    fileControl.uploadFile(file);
  }

  public void addFiles(int ctrlNum, boolean singleType, String... files) {
    URL[] urls = new URL[files.length];
    for (int i = 0; i < files.length; i++) {
      urls[i] = Attachments.get(files[i]);
    }
    addFiles(ctrlNum, singleType, urls);
  }

  public void addFiles(int ctrlNum, boolean singleType, URL... files) {
    UniversalControl uni = universalControl(ctrlNum);
    FileUniversalControlType fileControl;
    if (!singleType) {
      fileControl = uni.addResource(new FileUniversalControlType(uni));
    } else {
      fileControl = uni.addDefaultResource(new FileUniversalControlType(uni));
    }

    String firstFilename = fileControl.uploadMultiple(files);
    waiter.until(uni.getResourceExpectation(firstFilename));
  }

  public void addUrl(int ctrlNum, String url) {
    addUrl(ctrlNum, url, "");
  }

  public void addUrl(int ctrlNum, String url, String displayName) {
    addUrl(ctrlNum, url, displayName, null);
  }

  public void addUrl(int ctrlNum, String url, String displayName, Boolean preview) {
    UniversalControl universalControl = universalControl(ctrlNum);
    UrlUniversalControlType urlControl =
        universalControl.addResource(new UrlUniversalControlType(universalControl));
    urlControl.addUrl(url, displayName, preview);
  }

  public boolean setCheck(int ctrlnum, String value, boolean checked) {
    WebElement check =
        driver.findElement(
            By.xpath(
                "//input[@name="
                    + quoteXPath("c" + getControlNum(ctrlnum))
                    + " and @value = "
                    + quoteXPath(value)
                    + "]"));
    if (check.isSelected() != checked) {
      check.click();
      return true;
    }
    return false;
  }

  public T setCheckReload(int ctrlnum, String value, boolean checked) {
    return setCheckWaiter(ctrlnum, value, checked, getGeneralWaiter());
  }

  protected T setCheckWaiter(
      int ctrlnum, String value, boolean checked, WaitingPageObject<T> waiter) {
    if (setCheck(ctrlnum, value, checked)) {
      return waiter.get();
    }
    return actualPage();
  }

  public T setCheckAppear(int ctrlnum, String value, boolean checked, int appearCtrl) {
    return setCheckWaiter(ctrlnum, value, checked, getAppearWaiter(appearCtrl));
  }

  public T setCheckDisappear(int ctrlnum, String value, boolean checked, int disappearCtrl) {
    return setCheckWaiter(ctrlnum, value, checked, getDisappearWaiter(disappearCtrl));
  }

  public WaitingPageObject<T> getDisappearWaiter(int ctrlnum) {
    By by = By.id(getControlId(ctrlnum));
    return ExpectWaiter.waiter(ExpectedConditions.invisibilityOfElementLocated(by), this);
  }

  public WaitingPageObject<T> getAppearWaiter(int ctrlnum) {
    By by = By.id(getControlId(ctrlnum));
    return ExpectWaiter.waiter(ExpectedConditions.visibilityOfElementLocated(by), this);
  }

  public WaitingPageObject<T> getGeneralWaiter() {
    throw new UnsupportedOperationException();
  }

  public WaitingPageObject<T> getUpdateWaiter(int ctrlnum) {
    By by = By.id(getControlId(ctrlnum));
    WebElement existingControl = driver.findElement(by);
    return ExpectWaiter.waiter(
        ExpectedConditions2.updateOfElementLocated(existingControl, driver, by), this);
  }

  public ExpectedCondition<WebElement> getNewAttachmentExpectation(String item) {
    return ExpectedConditions.visibilityOfElementLocated(
        By.xpath(
            "//div[contains(@class, 'universalresources')]/div/ul/div/.//a[text()="
                + quoteXPath(item)
                + "]"));
  }

  public void waitForSelectedItem(String item) {
    waiter.until(getNewAttachmentExpectation(item));
  }

  /** Move values from left to right */
  public void selectShuffle(int ctrlnum, String name) {
    shuffleBox(ctrlnum).moveRightByText(name);
  }

  public ShuffleBox shuffleBox(int ctrlnum) {
    return new ShuffleBox(context, "c" + getControlNum(ctrlnum)).get();
  }

  public SelectUserControl selectUser(int ctrlNum) {
    return new SelectUserControl(context, ctrlNum, this).get();
  }

  public SelectGroupControl selectGroup(int ctrlNum) {
    return new SelectGroupControl(context, ctrlNum, this).get();
  }

  public SelectRoleControl selectRole(int ctrlNum) {
    return new SelectRoleControl(context, ctrlNum, this).get();
  }

  private EquellaSelect getDropDown(int ctrlNum) {
    return new EquellaSelect(context, driver.findElement(By.name("c" + getControlNum(ctrlNum))));
  }

  public String getSelectedValueDropDown(int ctrlNum) {
    return getDropDown(ctrlNum).getSelectedValue();
  }

  public CalendarControl calendar(int ctrlNum) {
    return new CalendarControl(this, ctrlNum).get();
  }

  public EmailSelectorControl emailSelector(int ctrlNum) {
    return new EmailSelectorControl(context, getControlNum(ctrlNum), this).get();
  }

  public AutoCompleteTermControl autoTermControl(int ctrlNum) {
    return new AutoCompleteTermControl(context, getControlNum(ctrlNum), this).get();
  }

  public PopupTermControl popupTermControl(int ctrlNum) {
    return new PopupTermControl(context, getControlNum(ctrlNum), this).get();
  }

  public NavigationBuilder navigation() {
    return new NavigationBuilder(context, this).get();
  }

  public boolean isEditboxDisabled(int ctrlNum) {
    WebElement field = driver.findElement(By.name("c" + getControlNum(ctrlNum)));
    return !field.isEnabled();
  }

  public boolean isDropDownDisabled(int ctrlNum) {
    return getDropDown(ctrlNum).isDisabled();
  }

  public GroupControl group(int ctrlNum, int treeOffset) {
    return new GroupControl(context, this, getControlNum(ctrlNum), treeOffset);
  }

  public ShuffleGroupControl shuffleGroup(int ctrlNum, int treenum) {
    return new ShuffleGroupControl(context, ctrlNum, this, treenum).get();
  }

  public MultiLingualEditbox multiEditbox(int ctrlNum) {
    return new MultiEditboxControl(context, ctrlNum, this).get().getEditbox();
  }

  public EditBoxControl editbox(int ctrlNum) {
    return new EditBoxControl(context, ctrlNum, this).get();
  }

  public WebElement textarea(int ctrlNum) {
    return driver.findElement(By.name("c" + getControlNum(ctrlNum)));
  }

  public RepeaterControl repeater(int ctrlNum) {
    return new RepeaterControl(context, ctrlNum, this).get();
  }

  public String getErrorMessage(int ctrlNum) {
    WebElement errorMessage =
        driver.findElement(
            By.xpath(
                "id("
                    + quoteXPath(getControlId(ctrlNum))
                    + ")/div/p[@class='ctrlinvalidmessage']"));
    waiter.until(ExpectedConditions.visibilityOf(errorMessage));
    return errorMessage.getText().trim();
  }
}
