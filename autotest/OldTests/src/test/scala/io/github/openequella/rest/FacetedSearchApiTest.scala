package io.github.openequella.rest

import com.fasterxml.jackson.databind.JsonNode
import io.github.openequella.Utils
import org.apache.commons.httpclient.methods.GetMethod
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

import scala.jdk.CollectionConverters.IteratorHasAsScala

class FacetedSearchApiTest extends AbstractRestApiTest {
  private val FACET_SEARCH_API_ENDPOINT = getTestConfig.getInstitutionUrl + "api/search2/facet"
  // Browse books hierarchy
  private val HIERARCHY_UUID         = "91a08805-d5f9-478d-aaaf-eff61a266667"
  private val FACET_AUTHOR_NODE      = "/item/copyright/authors/author"
  private val BASIC_ITEMS_COLLECTION = "b28f1ffe-2008-4f5e-d559-83c8acd79316"
  private val BOOK_COLLECTION_UUID   = "4c147089-cddb-e67c-b5ab-189614eb1463"
  private val CAL_BOOK_COLLECTION    = "4c147089-cddb-e67c-b5ab-189614eb1463"
  private val DRM_COLLECTION_UUID    = "9a961a09-0c6a-b24f-b05b-eb21a696528b"
  private val AUTHOR_JAMES           = "A James"

  @Test(description = "Search without extra params")
  def search(): Unit = {
    val params  = Seq(("nodes", FACET_AUTHOR_NODE))
    val results = getFacetSearch(params)
    assertEquals(results.size(), 6)
  }

  @Test(description = "Search with query")
  def searchWithQueryTest(): Unit = {
    val params  = Seq(("nodes", FACET_AUTHOR_NODE), ("query", "Book A v2"))
    val results = getFacetSearch(params)
    assertEquals(results.size(), 1)
    assertEquals(getFirstTermName(results), AUTHOR_JAMES)
  }

  @Test(description = "Search with whereClause")
  def searchWithWhereClause(): Unit = {
    val params  = Seq(("nodes", FACET_AUTHOR_NODE), ("whereClause", "where /xml/item/@version = 2"))
    val results = getFacetSearch(params)
    assertEquals(results.size(), 1)
    assertEquals(getFirstTermName(results), AUTHOR_JAMES)
  }

  @Test(description = "Search with collections")
  def searchWithCollections(): Unit = {
    // search for book collection
    val params  = Seq(("nodes", FACET_AUTHOR_NODE), ("collections", BOOK_COLLECTION_UUID))
    val results = getFacetSearch(params)
    assertEquals(results.size(), 4)

    // search with an irrelevant collection
    val paramsDrm  = Seq(("nodes", FACET_AUTHOR_NODE), ("collections", DRM_COLLECTION_UUID))
    val resultsDrm = getFacetSearch(paramsDrm)
    assertEquals(resultsDrm.size(), 0)
  }

  @Test(description = "Search with modifiedBefore")
  def searchWithModifiedBefore(): Unit = {
    val params  = Seq(("nodes", FACET_AUTHOR_NODE), ("modifiedBefore", "2023-11-09"))
    val results = getFacetSearch(params)
    assertEquals(results.size(), 2)
    assertEquals(getFirstTermName(results), AUTHOR_JAMES)
  }

  @Test(description = "Search with modifiedAfter")
  def searchWithModifiedAfter(): Unit = {
    val params  = Seq(("nodes", FACET_AUTHOR_NODE), ("modifiedAfter", "2024-03-14"))
    val results = getFacetSearch(params)
    assertEquals(results.size(), 5)
    assertEquals(getFirstTermName(results), "A James")
  }

  @Test(description = "Search with owner")
  def searchWithOwner(): Unit = {
    // auto test user
    val params =
      Seq(("nodes", FACET_AUTHOR_NODE), ("owner", "adfcaf58-241b-4eca-9740-6a26d1c3dd58"))
    val results = getFacetSearch(params)
    assertEquals(results.size(), 1)
    assertEquals(getFirstTermName(results), "C Candy")
  }

  @Test(description = "Search with mimeTypes")
  def searchWithMimeTypes(): Unit = {
    val params  = Seq(("nodes", FACET_AUTHOR_NODE), ("mimeTypes", "application/pdf"))
    val results = getFacetSearch(params)
    assertEquals(results.size(), 1)
    assertEquals(getFirstTermName(results), "F Frank")
  }

  @Test(description = "Search with musts")
  def searchWithMusts(): Unit = {
    val params  = Seq(("nodes", FACET_AUTHOR_NODE), ("musts", "version:2"))
    val results = getFacetSearch(params)
    assertEquals(results.size(), 1)
    assertEquals(getTermCount(results, AUTHOR_JAMES), 2)
  }

  @Test(description = "Search with status")
  def searchWithStatus(): Unit = {
    val params  = Seq(("nodes", FACET_AUTHOR_NODE), ("status", "ARCHIVED"))
    val results = getFacetSearch(params)
    assertEquals(results.size(), 1)
    assertEquals(getTermCount(results, AUTHOR_JAMES), 2)
  }

  @Test(description = "Search for hierarchy facets")
  def hierarchySearchTest(): Unit = {
    val params  = Seq(("nodes", FACET_AUTHOR_NODE), ("hierarchy", HIERARCHY_UUID))
    val results = getFacetSearch(params)
    assertEquals(results.size(), 4)
  }

  @Test(description = "Search hierarchy with duplicated criteria which should be ignored")
  def hierarchySearchIgnoreTest(): Unit = {
    val params = Seq(
      ("nodes", FACET_AUTHOR_NODE),
      ("hierarchy", HIERARCHY_UUID),
      ("status", "ARCHIVED")
    )
    val results = getFacetSearch(params)
    assertEquals(results.size(), 4)
  }

  @Test(description = "Search hierarchy with collections filter")
  def hierarchySearchWithCollectionsFilterTest(): Unit = {
    // Search with relevant collection.
    val params = Seq(
      ("nodes", FACET_AUTHOR_NODE),
      ("hierarchy", HIERARCHY_UUID),
      ("collections", CAL_BOOK_COLLECTION)
    )
    val results = getFacetSearch(params)
    assertEquals(results.size(), 4)

    // Search with irrelevant collection.
    val paramsBasic = Seq(
      ("nodes", FACET_AUTHOR_NODE),
      ("hierarchy", HIERARCHY_UUID),
      ("collections", BASIC_ITEMS_COLLECTION)
    )
    val resultsBasic = getFacetSearch(paramsBasic)
    assertEquals(resultsBasic.size(), 0)
  }

  /** Get the count of a term in the result.
    */
  private def getTermCount(result: JsonNode, termName: String): Int = {
    val scalaIterator = result.elements().asScala

    scalaIterator
      .filter(node =>
        Option(node.get("term"))
          .map(_.asText())
          .contains(termName)
      )
      .map(_.get("count").asInt())
      .toList
      .headOption
      .getOrElse(-1)
  }

  /** Get the name of the first term in the result.
    */
  private def getFirstTermName(results: JsonNode): String =
    Option(results.get(0)).flatMap(node => Option(node.get("term"))).map(_.asText()).orNull

  /** Get the facet search result.
    */
  private def getFacetSearch(queryParams: Seq[(String, String)] = Seq()): JsonNode = {
    val url        = Utils.buildUrl(FACET_SEARCH_API_ENDPOINT, queryParams)
    val method     = new GetMethod(url)
    val statusCode = makeClientRequest(method)
    assertEquals(statusCode, 200)
    mapper.readTree(method.getResponseBodyAsStream).get("results")
  }
}
