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

package com.tle.web.api.search

import cats.Semigroup
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNec
import cats.implicits._
import com.dytech.devlib.PropBagEx
import com.dytech.edge.exceptions.{BadRequestException, DRMException}
import com.tle.beans.entity.DynaCollection
import com.tle.beans.item.ItemStatus.{MODERATING, REJECTED, REVIEW}
import com.tle.beans.item.{Comment, Item, ItemIdKey, ItemStatus}
import com.tle.common.Check
import com.tle.common.beans.exception.NotFoundException
import com.tle.common.collection.AttachmentConfigConstants
import com.tle.common.interfaces.SimpleI18NString
import com.tle.common.search.whereparser.WhereParser
import com.tle.common.search.{DefaultSearch, PresetSearch}
import com.tle.common.searching.SortField
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.freetext.queries.FreeTextBooleanQuery
import com.tle.core.item.security.ItemSecurityConstants
import com.tle.core.item.serializer.ItemSerializerItemBean
import com.tle.core.item.serializer.ItemSerializerService.SerialisationCategory
import com.tle.core.security.ACLChecks.hasAcl
import com.tle.core.services.item.{FreetextResult, FreetextSearchResults}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.favourite.model.Bookmark
import com.tle.web.api.interfaces.beans.AbstractExtendableBean
import com.tle.web.api.item.equella.interfaces.beans.{DisplayField, EquellaItemBean}
import com.tle.web.api.item.impl.ItemLinkServiceImpl
import com.tle.web.api.item.interfaces.beans.AttachmentBean
import com.tle.web.api.search.AttachmentHelper.{
  isViewable,
  sanitiseAttachmentBean,
  toSearchResultAttachment
}
import com.tle.web.api.search.model.AdvancedSearchParameters.buildAdvancedSearchCriteria
import com.tle.web.api.search.model._

import java.time.format.DateTimeParseException
import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneId}
import java.util
import java.util.Date
import scala.jdk.CollectionConverters._

/** This object provides functions that help validate a variety of search criteria(e.g. the UUID of
  * a collection and the UUID of an advanced search) and create an instance of search and
  * search-related objects.
  *
  * It also provides functions that can convert EquellaItemBean and AttachmentBean to
  * SearchResultItem and SearchResultAttachment, respectively.
  */
object SearchHelper {
  implicit val freeTextBooleanQuerySemigroup: Semigroup[FreeTextBooleanQuery] =
    (x: FreeTextBooleanQuery, y: FreeTextBooleanQuery) => x.add(y)

  val privileges = Array(ItemSecurityConstants.VIEW_ITEM)

  /** Execute a search with provided search criteria.
    *
    * @param defaultSearch
    *   A set of search criteria
    * @param start
    *   The first record of a search result.
    * @param length
    *   The maximum number of search results, or -1 for all.
    * @param searchAttachments
    *   Whether to search attachments.
    */
  def search(
      defaultSearch: DefaultSearch,
      start: Int,
      length: Int,
      searchAttachments: Boolean
  ): FreetextSearchResults[FreetextResult] =
    LegacyGuice.freeTextService.search(defaultSearch, start, length, searchAttachments)

  /** Create a new search with search criteria. The search criteria include two parts.
    *   1. General criteria provided by `SearchPayload`. 2. Advanced search criteria defined by a
    *      list of `WizardControlFieldValue`.
    *
    * Note: Preset Search is used for searching a hierarchy topic result. If a Preset search is
    * provided, some general criteria are pre-defined and as a result those from the query params
    * will be ignored.
    *
    * @param payload
    *   Search payload.
    * @param fieldValues
    *   An option of an array of `WizardControlFieldValue`.
    * @param presetSearch
    *   Option of pre-defined search criteria such as Hierarchy topic. Default to None.
    * @return
    *   An instance of DefaultSearch
    */
  def createSearch(
      payload: SearchPayload,
      fieldValues: Option[Array[WizardControlFieldValue]] = None,
      presetSearch: Option[PresetSearch] = None
  ): DefaultSearch = {
    val search = presetSearch.getOrElse(new DefaultSearch)

    search.setUseServerTimeZone(true)
    search.setQuery(payload.query.orNull)
    search.setOwner(payload.owner.orNull)
    search.setMimeTypes(payload.mimeTypes.toList.asJava)

    search.setSortFields(handleOrder(payload))

    presetSearch match {
      case Some(
            _
          ) => // Collections and itemStatus have been added to the search criteria so ignore the query param for Collections and Item statuses.
      case None =>
        val itemStatus = if (payload.status.isEmpty) None else Some(payload.status.toList.asJava)
        val collectionUuids = handleCollections(payload.advancedSearch, payload.collections)
        search.setCollectionUuids(collectionUuids.orNull)
        search.setItemStatuses(itemStatus.orNull)
    }

    setDateRange(search, payload.modifiedAfter, payload.modifiedBefore)
    val dynaCollectionQuery: Option[FreeTextBooleanQuery] = handleDynaCollection(
      payload.dynaCollection
    )
    val whereQuery: Option[FreeTextBooleanQuery] = payload.whereClause.map(WhereParser.parse)
    val advSearchCriteria: Option[FreeTextBooleanQuery] =
      fieldValues.map(buildAdvancedSearchCriteria)

    val freeTextQuery: FreeTextBooleanQuery =
      List(dynaCollectionQuery, whereQuery, advSearchCriteria)
        .reduce(_ |+| _)
        .orNull

    search.setFreeTextQuery(freeTextQuery)

    handleMusts(payload.musts) foreach { case (field, value) =>
      search.addMust(field, value.asJavaCollection)
    }

    search
  }

  /** Set the date range to the search. If either start or end date is defined, sets the search’s
    * dateRange to [startDateTime, endDateTime].
    *
    * @param search
    *   An instance of DefaultSearch which the supplied date range is applied to.
    * @param start
    *   An optional string representing the start date in ISO format (yyyy-MM-dd).
    * @param end
    *   An optional string representing the end date in ISO format (yyyy-MM-dd).
    */
  def setDateRange(
      search: DefaultSearch,
      start: Option[String],
      end: Option[String]
  ): Unit = {
    // The time of start should be '00:00:00' whereas the time of end should be '23:59:59'.
    val startDate = parseDateString(start, LocalTime.MIN)
    val endDate   = parseDateString(end, LocalTime.MAX)

    if (startDate.isDefined || endDate.isDefined) {
      search.setDateRange(Array(startDate.orNull, endDate.orNull))
    }
  }

  /** Using a number of the fields from `params` determines what is the requested sort order and
    * captures that in a `SortField` which is used in setting the order in DefaultSearch. However if
    * none of the specified orders apply - or the order param is absent or not matched - then a
    * standard default order is applied.
    *
    * @param params
    *   the parameters supplied to a search request
    * @return
    *   the definition of ordering to be used with DefaultSearch
    */
  def handleOrder(params: SearchPayload): SortField = {
    val providedOrder = params.order.map(_.toLowerCase)

    def getDefaultSortField = DefaultSearch
      .getOrderType(providedOrder.orNull, params.query.orNull)
      .getSortField

    // Get sort field for extension sort orders (i.e. Task or Bookmark).
    def getExtensionSortField(id: String) =
      TaskSortOrder(id) orElse BookmarkSortOrder(id)

    val order: SortField =
      providedOrder.flatMap(getExtensionSortField).getOrElse(getDefaultSortField)

    if (params.reverseOrder) order.reversed() else order
  }

  /** Return a free text query based on what dynamic collection uuid is provided.
    * @param dynaCollectionUuid
    *   The uuid of a dynamic collection.
    * @return
    *   An option which wraps an instance of FreeTextBooleanQuery.
    */
  def handleDynaCollection(dynaCollectionUuid: Option[String]): Option[FreeTextBooleanQuery] = {
    dynaCollectionUuid
      .filter(_.nonEmpty)
      .flatMap(uuid => {
        val virtualDynaColl = LegacyGuice.dynaCollectionService.getByCompoundId(uuid)
        Option(virtualDynaColl) match {
          case Some(v) =>
            val dynaCollection: DynaCollection = v.getVt
            val uuidAndVirtual: Array[String]  = uuid.split(":")
            val virtual = if (uuidAndVirtual.length > 1) uuidAndVirtual(1) else null
            Some(LegacyGuice.dynaCollectionService.getSearchClause(dynaCollection, virtual))
          case None =>
            throw new NotFoundException(s"No dynamic collection matching UUID $uuid")
        }
      })
  }

  /** Parse a string to a new instance of Date in the format of "yyyy-MM-dd".
    * @param dateString
    *   The string to parse.
    * @param time
    *   The time added to a date.
    * @return
    *   An Option which wraps an instance of Date, combining the successfully parsed dateString and
    *   provided time (based on the system's default timezone).
    */
  def parseDateString(dateString: Option[String], time: LocalTime): Option[Date] = {
    dateString
      .filter(_.nonEmpty)
      .map(date =>
        try {
          val dateTime = LocalDateTime.of(LocalDate.parse(date), time)
          // Need to convert back to util.date to work compatibly with old methods.
          Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant)
        } catch {
          case _: DateTimeParseException => throw new BadRequestException(s"Invalid date: $date")
        }
      )
  }

  /** Return a list of Collection IDs, depending on if Advanced search is provided or not.
    * @param advancedSearch
    *   The UUID of an Advanced search.
    * @param collections
    *   A list of Collection IDs.
    * @return
    *   An option which wraps a list of Collection IDs.
    */
  def handleCollections(
      advancedSearch: Option[String],
      collections: Array[String]
  ): Option[java.util.Collection[String]] = {
    def checkCollection(collection: String): ValidatedNec[String, String] =
      Option(LegacyGuice.itemDefinitionService.getByUuid(collection))
        .toValidNec(s"No collection matching UUID $collection")
        .map(_.getUuid)

    advancedSearch
      .filter(_.nonEmpty)
      .map(adSearch =>
        Option(LegacyGuice.powerSearchService.getByUuid(adSearch))
          .map(ps => ps.getItemdefs.asScala.map(_.getUuid).toList)
          .toValidNec(s"No advanced search UUID matching $adSearch")
      )
      .getOrElse(
        Option
          .when(collections.nonEmpty)(
            collections
              .map(checkCollection)
              .toList
              .sequence
          )
          .getOrElse(Valid(List.empty[String]))
      ) match {
      case Invalid(err) => throw new NotFoundException(err.mkString_("\n"))
      case Valid(value) => Option.when(value.nonEmpty)(value.asJava)
    }
  }

  /** Takes a list of colon delimited strings (i.e. key:value), and splits them to build up a map.
    * Entries in the map can have multiple values, so each additional `key:value` will simply append
    * to the existing entry(key).
    *
    * @param musts
    *   a list of colon delimited strings to be split
    * @return
    *   the processed strings, ready for calls into `DefaultSearch.addMusts` or throws if there was
    *   an issue processing the strings
    */
  def handleMusts(musts: Array[String]): Map[String, List[String]] = {
    val delimiter      = ':'
    val fieldFormat    = s"([^$delimiter]+)" // Can contain any character except the delimiter.
    val valueFormat    = s"(.+)"             // Can contain any character.
    val mustExprFormat = s"$fieldFormat$delimiter$valueFormat".r

    def valid = (xs: Array[String]) => xs.forall(s => s.matches(mustExprFormat.regex))

    if (valid(musts)) {
      val initialEmptyMap = Map[String, List[String]]()
      musts.foldLeft(initialEmptyMap)((result, mustExpr) => {
        val mustExprFormat(k, v) = mustExpr
        result.get(k) match {
          case Some(existing) => result ++ Map(k -> (existing :+ v))
          case None           => result ++ Map(k -> List(v))
        }
      })
    } else {
      throw new BadRequestException("Provided 'musts' expression(s) was incorrectly formatted.")
    }
  }

  /** Create a serializer for ItemBean.
    */
  def createSerializer(itemIds: List[ItemIdKey]): ItemSerializerItemBean = {
    val ids = itemIds.map(_.getKey.asInstanceOf[java.lang.Long]).asJavaCollection
    val categories: java.util.Collection[SerialisationCategory] = List(
      SerialisationCategory.ALL
    ).asJavaCollection
    LegacyGuice.itemSerializerService.createItemBeanSerializer(
      ids,
      categories,
      false,
      privileges: _*
    )
  }

  /** Convert a SearchItem to an instance of SearchResultItem.
    *
    * @param item
    *   Details of an item to convert.
    * @param includeAttachments
    *   Controls whether to populate the 'attachments' property as that process can be intensive and
    *   slow down searches.
    * @return
    *   The result of converting `item` to a `SearchResultItem`.
    */
  def convertToItem(item: SearchItem, includeAttachments: Boolean = true): SearchResultItem = {
    val key  = item.idKey
    val bean = item.bean
    lazy val sanitisedAttachmentBeans =
      Option(bean.getAttachments).map(_.asScala.map(sanitiseAttachmentBean).toList)
    val rawItem = LegacyGuice.itemService.getUnsecureIfExists(key)

    SearchResultItem(
      uuid = key.getUuid,
      version = key.getVersion,
      name = Option(bean.getName),
      description = Option(bean.getDescription),
      status = bean.getStatus,
      createdDate = bean.getCreatedDate,
      modifiedDate = bean.getModifiedDate,
      collectionId = bean.getCollection.getUuid,
      commentCount = getItemCommentCount(key),
      starRatings = bean.getRating,
      attachmentCount = LegacyGuice.itemService.getAttachmentCountForItem(key),
      attachments =
        if (includeAttachments) convertToAttachment(sanitisedAttachmentBeans, key) else None,
      thumbnail = bean.getThumbnail,
      thumbnailDetails = getThumbnailDetails(key, bean),
      displayFields = getDisplayFields(bean),
      displayOptions = Option(bean.getDisplayOptions),
      keywordFoundInAttachment = item.keywordFound,
      links = getLinksFromBean(bean),
      bookmark = getBookmark(key),
      isLatestVersion = isLatestVersion(key),
      drmStatus = getItemDrmStatus(rawItem),
      moderationDetails = getModerationDetails(rawItem)
    )
  }

  /** Convert a list of AttachmentBean to a list of SearchResultAttachment
    */
  def convertToAttachment(
      attachmentBeans: Option[List[AttachmentBean]],
      itemKey: ItemIdKey
  ): Option[List[SearchResultAttachment]] = {
    lazy val hasRestrictedAttachmentPrivileges: Boolean =
      hasAcl(AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS)

    attachmentBeans.map(beans =>
      beans
        // Filter out restricted attachments if the user does not have permissions to view them
        .filter(isViewable(hasRestrictedAttachmentPrivileges))
        .map(toSearchResultAttachment(itemKey, _))
    )
  }

  def getItemDrmStatus(rawItem: Item): Option[DrmStatus] = {
    for {
      item     <- Option(rawItem)
      settings <- Option(item.getDrmSettings)
      termsAccepted =
        try {
          LegacyGuice.drmService.hasAcceptedOrRequiresNoAcceptance(item, false, false)
        } catch {
          // This exception is only thrown when the DRM has maximum number of acceptance allowable times.
          case _: DRMException => false
        }
      isAuthorised =
        try {
          LegacyGuice.drmService.isAuthorised(item, CurrentUser.getUserState.getIpAddress)
          true
        } catch {
          case _: DRMException => false
        }
    } yield {
      DrmStatus(termsAccepted, isAuthorised, settings.isAllowSummary)
    }
  }

  def getModerationDetails(rawItem: Item): Option[ModerationDetails] =
    for {
      item <- Option(rawItem) if Array(REJECTED, REVIEW, MODERATING).contains(item.getStatus)
      mod  <- Option(item.getModeration)
    } yield ModerationDetails(mod.getLastAction, mod.getStart, Option(mod.getRejectedMessage))

  def getItemComments(key: ItemIdKey): Option[java.util.List[Comment]] =
    Option(LegacyGuice.itemCommentService.getCommentsWithACLCheck(key, null, null, -1))

  def getItemCommentCount(key: ItemIdKey): Option[Integer] =
    Option(LegacyGuice.itemCommentService.getCommentCountWithACLCheck(key))

  /** Extract the value of 'links' from the 'extras' of AbstractExtendableBean.
    */
  def getLinksFromBean[T <: AbstractExtendableBean](bean: T): util.Map[String, String] =
    bean.get("links").asInstanceOf[java.util.Map[String, String]]

  /** Get the Bookmark linking to the Item.
    * @param itemID
    *   Unique Item ID
    */
  def getBookmark(itemID: ItemIdKey): Option[Bookmark] =
    Option(LegacyGuice.bookmarkService.getByItem(itemID)).map(Bookmark(_))

  /** Check whether a specific version is the latest version
    * @param itemID
    *   Unique Item ID
    * @return
    *   True if the version is the latest one
    */
  def isLatestVersion(itemID: ItemIdKey): Boolean =
    itemID.getVersion == LegacyGuice.itemService.getLatestVersion(itemID.getUuid)

  def getThumbnailDetails(
      itemKey: ItemIdKey,
      itemBean: EquellaItemBean
  ): Option[ThumbnailDetails] = {
    if (itemBean.getThumbnail == "none") {
      return None
    }

    lazy val hasRestrictedAttachmentPrivileges: Boolean =
      hasAcl(AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS)

    def determineThumbnailAttachment(
        attachmentBeans: List[AttachmentBean]
    ): Option[AttachmentBean] =
      prioritizeAttachments(attachmentBeans, getConfiguredThumbnailUuid(itemBean.getThumbnail))
        .find(isViewable(hasRestrictedAttachmentPrivileges))

    def prioritizeAttachments(
        attachments: List[AttachmentBean],
        preferredUuid: Option[String]
    ): List[AttachmentBean] = preferredUuid match {
      case Some(uuid) =>
        val (preferred, others) = attachments.partition(_.getUuid == uuid)
        preferred ++ others
      case None =>
        attachments
    }

    def determineThumbnailLink(searchResultAttachment: SearchResultAttachment): Option[String] =
      Option(searchResultAttachment)
        .filterNot(_.brokenAttachment)
        .filter(a =>
          (a.attachmentType, a.mimeType, a.hasGeneratedThumb) match {
            // If a file attachment has a generatedThumb we use it
            case ("file", _, Some(true)) => true
            // If a 'custom/resource' (i.e. oEQ resource attachment) is of a mimeType other than
            // those specified, we use the server provided thumbnail
            case ("custom/resource", Some(mimeType), _)
                if !List("equella/item", "equella/link", "text/html").contains(mimeType) =>
              true
            // For the custom attachment types pointing to external systems, we use the server
            // provided thumbnail - which is typically a thumbnail provided by those systems
            case (custom, _, _)
                if custom
                  .startsWith("custom/") && List("flickr", "googlebook", "kaltura", "youtube")
                  .contains(custom.split("/").last) =>
              true
            // For all others, no thumbnail is provided
            case _ => false
          }
        )
        .flatMap(_.links.asScala.get(ItemLinkServiceImpl.REL_THUMB))

    Option(itemBean.getAttachments)
      .map(_.asScala.toList)
      .flatMap(determineThumbnailAttachment)
      .map(sanitiseAttachmentBean)
      .map(toSearchResultAttachment(itemKey, _))
      .map(a =>
        ThumbnailDetails(
          attachmentType = a.attachmentType,
          mimeType = a.mimeType,
          link = determineThumbnailLink(a)
        )
      )
  }

  def getConfiguredThumbnailUuid(thumbnail: String): Option[String] = {
    val configuredThumbnailPrefix = "custom:"
    Option(thumbnail)
      .filter(_.startsWith(configuredThumbnailPrefix))
      .map(_.stripPrefix(configuredThumbnailPrefix))
  }

  def getDisplayFields(bean: EquellaItemBean): List[DisplayField] = {
    def standardDisplayFields = bean.getDisplayFields.asScala.toList

    def customDisplayFields: Option[DisplayField] = {
      def personalTags =
        Option(bean.getMetadata)
          .map(new PropBagEx(_).getNode("keywords"))
          .filter(!Check.isEmpty(_))
          .map(new SimpleI18NString(_)) // DisplayField requires its value to be an I18N string.
          .map(new DisplayField("node", new SimpleI18NString("Tags"), _))

      Option.when(bean.getStatus == ItemStatus.PERSONAL.toString)(personalTags).flatten
    }

    standardDisplayFields ++ customDisplayFields
  }

  /** Get a list highlighted text from the provided query.
    *
    * @param query
    *   An optional query string to parse and extract highlighted text from.
    */
  def getHighlightedList(query: Option[String]): List[String] =
    new DefaultSearch.QueryParser(query.orNull).getHilightedList.asScala.toList
}
