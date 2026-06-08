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

package com.tle.admin.graphql.conversion

import com.tle.beans.entity.itemdef.Wizard
import com.tle.common.security.streaming.XStreamSecurityManager
import io.github.openequella.graphql.api.views.CollectionWizardView

import scala.util.chaining.scalaUtilChainingOps

object CollectionWizardViewConverter {
  def toWizard(view: CollectionWizardView): Wizard = new Wizard().tap { w =>
    w.setName(view.name.orNull)
    w.setRedraftScript(view.redraftScript.orNull)
    w.setSaveScript(view.saveScript.orNull)
    w.setAllowNonSequentialNavigation(view.allowNonSequentialNavigation)
    w.setShowPageTitlesNextPrev(view.showPageTitlesNextPrev)
    w.setAdditionalCssClass(view.additionalCssClass.orNull)
    view.pages.foreach(xml => w.setPages(deserialiseXml(xml)))
    view.fixedMetadata.foreach(xml => w.setMetadata(deserialiseXml(xml)))
  }

  private def deserialiseXml[T](xml: String): T =
    XStreamSecurityManager.newXStream().fromXML(xml).asInstanceOf[T]
}
