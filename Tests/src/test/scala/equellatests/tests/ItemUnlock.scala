package equellatests.tests

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.searching.SearchPage
import com.tle.webtests.pageobject.viewitem.SummaryPage
import com.tle.webtests.pageobject.wizard.controls.UniversalControl
import com.tle.webtests.pageobject.wizard.controls.universal.{GenericAttachmentEditPage, YouTubeUniversalControlType}
import com.tle.webtests.pageobject.wizard.{ContributePage, WizardPageTab}
import equellatests.ShotProperties
import equellatests.domain.TestLogon
import org.scalacheck._
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import equellatests.TestChecker._
import equellatests.instgen.fiveo._

object ItemUnlock extends ShotProperties("Contribute") {

  val RENAMED_NAME = "A Video"
  val DISPLAY_NAME = "Original Displayname"

  property("unlock and resume") = forAll { (logon: TestLogon) =>
    withLogon(logon) { (context:PageContext) =>
      // contribute an item
      val itemName = context.getFullName("Item unlock and resume")
      val wizard = new ContributePage(context).load.openWizard("Youtube Channel Testing Collection")
      wizard.editbox(1, itemName)
      var control = wizard.universalControl(2)
      val youtube = control.addDefaultResource(new YouTubeUniversalControlType(control))
      youtube.search("maths", "The Khan Academy").selectVideo(1).setDisplayName(DISPLAY_NAME).save
      var item = wizard.save.publish
      assert(item.attachments.attachmentExists(DISPLAY_NAME))

      item.adminTab.edit
      // navigate away without saving, then view the item
      SearchPage.searchAndView(context, itemName)
      assert(item.isItemLocked)
      // unlock the item
      item.adminTab.unlockItem
      assert(!item.isItemLocked)

      item.adminTab.edit

      control = wizard.universalControl(2)
      control.editResource[GenericAttachmentEditPage, YouTubeUniversalControlType](new YouTubeUniversalControlType(control), DISPLAY_NAME).setDisplayName(RENAMED_NAME).save

      SearchPage.searchAndView(context, itemName)
      assert(item.attachments.attachmentExists(DISPLAY_NAME))

      // resume the item
      item.adminTab.resumeItem
      item = wizard.saveNoConfirm
      assert(item.attachments.attachmentCount == 1)
      assert(item.attachments.attachmentExists(RENAMED_NAME))
      true
    }
  }
}
