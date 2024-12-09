package equellatests.sections.wizard

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.generic.component.EquellaSelect
import equellatests.browserpage.WaitingBrowserPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}
import scala.jdk.CollectionConverters._

sealed trait AttachmentEditPage extends WaitingBrowserPage {

  def uc: UniversalControl

  def byForId(postfix: String): By = uc.byForId(sectionId + postfix)

  def ctx: PageContext = uc.ctx

  def pageBy: By = descriptionBy

  def sectionId: String

  def descriptionBy: By = byForId("_displayName")

  def descriptionField: WebElement = driver.findElement(descriptionBy)

  def restrictedBy: By = byForId("_restrictCheckbox")

  def restrictedField: Option[WebElement] = findElementO(restrictedBy)

  def restricted: Option[Boolean] = restrictedField.map(_.isSelected)

  def restricted_=(b: Boolean): Unit =
    if (!restricted.contains(b)) restrictedField.foreach(_.click())

  def suppressThumbField: Option[WebElement]

  def suppressThumb: Option[Boolean] = suppressThumbField.map(_.isSelected)

  def suppressThumb_=(b: Boolean) =
    if (!suppressThumb.contains(b)) suppressThumbField.foreach(_.click())

  def viewerSelect: Option[EquellaSelect] =
    findElementO(byForId("_viewers")).map(new EquellaSelect(ctx, _))

  def viewerOptions: Option[Set[String]] =
    viewerSelect.map(_.getOptionElements.asScala.map(_.getAttribute("value")).toSet - "")

  def viewer: Option[String] = viewerSelect.map(_.getSelectedValue)

  def viewer_=(viewer: Option[String]) =
    viewerSelect.foreach(vs => vs.selectByValue(viewer.getOrElse("")))

  def description: String = descriptionField.getAttribute("value")

  def description_=(name: String): Unit = {
    descriptionField.clear()
    descriptionField.sendKeys(name)
  }

  def details(): Iterable[(String, String)] =
    uc.elemForId("_dialog_fuh_d").findElements(By.tagName("tr")).asScala.map { tr =>
      (tr.findElement(By.xpath("td[1]")).getText, tr.findElement(By.xpath("td[2]")).getText)
    }

  def close(): WizardPageTab = {
    val closeButton = driver.findElement(By.xpath("//img[@class='modal_close']"))
    closeButton.click()
    waitFor(ExpectedConditions.stalenessOf(closeButton))
    uc.page.get()
  }

  def save(): WizardPageTab = {
    val expected = uc.updatedExpectation()
    val saveButton = uc
      .elemForId("_dialog")
      .findElement(
        By.xpath("//div[@class='modal-footer-inner']/button[normalize-space(text())='Save']")
      )
    saveButton.click()
    waitFor(expected)
    uc.page.get()
  }
}

class FileAttachmentEditPage(val uc: UniversalControl) extends AttachmentEditPage {
  def sectionId = "_dialog_fuh_fd"

  override def suppressThumbField: Option[WebElement] = findElementO(byForId("_st"))
}

class PackageAttachmentEditPage(val uc: UniversalControl) extends AttachmentEditPage {
  def sectionId = "_dialog_fuh_pd"

  override def suppressThumbField: Option[WebElement] = None
}
