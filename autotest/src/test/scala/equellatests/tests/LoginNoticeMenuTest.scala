package equellatests.tests

import com.tle.webtests.framework.PageContext
import equellatests.domain.RandomWord
import equellatests.instgen.fiveo.autoTestLogon
import equellatests.pages.{LoginNoticePage, LoginPage}
import equellatests.{PropertyBasedBrowserTest, ShotTest}
import org.openqa.selenium.By
import org.scalacheck.Prop
import org.scalacheck.Prop.forAll
import testng.annotation.NewUIOnly

/** Login notices are institution-wide state, so this suite must not run alongside anything else
  * touching the `fiveo` institution — which is why `tests.parallel` is honoured in
  * autotest/build.sbt.
  */
@NewUIOnly
class LoginNoticeMenuTest extends PropertyBasedBrowserTest with ShotTest {

  private val DISALLOWED_CONTENT_MESSAGE =
    "This notice contains content that is not allowed and could not be saved. Please refer to documentation for more information."

  test("pre login notice creation") {
    check(forAll { w1: RandomWord =>
      withLogon(autoTestLogon) { context =>
        val page   = LoginNoticePage(context).load()
        val notice = s"${w1.word}"
        page.setPreLoginNotice(notice)
        page.load()
        Prop(page.getPreNoticeFieldContents == notice)
          .label("Notice: " + notice + ", NoticeField: " + page.getPreNoticeFieldContents)
      }
    })
  }

  test("post login notice creation") {
    check(forAll { w1: RandomWord =>
      withLogon(autoTestLogon) { context =>
        val page   = LoginNoticePage(context).load()
        val notice = s"${w1.word}"
        page.setPostLoginNotice(notice)
        page.load()
        Prop(page.getPostNoticeFieldContents == notice)
          .label("Notice: " + notice + ", NoticeField: " + page.getPostNoticeFieldContents)
      }
    })
  }

  test("pre login notice clear") {
    check(forAll { w1: RandomWord =>
      withLogon(autoTestLogon) { context =>
        val page   = LoginNoticePage(context).load()
        val notice = s"${w1.word}"
        page.setPreLoginNotice(notice)
        page.clearPreLoginNotice()
        page.load()
        Prop(page.getPreNoticeFieldContents == "")
      }
    })
  }

  test("post login notice clear") {
    check(forAll { w1: RandomWord =>
      withLogon(autoTestLogon) { context =>
        val page   = LoginNoticePage(context).load()
        val notice = s"${w1.word}"
        page.setPostLoginNotice(notice)
        page.load()
        page.clearPostLoginNotice()
        page.load()
        Prop(page.getPostNoticeFieldContents == "")
      }
    })
  }

  test("prove existence on login page after creation") {
    check(forAll { w1: RandomWord =>
      withLogon(autoTestLogon) { context =>
        val page   = LoginNoticePage(context).load()
        val notice = s"${w1.word}"
        page.setPreLoginNotice(notice)
        page.load()
        val page2 = LoginPage(context).load()
        Prop(page2.findElementO(By.id("loginNotice")).get.getText == notice)
      }
    })
  }

  test("prove non-existence on login page after clear") {
    check(forAll { w1: RandomWord =>
      withLogon(autoTestLogon) { context =>
        val page   = LoginNoticePage(context).load()
        val notice = s"${w1.word}"
        page.setPreLoginNotice(notice)
        page.load()
        page.clearPreLoginNotice()
        page.load()
        val page2 = LoginPage(context).load()
        Prop(page2.findElementO(By.id("loginNotice")).isEmpty)
      }
    })
  }

  test("pre-login notice preserves trusted local images on login screen") {
    check(withLogon(autoTestLogon) { context =>
      val page          = LoginNoticePage(context).load()
      val localImageURL = context.getBaseUrl + "api/theme/newLogo.png"
      page.setPreLoginNoticeWithImageURL(localImageURL)
      page.save()

      val page2 = LoginPage(context).load()
      Prop(page2.loginNoticeHasImageWithSrc(localImageURL))
    })
  }

  test("pre login notice content sanitisation rejects an untrusted image, saving nothing") {
    check(withLogon(autoTestLogon) { context =>
      saveInvalidContentAndAssert(
        context,
        _.setPreLoginNoticeWithImageURL("https://example.com/badImage.png")
      )
    })
  }

  test("pre login notice content sanitisation rejects a javascript: link, saving nothing") {
    check(withLogon(autoTestLogon) { context =>
      saveInvalidContentAndAssert(
        context,
        _.setPreLoginNoticeWithLinkURL("javascript:alert('TEST')", "bad link")
      )
    })
  }

  private def saveInvalidContentAndAssert(
      context: PageContext,
      insertDisallowedContent: LoginNoticePage => Unit
  ): Prop = {
    val page            = LoginNoticePage(context).load()
    val originalContent = page.getPreNoticeFieldContents

    insertDisallowedContent(page)
    page.saveExpectingRejection(DISALLOWED_CONTENT_MESSAGE)

    // Not silently corrected and saved - the whole save is refused, so reloading the editor
    // must show nothing changed.
    page.load()
    Prop(page.getPreNoticeFieldContents == originalContent)
  }
}
