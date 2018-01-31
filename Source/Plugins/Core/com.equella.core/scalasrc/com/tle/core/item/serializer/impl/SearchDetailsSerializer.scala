package com.tle.core.item.serializer.impl

import javax.inject.Singleton

import com.tle.beans.entity.LanguageBundle
import com.tle.beans.entity.itemdef.SearchDetails
import com.tle.common.Pair
import com.tle.common.interfaces.I18NStrings
import com.tle.common.interfaces.equella.BundleString
import com.tle.core.guice.Bind
import com.tle.core.item.serializer.{ItemSerializerProvider, ItemSerializerState, XMLStreamer}
import com.tle.web.api.item.equella.interfaces.beans.{DisplayField, DisplayOptions, EquellaItemBean}
import org.hibernate.criterion.Projections

import scala.collection.JavaConverters._

@Bind
@Singleton
class SearchDetailsSerializer extends ItemSerializerProvider {

  val CATEGORY_DISPLAY = "display"

  override def prepareItemQuery(state: ItemSerializerState): Unit = if (state.hasCategory(CATEGORY_DISPLAY)) {
    val itemProjection = state.getItemProjection
    itemProjection.add(Projections.property("searchDetails"), "searchDetails")
    state.addCollectionQuery()
    state.getItemQuery.createAlias("itemDefinition.slow", "slow")
    itemProjection.add(Projections.property("slow.searchDetails"), "colSearchDetails")
  }

  override def performAdditionalQueries(state: ItemSerializerState): Unit = {

  }

  override def writeXmlResult(xml: XMLStreamer, state: ItemSerializerState, itemId: Long): Unit = ???

  override def writeItemBeanResult(equellaItemBean: EquellaItemBean, state: ItemSerializerState, itemId: Long): Unit =
    if (state.hasCategory(CATEGORY_DISPLAY)) {
      Option(state.getData[SearchDetails](itemId, "colSearchDetails")).foreach { csd =>
        equellaItemBean.setDisplayOptions(new DisplayOptions(csd.getAttDisplay, csd.isDisableThumbnail,
          csd.isStandardOpen, csd.isIntegrationOpen))
      }
      Option(state.getData[java.util.List[Pair[LanguageBundle, LanguageBundle]]](itemId, "searchDetails")).foreach { sd =>
        equellaItemBean.setDisplayFields {
          sd.asScala.map { p =>
            val name = BundleString.getString(p.getFirst)
            val html = BundleString.getString(p.getSecond)
            new DisplayField(name, html)
          }.asJava
        }
      }
    }
}
