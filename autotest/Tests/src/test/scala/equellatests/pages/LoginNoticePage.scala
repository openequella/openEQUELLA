package equellatests.pages

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.ExpectedConditions2
import equellatests.browserpage.NewTitledPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, Keys, WebElement}

case class LoginNoticePage(ctx: PageContext)
    extends NewTitledPage("Login Notice Editor", "page/loginconfiguration") {

  private def preNoticeApplyButton: WebElement = findElementById("preApplyButton")

  private def preNoticeClearButton: WebElement = findElementById("preClearButton")

  private def preNoticeField: WebElement = findElement(By.className("public-DraftEditor-content"))

  private def preNoticeAddImageButton: WebElement = findElement(By.className("rdw-image-wrapper"))

  private def preNoticeAddImagePopup: WebElement = findElement(By.className("rdw-image-modal"))

  private def preNoticeAddImageField: WebElement = findElement(By.name("imgSrc"))

  private def preNoticeAddImageURLButton: WebElement =
    preNoticeAddImagePopup.findElement(By.xpath("//span[text()='URL']"))

  private def preNoticeAddImageOK: WebElement =
    preNoticeAddImagePopup.findElement(By.xpath("//button[text()='Add']"))

  private def postNoticeApplyButton: WebElement = findElementById("postApplyButton")

  private def postNoticeClearButton: WebElement = findElementById("postClearButton")

  private def postNoticeField: WebElement = findElementById("postNoticeField")

  private def preTab: WebElement = findElementById("preTab")

  private def postTab: WebElement = findElementById("postTab")

  private def clearOkButton: WebElement = findElementById("okToClear")

  private def populatePreNoticeField(notice: String): Unit = {
    preNoticeField.sendKeys(Keys.chord(Keys.CONTROL, "a"))
    preNoticeField.sendKeys(Keys.DELETE)
    preNoticeField.sendKeys(notice)
  }

  private def waitForSnackBar(content: String): Unit = {
    waitFor(ExpectedConditions.textToBePresentInElementLocated(By.id("client-snackbar"), content))
  }

  private def waitForPostTab(): Unit = {
    waitFor(ExpectedConditions2.presenceOfElement(postNoticeField))
  }

  def setPreLoginNotice(notice: String): Unit = {
    populatePreNoticeField(notice)
    preNoticeApplyButton.click()
    waitForSnackBar("Login notice saved successfully.")
  }

  def setPreLoginNoticeWithImageURL(imgURL: String): Unit = {
    populatePreNoticeField("Image:")
    preNoticeAddImageButton.click()
    waitFor(ExpectedConditions.visibilityOf(preNoticeAddImagePopup))
    preNoticeAddImageURLButton.click()
    waitFor(ExpectedConditions.visibilityOf(preNoticeAddImageField))
    preNoticeAddImageField.click()
    preNoticeAddImageField.sendKeys(imgURL)
    waitFor(ExpectedConditions.elementToBeClickable(preNoticeAddImageOK))
    preNoticeAddImageOK.click()
    waitFor(ExpectedConditions.elementToBeClickable(preNoticeApplyButton))
    preNoticeApplyButton.click()
    waitForSnackBar("Login notice saved successfully.")
  }

  def clearPreLoginNotice(): Unit = {
    preNoticeClearButton.click()
    waitFor(ExpectedConditions2.presenceOfElement(clearOkButton))
    clearOkButton.click()
    waitForSnackBar("Login notice cleared successfully.")
  }

  def getPreNoticeFieldContents: String = {
    preNoticeField.getText
  }

  def setPostLoginNotice(notice: String): Unit = {
    gotoPostNoticeTab()
    populatePostNoticeField(notice)
    postNoticeApplyButton.click()
    waitForSnackBar("Login notice saved successfully.")
  }

  def clearPostLoginNotice(): Unit = {
    gotoPostNoticeTab()
    postNoticeClearButton.click()
    waitFor(ExpectedConditions2.presenceOfElement(clearOkButton))
    clearOkButton.click()
    waitForSnackBar("Login notice cleared successfully.")
  }

  def getPostNoticeFieldContents: String = {
    gotoPostNoticeTab()
    postNoticeField.getText
  }

  def gotoPostNoticeTab(): Unit = {
    this.postTab.click()
    waitForPostTab()
  }

  def populatePostNoticeField(notice: String): Unit = {
    gotoPostNoticeTab()
    postNoticeField.sendKeys(Keys.chord(Keys.CONTROL, "a"))
    postNoticeField.sendKeys(Keys.DELETE)
    postNoticeField.sendKeys(notice)
  }

}
