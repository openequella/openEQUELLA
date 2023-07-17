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

package com.tle.core.freetext.index

import com.dytech.edge.queries.FreeTextQuery
import com.tle.beans.Institution
import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.beans.item.{Item, ItemIdKey, ItemStatus}
import com.tle.common.i18n.{CurrentLocale, LangUtils}
import com.tle.common.institution.CurrentInstitution
import com.tle.common.search.DefaultSearch
import com.tle.common.searching.Search.SortType
import com.tle.common.settings.ConfigurationProperties
import com.tle.common.settings.standard.SearchSettings
import com.tle.core.events.services.EventService
import com.tle.core.freetext.index.AbstractIndexEngine.Searcher
import com.tle.core.freetext.indexer.StandardIndexer
import com.tle.core.institution.RunAsInstitution
import com.tle.core.item.dao.ItemDao
import com.tle.core.item.helper.ItemHelper
import com.tle.core.item.service.ItemService
import com.tle.core.plugins.impl.PluginServiceImpl
import com.tle.core.plugins.{AbstractPluginService, PluginService}
import com.tle.core.services.item.FreetextResult
import com.tle.core.services.user.UserPreferenceService
import com.tle.core.settings.service.ConfigurationService
import com.tle.core.zookeeper.ZookeeperService
import com.tle.freetext.{FreetextIndexConfiguration, FreetextIndexImpl, IndexedItem}
import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.search.{ChainedFilter, IndexSearcher}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, mockStatic, when}
import org.scalatest.funspec.FixtureAnyFunSpec
import org.scalatest.matchers.should._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, GivenWhenThen, Outcome}

import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.{Date, Locale, UUID}
import scala.jdk.CollectionConverters._

class ItemIndexTest
    extends FixtureAnyFunSpec
    with Matchers
    with GivenWhenThen
    with BeforeAndAfterAll
    with BeforeAndAfter {
  val inst = new Institution
  inst.setUniqueId(new scala.util.Random().nextLong)

  val collection = new ItemDefinition
  collection.setUuid(UUID.randomUUID().toString)

  val owner = "admin"

  def initialiseItemIndex(testCaseName: String): ItemIndex[FreetextResult] = {
    val freetextIndexConfiguration = new FreetextIndexConfiguration {
      override def getIndexPath: File =
        new File(Files.createTempDirectory(s"ItemIndexTest - $testCaseName").toFile.getAbsolutePath)

      override def getDefaultOperator: String = "AND"

      override def getSynchroniseMinutes: Int = 1

      override def getStopWordsFile: File = new File("/tmp/stopwords.txt")

      override def getAnalyzerLanguage: String = "en"
    }

    val mockedConfigurationService = mock(classOf[ConfigurationService])
    when(mockedConfigurationService.getProperties(any(classOf[ConfigurationProperties])))
      .thenReturn(new SearchSettings)

    val freetextIndex = new FreetextIndexImpl(
      freetextIndexConfiguration,
      mockedConfigurationService,
      mock(classOf[ItemDao]),
      mock(classOf[ItemService]),
      mock(classOf[ItemHelper]),
      mock(classOf[RunAsInstitution]),
      mock(classOf[EventService]),
      mock(classOf[ZookeeperService]),
      mock(classOf[UserPreferenceService]),
      mock(classOf[PluginService])
    )

    val itemIndex = new NormalItemIndex(freetextIndex)
    itemIndex.afterPropertiesSet()

    itemIndex
  }

  def generateIndexedItems(howMany: Int = 1,
                           itemName: String = "Test",
                           itemStatus: ItemStatus = ItemStatus.LIVE,
                           moderating: Boolean = false,
                           rating: Float = 3.5f,
                           dateModified: Date = new Date): List[IndexedItem] = {
    val indexer = new StandardIndexer

    Range(0, howMany)
      .map(key => {
        val item = new Item()
        item.setUuid(UUID.randomUUID().toString)
        item.setInstitution(inst)
        item.setItemDefinition(collection)
        item.setOwner(owner)
        item.setName(LangUtils.createTextTempLangugageBundle(itemName))
        item.setStatus(itemStatus)
        item.setModerating(moderating)
        item.setRating(rating)
        item.setDateCreated(new Date)
        item.setDateForIndex(dateModified)
        item.setDateModified(dateModified)

        val indexedItem = new IndexedItem(new ItemIdKey(key, UUID.randomUUID().toString, 1), inst)
        indexedItem.setItem(item)
        indexedItem.setAdd(true)
        indexedItem.setNewSearcherRequired(true)
        indexer.getBasicFields(indexedItem).asScala.foreach(indexedItem.getItemdoc.add)

        indexedItem
      })
      .toList
  }

  def buildDefaultSearch: DefaultSearch = {
    val search = new DefaultSearch
    // Don't get OEQ permission stuff involved so that we don't have to change and mock
    // lots of code related to permissions.
    search.setPrivilege(null)
    search
  }

  type SearchResult = Array[Document]

  // Build an anonymous class for interface Searcher and make the search method
  // return an array of documents so that we can easliy verify whether the documents
  // have correct fields and values.
  def buildSearcher(itemIndex: ItemIndex[_], searchConfig: DefaultSearch) =
    new Searcher[SearchResult] {
      override def search(searcher: IndexSearcher): SearchResult = {
        def filters =
          new ChainedFilter(itemIndex.getFilters(searchConfig).asScala.toArray, ChainedFilter.AND)

        def query = itemIndex.getQuery(searchConfig, searcher.getIndexReader, false)

        def sorter = itemIndex.getSorter(searchConfig)

        searcher.search(query, filters, 10, sorter).scoreDocs.map(d => searcher.doc(d.doc))
      }
    }

  override type FixtureParam = (ItemIndex[FreetextResult], IndexWriter, DefaultSearch)

  override def withFixture(test: OneArgTest): Outcome = {
    val itemIndex   = initialiseItemIndex(test.name)
    val indexWriter = itemIndex.getTrackingIndexWriter.getIndexWriter

    try withFixture(test.toNoArgTest(itemIndex, indexWriter, buildDefaultSearch))
    finally indexWriter.close()
  }

  override def beforeAll = {
    mockStatic(classOf[CurrentInstitution])
    when(CurrentInstitution.get()).thenReturn(inst)

    mockStatic(classOf[CurrentLocale])
    when(CurrentLocale.getLocale).thenReturn(Locale.getDefault)

    AbstractPluginService.thisService = mock(classOf[PluginServiceImpl])
  }

  describe("index manipulation") {
    def verifyDocumentNumber(itemIndex: ItemIndex[_], expected: Int) =
      itemIndex.count(buildDefaultSearch, false) shouldBe expected

    it("creates indexes for new items") { f =>
      val (itemIndex, indexWriter, _) = f
      Given("a list of Items to be indexed")
      val howMany      = 5
      val indexedItems = generateIndexedItems(howMany).asJava

      When("ItemIndex.indexBatch is invoked")
      itemIndex.indexBatch(indexedItems)
      indexWriter.commit()

      Then("indexes should be created for the Items")
      verifyDocumentNumber(itemIndex, howMany)
    }

    it("deletes indexes for Institution") { f =>
      val (itemIndex, indexWriter, _) = f

      Given("the ID of an Institution")
      val id = inst.getUniqueId

      When("ItemIndex.deleteForInstitution is invoked")
      itemIndex.deleteForInstitution(id)
      indexWriter.commit()

      Then("indexes of the Institution should be deleted")
      verifyDocumentNumber(itemIndex, 0)
    }
  }

  describe("searching") {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

    def createIndexes(itemIndex: ItemIndex[_], indexedItems: List[IndexedItem]): Unit = {
      itemIndex.indexBatch(indexedItems.asJava)
      itemIndex.getTrackingIndexWriter.getIndexWriter.commit()
    }

    describe("filtering") {
      it("supports filtering by text field") { f =>
        val (itemIndex, _, searchConfig) = f

        Given("a list of Items where Item statuses are different")
        val liveItems  = generateIndexedItems(1)
        val draftItems = generateIndexedItems(2, itemStatus = ItemStatus.DRAFT)
        createIndexes(itemIndex, liveItems ++ draftItems)

        When("Item status Live is set in the search configuration")
        searchConfig.setItemStatuses(ItemStatus.LIVE)

        Then("the search result should only return Live Items")
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        result.length shouldBe 1
        result.map(_.get(FreeTextQuery.FIELD_ITEMSTATUS)) shouldBe Array(ItemStatus.LIVE.toString)
      }

      it("supports filtering by numerical field") { f =>
        val (itemIndex, _, searchConfig) = f
        val rating                       = "5.00"

        Given("a list of Items where Item ratings are either 5 or 1")
        val highRatingItems = generateIndexedItems(1, rating = 5.0f)
        val lowRatingItems  = generateIndexedItems(2, rating = 1.0f)
        createIndexes(itemIndex, highRatingItems ++ lowRatingItems)

        When("Item rating is set to 5 in the search configuration")
        searchConfig.addMust(FreeTextQuery.FIELD_RATING, rating)

        Then("the search result should only return Items where rating is 5")
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))

        result.length shouldBe 1
        result.map(_.get(FreeTextQuery.FIELD_RATING)) shouldBe Array(rating)
      }

      it("supports filtering by boolean field") { f =>
        val (itemIndex, _, searchConfig) = f

        Given("a list of Items where one item is in moderation")
        val inModerationItems = generateIndexedItems(1, moderating = true)
        val normalItems       = generateIndexedItems(2)
        createIndexes(itemIndex, inModerationItems ++ normalItems)

        When("the flag of Moderating is set to true in the search configuration")
        searchConfig.setModerating(true)

        Then("the search result should return Items in moderation")
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        result.length shouldBe 1
        result.map(_.get(FreeTextQuery.FIELD_MODERATING)).map(_.toBoolean) shouldBe Array(true)
      }

      it("supports filtering by date range field") { f =>
        val (itemIndex, _, searchConfig) = f

        Given("a list of Items where only one item is last modified yesterday")
        val start          = dateFormatter.parse("2023-07-10")
        val end            = dateFormatter.parse("2023-07-20")
        val modifiedDate   = dateFormatter.parse("2023-07-15")
        val outOfRangeDate = dateFormatter.parse("2023-07-25")

        createIndexes(itemIndex,
                      generateIndexedItems(1, dateModified = modifiedDate) ++ generateIndexedItems(
                        2,
                        dateModified = outOfRangeDate))
        When("a date range for yesterday is set in the search configuration")
        searchConfig.setDateRange(Array(start, end))

        Then("the search result should only return Items modified yesterday")
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        result.length shouldBe 1

        val realModifiedDate =
          dateFormatter.parse(result.head.get(FreeTextQuery.FIELD_REALLASTMODIFIED))
        realModifiedDate should be > start
        realModifiedDate should be < end
      }
    }

    describe("sorting") {
      it("supports sorting by text field") { f =>
        val (itemIndex, _, searchConfig) = f

        Given("a list of Items that have different names")
        val c     = "c"
        val java  = "java"
        val scala = "scala"
        val items =
          List(java, scala, c).flatMap(itemName => generateIndexedItems(itemName = itemName))
        createIndexes(itemIndex, items)

        When("the sorting order is 'name' in the search configuration")
        searchConfig.setSortType(SortType.NAME)

        Then("the search result should be order by by Item name")
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        result.map(_.get(FreeTextQuery.FIELD_NAME)) shouldBe Array(c, java, scala)
      }

      it("supports sorting by numerical field") { f =>
        val (itemIndex, _, searchConfig) = f

        Given("a list of Items that have different ratings")
        val great = 5.0f
        val good  = 4.0f
        val ok    = 3.0f
        val items = List(ok, great, good).flatMap(rating => generateIndexedItems(rating = rating))
        createIndexes(itemIndex, items)

        When("the sorting order is 'name' in the search configuration")
        searchConfig.setSortType(SortType.RATING)

        Then("the search result should be order by by Item name")
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        result.map(_.get(FreeTextQuery.FIELD_RATING).toFloat) shouldBe Array(great, good, ok)
      }

      it("supports sorting by date field") { f =>
        val (itemIndex, _, searchConfig) = f

        Given("a list of Items that have different last modified dates")
        val monday    = dateFormatter.parse("2023-07-10")
        val tuesday   = dateFormatter.parse("2023-07-11")
        val wednesday = dateFormatter.parse("2023-07-12")
        val items = List(monday, tuesday, wednesday).flatMap(dateModified =>
          generateIndexedItems(dateModified = dateModified))
        createIndexes(itemIndex, items)

        When("the sorting order is 'name' in the search configuration")
        searchConfig.setSortType(SortType.DATEMODIFIED)

        Then("the search result should be order by by Item name")
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        val dates =
          result.map(d => dateFormatter.parse(d.get(FreeTextQuery.FIELD_REALLASTMODIFIED)))
        val isOrderedByLastModifiedDate = dates.tail
          .foldLeft((true, dates.head)) {
            case ((orderedByLastModifiedDate, prevDate), currentDate) =>
              (orderedByLastModifiedDate && prevDate.after(currentDate), currentDate)
          }
          ._1

        isOrderedByLastModifiedDate should equal(true)

      }
    }
  }

}
