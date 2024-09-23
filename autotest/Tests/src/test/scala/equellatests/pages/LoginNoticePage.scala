package equellatests.pages

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.ExpectedConditions2
import equellatests.browserpage.NewTitledPage
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, Keys, WebElement}

case class LoginNoticePage(ctx: PageContext)
    extends NewTitledPage("Login notice editor", "page/loginconfiguration") {
  private val SUCCESS_MSG = "Saved successfully."

  private def saveButton: WebElement = findElementById("_saveButton")

  private def preNoticeField: WebElement = findElementById("tinymce")

  private val preNoticeIFrameClassName: By = By.className("tox-edit-area__iframe")
  private def preNoticeIFrame: WebElement  = findElement(preNoticeIFrameClassName)

  private def preNoticeAddImageButton: WebElement =
    findElement(By.cssSelector("button[aria-label='Insert/edit image']"))

  private def preNoticeAddImagePopup: WebElement = findElement(By.className("tox-dialog"))

  private def preNoticeAddImageForm: WebElement =
    findElement(By.xpath("//*[contains(@class,'tox-form__group')]"))

  private def preNoticeAddImageField: WebElement =
    preNoticeAddImageForm.findElement(By.xpath("//input[contains(@type,'url')]"))

  private def preNoticeAddImageOK: WebElement =
    preNoticeAddImagePopup.findElement(By.xpath("//button[text()='Save' and @class='tox-button']"))

  private def postNoticeClearButton: WebElement = findElementById("postClearButton")

  private def postNoticeField: WebElement = findElementById("postNoticeField")

  private def clearOkButton: WebElement = findElementById("okToClear")

  private def switchToTinyMCEIFrame(): Unit = {
    waiter.until(ExpectedConditions.presenceOfElementLocated(preNoticeIFrameClassName))
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

  def setPreLoginNotice(notice: String): Unit = {
    clearAndPopulatePreNoticeField(notice)
    switchFromTinyMCEIFrame()
    save()
  }

  def setPreLoginNoticeWithImageURL(imgURL: String): Unit = {
    clearAndPopulatePreNoticeField("Image Test:")
    switchFromTinyMCEIFrame()
    preNoticeAddImageButton.click()
    waitFor(ExpectedConditions.visibilityOf(preNoticeAddImagePopup))
    waitFor(ExpectedConditions.visibilityOf(preNoticeAddImageField))
    preNoticeAddImageField.click()
    preNoticeAddImageField.sendKeys(imgURL)
    waitFor(ExpectedConditions.textToBePresentInElementValue(preNoticeAddImageField, imgURL))
    waitFor(ExpectedConditions.elementToBeClickable(preNoticeAddImageOK))
    preNoticeAddImageOK.click()
    save()
  }

  def clearPreLoginNotice(): Unit = {
    clearAndPopulatePreNoticeField("")
    switchFromTinyMCEIFrame()
    save()
  }

  def getPreNoticeFieldContents: String = {
    switchToTinyMCEIFrame()
    val text = preNoticeField.getText
    switchFromTinyMCEIFrame()
    text
  }

  def setPostLoginNotice(notice: String): Unit = {
    populatePostNoticeField(notice)
    save()
  }

  def clearPostLoginNotice(): Unit = {
    postNoticeClearButton.click()
    waitFor(ExpectedConditions2.presenceOfElement(clearOkButton))
    clearOkButton.click()
    save()
  }

  def getPostNoticeFieldContents: String = {
    postNoticeField.getText
  }

  def populatePostNoticeField(notice: String): Unit = {
    postNoticeField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.BACK_SPACE))
    postNoticeField.sendKeys(notice)
  }

  def save(): Unit = {
    def saveButtonActive: Boolean = saveButton.getAttribute("disabled") == null
    def clickSaveButton(): Unit =
      new Actions(driver)
        .moveToElement(saveButton)
        .click()
        .perform()

    // A retry for clicking the save button, as sometimes delays due to showing and then
    // hiding a dialog can trip things up
    waiter.until(_ => {
      if (saveButtonActive) clickSaveButton()

      // Once the click has been genuinely received, then the button will no longer be active
      // and we can move on to checking for the expectant snackbar.
      !saveButtonActive
    })
    waitForSnackBar(SUCCESS_MSG)
  }
}
