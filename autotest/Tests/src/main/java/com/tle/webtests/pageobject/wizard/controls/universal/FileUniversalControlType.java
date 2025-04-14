package com.tle.webtests.pageobject.wizard.controls.universal;

import com.google.common.collect.Sets;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import com.tle.webtests.framework.EBy;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class FileUniversalControlType extends AbstractAttachmentDialogPage<FileUniversalControlType>
    implements AttachmentType<FileUniversalControlType, FileAttachmentEditPage> {
  private WebElement getFileUpload() {
    return byWizId("_dialog_fuh_fileUpload");
  }

  private WebElement getMainDiv() {
    return driver.findElement(
        By.xpath("id('" + getWizid() + "_dialog')//div[contains(@class,'fileHandler')]"));
  }

  @FindBy(xpath = "id('uploads')/div[contains(@class, 'uploadsprogress')]")
  private WebElement uploadsDiv;

  private WebElement getAddScrap() {
    return driver.findElement(By.id(getWizid() + "_dialog_fuh_filesFromScrapbookLink"));
  }

  public FileUniversalControlType(UniversalControl universalControl) {
    super(universalControl);
  }

  @Override
  protected WebElement findLoadedElement() {
    return getMainDiv();
  }

  @Override
  public String getType() {
    return "Upload a file";
  }

  public String uploadFile(URL url) {
    String filePath = getPathFromUrl(url);
    String nameOnly = PathUtils.getFilenameFromFilepath(filePath);
    upload(filePath, BUTTON_ADD, control.attachNameWaiter(nameOnly, false));
    return nameOnly;
  }

  public void uploadFile(URL url, String name) {
    String filePath = getPathFromUrl(url);
    upload(filePath, BUTTON_ADD, control.attachNameWaiter(name, false));
  }

  public String uploadMultiple(URL[] urls) {
    String[] expectedFilenames = new String[urls.length];
    Set<String> filenameSet = Sets.newHashSet();
    int i = 0;
    for (URL file : urls) {
      expectedFilenames[i] =
          uniqueFilename(filenameSet, PathUtils.getFilenameFromFilepath(file.getPath()));
      i++;
    }
    uploadMultiple(urls, expectedFilenames);
    return expectedFilenames[0];
  }

  private String uniqueFilename(Set<String> lowerFilenames, String filename) {
    String lowerCaseFilename = filename.toLowerCase();
    if (!lowerFilenames.contains(lowerCaseFilename)) {
      lowerFilenames.add(lowerCaseFilename);
      return filename;
    }

    final Pair<String, String> nameParts = PathUtils.fileParts(filename);
    final String name = nameParts.getFirst();
    final String extension = nameParts.getSecond();
    int upto = 2;
    while (true) {
      final String uniqueName =
          name + '(' + upto + ')' + (Check.isEmpty(extension) ? "" : '.' + extension);
      String uniqueLower = uniqueName.toLowerCase();
      if (!lowerFilenames.contains(uniqueLower)) {
        lowerFilenames.add(uniqueLower);
        return uniqueName;
      }
      upto++;
    }
  }

  public void uploadMultiple(URL[] urls, String[] expectedFilenames) {
    int i = 0;
    for (URL url : urls) {
      uploadMultiple(getPathFromUrl(url), expectedFilenames[i]);
      i++;
    }
    WebElement addButton = waitForElement(getMainDiv(), EBy.buttonText("Add"));
    ExpectedCondition<Boolean> disappears = removalCondition(addButton);
    addButton.click();
    waiter.until(disappears);
  }

  public static URL fileToURL(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public String uploadFile(File file) {
    return uploadFile(fileToURL(file));
  }

  private void uploadMultiple(String filename, String expectedFilename) {
    getFileUpload().sendKeys(filename);
    WebElement uploadRow =
        waitForElement(
            uploadsDiv, By.xpath("ul/div[.//div[text() = " + quoteXPath(expectedFilename) + "]]"));
    waitForElement(
        uploadRow,
        new ByChained(
            By.xpath(
                "div/div/div[contains(@class, 'actions')]/div/button[contains(@title, 'Delete"
                    + " file')]")));
  }

  public FileUniversalControlType uploadError(URL file, String errorMessage) {
    ExpectedCondition<?> errorExpectation = getErrorExpectation(errorMessage);
    getFileUpload().sendKeys(getPathFromUrl(file));
    return ExpectWaiter.waiter(errorExpectation, this).get();
  }

  private ExpectedCondition<?> getErrorExpectation(String msg) {
    String xpath =
        "id('"
            + getWizid()
            + "_dialog')//div[text() = "
            + quoteXPath(msg)
            + " and @role = 'alert']";
    return ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath));
  }

  public FileAttachmentEditPage uploadZip(URL fileUrl) {
    String zipFile = uploadFile(fileUrl);
    FileAttachmentEditPage fed = control.editResource(fileEditor(), zipFile);
    return fed.unzip().selectAll();
  }

  public <T extends PageObject> T uploadPackage(URL url, WaitingPageObject<T> backTo) {
    return upload(url, BUTTON_ADD, backTo);
  }

  public <T extends PageObject> T uploadPackageReplace(URL url, WaitingPageObject<T> backTo) {
    return upload(url, BUTTON_REPLACE, backTo);
  }

  public PackageAttachmentEditPage uploadPackageOption(URL url) {
    PackageOptionsPage options = upload(url, BUTTON_NEXT, new PackageOptionsPage(this));
    return options.treatAsPackage();
  }

  public PackageAttachmentEditPage uploadQTITestOption(URL url) {
    PackageOptionsPage options = upload(url, BUTTON_NEXT, new PackageOptionsPage(this));
    return options.treatAsQTIQuiz();
  }

  public PackageAttachmentEditPage uploadMETSOption(URL url) {
    PackageOptionsPage options = upload(url, BUTTON_NEXT, new PackageOptionsPage(this));
    return options.treatAsMETS();
  }

  public <T extends PageObject> T upload(
      URL fileUrl, String nextButton, WaitingPageObject<T> nextPage) {
    return upload(getPathFromUrl(fileUrl), nextButton, nextPage);
  }

  public <T extends PageObject> T upload(
      String filename, String nextButton, WaitingPageObject<T> nextPage) {
    ExpectedCondition<?> updateButtons = ajaxUpdateCondition(By.xpath(getButtonbar()));
    waitForHiddenElement(getFileUpload());
    getFileUpload().sendKeys(filename);
    waiter.until(updateButtons);
    driver.findElement(buttonBy(nextButton)).click();
    return nextPage.get();
  }

  public void importFromScrapbook(String description) {
    WaitingPageObject<UniversalControl> waiter = control.attachNameWaiter(description, false);
    getAddScrap().click();

    SelectionSession selectionSession =
        ExpectWaiter.waiter(
                ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                    wizIdIdXPath("_dialog", "/div/iframe")),
                new SelectionSession(context))
            .get();
    new MyResourcesPage(context, "scrapbook")
        .results()
        .getResultForTitle(description, 1)
        .clickAction("Select", selectionSession);
    selectionSession.finishedSelecting(this);
    getAddButton().click();
    waiter.get();
  }

  @Override
  public FileAttachmentEditPage edit() {
    return fileEditor().get();
  }

  public FileAttachmentEditPage fileEditor() {
    return new FileAttachmentEditPage(control);
  }

  public PackageAttachmentEditPage pkgEditor() {
    return new PackageAttachmentEditPage(control);
  }

  public UniversalControl getControl() {
    return control;
  }

  public FileAttachmentEditPage uploadZip(File file) {
    return uploadZip(fileToURL(file));
  }

  public PickAttachmentTypeDialog uploadAndGoBack(URL fileUrl) {
    return upload(fileUrl, BUTTON_BACK, new PickAttachmentTypeDialog(control));
  }
}
