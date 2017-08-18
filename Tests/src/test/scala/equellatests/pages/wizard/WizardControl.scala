package equellatests.pages.wizard

import org.openqa.selenium.{By, WebDriver, WebElement}

trait WizardControl {
  def page: WizardPageTab
  def driver: WebDriver
  def ctrlNum : Int
  def ctrlId = s"p${page.pageNum}c$ctrlNum"
  def idFor(postfix: String) : String = ctrlId + postfix
  def byForId(postfix: String) : By = By.id(idFor(postfix))
  def elemForId(postfix: String): WebElement = driver.findElement(byForId(postfix))
}
