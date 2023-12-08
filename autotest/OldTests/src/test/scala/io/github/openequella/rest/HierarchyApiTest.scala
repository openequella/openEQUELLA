package io.github.openequella.rest

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.httpclient.methods.{DeleteMethod, GetMethod, PostMethod}
import org.testng.Assert.{assertEquals, assertFalse, assertTrue}
import org.testng.annotations.Test

import scala.jdk.CollectionConverters._

class HierarchyApiTest extends AbstractRestApiTest {
  private val HIERARCHY_API_ENDPOINT        = getTestConfig.getInstitutionUrl + "api/hierarchy"
  private val BROWSE_HIERARCHY_API_ENDPOINT = getTestConfig.getInstitutionUrl + "api/browsehierarchy2"

  private val itemUuid = "cadcd296-a4d7-4024-bb5d-6c7507e6872a"
  private val version  = 2

  private val normaTopicUuid = "6135b550-ce1c-43c2-b34c-0a3cf793759d"

  @Test(description = "Add a key resources to a non-existing topic")
  def topicNotFound(): Unit = {
    val compoundUuid = "non-existing-uuid"
    // add key resource
    post(compoundUuid, itemUuid, version, 404)
  }

  @Test(description = "Add an non-existing key resources to an unknown topic")
  def addItemNotFound(): Unit = {
    // add key resource
    post(normaTopicUuid, "non-existing-uuid", version, 404)
  }

  @Test(description = "Add a key resources")
  def addKeyResource(): Unit = {
    assertAddKeyResourceIsSucceed(normaTopicUuid)
  }

  @Test(description = "Add a duplicated key resources", dependsOnMethods = Array("addKeyResource"))
  def addDuplicatedKeyResource(): Unit = {
    // add key resource
    post(normaTopicUuid, itemUuid, version, 409)
  }

  @Test(description = "Delete non key resource from topic")
  def deleteItemNotFound(): Unit = {
    // delete key resource
    delete(normaTopicUuid, "e35390cf-7c45-4f71-bb94-e6ccc1f09394", 1, 404)
  }

  @Test(description = "Delete a key resources",
        dependsOnMethods = Array("addDuplicatedKeyResource"))
  def deleteKeyResource(): Unit = {
    assertDeleteKeyResourceIsSucceed(normaTopicUuid)
  }

  @Test(description = "Add a key resource to a virtual topic")
  def addKeyResourceToVirtualTopic(): Unit = {
    assertAddKeyResourceIsSucceed("886aa61d-f8df-4e82-8984-c487849f80ff%3AA%20James", itemUuid, 1)
  }

  @Test(description = "Delete a key resource from a virtual topic",
        dependsOnMethods = Array("addKeyResourceToVirtualTopic"))
  def deleteKeyResourceFromVirtualTopic(): Unit = {
    assertDeleteKeyResourceIsSucceed("886aa61d-f8df-4e82-8984-c487849f80ff%3AA%20James",
                                     itemUuid,
                                     1)
  }

  @Test(description = "Add a key resource to a sub virtual topic")
  def addKeyResourceToSubVirtualTopic(): Unit = {
    assertAddKeyResourceIsSucceed(
      "46249813-019d-4d14-b772-2a8ca0120c99%3AHobart%2C886aa61d-f8df-4e82-8984-c487849f80ff%3AA%2BJames")
  }

  @Test(description = "Delete a key resource to a sub virtual topic",
        dependsOnMethods = Array("addKeyResourceToSubVirtualTopic"))
  def deleteKeyResourceFromSubVirtualTopic(): Unit = {
    assertDeleteKeyResourceIsSucceed(
      "46249813-019d-4d14-b772-2a8ca0120c99%3AHobart%2C886aa61d-f8df-4e82-8984-c487849f80ff%3AA%2BJames")
  }

  @Test(description = "Add a key resource without modify key resource permission")
  def noEditPermission(): Unit = {
    makeClientRequest(
      authHelper.buildLoginMethod(AbstractRestApiTest.LOW_PRIVILEGE_USERNAME,
                                  AbstractRestApiTest.PASSWORD))
    post("91a08805-d5f9-478d-aaaf-eff61a266667", itemUuid, version, 403)
    // Login as a normal user in the end to let other test cases properly run.
    login()
  }

  // Add a key resource
  private def post(compoundUuid: String,
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
  private def delete(compoundUuid: String,
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
    keyResources.elements.asScala.exists(resource =>
      resource.get("uuid").asText == itemUuid && resource.get("version").asInt == itemVersion)
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
                                            itemUuid: String = itemUuid,
                                            version: Int = version): Unit = {
    val hierarchyTopic = getHierarchyTopic(compoundUuid)

    // make sure the ket resource is not existing
    assertFalse(containsKeyResource(hierarchyTopic.get("keyResources"), itemUuid, version))

    // add key resource
    post(compoundUuid, itemUuid, version, 200)
    val newHierarchyTopic = getHierarchyTopic(compoundUuid)
    // make sure key resources is added
    assertTrue(containsKeyResource(newHierarchyTopic.get("keyResources"), itemUuid, version))
  }

  private def assertDeleteKeyResourceIsSucceed(compoundUuid: String,
                                               itemUuid: String = itemUuid,
                                               version: Int = version): Unit = {
    val hierarchyTopic = getHierarchyTopic(compoundUuid);

    // make sure the ket resource is existing
    assertTrue(containsKeyResource(hierarchyTopic.get("keyResources"), itemUuid, version))

    // delete key resource
    delete(compoundUuid, itemUuid, version, 200)
    val newHierarchyTopic = getHierarchyTopic(compoundUuid)
    assertFalse(containsKeyResource(newHierarchyTopic.get("keyResources"), itemUuid, version))
  }
}
