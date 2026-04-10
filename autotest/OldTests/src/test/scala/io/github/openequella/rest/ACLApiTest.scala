package io.github.openequella.rest

import org.apache.commons.httpclient.methods.GetMethod
import org.apache.http.client.utils.URIBuilder
import org.testng.Assert.assertEquals
import org.testng.annotations.{DataProvider, Test}

sealed trait PrivilegeTest {
  def useLowPrivilegeUser: Boolean
  def isGranted: Boolean
}

case class HierarchyPrivilegeTest(topic: String, useLowPrivilegeUser: Boolean, isGranted: Boolean)
    extends PrivilegeTest
case class SettingPrivilegeTest(setting: String, useLowPrivilegeUser: Boolean, isGranted: Boolean)
    extends PrivilegeTest

class ACLApiTest extends AbstractRestApiTest {

  private val BASIC_ACL_ENDPOINT     = s"${getTestConfig.getInstitutionUrl}api/acl"
  private val HIERARCHY_ACL_ENDPOINT = s"$BASIC_ACL_ENDPOINT/privilegecheck/hierarchy"
  private val SETTING_ACL_ENDPOINT   = s"$BASIC_ACL_ENDPOINT/privilegecheck/setting"

  private val VIEW_HIERARCHY_TOPIC = "VIEW_HIERARCHY_TOPIC"
  private val EDIT_SYSTEM_SETTINGS = "EDIT_SYSTEM_SETTINGS"

  private val NORMAL_TOPIC_ID  = "6135b550-ce1c-43c2-b34c-0a3cf793759d"
  private val VIRTUAL_TOPIC_ID = "886aa61d-f8df-4e82-8984-c487849f80ff%3AA%20James"
  private val SEARCH_SETTING   = "searching"

  @DataProvider(name = "testDataProvider")
  def testDataProvider: Array[PrivilegeTest] = {
    Array(
      HierarchyPrivilegeTest(NORMAL_TOPIC_ID, useLowPrivilegeUser = false, isGranted = true),
      HierarchyPrivilegeTest(VIRTUAL_TOPIC_ID, useLowPrivilegeUser = false, isGranted = true),
      SettingPrivilegeTest(SEARCH_SETTING, useLowPrivilegeUser = false, isGranted = true),
      HierarchyPrivilegeTest(NORMAL_TOPIC_ID, useLowPrivilegeUser = true, isGranted = false),
      HierarchyPrivilegeTest(VIRTUAL_TOPIC_ID, useLowPrivilegeUser = true, isGranted = false),
      SettingPrivilegeTest(SEARCH_SETTING, useLowPrivilegeUser = true, isGranted = false)
    )
  }

  @Test(
    dataProvider = "testDataProvider",
    description = "Check whether a specified privilege is granted to the current user for an entity"
  )
  def checkPrivilegeForEntity(testData: PrivilegeTest): Unit = {
    if (testData.useLowPrivilegeUser) loginAsLowPrivilegeUser()

    val url = testData match {
      case HierarchyPrivilegeTest(topic, _, _) =>
        new URIBuilder(s"$HIERARCHY_ACL_ENDPOINT/$topic")
          .addParameter("privilege", VIEW_HIERARCHY_TOPIC)
          .build()
          .toString
      case SettingPrivilegeTest(setting, _, _) =>
        new URIBuilder(s"$SETTING_ACL_ENDPOINT/$setting")
          .addParameter("privilege", EDIT_SYSTEM_SETTINGS)
          .build()
          .toString
    }

    val method     = new GetMethod(url)
    val statusCode = makeClientRequest(method)
    assertEquals(statusCode, 200)

    val response = mapper.readTree(method.getResponseBodyAsStream)
    assertEquals(response.asBoolean(), testData.isGranted)
  }
}
