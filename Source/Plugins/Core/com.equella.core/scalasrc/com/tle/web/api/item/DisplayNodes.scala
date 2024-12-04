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

package com.tle.web.api.item

import com.dytech.devlib.PropBagEx
import com.tle.beans.entity.itemdef.DisplayNode
import com.tle.common.Utils
import com.tle.common.i18n.LangUtils
import com.tle.web.api.item.interfaces.beans.MetaDisplay
import scala.jdk.CollectionConverters._

object DisplayNodes {

  def create(itemxml: PropBagEx)(dn: DisplayNode): Option[MetaDisplay] = {
    val nodePath = dn.getNode
    val valueText = if (nodePath.indexOf('@') != -1) {
      itemxml.getNode(nodePath)
    } else {
      val _splitter = dn.getSplitter
      val splitter  = if (dn.getType == "text") Utils.unent(_splitter) else _splitter
      itemxml
        .iterateAll(nodePath)
        .iterator()
        .asScala
        .map { x =>
          LangUtils.getString(LangUtils.getBundleFromXml(x), "")
        }
        .mkString(splitter)
    }
    if (valueText.nonEmpty) Some {
      MetaDisplay(LangUtils.getString(dn.getTitle), valueText, !dn.isDoubleMode, dn.getType)
    }
    else None
  }
}
