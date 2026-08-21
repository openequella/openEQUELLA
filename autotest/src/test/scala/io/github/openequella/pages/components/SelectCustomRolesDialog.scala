package io.github.openequella.pages.components

import com.tle.webtests.framework.PageContext
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions

/** Select custom roles dialog. */
class SelectCustomRolesDialog(context: PageContext) extends SelectRolesDialog(context) {

  /** Input value to the custom role input.
    *
    * @param roleName
    *   the name of the custom role.
    */
  def inputCustomRole(roleName: String): Unit = {
    val customRoleInput = getRoleDialog.findElement(
      By.xpath(".//label[text()='Enter IdP role']/following-sibling::div/input")
    )
    clearText(customRoleInput)
    customRoleInput.sendKeys(roleName)
  }

  /** Click the delete button to delete the role.
    *
    * @param customRole
    *   the name of the custom role.
    * @param oeqRole
    *   the name of the oeq role.
    */
  def deleteRole(customRole: String, oeqRole: String): Unit = {
    val customRoleRecord = waiter.until(
      ExpectedConditions.presenceOfNestedElementLocatedBy(
        getRoleDialog,
        By.xpath(
          s".//th[.='$customRole']/parent::tr"
        )
      )
    )

    val oeqRoleRecord =
      customRoleRecord.findElement(By.xpath(s".//td//p[contains(.,'$oeqRole')]/ancestor::li"))

    val deleteIcon = oeqRoleRecord.findElement(By.xpath("//button[@aria-label='Delete']"))
    deleteIcon.click()
  }
}
