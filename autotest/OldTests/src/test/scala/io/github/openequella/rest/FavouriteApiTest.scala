/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.openequella.rest

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.JsonNode
import io.github.openequella.Utils
import org.apache.commons.httpclient.methods.{
  DeleteMethod,
  GetMethod,
  PostMethod,
  StringRequestEntity
}
import org.testng.Assert.{assertEquals, assertNotNull, assertTrue}
import org.apache.commons.httpclient.HttpStatus
import org.testng.annotations.Test

import scala.jdk.CollectionConverters._

class FavouriteApiTest extends AbstractRestApiTest {
  private val FAVOURITE_ITEM_API_ENDPOINT =
    s"${getTestConfig.getInstitutionUrl}api/favourite/item"
  private val FAVOURITE_SEARCH_API_ENDPOINT =
    s"${getTestConfig.getInstitutionUrl}api/favourite/search"
  private val ITEM_KEY =
    "8a9ea41c-e28d-45de-b0a5-d75bca48d701/1"
  private val SEARCH_A = "Search A"
  private val SEARCH_B = "Search B"
  private val SEARCH_C = "Search C"
  private val SEARCH_D = "Search D"

  private var bookmarkId: Long = 0L
  private var searchId: Long   = 0L

  @Test
  def testAddFavouriteItem(): Unit = {
    val method              = new PostMethod(FAVOURITE_ITEM_API_ENDPOINT)
    val tags: Array[String] = Array("a", "b")

    val body: ObjectNode = mapper.createObjectNode()
    body.put("itemID", ITEM_KEY)
    body.set("keywords", mapper.valueToTree(tags))
    body.put("isAlwaysLatest", true)

    method.setRequestEntity(
      new StringRequestEntity(body.toString, "application/json", "UTF-8")
    )
    assertEquals(makeClientRequest(method), HttpStatus.SC_CREATED)

    val response: JsonNode      = mapper.readTree(method.getResponseBodyAsStream)
    val keywords: Array[String] = response.get("keywords").elements().asScala.map(_.asText).toArray

    assertEquals(response.get("itemID").asText(), ITEM_KEY)
    assertTrue(response.get("isAlwaysLatest").asBoolean())
    assertEquals(keywords, tags)
    assertNotNull(response.get("bookmarkID"))
    bookmarkId = response.get("bookmarkID").asLong()
  }

  @Test(dependsOnMethods = Array("testAddFavouriteItem"))
  def testRemoveFavouriteItemNoPermission(): Unit = {
    loginAsLowPrivilegeUser()
    val method = new DeleteMethod(s"$FAVOURITE_ITEM_API_ENDPOINT/$bookmarkId")
    assertEquals(makeClientRequest(method), HttpStatus.SC_FORBIDDEN)
    login()
  }

  @Test(dependsOnMethods = Array("testRemoveFavouriteItemNoPermission"))
  def testRemoveFavouriteItem(): Unit = {
    val method = new DeleteMethod(s"$FAVOURITE_ITEM_API_ENDPOINT/$bookmarkId")
    assertEquals(makeClientRequest(method), HttpStatus.SC_NO_CONTENT)
    // Try to delete again and the response code should be 404 as the favourite item is already deleted.
    assertEquals(makeClientRequest(method), HttpStatus.SC_NOT_FOUND)
  }

  @Test
  def testAddFavouriteSearch(): Unit = {
    val searchName = SEARCH_D
    val searchPath =
      "/page/search?searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22RANK%22%2C%22rawMode%22%3Afalse%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22%22%2C%22collections%22%3A%5B%5D%2C%22lastModifiedDateRange%22%3A%7B%7D%2C%22mimeTypeFilters%22%3A%5B%5D%2C%22dateRangeQuickModeEnabled%22%3Atrue%7D"

    val method           = new PostMethod(FAVOURITE_SEARCH_API_ENDPOINT)
    val body: ObjectNode = mapper.createObjectNode()
    body.put("name", searchName)
    body.put("url", searchPath)

    method.setRequestEntity(
      new StringRequestEntity(body.toString, "application/json", "UTF-8")
    )

    assertEquals(makeClientRequest(method), HttpStatus.SC_CREATED)
    val response: JsonNode = mapper.readTree(method.getResponseBodyAsStream)
    assertEquals(response.get("name").asText(), searchName)
    assertEquals(response.get("url").asText(), searchPath)
    searchId = response.get("id").asLong()
  }

  @Test(
    description = "Delete Favourite search without permission",
    dependsOnMethods = Array("testAddFavouriteSearch")
  )
  def testDeleteFavouriteSearchWithoutPermission(): Unit = {
    loginAsLowPrivilegeUser()
    val method = new DeleteMethod(s"$FAVOURITE_SEARCH_API_ENDPOINT/$searchId")
    assertEquals(makeClientRequest(method), HttpStatus.SC_FORBIDDEN)
    login()
  }

  @Test(
    description = "Delete Favourite search ",
    dependsOnMethods = Array("testDeleteFavouriteSearchWithoutPermission")
  )
  def testDeleteFavouriteSearch(): Unit = {
    val method = new DeleteMethod(s"$FAVOURITE_SEARCH_API_ENDPOINT/$searchId")
    assertEquals(makeClientRequest(method), HttpStatus.SC_NO_CONTENT)
    // Try to delete again and the response code should be 404 as the search is already deleted.
    assertEquals(makeClientRequest(method), HttpStatus.SC_NOT_FOUND)
  }

  @Test(
    description = "Get all favourite searches",
    dependsOnMethods = Array("testDeleteFavouriteSearch")
  )
  def testGetFavouriteSearch(): Unit = {
    val response: JsonNode = getFavouriteSearchResults()
    assertEquals(response.get("results").elements().asScala.size, 3)
    // Should be ordered by added time by default.
    assertEquals(getFirstFavouriteSearchName(response), SEARCH_C)
  }

  @Test(
    description = "Get all favourite searches ordered by name",
    dependsOnMethods = Array("testGetFavouriteSearch")
  )
  def testGetFavouriteSearchWithOrder(): Unit = {
    val response: JsonNode = getFavouriteSearchResults(Seq("order" -> "name"))
    assertEquals(getFirstFavouriteSearchName(response), SEARCH_A)
  }

  @Test(
    description = "Get all favourite searches within a date range",
    dependsOnMethods = Array("testGetFavouriteSearch")
  )
  def testGetFavouriteSearchWithDate(): Unit = {
    val response: JsonNode = getFavouriteSearchResults(
      Seq("addedAfter" -> "2001-01-01", "addedBefore" -> "2011-01-01")
    )
    assertEquals(getFirstFavouriteSearchName(response), SEARCH_B)
  }

  @Test(
    description = "Get all favourite searches with start and length queries",
    dependsOnMethods = Array("testGetFavouriteSearch")
  )
  def testStartAndLength(): Unit = {
    val response: JsonNode = getFavouriteSearchResults(Seq("start" -> "1", "length" -> "1"))
    assertEquals(getFirstFavouriteSearchName(response), SEARCH_B)
  }

  private def getFavouriteSearchResults(queryParams: Seq[(String, String)] = Seq()): JsonNode = {
    val url    = Utils.buildUrl(FAVOURITE_SEARCH_API_ENDPOINT, queryParams)
    val method = new GetMethod(url)
    assertEquals(makeClientRequest(method), HttpStatus.SC_OK)
    mapper.readTree(method.getResponseBodyAsStream)
  }

  private def getFirstFavouriteSearchName(response: JsonNode): String =
    response.get("results").get(0).get("name").asText()
}
