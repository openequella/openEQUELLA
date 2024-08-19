package io.github.openequella.rest

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.httpclient.methods.{DeleteMethod, GetMethod, PostMethod}
import org.testng.Assert.{assertEquals, assertFalse, assertTrue}
import org.testng.annotations.Test

import scala.jdk.CollectionConverters._

class HierarchyApiTest extends AbstractRestApiTest {
  private val HIERARCHY_API_ENDPOINT        = getTestConfig.getInstitutionUrl + "api/hierarchy"
  private val BROWSE_HIERARCHY_API_ENDPOINT = getTestConfig.getInstitutionUrl + "api/browsehierarchy2"

  private val DEFAULT_ITEM_UUID = "cadcd296-a4d7-4024-bb5d-6c7507e6872a"
  private val DEFAULT_VERSION   = 2

  private val nonExistingUuid       = "non-existing-uuid"
  private val normaTopicUuid        = "6135b550-ce1c-43c2-b34c-0a3cf793759d"
  private val virtualTopicJamesUuid = "886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw=="
  private val virtualTopicHobartUuid =
    "46249813-019d-4d14-b772-2a8ca0120c99:SG9iYXJ,886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw=="

  @Test(description = "Get ACLs for a non-existing topic")
  def aclNotFound(): Unit = getAcls(nonExistingUuid, 404)

  @Test(description = "Get ACLs for a topic")
  def aclsForTopic(): Unit = {
    val result = getAcls(normaTopicUuid, 200)
    assertAcls(hasPermission = true, result)
  }

  @Test(description = "Get ACLs for a virtual topic")
  def aclsForVirtualTopic(): Unit = {
    val result = getAcls(virtualTopicJamesUuid, 200)
    assertAcls(hasPermission = true, result)
  }

  @Test(description = "Low privilege user gets ACLs for a topic")
  def lowPrivilegeAclsForTopic(): Unit = {
    loginAsLowPrivilegeUser()
    val result = getAcls(normaTopicUuid, 200)
    assertAcls(hasPermission = false, result)
    // Login as a normal user in the end to let other test cases properly run.
    login()
  }

  @Test(description = "Add a key resources to a non-existing topic")
  def topicNotFound(): Unit = {
    val compoundUuid = "non-existing-uuid"
    // add key resource
    addKeyResource(compoundUuid, DEFAULT_ITEM_UUID, DEFAULT_VERSION, 404)
  }

  @Test(description = "Add an non-existing key resources to an unknown topic")
  def addItemNotFound(): Unit = {
    // add key resource
    addKeyResource(normaTopicUuid, "non-existing-uuid", DEFAULT_VERSION, 404)
  }

  @Test(description = "Add a key resources")
  def addKeyResourceToTopic(): Unit = {
    assertAddKeyResourceIsSucceed(normaTopicUuid)
  }

  @Test(description = "Version 0 should add a latest item as a key resource")
  def addLatestKeyResourceToTopic(): Unit = {
    assertAddKeyResourceIsSucceed(normaTopicUuid, "2534e329-e37e-4851-896e-51d8b39104c4", 0, 1)
  }

  @Test(description = "Add a duplicated key resources",
        dependsOnMethods = Array("addKeyResourceToTopic"))
  def addDuplicatedKeyResource(): Unit = {
    // add key resource
    addKeyResource(normaTopicUuid, DEFAULT_ITEM_UUID, DEFAULT_VERSION, 409)
  }

  @Test(description = "Delete non key resource from topic")
  def deleteItemNotFound(): Unit = {
    // delete key resource
    deleteKeyResource(normaTopicUuid, "e35390cf-7c45-4f71-bb94-e6ccc1f09394", 1, 404)
  }

  @Test(description = "Delete a key resources",
        dependsOnMethods = Array("addDuplicatedKeyResource"))
  def deleteKeyResourceFromTopic(): Unit = {
    assertDeleteKeyResourceIsSucceed(normaTopicUuid)
  }

  @Test(description = "Add a key resource to a virtual topic")
  def addKeyResourceToVirtualTopic(): Unit = {
    assertAddKeyResourceIsSucceed(virtualTopicJamesUuid, DEFAULT_ITEM_UUID, 1, 1)
  }

  @Test(description = "Delete a key resource from a virtual topic",
        dependsOnMethods = Array("addKeyResourceToVirtualTopic"))
  def deleteKeyResourceFromVirtualTopic(): Unit = {
    assertDeleteKeyResourceIsSucceed(virtualTopicJamesUuid, DEFAULT_ITEM_UUID, 1)
  }

  @Test(description = "Add a key resource to a sub virtual topic")
  def addKeyResourceToSubVirtualTopic(): Unit = {
    // 46249813-019d-4d14-b772-2a8ca0120c99:Hobart,886aa61d-f8df-4e82-8984-c487849f80ff:A James
    assertAddKeyResourceIsSucceed(virtualTopicHobartUuid)
  }

  @Test(description = "Delete a key resource to a sub virtual topic",
        dependsOnMethods = Array("addKeyResourceToSubVirtualTopic"))
  def deleteKeyResourceFromSubVirtualTopic(): Unit = {
    assertDeleteKeyResourceIsSucceed(virtualTopicHobartUuid)
  }

  @Test(description = "Add a key resource without modify key resource permission")
  def noEditPermission(): Unit = {
    loginAsLowPrivilegeUser()
    addKeyResource("91a08805-d5f9-478d-aaaf-eff61a266667", DEFAULT_ITEM_UUID, DEFAULT_VERSION, 403)
    // Login as a normal user in the end to let other test cases properly run.
    login()
  }

  private def getAcls(compoundUuid: String, expectedCode: Int): JsonNode = {
    val url        = HIERARCHY_API_ENDPOINT + s"/${compoundUuid}/my-acls"
    val method     = new GetMethod(url)
    val statusCode = makeClientRequest(method)
    assertEquals(statusCode, expectedCode)
    mapper.readTree(method.getResponseBody)
  }

  private def assertAcls(hasPermission: Boolean, acls: JsonNode): Unit = {
    assertEquals(acls.get("VIEW_HIERARCHY_TOPIC").asBoolean(), hasPermission)
    assertEquals(acls.get("EDIT_HIERARCHY_TOPIC").asBoolean(), hasPermission)
    assertEquals(acls.get("MODIFY_KEY_RESOURCE").asBoolean(), hasPermission)
  }

  // Add a key resource
  private def addKeyResource(compoundUuid: String,
                             itemUuid: String,
                             itemVersion: Int,
                             expectedCode: Int): JsonNode = {
    val url        = HIERARCHY_API_ENDPOINT + s"/${compoundUuid}/keyresource/${itemUuid}/${itemVersion}"
    val method     = new PostMethod(url)
    val statusCode = makeClientRequest(method)
    assertEquals(statusCode, expectedCode)
    mapper.readTree(method.getResponseBody)
  }

  // Delete a key resource
  private def deleteKeyResource(compoundUuid: String,
                                itemUuid: String,
                                itemVersion: Int,
                                expectedCode: Int): JsonNode = {
    val url        = HIERARCHY_API_ENDPOINT + s"/${compoundUuid}/keyresource/${itemUuid}/${itemVersion}"
    val method     = new DeleteMethod(url)
    val statusCode = makeClientRequest(method)
    assertEquals(statusCode, expectedCode)
    mapper.readTree(method.getResponseBody)
  }

  // Check if the item is in the key resource result
  private def containsKeyResource(keyResources: JsonNode,
                                  itemUuid: String,
                                  itemVersion: Int): Boolean = {
    keyResources.elements.asScala.exists(
      resource =>
        resource.get("item").get("uuid").asText == itemUuid && resource
          .get("item")
          .get("version")
          .asInt == itemVersion)
  }

  // Get full information of a topic
  private def getHierarchyTopic(compoundUuid: String): JsonNode = {
    val url        = BROWSE_HIERARCHY_API_ENDPOINT + "/" + compoundUuid
    val method     = new GetMethod(url)
    val statusCode = makeClientRequest(method)
    assertEquals(statusCode, 200)
    mapper.readTree(method.getResponseBody)
  }

  private def assertAddKeyResourceIsSucceed(compoundUuid: String,
                                            itemUuid: String = DEFAULT_ITEM_UUID,
                                            version: Int = DEFAULT_VERSION,
                                            expectedVersion: Int = DEFAULT_VERSION): Unit = {
    val hierarchyTopic = getHierarchyTopic(compoundUuid)

    // make sure the ket resource is not existing
    assertFalse(containsKeyResource(hierarchyTopic.get("keyResources"), itemUuid, version))

    // add key resource
    addKeyResource(compoundUuid, itemUuid, version, 200)
    val newHierarchyTopic = getHierarchyTopic(compoundUuid)
    // make sure key resources is added
    assertTrue(
      containsKeyResource(newHierarchyTopic.get("keyResources"), itemUuid, expectedVersion))
  }

  private def assertDeleteKeyResourceIsSucceed(compoundUuid: String,
                                               itemUuid: String = DEFAULT_ITEM_UUID,
                                               version: Int = DEFAULT_VERSION): Unit = {
    val hierarchyTopic = getHierarchyTopic(compoundUuid);

    // make sure the ket resource is existing
    assertTrue(containsKeyResource(hierarchyTopic.get("keyResources"), itemUuid, version))

    // delete key resource
    deleteKeyResource(compoundUuid, itemUuid, version, 200)
    val newHierarchyTopic = getHierarchyTopic(compoundUuid)
    assertFalse(containsKeyResource(newHierarchyTopic.get("keyResources"), itemUuid, version))
  }
}
