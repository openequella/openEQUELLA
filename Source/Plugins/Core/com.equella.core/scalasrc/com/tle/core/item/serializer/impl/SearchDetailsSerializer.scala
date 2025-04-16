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

package com.tle.core.item.serializer.impl

import com.tle.beans.cal.{CALHolding, CALPortion}
import com.tle.beans.entity.LanguageBundle
import com.tle.beans.entity.itemdef.SearchDetails
import com.tle.cal.dao.CALDao
import com.tle.cal.service.CALService
import com.tle.common.Pair
import com.tle.common.interfaces.SimpleI18NString
import com.tle.common.interfaces.equella.BundleString
import com.tle.core.guice.Bind
import com.tle.core.item.serializer.ItemSerializerService.SerialisationCategory
import com.tle.core.item.serializer.{ItemSerializerProvider, ItemSerializerState, XMLStreamer}
import com.tle.web.api.item.equella.interfaces.beans.{DisplayField, DisplayOptions, EquellaItemBean}
import com.tle.web.resources.ResourcesService
import javax.inject.{Inject, Singleton}
import org.hibernate.criterion.Projections
import scala.jdk.CollectionConverters._
import scala.collection.mutable

@Bind
@Singleton
class SearchDetailsSerializer extends ItemSerializerProvider {

  private val R = ResourcesService.getResourceHelper(getClass)
  @Inject
  var calDao: CALDao = _
  @Inject
  var calService: CALService = _

  override def prepareItemQuery(state: ItemSerializerState): Unit =
    if (state.hasCategory(SerialisationCategory.DISPLAY)) {
      val itemProjection = state.getItemProjection
      itemProjection.add(Projections.property("searchDetails"), "searchDetails")
      state.addCollectionQuery()
      state.getItemQuery.createAlias("itemDefinition.slow", "slow")
      itemProjection.add(Projections.property("slow.searchDetails"), "colSearchDetails")
    }

  override def performAdditionalQueries(state: ItemSerializerState): Unit =
    if (state.hasCategory(SerialisationCategory.DISPLAY)) {
      val itemIds     = state.getItemKeys
      val holdingsMap = calDao.getHoldingsForItemIds(itemIds).asScala
      val portionsMap = calDao.getPortionsForItemIds(itemIds).asScala.groupBy(_.getItem.getId)
      holdingsMap.foreach { case (itemId, ch) =>
        portionsMap
          .get(itemId)
          .flatMap(_.headOption)
          .foreach(cp => state.setData(itemId, "caldata", (ch, cp)))
      }
    }

  override def writeXmlResult(xml: XMLStreamer, state: ItemSerializerState, itemId: Long): Unit = {}

  override def writeItemBeanResult(
      equellaItemBean: EquellaItemBean,
      state: ItemSerializerState,
      itemId: Long
  ): Unit =
    if (state.hasCategory(SerialisationCategory.DISPLAY)) {
      Option(state.getData[SearchDetails](itemId, "colSearchDetails")).foreach { csd =>
        equellaItemBean.setDisplayOptions(
          new DisplayOptions(
            csd.getAttDisplay,
            csd.isDisableThumbnail,
            csd.isStandardOpen,
            csd.isIntegrationOpen
          )
        )
      }
      val displayNodes = Option(
        state.getData[java.util.List[Pair[LanguageBundle, LanguageBundle]]](itemId, "searchDetails")
      ).map { sd =>
        sd.asScala.map { p =>
          val name = BundleString.getString(p.getFirst)
          val html = BundleString.getString(p.getSecond)
          new DisplayField("node", name, html)
        }
      }
      val citation = Option(state.getData[(CALHolding, CALPortion)](itemId, "caldata")).map {
        case (ch, cp) =>
          val citation = calService.citate(ch, cp)
          new DisplayField(
            "cal-citation",
            new SimpleI18NString(R.getString("list.citation")),
            new SimpleI18NString(citation)
          )
      }
      equellaItemBean.setDisplayFields(
        (displayNodes.getOrElse(mutable.Buffer.empty) ++ citation).asJava
      )
    }
}
