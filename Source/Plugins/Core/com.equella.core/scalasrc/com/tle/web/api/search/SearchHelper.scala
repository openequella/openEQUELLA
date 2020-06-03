package com.tle.web.api.search

import java.text.{ParseException, SimpleDateFormat}
import java.util.Date
import com.dytech.edge.exceptions.BadRequestException
import com.tle.beans.entity.DynaCollection
import com.tle.beans.item.{ItemIdKey, ItemStatus}
import com.tle.common.Check
import com.tle.common.Utils.parseDate
import com.tle.common.beans.exception.NotFoundException
import com.tle.common.search.DefaultSearch
import com.tle.common.search.whereparser.WhereParser
import com.tle.core.freetext.queries.FreeTextBooleanQuery
import com.tle.core.item.serializer.{ItemSerializerItemBean, ItemSerializerService}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.interfaces.beans.AbstractExtendableBean
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean
import com.tle.web.api.item.interfaces.beans.AttachmentBean
import com.tle.web.api.search.model.{SearchResultAttachment, SearchParam, SearchResultItem}
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object SearchHelper {

  /**
    * Create a new search with search criteria.
    * The search criteria is dependent on what parameters are passed in.
    * @param params Search parameters.
    * @return An instance of DefaultSearch
    */
  def createSearch(params: SearchParam): DefaultSearch = {
    val search = new DefaultSearch
    search.setQuery(params.query)
    search.setOwner(params.owner)

    val orderType = DefaultSearch.getOrderType(params.order, params.query)
    search.setSortFields(orderType.getSortField(params.reverseOrder))

    val collectionUuids = handleCollections(params.advancedSearch, params.collections)
    search.setCollectionUuids(collectionUuids.orNull)

    val itemStatus = handleItemStatus(params.status)
    search.setItemStatuses(itemStatus.orNull)

    val modifiedBefore = handleModifiedDate(params.modifiedBefore)
    val modifiedAfter  = handleModifiedDate(params.modifiedAfter)
    if (modifiedBefore.isDefined || modifiedAfter.isDefined) {
      search.setDateRange(Array(modifiedAfter.orNull, modifiedBefore.orNull))
    }

    val dynaCollectionQuery = handleDynaCollection(params.dynaCollection)
    val whereQuery          = handleWhereClause(params.whereClause)
    val freeTextQuery = dynaCollectionQuery match {
      case Some(q) => q.add(whereQuery.orNull)
      case None    => whereQuery.orNull
    }
    search.setFreeTextQuery(freeTextQuery)

    search
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
    * Return a free text query based on what where clause is provided.
    * @param whereClause A where clause which is added to the search SQL.
    * @return An option which wraps an instance of FreeTextBooleanQuery.
    */
  def handleWhereClause(whereClause: String): Option[FreeTextBooleanQuery] = {
    if (Check.isEmpty(whereClause)) {
      return None
    }
    Some(WhereParser.parse(whereClause))
  }

  /**
    * Parse a string to a new instance of Date in the format of "yyyy-MM-dd".
    * @param date The string to parse.
    * @return An option which wraps an instace of Date.
    */
  def handleModifiedDate(date: String): Option[Date] = {
    if (Check.isEmpty(date)) {
      return None
    }
    try {
      Some(parseDate(date, new SimpleDateFormat("yyyy-MM-dd")))
    } catch {
      case _: ParseException => throw new BadRequestException(s"Invalid date: $date")
    }
  }

  /**
    * Convert a list of strings to another list of ItemStatus.
    * @param list A list of strings to convert
    * @return An option which wraps a list of ItemStatus.
    */
  def handleItemStatus(list: Array[String]): Option[java.util.List[ItemStatus]] = {
    if (list.isEmpty) {
      return None
    }

    val itemStatusList = list
      .map(s => {
        try {
          ItemStatus.valueOf(s.toUpperCase())
        } catch {
          case _: IllegalArgumentException =>
            throw new BadRequestException(s"Invalid Item status: $s")
        }
      })
      .toList
      .asJava
    Some(itemStatusList)
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
    * Create a serializer for ItemBean.
    */
  def createSerializer(itemIds: List[ItemIdKey]): ItemSerializerItemBean = {
    val ids      = itemIds.map(_.getKey.asInstanceOf[java.lang.Long]).asJavaCollection
    val category = List(ItemSerializerService.CATEGORY_ALL).asJavaCollection
    LegacyGuice.itemSerializerService.createItemBeanSerializer(ids, category, false)
  }

  /**
    * Convert a tuple of ItemIdKey and EquellaItemBean to an instance of SearchResultItem.
    * @param itemKeyAndBean An EquellaItemBean and its ItemIdKey.
    * @return An instance of SearchResultItem.
    */
  def convertToItem(itemKeyAndBean: (ItemIdKey, EquellaItemBean)): SearchResultItem = {
    val key          = itemKeyAndBean._1
    val bean         = itemKeyAndBean._2
    val commentCount = LegacyGuice.itemCommentService.getComments(key, null, null, -1).size()
    SearchResultItem(
      key.getUuid,
      Option(bean.getName),
      Option(bean.getDescription),
      bean.getStatus,
      bean.getCreatedDate,
      bean.getModifiedDate,
      bean.getCollection.getUuid,
      commentCount,
      convertToAttachment(bean.getAttachments),
      bean.getThumbnail,
      bean.getDisplayFields.asScala.toList,
      Option(bean.getDisplayOptions),
      getLinksFromBean(bean)
    )
  }

  /**
    * Convert a list of AttachmentBean to a list of SearchResultAttachment
    */
  def convertToAttachment(
      attachmentBeans: java.util.List[AttachmentBean]): List[SearchResultAttachment] = {
    attachmentBeans.asScala
      .map(
        att =>
          SearchResultAttachment(att.getRawAttachmentType,
                                 att.getUuid,
                                 Option(att.getDescription),
                                 att.isPreview,
                                 getLinksFromBean(att)))
      .toList
  }

  /**
    * Extract the value of 'links' from the 'extras' of AbstractExtendableBean.
    */
  def getLinksFromBean[T <: AbstractExtendableBean](bean: T) =
    bean.get("links").asInstanceOf[java.util.Map[String, String]]
}
