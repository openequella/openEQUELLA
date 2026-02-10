package io.github.openequella

import org.openqa.selenium.By

/** Utility object for locating elements.
  */
object Locators {

  /** CSS attribute selector: [data-testid="..."]
    *
    * @param testId
    *   The value of the data-testid attribute to locate.
    */
  def byTestId(testId: String): By = By.cssSelector(s"[data-testid=$testId]")

  /** CSS attribute selector: [aria-label="..."]
    *
    * @param label
    *   The value of the aria-label attribute to locate.
    */
  def byAriaLabel(label: String): By = By.cssSelector(s"[aria-label=$label]")
}
