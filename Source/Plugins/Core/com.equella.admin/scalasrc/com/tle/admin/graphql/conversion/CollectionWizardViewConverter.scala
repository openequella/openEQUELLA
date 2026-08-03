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
import com.tle.core.xstream.ExtXStream
import io.github.openequella.graphql.api.views.CollectionWizardView

import scala.util.chaining.scalaUtilChainingOps

object CollectionWizardViewConverter {

  /** The server writes these blobs with [[ExtXStream]], so we have to read them with it too — a
    * stock XStream instance cannot read oEQ's legacy singleton-map language bundles.
    *
    * A fresh instance per conversion, deliberately: `ExtXStream` enables annotation auto-detection,
    * which reconfigures the instance as it encounters annotated types mid-stream. XStream documents
    * that as a concurrency hazard, and these conversions run on Swing worker threads with several
    * entity editors potentially open at once. Construction is trivial next to the network round
    * trip that fetched the blob.
    *
    * The server-side `CollectionWizard` type documents why the blobs are opaque in the first place.
    */
  private def xstream = new ExtXStream(null)

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

  def fromWizard(wizard: Wizard): CollectionWizardView =
    CollectionWizardView(
      name = Option(wizard.getName),
      redraftScript = Option(wizard.getRedraftScript),
      saveScript = Option(wizard.getSaveScript),
      allowNonSequentialNavigation = wizard.isAllowNonSequentialNavigation,
      showPageTitlesNextPrev = wizard.isShowPageTitlesNextPrev,
      additionalCssClass = Option(wizard.getAdditionalCssClass),
      pages = Option(wizard.getPages).map(serialiseXml),
      fixedMetadata = Option(wizard.getMetadata).map(serialiseXml)
    )

  private def deserialiseXml[T](xml: String): T =
    xstream.fromXML(xml).asInstanceOf[T]

  private def serialiseXml(obj: Any): String =
    xstream.toXML(obj)
}
