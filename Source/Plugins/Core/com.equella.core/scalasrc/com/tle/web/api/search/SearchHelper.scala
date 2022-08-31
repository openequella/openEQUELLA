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
import com.tle.common.search.DefaultSearch
import com.tle.common.search.whereparser.WhereParser
import com.tle.common.searching.SortField
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.freetext.queries.FreeTextBooleanQuery
import com.tle.core.item.security.ItemSecurityConstants
import com.tle.core.item.serializer.{ItemSerializerItemBean, ItemSerializerService}
import com.tle.core.security.ACLChecks.hasAcl
import com.tle.core.services.item.{FreetextResult, FreetextSearchResults}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.interfaces.beans.AbstractExtendableBean
import com.tle.web.api.item.equella.interfaces.beans.{DisplayField, EquellaItemBean}
import com.tle.web.api.item.impl.ItemLinkServiceImpl
import com.tle.web.api.item.interfaces.beans.AttachmentBean
import com.tle.web.api.search.AttachmentHelper.{
  isViewable,
  sanitiseAttachmentBean,
  toSearchResultAttachment
}
import com.tle.web.api.search.model.AdditionalSearchParameters.buildAdvancedSearchCriteria
import com.tle.web.api.search.model._

import java.time.format.DateTimeParseException
import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneId}
import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

/**
  * This object provides functions that help validate a variety of search criteria(e.g.
  * the UUID of a collection and the UUID of an advanced search) and create an instance
  * of search and search-related objects.
  *
  * It also provides functions that can convert EquellaItemBean and AttachmentBean to
  * SearchResultItem and SearchResultAttachment, respectively.
  */
object SearchHelper {
  implicit val freeTextBooleanQuerySemigroup: Semigroup[FreeTextBooleanQuery] =
    (x: FreeTextBooleanQuery, y: FreeTextBooleanQuery) => x.add(y)

  val privileges = Array(ItemSecurityConstants.VIEW_ITEM)

  /**
    * Execute a search with provided search criteria.
    *
    * @param defaultSearch A set of search criteria
    * @param start The first record of a search result.
    * @param length The maximum number of search results, or -1 for all.
    * @param searchAttachments Whether to search attachments.
    */
  def search(defaultSearch: DefaultSearch,
             start: Int,
             length: Int,
             searchAttachments: Boolean): FreetextSearchResults[FreetextResult] =
    LegacyGuice.freeTextService.search(defaultSearch, start, length, searchAttachments)

  /**
    * Create a new search with search criteria. The search criteria include two parts.
    * 1. General criteria provided by `SearchParam`.
    * 2. Advanced search criteria defined by a list of `WizardControlFieldValue`.
    *
    * @param params Search parameters.
    * @param fieldValues An option of an array of `WizardControlFieldValue`.
    * @return An instance of DefaultSearch
    */
  def createSearch(params: SearchParam,
                   fieldValues: Option[Array[WizardControlFieldValue]] = None): DefaultSearch = {
    val search = new DefaultSearch
    search.setUseServerTimeZone(true)
    search.setQuery(params.query)
    search.setOwner(params.owner)
    search.setMimeTypes(params.mimeTypes.toList.asJava)

    search.setSortFields(handleOrder(params))

    val collectionUuids = handleCollections(params.advancedSearch, params.collections)
    search.setCollectionUuids(collectionUuids.orNull)

    val itemStatus = if (params.status.isEmpty) None else Some(params.status.toList.asJava)
    search.setItemStatuses(itemStatus.orNull)

    // The time of start should be '00:00:00' whereas the time of end should be '23:59:59'.
    val modifiedAfter  = handleModifiedDate(params.modifiedAfter, LocalTime.MIN)
    val modifiedBefore = handleModifiedDate(params.modifiedBefore, LocalTime.MAX)
    if (modifiedBefore.isDefined || modifiedAfter.isDefined) {
      search.setDateRange(Array(modifiedAfter.orNull, modifiedBefore.orNull))
    }
    val dynaCollectionQuery: Option[FreeTextBooleanQuery] = handleDynaCollection(
      params.dynaCollection)
    val whereQuery: Option[FreeTextBooleanQuery] = Option(params.whereClause).map(WhereParser.parse)
    val advSearchCriteria: Option[FreeTextBooleanQuery] =
      fieldValues.map(buildAdvancedSearchCriteria)

    val freeTextQuery: FreeTextBooleanQuery =
      List(dynaCollectionQuery, whereQuery, advSearchCriteria)
        .reduce(_ |+| _)
        .orNull

    search.setFreeTextQuery(freeTextQuery)

    handleMusts(params.musts) foreach {
      case (field, value) => search.addMust(field, value.asJavaCollection)
    }

    search
  }

  /**
    * Using a number of the fields from `params` determines what is the requested sort order and
    * captures that in a `SortField` which is used in setting the order in DefaultSearch. However
    * if none of the specified orders apply - or the order param is absent or not matched - then
    * a standard default order is applied.
    *
    * @param params the parameters supplied to a search request
    * @return the definition of ordering to be used with DefaultSearch
    */
  def handleOrder(params: SearchParam): SortField = {
    val providedOrder = Option(params.order).map(_.toLowerCase)
    val order: SortField = providedOrder.flatMap(TaskSortOrder.apply) getOrElse DefaultSearch
      .getOrderType(providedOrder.orNull, params.query)
      .getSortField

    if (params.reverseOrder) order.reversed() else order
  }

  /**
    * Return a free text query based on what dynamic collection uuid is provided.
    * @param dynaCollectionUuid The uuid of a dynamic collection.
    * @return An option which wraps an instance of FreeTextBooleanQuery.
    */
  def handleDynaCollection(dynaCollectionUuid: String): Option[FreeTextBooleanQuery] = {
    if (Check.isEmpty(dynaCollectionUuid)) {
      return None
    }
    val virtualDynaColl = LegacyGuice.dynaCollectionService.getByCompoundId(dynaCollectionUuid)
    Option(virtualDynaColl) match {
      case Some(v) =>
        val dynaCollection: DynaCollection = v.getVt
        val uuidAndVirtual: Array[String]  = dynaCollectionUuid.split(":")
        val virtual                        = if (uuidAndVirtual.length > 1) uuidAndVirtual(1) else null
        Some(LegacyGuice.dynaCollectionService.getSearchClause(dynaCollection, virtual))
      case None =>
        throw new NotFoundException(s"No dynamic collection matching UUID $dynaCollectionUuid")
    }
  }

  /**
    * Parse a string to a new instance of Date in the format of "yyyy-MM-dd".
    * @param dateString The string to parse.
    * @param time The time added to a date.
    * @return An Option which wraps an instance of Date, combining the successfully parsed dateString and provided time (based on the system's default timezone).
    */
  def handleModifiedDate(dateString: String, time: LocalTime): Option[Date] = {
    if (Check.isEmpty(dateString)) {
      return None
    }
    try {
      val dateTime = LocalDateTime.of(LocalDate.parse(dateString), time)
      //Need to convert back to util.date to work compatibly with old methods.
      Some(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant))
    } catch {
      case _: DateTimeParseException => throw new BadRequestException(s"Invalid date: $dateString")
    }
  }

  /**
    * Return a list of Collection IDs, depending on if Advanced search is provided or not.
    * @param advancedSearch The UUID of an Advanced search.
    * @param collections A list of Collection IDs.
    * @return An option which wraps a list of Collection IDs.
    */
  def handleCollections(advancedSearch: String,
                        collections: Array[String]): Option[java.util.Collection[String]] = {
    if (!Check.isEmpty(advancedSearch)) {
      Option(LegacyGuice.powerSearchService.getByUuid(advancedSearch)) match {
        case Some(ps) =>
          var collectionUuids = ListBuffer[String]()
          ps.getItemdefs.asScala.foreach(collectionUuids += _.getUuid)
          return Some(collectionUuids.toList.asJava)
        case None =>
          throw new NotFoundException(s"No advanced search UUID matching $advancedSearch")
      }
    }

    if (collections.isEmpty) {
      return None
    }

    val collectionIds = ListBuffer[String]()
    collections.foreach(c =>
      Option(LegacyGuice.itemDefinitionService.getByUuid(c)) match {
        case Some(_) => collectionIds += c
        case None    => throw new NotFoundException(s"No collection UUID matching $c")
    })
    Some(collectionIds.toList.asJava)
  }

  /**
    * Takes a list of colon delimited strings (i.e. key:value), and splits them to build up a map.
    * Entries in the map can have multiple values, so each additional `key:value` will simply append
    * to the existing entry(key).
    *
    * @param musts a list of colon delimited strings to be split
    * @return the processed strings, ready for calls into `DefaultSearch.addMusts` or throws if there
    *         was an issue processing the strings
    */
  def handleMusts(musts: Array[String]): Map[String, List[String]] = {
    val delimiter         = ':'
    val oneOrMoreNonDelim = s"([^$delimiter]+)"
    val mustExprFormat    = s"$oneOrMoreNonDelim$delimiter$oneOrMoreNonDelim".r

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

  /**
    * Create a serializer for ItemBean.
    */
  def createSerializer(itemIds: List[ItemIdKey]): ItemSerializerItemBean = {
    val ids      = itemIds.map(_.getKey.asInstanceOf[java.lang.Long]).asJavaCollection
    val category = List(ItemSerializerService.CATEGORY_ALL).asJavaCollection
    LegacyGuice.itemSerializerService.createItemBeanSerializer(ids, category, false, privileges: _*)
  }

  /**
    * Convert a SearchItem to an instance of SearchResultItem.
    *
    * @param item Details of an item to convert.
    * @param includeAttachments Controls whether to populate the 'attachments' property as that
    *                           process can be intensive and slow down searches.
    * @return The result of converting `item` to a `SearchResultItem`.
    */
  def convertToItem(item: SearchItem, includeAttachments: Boolean = true): SearchResultItem = {
    val key  = item.idKey
    val bean = item.bean
    val sanitisedAttachmentBeans =
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
      attachmentCount = Option(bean.getAttachments).map(_.size).getOrElse(0),
      attachments =
        if (includeAttachments) convertToAttachment(sanitisedAttachmentBeans, key) else None,
      thumbnail = bean.getThumbnail,
      thumbnailDetails = getThumbnailDetails(sanitisedAttachmentBeans, key),
      displayFields = getDisplayFields(bean),
      displayOptions = Option(bean.getDisplayOptions),
      keywordFoundInAttachment = item.keywordFound,
      links = getLinksFromBean(bean),
      bookmarkId = getBookmarkId(key),
      isLatestVersion = isLatestVersion(key),
      drmStatus = getItemDrmStatus(rawItem),
      moderationDetails = getModerationDetails(rawItem),
    )
  }

  /**
    * Convert a list of AttachmentBean to a list of SearchResultAttachment
    */
  def convertToAttachment(attachmentBeans: Option[List[AttachmentBean]],
                          itemKey: ItemIdKey): Option[List[SearchResultAttachment]] = {
    lazy val hasRestrictedAttachmentPrivileges: Boolean =
      hasAcl(AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS)

    attachmentBeans.map(
      beans =>
        beans
        // Filter out restricted attachments if the user does not have permissions to view them
          .filter(isViewable(hasRestrictedAttachmentPrivileges))
          .map(toSearchResultAttachment(itemKey, _))
    )
  }

  def getItemDrmStatus(rawItem: Item): Option[DrmStatus] = {
    for {
      item <- Option(rawItem)
      _    <- Option(item.getDrmSettings)
      termsAccepted = try {
        LegacyGuice.drmService.hasAcceptedOrRequiresNoAcceptance(item, false, false)
      } catch {
        // This exception is only thrown when the DRM has maximum number of acceptance allowable times.
        case _: DRMException => false
      }
      isAuthorised = try {
        LegacyGuice.drmService.isAuthorised(item, CurrentUser.getUserState.getIpAddress)
        true
      } catch {
        case _: DRMException => false
      }
    } yield {
      DrmStatus(termsAccepted, isAuthorised)
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

  /**
    * Extract the value of 'links' from the 'extras' of AbstractExtendableBean.
    */
  def getLinksFromBean[T <: AbstractExtendableBean](bean: T) =
    bean.get("links").asInstanceOf[java.util.Map[String, String]]

  /**
    * Find the Bookmark linking to the Item and return the Bookmark's ID.
    * @param itemID Unique Item ID
    * @return Unique Bookmark ID
    */
  def getBookmarkId(itemID: ItemIdKey): Option[Long] =
    Option(LegacyGuice.bookmarkService.getByItem(itemID)).map(_.getId)

  /**
    * Check whether a specific version is the latest version
    * @param itemID Unique Item ID
    * @return True if the version is the latest one
    */
  def isLatestVersion(itemID: ItemIdKey): Boolean =
    itemID.getVersion == LegacyGuice.itemService.getLatestVersion(itemID.getUuid)

  def getThumbnailDetails(attachmentBeans: Option[List[AttachmentBean]],
                          itemKey: ItemIdKey): Option[ThumbnailDetails] = {
    lazy val hasRestrictedAttachmentPrivileges: Boolean =
      hasAcl(AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS)

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
        })
        .flatMap(_.links.asScala.get(ItemLinkServiceImpl.REL_THUMB))

    attachmentBeans
      .flatMap(
        _.find(isViewable(hasRestrictedAttachmentPrivileges))
      )
      .map(toSearchResultAttachment(itemKey, _))
      .map(
        a =>
          ThumbnailDetails(attachmentType = a.attachmentType,
                           mimeType = a.mimeType,
                           link = determineThumbnailLink(a)))
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
}
