package com.tle.core.dashboard

import com.tle.beans.entity.LanguageBundle
import com.tle.beans.item.ItemStatus
import com.tle.common.i18n.{CurrentLocale, LangUtils}
import com.tle.common.portal.entity.impl.PortletRecentContrib
import com.tle.common.portal.entity.{Portlet, PortletPreference}
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.common.workflow.Trend
import com.tle.core.dashboard.model._
import com.tle.core.dashboard.service.DashboardService.DASHBOARD_LAYOUT
import com.tle.core.dashboard.service.DashboardServiceImpl
import com.tle.core.portal.service.PortletService
import com.tle.core.settings.service.ConfigurationService
import com.tle.web.resources.{PluginResourceHelper, ResourcesService}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, mockStatic, when}
import org.scalatest.GivenWhenThen
import org.scalatest.Inside.inside
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.TableFor2
import org.scalatest.prop.Tables.Table

import java.util.UUID
import scala.jdk.CollectionConverters._

class DashboardServiceTest extends AnyFunSpec with Matchers with GivenWhenThen {
  val mockPortletService: PortletService             = mock(classOf[PortletService])
  val mockConfigurationService: ConfigurationService = mock(classOf[ConfigurationService])
  val dashboardService = new DashboardServiceImpl(mockPortletService, mockConfigurationService)

  val userId = UUID.randomUUID().toString
  mockStatic(classOf[CurrentUser])
  when(CurrentUser.wasAutoLoggedIn).thenReturn(false)
  when(CurrentUser.isGuest).thenReturn(false)
  when(CurrentUser.getUserID).thenReturn(userId)

  // A mock of ResourcesService.getResourceHelper is required due to the use of `CoreStrings` in Recent Contributions portlet.
  val mockResourceHelper = mock(classOf[PluginResourceHelper])
  mockStatic(classOf[ResourcesService])
  when(ResourcesService.getResourceHelper(any[Object])).thenReturn(mockResourceHelper)

  val portletName = "testing portlet"
  mockStatic(classOf[LangUtils])
  when(LangUtils.getString(any[LanguageBundle]())).thenReturn(portletName)

  describe("Dashboard layout") {
    it("should return None when no layout is configured") {
      when(mockConfigurationService.getProperty(DASHBOARD_LAYOUT)).thenReturn(null)
      dashboardService.getDashboardLayout shouldBe None
    }

    // todo: add more tests here after the support for configuring layout is implemented
  }

  describe("Portlet details") {
    it("collects portlet common details") {
      Given("a portlet and its preference")
      val searchPortlet = new Portlet("search")
      val uuid          = UUID.randomUUID().toString
      searchPortlet.setUuid(uuid)
      searchPortlet.setOwner(userId)
      searchPortlet.setInstitutional(false)
      searchPortlet.setCloseable(true)
      searchPortlet.setMinimisable(true)
      when(mockPortletService.canDelete(searchPortlet)).thenReturn(true)
      when(mockPortletService.canEdit(searchPortlet)).thenReturn(true)
      val preference = new PortletPreference(searchPortlet, userId)
      preference.setOrder(1)
      preference.setPosition(1)
      preference.setMinimised(true)
      when(mockPortletService.getPreference(searchPortlet)).thenReturn(preference)

      Then("the common details should contain both the general configurations and user preference")
      val result = dashboardService.buildPortletDetails(searchPortlet)
      result shouldBe a[Right[_, SearchPortlet]]
      inside(result) { case Right(SearchPortlet(details)) =>
        details shouldBe PortletBase(
          uuid = uuid,
          name = portletName,
          isInstitutionWide = false,
          isClosed = false,
          isMinimised = true,
          canClose = false,
          canDelete = true,
          canEdit = true,
          canMinimise = true,
          column = 1,
          order = 1
        )
      }
    }

    it("collects additional details for Formatted text portlet") {
      Given("a Formatted portlet")
      val formattedTextPortlet = new Portlet("html")
      val rawHtml              = "<b>hello</b>"
      formattedTextPortlet.setConfig(rawHtml)

      Then("the configured raw HTML content should be included")
      val result = dashboardService.buildPortletDetails(formattedTextPortlet)
      result shouldBe a[Right[_, FormattedTextPortlet]]
      inside(result) { case Right(FormattedTextPortlet(_, html)) =>
        html shouldBe rawHtml
      }
    }

    it("collects additional details for Recent contribution portlet") {
      Given("a Recent contribution portlet")
      val recentContributionsPortlet = new Portlet("recent")
      val recentConfig               = new PortletRecentContrib
      val searchQuery                = "hello"
      val ageDays                    = 3
      recentConfig.setQuery(searchQuery)
      recentConfig.setAgeDays(ageDays)
      recentContributionsPortlet.setExtraData(recentConfig)
      recentContributionsPortlet.setAttribute("status", "live")

      Then("the configured contribution search criteria should be included")
      val result = dashboardService.buildPortletDetails(recentContributionsPortlet)
      result shouldBe a[Right[_, RecentContributionsPortlet]]
      inside(result) {
        case Right(
              RecentContributionsPortlet(
                _,
                collectionUuids,
                query,
                maxAge,
                itemStatus,
                isShowTitleOnly
              )
            ) =>
          collectionUuids shouldBe None
          query shouldBe Some(searchQuery)
          maxAge shouldBe Some(ageDays)
          itemStatus shouldBe Some(ItemStatus.LIVE)
          isShowTitleOnly shouldBe false
      }
    }

    it("collects additional details for Task statistics portlet") {
      Given("a Task statistics portlet")
      val taskStatisticsPortlet = new Portlet("taskstatistics")
      taskStatisticsPortlet.setAttribute("trend", "WEEK")

      Then("the configured trend should be included")
      val result = dashboardService.buildPortletDetails(taskStatisticsPortlet)
      result shouldBe a[Right[_, TaskStatisticsPortlet]]
      inside(result) { case Right(TaskStatisticsPortlet(_, trend)) =>
        trend shouldBe Trend.WEEK
      }
    }

    it("returns an error if the portlet type is unknown") {
      Given("a portlet with an unknown type")
      val unknownPortlet = new Portlet("unknownType")

      Then("an error should be returned")
      val result = dashboardService.buildPortletDetails(unknownPortlet)
      result shouldBe a[Left[String, _]]
      inside(result) { case Left(errorMessage) =>
        errorMessage should startWith(s"Invalid portlet type 'unknownType' configured for Portlet")
      }
    }

    it("returns an error if the configuration of Recent contribution portlet is invalid") {
      val invalidConfigs: TableFor2[Object, String] = Table(
        ("invalid config", "errorMessage"),
        (
          null,
          "No configuration found for Recent Contribution portlet"
        ),
        (
          new Object,
          "Failed to retrieve the configuration of Recent Contribution portlet"
        )
      )

      forAll(invalidConfigs) { (invalidConfig, errorMessageStart) =>
        Given("a Recent contribution portlet with invalid configuration")
        val recentContributionsPortlet = new Portlet("recent")
        recentContributionsPortlet.setExtraData(invalidConfig)

        Then("an error should be returned")
        val result = dashboardService.buildPortletDetails(recentContributionsPortlet)
        result shouldBe a[Left[String, _]]
        inside(result) { case Left(errorMessage) =>
          errorMessage should startWith(errorMessageStart)
        }
      }
    }

    it("returns an error if the trend of Task statistics portlet is invalid") {
      Given("a Task statistics portlet with an invalid trend")
      val portlet = new Portlet("taskstatistics")
      portlet.setAttribute("trend", "YEAR")

      Then("an error should be returned")
      val result = dashboardService.buildPortletDetails(portlet)
      result shouldBe a[Left[String, _]]
      inside(result) { case Left(errorMessage) =>
        errorMessage should startWith("Unknown trend 'YEAR' configured for Portlet")
      }
    }

    it("should return a list of viewable portlets") {
      Given("a valid portlet and an invalid portlet")
      val validPortlet = new Portlet
      validPortlet.setType("search")
      val uuid = UUID.randomUUID().toString
      validPortlet.setUuid(uuid)

      val invalidPortlet = new Portlet("unknownType")

      when(mockPortletService.getViewablePortletsForDisplay)
        .thenReturn(
          List(
            validPortlet,
            invalidPortlet
          ).asJava
        )

      Then("only the valid portlet should be returned")
      val result = dashboardService.getViewablePortlets
      result should have size 1
      result.head.commonDetails.uuid shouldBe uuid
    }
  }
}
