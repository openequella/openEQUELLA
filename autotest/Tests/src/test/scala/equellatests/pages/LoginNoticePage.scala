package equellatests.pages

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.ExpectedConditions2
import equellatests.browserpage.NewTitledPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, Keys, WebElement}

case class LoginNoticePage(ctx: PageContext)
    extends NewTitledPage("Login notice editor", "page/loginconfiguration") {

  private def saveButton: WebElement = findElementById("SaveButton")

  private def preNoticeField: WebElement = findElementById("tinymce")

  private def preNoticeIFrame: WebElement = findElement(By.className("tox-edit-area__iframe"))

  private def preNoticeAddImageButton: WebElement =
    findElement(By.cssSelector("button[aria-label='Insert/edit image']"))

  private def preNoticeAddImagePopup: WebElement = findElement(By.className("tox-dialog"))

  private def preNoticeAddImageForm: WebElement =
    findElement(By.xpath("//*[contains(@class,'tox-form__group')]"))

  private def preNoticeAddImageField: WebElement =
    preNoticeAddImageForm.findElement(By.xpath("//input[contains(@aria-pressed,'false')]"))

  private def preNoticeAddImageOK: WebElement =
    preNoticeAddImagePopup.findElement(By.xpath("//button[text()='Save']"))

  private def postNoticeClearButton: WebElement = findElementById("postClearButton")

  private def postNoticeField: WebElement = findElementById("postNoticeField")

  private def postTab: WebElement = findElementById("postTab")

  private def clearOkButton: WebElement = findElementById("okToClear")

  private def switchToTinyMCEIFrame(): Unit = {
    driver.switchTo().frame(preNoticeIFrame)
    waiter.until(ExpectedConditions.presenceOfElementLocated(By.id("tinymce")))
  }

  private def switchFromTinyMCEIFrame(): Unit = {
    driver.switchTo().defaultContent()
    waiter.until(
      ExpectedConditions.presenceOfElementLocated(By.xpath("//h5[text()='Login notice editor']")))
  }

  private def clearAndPopulatePreNoticeField(notice: String): Unit = {
    switchToTinyMCEIFrame()
    preNoticeField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.BACK_SPACE))

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
    clearAndPopulatePreNoticeField(notice)
    switchFromTinyMCEIFrame()
    saveButton.click()
    waitForSnackBar("Login notice saved successfully.")
  }

  def setPreLoginNoticeWithImageURL(imgURL: String): Unit = {
    clearAndPopulatePreNoticeField("Image Test: ")
    switchFromTinyMCEIFrame()
    preNoticeAddImageButton.click()
    waitFor(ExpectedConditions.visibilityOf(preNoticeAddImagePopup))
    waitFor(ExpectedConditions.visibilityOf(preNoticeAddImageField))
    preNoticeAddImageField.click()
    preNoticeAddImageField.sendKeys(imgURL)
    waitFor(ExpectedConditions.textToBePresentInElementValue(preNoticeAddImageField, imgURL))
    waitFor(ExpectedConditions.elementToBeClickable(preNoticeAddImageOK))
    preNoticeAddImageOK.click()
    saveButton.click()
    waitForSnackBar("Login notice saved successfully.")
  }

  def clearPreLoginNotice(): Unit = {
    clearAndPopulatePreNoticeField("")
    switchFromTinyMCEIFrame()
    saveButton.click()
    waitForSnackBar("Login notice cleared successfully.")
  }

  def getPreNoticeFieldContents: String = {
    switchToTinyMCEIFrame()
    val text = preNoticeField.getText
    switchFromTinyMCEIFrame()
    text
  }

  def setPostLoginNotice(notice: String): Unit = {
    gotoPostNoticeTab()
    populatePostNoticeField(notice)
    saveButton.click()
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
    postNoticeField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.BACK_SPACE))
    postNoticeField.sendKeys(notice)
  }

}
