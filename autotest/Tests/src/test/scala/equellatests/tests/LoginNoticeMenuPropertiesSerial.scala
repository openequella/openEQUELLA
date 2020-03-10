package equellatests.tests

import equellatests.ShotProperties
import equellatests.domain.RandomWord
import equellatests.instgen.fiveo.autoTestLogon
import equellatests.pages.{LoginNoticePage, LoginPage}
import org.openqa.selenium.By
import org.scalacheck.Prop
import org.scalacheck.Prop.forAll

object LoginNoticeMenuPropertiesSerial extends ShotProperties("Login Notice Menu Properties") {

  property("pre login notice creation") = forAll { w1: RandomWord =>
    withLogon(autoTestLogon) { context =>
      val page   = LoginNoticePage(context).load()
      val notice = s"${w1.word}"
      page.setPreLoginNotice(notice)
      page.load()
      Prop(page.getPreNoticeFieldContents == notice)
        .label("Notice: " + notice + ", NoticeField: " + page.getPreNoticeFieldContents)
    }
  }

  property("post login notice creation") = forAll { w1: RandomWord =>
    withLogon(autoTestLogon) { context =>
      val page   = LoginNoticePage(context).load()
      val notice = s"${w1.word}"
      page.setPostLoginNotice(notice)
      page.load()
      page.gotoPostNoticeTab()
      Prop(page.getPostNoticeFieldContents == notice)
        .label("Notice: " + notice + ", NoticeField: " + page.getPostNoticeFieldContents)
    }
  }

  property("pre login notice clear") = forAll { w1: RandomWord =>
    withLogon(autoTestLogon) { context =>
      val page   = LoginNoticePage(context).load()
      val notice = s"${w1.word}"
      page.setPreLoginNotice(notice)
      page.clearPreLoginNotice()
      page.load()
      Prop(page.getPreNoticeFieldContents == "")
    }
  }

  property("post login notice clear") = forAll { w1: RandomWord =>
    withLogon(autoTestLogon) { context =>
      val page   = LoginNoticePage(context).load()
      val notice = s"${w1.word}"
      page.setPostLoginNotice(notice)
      page.load()
      page.clearPostLoginNotice()
      page.load()
      page.gotoPostNoticeTab()
      Prop(page.getPostNoticeFieldContents == "")
    }
  }

  property("prove existence on login page after creation") = forAll { w1: RandomWord =>
    withLogon(autoTestLogon) { context =>
      val page   = LoginNoticePage(context).load()
      val notice = s"${w1.word}"
      page.setPreLoginNotice(notice)
      page.load()
      val page2 = LoginPage(context).load()
      Prop(page2.findElementO(By.id("loginNotice")).get.getText == notice)
    }
  }

  property("prove non-existence on login page after clear") = forAll { w1: RandomWord =>
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
  }

  property("pre login notice creation with image, check login screen for image") =
    withLogon(autoTestLogon) { context =>
      val page = LoginNoticePage(context).load()
      val equellaGithubAvatarURL =
        "https://raw.githubusercontent.com/openequella/openEQUELLA/develop/autotest/Tests/tests/fiveo/institution/items/42/216490/cat1.jpg"
      page.setPreLoginNoticeWithImageURL(equellaGithubAvatarURL)

      val page2 = LoginPage(context).load()
      Prop(page2.loginNoticeHasImageWithSrc(equellaGithubAvatarURL))
    }
}
