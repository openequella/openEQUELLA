package io.github.openequella.pages.components

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.AbstractPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, Keys, WebElement}

/** Select roles dialog. */
class SelectRolesDialog(context: PageContext) extends AbstractPage[SelectRolesDialog](context) {

  /** Get the select role dialog element.
    */
  def getRoleDialog: WebElement = {
    val dialog = By.xpath("//div[@role='dialog']")
    waiter.until(ExpectedConditions.presenceOfElementLocated(dialog))
  }

  /** Search roles by keyword.
    *
    * @param keyword
    *   the keyword to search. If keyword is None, it will the search without keyword.
    */
  def searchRoles(keyword: Option[String]): Unit = {
    val searchInput = getRoleDialog.findElement(
      By.xpath("//label[text()='Search roles']/following-sibling::div/input")
    )
    clearText(searchInput)
    keyword match {
      case Some(value) => searchInput.sendKeys(value)
      case None        => ()
    }
    searchInput.sendKeys(Keys.ENTER);
  }

  /** Click the select button to select the role.
    *
    * @param roleName
    *   the name of the role.
    */
  def selectRole(roleName: String): Unit = {
    val selectIcon = waiter.until(
      ExpectedConditions.presenceOfNestedElementLocatedBy(
        getRoleDialog,
        By.xpath(
          s".//span[text()='$roleName']/ancestor::div[contains(@class, 'MuiListItemButton-root')]//button[@aria-label='Select']"
        )
      )
    )
    selectIcon.click()
  }

  /** Click the delete button to delete the role.
    */
  def deleteRole(roleName: String): Unit = {
    val deleteIcon = waiter.until(
      ExpectedConditions.presenceOfNestedElementLocatedBy(
        getRoleDialog,
        By.xpath(
          s"//p[contains(.,'$roleName')]/ancestor::div[contains(@class, 'MuiListItem-root')]//button[@aria-label='Delete']"
        )
      )
    )
    deleteIcon.click()
  }

  /** Click the cancel button to cancel the selection.
    */
  def cancel(): Unit = {
    val cancelButton = getRoleDialog.findElement(By.xpath("//button[text()='CANCEL']"))
    cancelButton.click()
  }

  /** Click the ok button to confirm the selection.
    */
  def ok(): Unit = {
    val okButton = getRoleDialog.findElement(By.xpath("//button[text()='OK']"))
    okButton.click()
  }
}
