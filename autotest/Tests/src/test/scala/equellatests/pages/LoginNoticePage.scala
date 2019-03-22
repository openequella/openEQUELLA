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

  private def preNoticeField: WebElement = findElement(By.className("mce-content-body"))

  private def preNoticeIFrame: WebElement = findElement(By.className("tox-edit-area__iframe"))

  private def preNoticeAddImageButton: WebElement =
    findElement(By.cssSelector("button[aria-label='Insert/edit image']"))

  private def preNoticeAddImagePopup: WebElement = findElement(By.className("tox-dialog"))

  private def preNoticeAddImageForm: WebElement =
    findElement(By.xpath("//*[contains(@class,'tox-form__group')]"))

  private def preNoticeAddImageField: WebElement =
    preNoticeAddImageForm.findElement(By.xpath("//input"))

  private def preNoticeAddImageOK: WebElement =
    preNoticeAddImagePopup.findElement(By.xpath("//button[text()='Save']"))

  private def postNoticeApplyButton: WebElement = findElementById("postApplyButton")

  private def postNoticeClearButton: WebElement = findElementById("postClearButton")

  private def postNoticeField: WebElement = findElementById("postNoticeField")

  private def preTab: WebElement = findElementById("preTab")

  private def postTab: WebElement = findElementById("postTab")

  private def clearOkButton: WebElement = findElementById("okToClear")

  private def populatePreNoticeField(notice: String): Unit = {
    preNoticeField.sendKeys(notice)
    waitFor(ExpectedConditions.textToBePresentInElement(preNoticeField, notice))
  }

  private def switchToTinyMCEIFrame(): Unit = {
    driver.switchTo().frame(preNoticeIFrame)
  }

  private def switchFromTinyMCEIFrame(): Unit = {
    driver.switchTo().defaultContent()
  }

  private def clearandPopulatePreNoticeField(notice: String): Unit = {
    switchToTinyMCEIFrame()
    preNoticeField.sendKeys(Keys.chord(Keys.CONTROL, "a"))
    preNoticeField.sendKeys(Keys.DELETE)
    preNoticeField.sendKeys(notice)
    waitFor(ExpectedConditions.textToBePresentInElement(preNoticeField, notice))
  }

  private def waitForSnackBar(content: String): Unit = {
    waitFor(ExpectedConditions.textToBePresentInElementLocated(By.id("client-snackbar"), content))
  }

  private def waitForPostTab(): Unit = {
    waitFor(ExpectedConditions2.presenceOfElement(postNoticeField))
  }

  def setPreLoginNotice(notice: String): Unit = {
    clearandPopulatePreNoticeField(notice)
    switchFromTinyMCEIFrame()
    preNoticeApplyButton.click()
    waitForSnackBar("Login notice saved successfully.")
  }

  def setPreLoginNoticeWithImageURL(imgURL: String): Unit = {
    clearandPopulatePreNoticeField("Image Test: ")
    switchFromTinyMCEIFrame()
    preNoticeAddImageButton.click()
    waitFor(ExpectedConditions.visibilityOf(preNoticeAddImagePopup))
    waitFor(ExpectedConditions.visibilityOf(preNoticeAddImageField))
    preNoticeAddImageField.click()
    preNoticeAddImageField.sendKeys(imgURL)
    waitFor(ExpectedConditions.textToBePresentInElementValue(preNoticeAddImageField, imgURL))
    waitFor(ExpectedConditions.elementToBeClickable(preNoticeAddImageOK))
    preNoticeAddImageOK.click()
    preNoticeApplyButton.click()
    waitForSnackBar("Login notice saved successfully.")
  }

  def clearPreLoginNotice(): Unit = {
    clearandPopulatePreNoticeField("")
    switchFromTinyMCEIFrame()
    preNoticeApplyButton.click()
    waitForSnackBar("Login notice cleared successfully.")
  }

  def getPreNoticeFieldContents: String = {
    switchToTinyMCEIFrame()
    val text = preNoticeField.getText
    switchFromTinyMCEIFrame()
    return text
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
