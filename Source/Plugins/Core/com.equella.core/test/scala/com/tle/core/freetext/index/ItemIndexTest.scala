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

import com.dytech.devlib.PropBagEx
import com.dytech.edge.queries.FreeTextQuery
import com.tle.beans.Institution
import com.tle.beans.entity.Schema
import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.beans.item.{Item, ItemIdKey, ItemStatus}
import com.tle.common.i18n.{CurrentLocale, LangUtils}
import com.tle.common.institution.CurrentInstitution
import com.tle.common.search.DefaultSearch
import com.tle.common.searching.Search.SortType
import com.tle.common.security.SecurityConstants
import com.tle.common.settings.ConfigurationProperties
import com.tle.common.settings.standard.SearchSettings
import com.tle.common.usermanagement.user.{AbstractUserState, CurrentUser, DefaultUserState}
import com.tle.common.{NamedThreadFactory, Triple}
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
import org.apache.lucene.document.{Document, Field, FieldType}
import org.apache.lucene.index.IndexOptions
import org.apache.lucene.search.TotalHits.Relation
import org.apache.lucene.search._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyInt}
import org.mockito.Mockito._
import org.scalatest.funspec.FixtureAnyFunSpec
import org.scalatest.matchers.should._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, GivenWhenThen, Outcome}

import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, LinkedBlockingQueue}
import java.util.{Date, Locale, UUID}
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.reflect.io.Directory
import scala.util.Random

class ItemIndexTest
    extends FixtureAnyFunSpec
    with Matchers
    with GivenWhenThen
    with BeforeAndAfterAll
    with BeforeAndAfter {
  val schemaUuid      = "adfcaf58-241b-4eca-9740-6a26d1c3dd58"
  val collectionUuid  = "bdfcaf58-241b-4eca-9740-6a26d1c3dd58"
  val itemUuid        = "zzzcaf58-241b-4eca-9740-6a26d1c3dd58"
  val currentUserUuid = "sdscaf66-241b-4eca-9740-6a26d1c3dd58"

  val inst = new Institution
  inst.setUniqueId(2023L)

  val schema = new Schema
  schema.setUuid(schemaUuid)
  schema.setDefinition(
    new PropBagEx(
      "<xml><item><name field='true'></name><description field='true'></description></item></xml>"
    )
  )

  val collection = new ItemDefinition
  collection.setUuid(collectionUuid)
  collection.setSchema(schema)

  val owner = "admin"

  val indexRootDirectoryName = "ItemIndexTest"

  val commonAclEntryID = 1000L
  val ownerAclEntryID  = 1001L

  // The field format for ACL permission is "ACLx-y" where "x" is the shortcut for the permission
  // and "y" is the unique ID of the permission. The mapping between permissions and their shortcuts
  // is defined in class `ItemIndex`.
  def aclField(privilege: String, aclEntryID: Long) =
    s"${ItemIndex.convertStdPriv(privilege)}$aclEntryID"
  // The value format for ACL permission is "xxxG" where "xxx" must be a three-digit number which starts
  // from 000 and "G" stands for "Grant".
  val aclValue = "001G"

  def initialiseItemIndex(testCaseName: String): ItemIndex[FreetextResult] = {
    val freetextIndexConfiguration = new FreetextIndexConfiguration {
      override def getIndexPath: File =
        new File(
          Files
            .createTempDirectory(s"$indexRootDirectoryName - $testCaseName")
            .toFile
            .getAbsolutePath
        )

      override def getDefaultOperator: String = "AND"

      override def getSynchroniseMinutes: Int = 1

      override def getStopWordsFile: File =
        new File(s"${getClass.getResource("/itemindex/stopwords.txt").getPath}")

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

  def generateIndexedItems(
      howMany: Int = 1,
      itemName: String = "Test",
      itemStatus: ItemStatus = ItemStatus.LIVE,
      moderating: Boolean = false,
      rating: Float = 3.5f,
      dateModified: Date = new Date,
      itemDescription: String = "",
      properties: PropBagEx = new PropBagEx,
      privilege: Option[String] = None,
      itemDef: ItemDefinition = collection,
      itemUuid: String = itemUuid,
      key: Long = Random.nextLong()
  ): List[IndexedItem] = {
    val indexer = new StandardIndexer

    Range(0, howMany)
      .map(_ => {
        val item = new Item()
        item.setUuid(itemUuid)
        item.setInstitution(inst)
        item.setItemDefinition(itemDef)
        item.setOwner(owner)
        item.setName(LangUtils.createTextTempLangugageBundle(itemName))
        item.setDescription(LangUtils.createTextTempLangugageBundle(itemDescription))
        item.setStatus(itemStatus)
        item.setModerating(moderating)
        item.setRating(rating)
        item.setDateCreated(new Date)
        item.setDateForIndex(dateModified)
        item.setDateModified(dateModified)

        val indexedItem = new IndexedItem(new ItemIdKey(key, itemUuid, 1), inst)
        indexedItem.setItem(item)
        indexedItem.setItemXml(properties)
        indexedItem.setAdd(true)
        indexedItem.setNewSearcherRequired(true)
        indexer.getBasicFields(indexedItem).asScala.foreach(indexedItem.getItemdoc.add)

        privilege match {
          case Some(p) =>
            val ft = new FieldType()
            ft.setIndexOptions(IndexOptions.DOCS)
            ft.setStored(true)
            ft.setTokenized(false)
            indexedItem.getAclMap.put(
              SecurityConstants.DISCOVER_ITEM,
              List(
                new Field(aclField(p, commonAclEntryID), aclValue, ft),
                new Field(aclField(p, ownerAclEntryID), aclValue, ft)
              ).asJava
            )
            indexer.addAllFields(indexedItem.getItemdoc, indexedItem.getACLEntries(p))
          case None =>
        }

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

  /** Build an anonymous class for interface Searcher and make the search method return an array of
    * documents so that we can easily verify whether the documents have correct fields and values.
    */
  def buildSearcher(itemIndex: ItemIndex[_], searchConfig: DefaultSearch) =
    new Searcher[SearchResult] {
      override def search(searcher: IndexSearcher): SearchResult = {
        def query = itemIndex.getQuery(searchConfig, searcher.getIndexReader, false)

        def sorter = itemIndex.getSorter(searchConfig)

        searcher
          .search(query, 10, sorter)
          .scoreDocs
          .map(d => searcher.storedFields().document(d.doc))
      }
    }

  override type FixtureParam = (ItemIndex[FreetextResult], DefaultSearch)

  override def withFixture(test: OneArgTest): Outcome = {
    val itemIndex = initialiseItemIndex(test.name)

    try withFixture(test.toNoArgTest(itemIndex, buildDefaultSearch))
  }
  private def prepareMocks = {
    mockStatic(classOf[CurrentInstitution])
    when(CurrentInstitution.get()).thenReturn(inst)

    mockStatic(classOf[CurrentLocale])
    when(CurrentLocale.getLocale).thenReturn(Locale.getDefault)

    val userState: AbstractUserState = new DefaultUserState
    // User state contains a `Triple` where the first element is a list of common ACL expression and the second element is
    // a list of Owner ACL expressions and the third element is a list of Not Owner ACL expressions.
    // Not Owner ACL expressions work similarily to Owner ACL expressions, so we just add mocks for the first two lists.
    // This will test whether the mix of Common ACL expressions and Owner ACL expressions work correctly.
    userState.setAclExpressions(
      new Triple(
        java.util.Collections.singleton(commonAclEntryID),
        java.util.Collections.singleton(ownerAclEntryID),
        java.util.Collections.emptyList()
      )
    )
    mockStatic(classOf[CurrentUser])
    when(CurrentUser.getUserState).thenReturn(userState)
    when(CurrentUser.getUserID).thenReturn(currentUserUuid)

    AbstractPluginService.thisService = mock(classOf[PluginServiceImpl])
  }

  override def beforeAll = prepareMocks

  override def afterAll = {
    new File(System.getProperty("java.io.tmpdir")).listFiles
      .filter(_.isDirectory)
      .filter(_.getName.contains(indexRootDirectoryName))
      .map(new Directory(_))
      .foreach(_.deleteRecursively)
  }

  def createIndexes(itemIndex: ItemIndex[_], indexedItems: List[IndexedItem]): Unit = {
    itemIndex.indexBatch(indexedItems.asJava)
  }

  describe("index manipulation") {
    def verifyDocumentNumber(itemIndex: ItemIndex[_], expected: Int) =
      itemIndex.count(buildDefaultSearch, false) shouldBe expected

    it("creates indexes for new items") { f =>
      val (itemIndex, _) = f

      Given("a list of Items to be indexed")
      val howMany      = 5
      val indexedItems = generateIndexedItems(howMany).asJava

      When("ItemIndex.indexBatch is invoked")
      itemIndex.indexBatch(indexedItems)

      Then("indexes should be created for the Items")
      verifyDocumentNumber(itemIndex, howMany)
    }

    it("deletes indexes for Institution") { f =>
      val (itemIndex, _) = f

      Given("the ID of an Institution which already has indexes generated")
      val id = inst.getUniqueId
      itemIndex.indexBatch(generateIndexedItems(2).asJava)

      When("ItemIndex.deleteForInstitution is invoked")
      itemIndex.deleteForInstitution(id)

      Then("indexes of the Institution should be deleted")
      verifyDocumentNumber(itemIndex, 0)
    }

    it("skips indexing oversized terms") { f =>
      val (itemIndex, searchConfig) = f
      val oversizedField            = "bigvalue"
      val oversizedItemName         = "Oversized Item"

      Given("An document that has oversized terms")
      def oversizedTerm: Field = {
        val sb = new StringBuilder
        for (i <- 1 to 100000) {
          sb.append(s"hello world $i")
        }
        val fieldType = new FieldType()
        fieldType.setTokenized(false)
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
        new Field(oversizedField, sb.toString(), fieldType)
      }

      val normalItem    = generateIndexedItems()
      val oversizedItem = generateIndexedItems(itemName = oversizedItemName)
      oversizedItem.head.getItemdoc.add(oversizedTerm)

      When("ItemIndex.indexBatch is invoked")
      itemIndex.indexBatch((normalItem ++ oversizedItem).asJava)

      Then("indexes should be created without oversized terms")
      searchConfig.setQuery(oversizedItemName)
      val result: SearchResult = itemIndex.search(buildSearcher(itemIndex, searchConfig))
      result.length shouldBe 1
      result.head.get(oversizedField) shouldBe null
    }
  }

  describe("document searching") {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

    describe("basic filtering") {
      it("supports filtering by text field") { f =>
        val (itemIndex, searchConfig) = f

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
        val (itemIndex, searchConfig) = f
        val rating                    = "5.00"

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
        val (itemIndex, searchConfig) = f

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
        val (itemIndex, searchConfig) = f

        Given("a list of Items where only one item is last modified within the date range")
        val start          = dateFormatter.parse("2023-07-10")
        val end            = dateFormatter.parse("2023-07-20")
        val modifiedDate   = dateFormatter.parse("2023-07-15")
        val outOfRangeDate = dateFormatter.parse("2023-07-25")

        createIndexes(
          itemIndex,
          generateIndexedItems(1, dateModified = modifiedDate) ++ generateIndexedItems(
            2,
            dateModified = outOfRangeDate
          )
        )
        When("the search configuration uses this date range for last modified date")
        searchConfig.setDateRange(Array(start, end))

        Then("the search result should only return Items modified within the date range")
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        result.length shouldBe 1

        val realModifiedDate =
          dateFormatter.parse(result.head.get(FreeTextQuery.FIELD_REALLASTMODIFIED))
        realModifiedDate should be > start
        realModifiedDate should be < end
      }
    }

    describe("advanced filtering") {
      val newCollection = new ItemDefinition()
      newCollection.setUuid("46392820-5bce-3d29-b4b3-61131cfe20a4")

      it("supports filtering by ACL expressions") { f =>
        val (itemIndex, searchConfig) = f

        Given("two Items where the first one requires ACL 'DISCOVER_ITEM'")
        val itemName = "acl_item"
        val permissionItem = generateIndexedItems(
          itemName = itemName,
          privilege = Option(SecurityConstants.DISCOVER_ITEM)
        )
        val nonPermissionItem = generateIndexedItems()
        createIndexes(itemIndex, permissionItem ++ nonPermissionItem)

        When("ACL 'DISCOVER_ITEM' is configured in the search configuration")
        searchConfig.setPrivilege(SecurityConstants.DISCOVER_ITEM)

        Then("the search result should only return the Item that require this ACL")
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        result.map(_.get(FreeTextQuery.FIELD_NAME)) shouldBe Array(itemName)
      }

      it("supports filtering by Must clauses") { f =>
        val (itemIndex, searchConfig) = f
        val moderatingItemName        = "moderating item"

        Given("Items generated in different Collections with different Item status")
        val newCollectionDraftItem =
          generateIndexedItems(itemStatus = ItemStatus.DRAFT, itemDef = newCollection)
        val newCollectionModeratingItem = generateIndexedItems(
          itemStatus = ItemStatus.MODERATING,
          itemName = moderatingItemName,
          itemDef = newCollection
        )
        val oldCollectionItem = generateIndexedItems(itemStatus = ItemStatus.DRAFT)

        createIndexes(
          itemIndex,
          newCollectionDraftItem ++ newCollectionModeratingItem ++ oldCollectionItem
        )

        When("a search configuration has Must clauses for Collection and Item status")
        searchConfig.addMust(FreeTextQuery.FIELD_ITEMDEFID, newCollection.getUuid)
        searchConfig.addMust(FreeTextQuery.FIELD_ITEMSTATUS, List("moderating").asJava)

        Then(
          "the search result should only return Items that belong to the specified Collection and have the specified Item status"
        )
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        result.map(_.get(FreeTextQuery.FIELD_NAME)) shouldBe Array(moderatingItemName)
      }

      it("supports filtering by Must Not clauses") { f =>
        val (itemIndex, searchConfig) = f
        val draftItemName             = "draft item"

        Given("Items generated in different Collections with different Item status")
        val newCollectionDraftItem =
          generateIndexedItems(itemStatus = ItemStatus.DRAFT, itemDef = newCollection)
        val newCollectionModeratingItem =
          generateIndexedItems(itemStatus = ItemStatus.MODERATING, itemDef = newCollection)
        val oldCollectionDraftItem =
          generateIndexedItems(itemStatus = ItemStatus.DRAFT, itemName = draftItemName)
        val oldCollectionModeratingItem = generateIndexedItems(itemStatus = ItemStatus.MODERATING)

        createIndexes(
          itemIndex,
          newCollectionDraftItem ++ newCollectionModeratingItem ++ oldCollectionDraftItem ++ oldCollectionModeratingItem
        )

        When("a search configuration has multiple Must Not clause for Collection and Item status")
        searchConfig.addMustNot(FreeTextQuery.FIELD_ITEMDEFID, newCollection.getUuid)
        searchConfig.addMustNot(FreeTextQuery.FIELD_ITEMSTATUS, List("moderating").asJava)

        Then(
          "the search result should only return Items that don't belong to the specified Collection and don't have the specified Item status"
        )
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        result.map(_.get(FreeTextQuery.FIELD_NAME)) shouldBe Array(draftItemName)

      }
    }

    describe("sorting") {
      it("supports sorting by text field") { f =>
        val (itemIndex, searchConfig) = f

        Given("a list of Items that have different names")
        val c     = "c"
        val java  = "java"
        val scala = "scala"
        val items =
          List(java, scala, c).flatMap(itemName => generateIndexedItems(itemName = itemName))
        createIndexes(itemIndex, items)

        When("the sorting order is 'name' in the search configuration")
        searchConfig.setSortType(SortType.NAME)

        Then("the search result should be ordered by by Item name")
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        result.map(_.get(FreeTextQuery.FIELD_NAME)) shouldBe Array(c, java, scala)
      }

      it("supports sorting by numerical field") { f =>
        val (itemIndex, searchConfig) = f

        Given("a list of Items that have different ratings")
        val great = 5.0f
        val good  = 4.0f
        val ok    = 3.0f
        val items = List(ok, great, good).flatMap(rating => generateIndexedItems(rating = rating))
        createIndexes(itemIndex, items)

        When("the sorting order is 'rating' in the search configuration")
        searchConfig.setSortType(SortType.RATING)

        Then("the search result should be ordered by by Item rating")
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        result.map(_.get(FreeTextQuery.FIELD_RATING).toFloat) shouldBe Array(great, good, ok)
      }

      it("supports sorting by date field") { f =>
        val (itemIndex, searchConfig) = f

        Given("a list of Items that have different last modified dates")
        val monday    = dateFormatter.parse("2023-07-10")
        val tuesday   = dateFormatter.parse("2023-07-11")
        val wednesday = dateFormatter.parse("2023-07-12")
        val items = List(monday, wednesday, tuesday).flatMap(dateModified =>
          generateIndexedItems(dateModified = dateModified)
        )
        createIndexes(itemIndex, items)

        When("the sorting order is 'date modified' in the search configuration")
        searchConfig.setSortType(SortType.DATEMODIFIED)

        Then("the search result should be ordered by by date modified")
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

    describe("stemming") {
      it("supports stemming English words") { f =>
        val (itemIndex, searchConfig) = f

        Given("a list of Items that have different names")
        val items =
          List("testing", "tested", "other").flatMap(itemName =>
            generateIndexedItems(itemName = itemName)
          )
        createIndexes(itemIndex, items)

        When("the query of search configuration is stem word 'test'")
        searchConfig.setQuery("test")

        Then(
          "the search result should include all the Items where names are in different forms of 'test'"
        )
        val result = itemIndex.search(buildSearcher(itemIndex, searchConfig))
        val itemNames =
          result.map(_.get(FreeTextQuery.FIELD_NAME))
        itemNames shouldBe Array("testing", "tested")
      }
    }

    describe("stopping") {
      it("supports removing stopping words from search query") { f =>
        val (itemIndex, searchConfig) = f

        Given("a search query which multiple terms and stopping words")
        val query = "the java and scala are interesting"

        When("the query is set in the search configuration")
        searchConfig.setQuery(query)

        Then("the search API should be called with those terms without the stopping words")
        // Mock an IndexSearcher.
        val mockedSearcher = mock(classOf[IndexSearcher])
        doReturn(
          new TopFieldDocs(
            new TotalHits(0, Relation.EQUAL_TO),
            Array.empty[ScoreDoc],
            Array.empty[SortField]
          )
        )
          .when(mockedSearcher)
          .search(any(classOf[Query]), anyInt(), any(classOf[Sort]))

        // Do the search with the mocked IndexSearcher.
        buildSearcher(itemIndex, searchConfig).search(mockedSearcher)

        // Verify the query passed to the mocked IndexSearcher.
        val queryCaptor = ArgumentCaptor.forClass[Query, Query](classOf[Query])

        verify(mockedSearcher).search(
          queryCaptor.capture(),
          ArgumentCaptor.forClass[Int, Int](classOf[Int]).capture(),
          ArgumentCaptor.forClass[Sort, Sort](classOf[Sort]).capture()
        )

        val processedQuery = queryCaptor.getValue.toString
        processedQuery should (include("(name_vectored:java)^2.0 (body:java)^1.0") and
          include("(name_vectored:scala)^2.0 (body:scala)^1.0") and
          include("(name_vectored:interest)^2.0 (body:interest)^1.0"))
      }
    }

    describe("classification searching") {
      it("search classifications through schema nodes") { f =>
        val (itemIndex, searchConfig) = f
        val java8                     = "java 8"
        val java11                    = "java 11"
        val scala                     = "scala 3"
        val node                      = "/item/name"
        def properties(name: String)  = new PropBagEx(s"<xml><item><name>$name</name></item></xml>")

        Given(s"a list of Items where the schema node for Item name is $node")
        val java8Item  = generateIndexedItems(itemName = java8, properties = properties(java8))
        val java11Item = generateIndexedItems(itemName = java11, properties = properties(java11))
        val scalaItem  = generateIndexedItems(itemName = scala, properties = properties(scala))

        createIndexes(itemIndex, java8Item ++ java11Item ++ scalaItem)

        When("a classification search is performed to search for this schema node and a query")
        searchConfig.setQuery("java")
        val result = itemIndex.matrixSearch(searchConfig, List("/item/name").asJava, false, false)

        Then("the search result should only return classifications that match the query")
        result.getEntries.size() shouldBe 2

        result.getEntries.asScala.flatMap(_.getFieldValues.asScala).toArray shouldBe Array(
          java11,
          java8
        )
      }
    }
  }

  describe("term searching") {
    it("supports making a term suggestion") { f =>
      val (itemIndex, _) = f
      Given("an Item where the keyword is in the description")
      createIndexes(itemIndex, generateIndexedItems(11, itemDescription = "This is an apple."))

      When("the search query is partial of the keyword")
      val suggestion = itemIndex.suggestTerm(buildDefaultSearch, "appl", false)

      Then("the search result should return the full word of the keyword")
      suggestion shouldBe "apple"
    }
  }

  describe("concurrency") {
    it("supports writing and reading indexes concurrently") { f =>
      val (itemIndex, searchConfig) = f

      // We have 10000 Items to be contributed.
      val range = Range(0, 10000)

      // Blocking queue for new Items.
      val newItems = new LinkedBlockingQueue[String](
        range.map(_ => Random.alphanumeric take 10 mkString "").toList.asJava
      )

      // Blocking queue for new indexes.
      val indexQueue = new LinkedBlockingQueue[String](10)

      // Counter used to count successful index reading.
      val successfulReading: AtomicInteger = new AtomicInteger(0)

      // Thread pools for Index writing and reading. Allocate half of the available processors to each pool.
      val processors = Runtime.getRuntime.availableProcessors
      val writingPool = ExecutionContext.fromExecutor(
        Executors.newScheduledThreadPool(processors / 2, new CustomThreadFactory("writing pool"))
      )
      val readingPool = ExecutionContext.fromExecutor(
        Executors.newScheduledThreadPool(processors / 2, new CustomThreadFactory("reading pool"))
      )

      // Initialise the mocks in one thread.
      def initialise(): Unit = {
        if (!CustomThreadFactory.isInitialized.get()) {
          prepareMocks
          CustomThreadFactory.isInitialized.set(true)
        }
      }

      Given("A task to write indexes for 10000 Items in a dedicated writing thread pool")
      def writingTask =
        range.map(_ =>
          Future {
            initialise()

            val itemName = newItems.take
            createIndexes(
              itemIndex,
              generateIndexedItems(itemName = itemName, itemUuid = UUID.randomUUID().toString)
            )
            indexQueue.put(itemName)
          }(writingPool)
        )

      Given("A task to read indexes for 10000 Items in the dedicated reading thread pool")
      def readingTask =
        range.map(_ =>
          Future {
            initialise()

            @tailrec
            def search(itemName: String, config: DefaultSearch, retries: Int): Int = {
              val result = itemIndex.search(buildSearcher(itemIndex, config))
              result.length match {
                case 1 =>
                  successfulReading.incrementAndGet()
                case 0 =>
                  if (retries < 3) {
                    // Sleep a little while as the indexes were not ready in last attempt.
                    Thread.sleep(100)
                    search(itemName, config, retries + 1)
                  } else {
                    throw new RuntimeException(
                      s"Tried 3 times to read indexes for Item $itemName but still got nothing back"
                    )
                  }
                case incorrect =>
                  throw new RuntimeException(
                    s"Found $incorrect Items for $itemName but there should be one only"
                  )
              }
            }

            val itemName = indexQueue.take
            searchConfig.setQuery(itemName)
            search(itemName, searchConfig, 0)
          }(readingPool)
        )

      When("the two tasks are executed simultaneously")
      // Run the task and wait for the result.
      Await.result(
        Future
          .sequence(writingTask ++ readingTask),
        scala.concurrent.duration.Duration.Inf
      )

      Then("Each task should complete 10000 computations successfully")
      // If reading is all good, then writing must also be good.
      successfulReading.get() shouldBe range.end
    }
  }
}

/** Thread factory used to support custom naming and initialisation of mocks.
  */
class CustomThreadFactory(name: String) extends NamedThreadFactory(name)

object CustomThreadFactory {
  val isInitialized: ThreadLocal[Boolean] = ThreadLocal.withInitial(() => false)
}
