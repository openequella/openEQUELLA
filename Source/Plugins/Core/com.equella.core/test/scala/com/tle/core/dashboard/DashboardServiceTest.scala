package com.tle.core.dashboard

import com.tle.beans.entity.LanguageBundle
import com.tle.beans.item.ItemStatus
import com.tle.common.beans.exception.NotFoundException
import com.tle.common.i18n.{CurrentLocale, LangUtils}
import com.tle.common.portal.PortletTypeDescriptor
import com.tle.common.portal.entity.impl.PortletRecentContrib
import com.tle.common.portal.entity.{Portlet, PortletPreference}
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.common.workflow.Trend
import com.tle.core.dashboard.model._
import com.tle.core.dashboard.service.DashboardService.DASHBOARD_LAYOUT
import com.tle.core.dashboard.service.{DashboardLayout, DashboardServiceImpl}
import com.tle.core.portal.service.PortletService
import com.tle.core.services.user.UserPreferenceService
import com.tle.exceptions.AccessDeniedException
import com.tle.web.portal.events.PortletsUpdatedEvent.PortletUpdateEventType
import com.tle.web.portal.service.PortletWebService
import com.tle.web.workflow.portal.TaskStatisticsPortletEditor.KEY_DEFAULT_TREND
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyBoolean, argThat, eq => eqMatcher}
import org.mockito.Mockito.{doNothing, mock, mockStatic, verify, when}
import org.scalatest.Inside.inside
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.TableFor2
import org.scalatest.prop.Tables.Table
import org.scalatest.{EitherValues, GivenWhenThen}

import java.util.UUID
import scala.jdk.CollectionConverters._

class DashboardServiceTest extends AnyFunSpec with Matchers with GivenWhenThen with EitherValues {
  val mockPortletService: PortletService               = mock(classOf[PortletService])
  val mockPortletWebService: PortletWebService         = mock(classOf[PortletWebService])
  val mockUserPreferenceService: UserPreferenceService = mock(classOf[UserPreferenceService])
  val dashboardService =
    new DashboardServiceImpl(mockPortletService, mockPortletWebService, mockUserPreferenceService)

  val userId = UUID.randomUUID().toString
  mockStatic(classOf[CurrentUser])
  when(CurrentUser.wasAutoLoggedIn).thenReturn(false)
  when(CurrentUser.isGuest).thenReturn(false)
  when(CurrentUser.getUserID).thenReturn(userId)

  val portletName = "testing portlet"
  mockStatic(classOf[LangUtils])
  when(LangUtils.getString(any[LanguageBundle]())).thenReturn(portletName)

  val runtimeError = "java.sql.SQLException: Connection refused"

  describe("Dashboard layout") {
    it("should return None when no layout is configured") {
      when(mockUserPreferenceService.getPreference(DASHBOARD_LAYOUT)).thenReturn(null)
      dashboardService.getDashboardLayout shouldBe None
    }

    it("supports updating the layout via UserPreferenceService") {
      When("a layout update succeeds")
      val newLayout = DashboardLayout.SingleColumn
      val result    = dashboardService.updateDashboardLayout(newLayout)
      result shouldBe a[Right[_, _]]

      Then("the layout is stored as a user preference")
      val preference = ArgumentCaptor.forClass(classOf[String])
      val data       = ArgumentCaptor.forClass(classOf[String])
      verify(mockUserPreferenceService).setPreference(preference.capture(), data.capture())
      preference.getValue shouldBe DASHBOARD_LAYOUT
      data.getValue shouldBe newLayout.toString
    }

    it("returns an error if the layout update fails") {

      When("UserPreferenceService throws an exception")
      when(mockUserPreferenceService.setPreference(any[String](), any[String]()))
        .thenThrow(new RuntimeException(runtimeError))

      Then("the failure is captured and returned as an error message")
      val result = dashboardService.updateDashboardLayout(DashboardLayout.SingleColumn)
      result shouldBe Left(s"Failed to update Dashboard layout: $runtimeError")
    }
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
      val result = dashboardService.buildPortletDetails(searchPortlet, Option(preference))
      result.value shouldBe SearchPortlet(
        commonDetails = PortletBase(
          uuid = uuid,
          name = portletName,
          isInstitutionWide = false,
          isClosed = false,
          isMinimised = true,
          canClose = false,
          canDelete = true,
          canEdit = true,
          canMinimise = true,
          column = PortletColumn.right,
          order = 1
        )
      )
    }

    it("collects additional details for Formatted text portlet") {
      Given("a Formatted portlet")
      val formattedTextPortlet = new Portlet("html")
      val rawHtml              = "<b>hello</b>"
      formattedTextPortlet.setConfig(rawHtml)

      Then("the configured raw HTML content should be included")
      val result = dashboardService.buildPortletDetails(formattedTextPortlet, None)
      result.value.asInstanceOf[FormattedTextPortlet].rawHtml shouldBe rawHtml
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
      val result = dashboardService.buildPortletDetails(recentContributionsPortlet, None)
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
      taskStatisticsPortlet.setAttribute(KEY_DEFAULT_TREND, "WEEK")

      Then("the configured trend should be included")
      val result = dashboardService.buildPortletDetails(taskStatisticsPortlet, None)
      result.value.asInstanceOf[TaskStatisticsPortlet].trend shouldBe Trend.WEEK
    }

    it("returns an error if the portlet type is unknown") {
      Given("a portlet with an unknown type")
      val unknownPortlet = new Portlet("unknownType")

      Then("an error should be returned")
      val result = dashboardService.buildPortletDetails(unknownPortlet, None)
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
        val result = dashboardService.buildPortletDetails(recentContributionsPortlet, None)
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
      val result = dashboardService.buildPortletDetails(portlet, None)
      inside(result) { case Left(errorMessage) =>
        errorMessage should startWith("Unknown trend 'YEAR' configured for Portlet")
      }
    }

    it("should return a list of viewable portlets") {
      Given("a viewable portlet, a closed portlet and an invalid portlet")
      val viewableSearchPortlet = new Portlet
      viewableSearchPortlet.setType("search")
      val viewableSearchPortletUuid = UUID.randomUUID().toString
      viewableSearchPortlet.setUuid(viewableSearchPortletUuid)

      val closedBrowsePortlet = new Portlet
      closedBrowsePortlet.setType("browse")
      val closedBrowsePortletUuid = UUID.randomUUID().toString
      closedBrowsePortlet.setUuid(closedBrowsePortletUuid)
      val pref = new PortletPreference(closedBrowsePortlet, userId)
      pref.setClosed(true)
      when(mockPortletService.getPreference(argThat[Portlet](_.getUuid == closedBrowsePortletUuid)))
        .thenReturn(pref)

      val invalidPortlet = new Portlet("unknownType")

      when(mockPortletService.getViewablePortletsForDisplay)
        .thenReturn(
          List(
            viewableSearchPortlet,
            closedBrowsePortlet,
            invalidPortlet
          ).asJava
        )

      Then("only the viewable portlet should be returned")
      val result = dashboardService.getViewablePortlets
      result should have size 1
      result.head.commonDetails.uuid shouldBe viewableSearchPortletUuid
    }
  }

  describe("Creatable portlets") {
    // These names and descriptions should be language string keys but defined here as the actual strings, and then mock `CurrentLocale`
    // to return them to simplify testing.
    val searchPortletName = "Quick search"
    val searchPortletDesc = "Quick search description"
    val browsePortletName = "Browse"
    val browsePortletDesc = "Browse portlet description"
    val mockCurrentLocale = mockStatic(classOf[CurrentLocale])
    mockCurrentLocale
      .when(() => CurrentLocale.get(any[String]()))
      .thenAnswer(invocation => invocation.getArgument(0))

    val searchPortlet =
      new PortletTypeDescriptor("search", searchPortletName, searchPortletDesc, null)
    val browsePortlet =
      new PortletTypeDescriptor("browse", browsePortletName, browsePortletDesc, null)
    val rssPortlet     = new PortletTypeDescriptor("rss", "Rss", "Rss portlet description", null)
    val unknownPortlet = new PortletTypeDescriptor("unknown", "Unknown", "Unknown portlet", null)

    it("should return a list of creatable portlet types") {
      Given("a list of valid, invalid and deprecated portlet types")
      when(mockPortletService.listContributableTypes(false))
        .thenReturn(
          List(
            searchPortlet,
            browsePortlet,
            rssPortlet,
            unknownPortlet
          ).asJava
        )

      Then("only the valid portlet types are returned and ordered by name")
      val result = dashboardService.getCreatablePortlets
      result shouldBe List(
        PortletCreatable(PortletType.browse, browsePortletName, browsePortletDesc),
        PortletCreatable(PortletType.search, searchPortletName, searchPortletDesc)
      )
    }
  }

  describe("Closed portlets") {
    it("should return the basic information of closed portlets") {
      Given("two closed portlets")
      val searchPortlet     = new Portlet
      val searchPortletUuid = UUID.randomUUID().toString
      // Set UUID, but do not need to set name due to the mocking of LangUtils in this suite.
      searchPortlet.setUuid(searchPortletUuid)

      val browsePortlet     = new Portlet
      val browsePortletUuid = UUID.randomUUID().toString
      browsePortlet.setUuid(browsePortletUuid)

      when(mockPortletService.getViewableButClosedPortlets)
        .thenReturn(List(searchPortlet, browsePortlet).asJava)

      Then("their UUIDs and names should be returned")
      val result = dashboardService.getClosedPortlets
      result shouldBe List(
        PortletClosed(searchPortletUuid, portletName),
        PortletClosed(browsePortletUuid, portletName)
      )
    }
  }

  describe("Update portlet preference") {
    val uuid          = UUID.randomUUID().toString
    val searchPortlet = new Portlet("search")
    searchPortlet.setUuid(uuid)
    when(mockPortletService.getByUuid(uuid)).thenReturn(searchPortlet)

    val updates = PortletPreferenceUpdate(
      isClosed = true,
      isMinimised = false,
      column = 0,
      order = 1
    )

    it("should update the preference for a portlet") {
      Given("a portlet that can be identified by the given UUID and new preference updates")
      val result = dashboardService.updatePortletPreference(uuid, updates)

      Then("the update should be successfully applied to the correct portlet")
      result shouldBe a[Right[_, _]]

      val targetPortlet = ArgumentCaptor.forClass(classOf[Portlet])
      val preference    = ArgumentCaptor.forClass(classOf[PortletPreferenceUpdate])
      verify(mockPortletService).updatePreference(targetPortlet.capture(), preference.capture())
      preference.getValue shouldBe updates
      targetPortlet.getValue shouldBe searchPortlet
    }

    it("should return NotFoundException if the portlet does not exist") {
      Given("a non-existing portlet UUID")
      when(mockPortletService.getByUuid(uuid)).thenReturn(null)

      Then("a NotFoundException is returned")
      val result = dashboardService.updatePortletPreference(uuid, updates)
      result.left.value
        .asInstanceOf[NotFoundException]
        .getMessage shouldBe s"Portlet with UUID $uuid not found"
    }

    it("should capture other exception thrown from the update") {
      Given("an existing portlet")
      when(mockPortletService.getByUuid(uuid)).thenReturn(searchPortlet)

      When("the preference update throws an exception")
      when(mockPortletService.updatePreference(any[Portlet](), any[PortletPreferenceUpdate]()))
        .thenThrow(new RuntimeException(runtimeError))

      Then("the exception is captured and returned")
      val result = dashboardService.updatePortletPreference(uuid, updates)
      result.left.value.asInstanceOf[RuntimeException].getMessage shouldBe runtimeError

      // Reset the mock in the end.
      doNothing().when(mockPortletService).updatePreference(any(), any())
    }

    it("should fire a Legacy Portlet Updated event when restoring a Legacy Content portlet") {
      Given("a closed Legacy Content portlet ")
      val lcp = new Portlet("myresources")
      lcp.setUuid(uuid)
      val pref = new PortletPreference(lcp, userId)
      pref.setClosed(true)
      when(mockPortletService.getByUuid(uuid)).thenReturn(lcp)
      when(mockPortletService.getPreference(searchPortlet)).thenReturn(pref)

      When("the preference is updated to restore the portlet")
      val result = dashboardService.updatePortletPreference(uuid, updates.copy(isClosed = false))

      Then("the update is successful and a Legacy Portlet Updated event is fired")
      result shouldBe a[Right[_, _]]
      val eventCaptor = ArgumentCaptor.forClass(classOf[PortletUpdateEventType])
      verify(mockPortletWebService).firePortletsChanged(
        any(),
        eqMatcher(userId),
        eqMatcher(uuid),
        anyBoolean(),
        eventCaptor.capture()
      )
      eventCaptor.getValue shouldBe PortletUpdateEventType.CREATED
    }
  }

  describe("Delete portlet") {
    val uuid          = UUID.randomUUID().toString
    val searchPortlet = new Portlet("search")
    searchPortlet.setUuid(uuid)

    it("only allows the owner to delete a portlet by UUID") {
      Given("a portlet that belongs to the current user")
      searchPortlet.setOwner(userId)
      when(mockPortletService.getByUuid(uuid)).thenReturn(searchPortlet)

      Then("the portlet should be deleted")
      val result = dashboardService.deletePortlet(uuid)
      result shouldBe a[Right[_, _]]

      val target                = ArgumentCaptor.forClass(classOf[Portlet])
      val checkPortletReference = ArgumentCaptor.forClass(classOf[Boolean])
      verify(mockPortletService).delete(target.capture(), checkPortletReference.capture())
      target.getValue shouldBe searchPortlet
      checkPortletReference.getValue shouldBe true
    }

    it("should return AccessDeniedException if the portlet belongs to another user") {
      Given("a portlet belonging to another user")
      val anotherPortlet = new Portlet("search")
      anotherPortlet.setUuid(uuid)
      anotherPortlet.setOwner("anotherUser")
      when(mockPortletService.getByUuid(uuid)).thenReturn(anotherPortlet)

      Then("an AccessDeniedException is returned")
      val result = dashboardService.deletePortlet(uuid)
      result.left.value
        .asInstanceOf[AccessDeniedException]
        .getMessage shouldBe s"No permission to delete portlet $uuid."
    }

    it("should return NotFoundException if the portlet does not exist") {
      Given("a non-existing portlet UUID")
      when(mockPortletService.getByUuid(uuid)).thenReturn(null)

      Then("a NotFoundException is returned")
      val result = dashboardService.deletePortlet(uuid)
      result.left.value
        .asInstanceOf[NotFoundException]
        .getMessage shouldBe s"Portlet with UUID $uuid not found"
    }

    it("should capture other exception thrown from the delete operation") {
      Given("a portlet belonging to the current user")
      searchPortlet.setOwner(userId)
      when(mockPortletService.getByUuid(uuid)).thenReturn(searchPortlet)

      When("the deletion causes an exception")
      when(mockPortletService.delete(any[Portlet](), any[Boolean]()))
        .thenThrow(new RuntimeException(runtimeError))

      Then("the exception is captured and returned")
      val result = dashboardService.deletePortlet(uuid)
      result.left.value.asInstanceOf[RuntimeException].getMessage shouldBe runtimeError
    }
  }
}
