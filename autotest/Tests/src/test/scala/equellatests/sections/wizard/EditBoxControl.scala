package equellatests.sections.wizard

import org.openqa.selenium.By

case class EditBoxControl(val page: WizardPageTab, val ctrlNum: Int) extends WizardControl {
  override def pageBy = By.xpath(s"id(${quoteXPath(ctrlId)})//div[@class='input text']/input")

  def value : String = pageElement.getAttribute("value")

  def value_=(v: String): Unit = {
    pageElement.clear()
    pageElement.sendKeys(v)
  }
}
