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

  // Deliberately points at the upstream GitHub repository rather than a local path: the test needs a
  // remotely hosted image to check that the login notice renders it. The path is upstream's layout,
  // so it does not track moves of our own fixture tree and must not be "corrected" to match it.
  private val equellaGithubAvatarURL =
    "https://raw.githubusercontent.com/openequella/openEQUELLA/develop/autotest/Tests/tests/fiveo/institution/items/42/216490/cat1.jpg"

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
      val page = LoginNoticePage(context).load()
      page.setPreLoginNoticeWithImageURL(equellaGithubAvatarURL)

      val page2 = LoginPage(context).load()
      Prop(page2.loginNoticeHasImageWithSrc(equellaGithubAvatarURL))
    })
  }
}
