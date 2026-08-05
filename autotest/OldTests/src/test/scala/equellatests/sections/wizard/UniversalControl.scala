package equellatests.sections.wizard

import equellatests.domain.TestFile
import org.openqa.selenium.{By, StaleElementReferenceException, WebElement}
import org.openqa.selenium.support.pagefactory.ByChained
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions}

import scala.util.Try

class UniversalControl(val page: WizardPageTab, val ctrlNum: Int) extends WizardControl {

  private def isElementPresent(element: WebElement): Boolean =
    Try(element.isDisplayed).getOrElse(false)

  private def actionLinkBy(action: String) =
    By.xpath("div/div/div[contains(@class, 'actions')]/div/a[text()=" + quoteXPath(action) + "]")

  private val cancelBtnBy =
    By.xpath(
      "div/div/div[contains(@class, 'actions')]/div/button[contains(@title, 'Cancel upload')]"
    )

  private def rowForDescription(description: String, disabled: Boolean) =
    Try(pageElement.findElement(rowDescriptionBy(description, disabled))).toOption

  def editResource[A <: AttachmentEditPage](
      description: String,
      condition: ExpectedCondition[A]
  ): Option[A] = {
    rowForDescription(description, false).map { row =>
      row.findElement(actionLinkBy("Edit")).click()
      waitFor(condition)
    }
  }

  def errorExpectation(msg: String) = {
    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
      pageBy,
      By.xpath(s"//div[contains(@class, 'universalresources')]//div[text() = ${quoteXPath(msg)}]")
    )
  }

  def uploadInline[A](tf: TestFile, actualFilename: String, after: ExpectedCondition[A]): A = {
    elemForId("_fileUpload_file").sendKeys(TestFile.realFile(tf, actualFilename).getAbsolutePath)
    waitFor(after)
  }

  def cancelUpload(actualFilename: String) = {
    rowForDescription(actualFilename, true).map { row =>
      row.findElement(cancelBtnBy).click()
      // make sure the element is till in dom before monitoring its state
      if (isElementPresent(row)) {
        waitFor(ExpectedConditions.stalenessOf(row))
      }
    }
  }

  private def rowDescriptionBy(title: String, disabled: Boolean) =
    By.xpath(
      ".//ul/div[.//" + (if (disabled) "div" else "a") + "[text()=" + quoteXPath(title) + "]]"
    )

  def attachNameWaiter(description: String, disabled: Boolean): ExpectedCondition[_] = {
    ExpectedConditions.visibilityOfElementLocated(
      new ByChained(pageBy, rowDescriptionBy(description, disabled))
    )
  }

  def pageBy = By.id(idFor("universalresources"))

}
