package equellatests.tests

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
  * OldTests/build.sbt.
  */
@NewUIOnly
class LoginNoticeMenuTest extends PropertyBasedBrowserTest with ShotTest {

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

  test("pre login notice creation with image, check login screen for image") {
    check(withLogon(autoTestLogon) { context =>
      val page          = LoginNoticePage(context).load()
      val localImageURL = context.getBaseUrl + "api/theme/newLogo.png"
      page.setPreLoginNoticeWithImageURL(localImageURL)

      val page2 = LoginPage(context).load()
      Prop(page2.loginNoticeHasImageWithSrc(localImageURL))
    })
  }

  test("pre login notice content sanitisation strips an untrusted image") {
    check(withLogon(autoTestLogon) { context =>
      val page        = LoginNoticePage(context).load()
      val badImageURL = "https://example.com/badImage.png"
      page.setPreLoginNoticeWithImageURL(badImageURL)

      val page2      = LoginPage(context).load()
      val noticeText = page2.findElementO(By.id("loginNotice")).map(_.getText).getOrElse("")

      // Not just "the bad image is gone" - also confirm the accompanying text
      // setPreLoginNoticeWithImageURL types alongside it survived, proving sanitisation stripped
      // the untrusted src specifically rather than wiping the whole notice.
      Prop(page2.loginNoticeHasNoImageWithSrc(badImageURL) && noticeText.contains("Image Test:"))
    })
  }

  test("pre login notice content sanitisation strips a javascript: link") {
    check(withLogon(autoTestLogon) { context =>
      val page     = LoginNoticePage(context).load()
      val linkText = "bad link"
      page.setPreLoginNoticeWithLinkURL("javascript:alert('TEST')", linkText)

      val loginPage  = LoginPage(context).load()
      val noticeText = loginPage.findElementO(By.id("loginNotice")).map(_.getText).getOrElse("")

      Prop(noticeText.contains(linkText) && !loginPage.loginNoticeHasLinkWithText(linkText))
    })
  }
}
