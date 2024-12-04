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

package com.tle.web.api.drm

import com.tle.beans.item.DrmSettings
import com.tle.core.i18n.CoreStrings
import com.tle.web.viewitem.I18nDRM
import scala.jdk.CollectionConverters._

case class DrmParties(
    /** Server side language string for DRM party. */
    title: String,
    /** A list of text consisting each party's name and email. */
    partyList: List[String]
)

case class DrmCustomTerms(
    /** Server side language string for DRM terms. */
    title: String,
    /** Terms of using the Item. */
    terms: String
)

case class DrmAgreements(
    /** Text describing what regular permissions are granted to the user. */
    regularPermission: Option[String],
    /** Text describing what additional permissions are granted to the user. */
    additionalPermission: Option[String],
    /** Text describing that the use of Item is limited to education sector. */
    educationSector: Option[String],
    /** Text describing parties related to the Item. */
    parties: Option[DrmParties],
    /** Other terms and conditions applied to the Item. */
    customTerms: Option[DrmCustomTerms]
)

case class ItemDrmDetails(
    /** Server side language string used as the DRM acceptance title */
    title: String = CoreStrings.text("summary.content.termsofuse.title"),
    /** Server side language string used as the DRM acceptance subtitle */
    subtitle: String = CoreStrings.text("summary.content.termsofuse.terms.title"),
    /** Server side language string used as the DRM acceptance description */
    description: String = CoreStrings.text("summary.content.termsofuse.terms.description"),
    /** All terms and conditions that user must accept to use the Item */
    agreements: DrmAgreements
)

object ItemDrmDetails {
  def buildPermissionText(permissions: String, title: String): Option[String] = {
    if (permissions.nonEmpty) Option(s"$title $permissions") else None
  }

  def apply(drmSettings: DrmSettings): ItemDrmDetails = {
    val drmI18n = new I18nDRM(drmSettings)

    val customTerms =
      Option(drmI18n.getTerms).map(terms =>
        DrmCustomTerms(CoreStrings.text("drm.mustagree"), terms)
      )

    val regularPermission =
      buildPermissionText(drmI18n.getPermissions1List, drmI18n.getItemMayFreelyBeText)
    val additionalPermission =
      buildPermissionText(drmI18n.getPermissions2List, drmI18n.getAdditionallyUserMayText)

    val educationSector =
      if (drmI18n.isUseEducation) Option(drmI18n.getEducationSectorText) else None

    val parties = if (drmI18n.isAttribution && !drmI18n.getParties.isEmpty) {
      Option(
        DrmParties(
          drmI18n.getAttributeOwnersText,
          drmI18n.getParties.asScala.map(p => s"${p.getName} ${p.getEmail}").toList
        )
      )
    } else {
      None
    }

    val agreements = DrmAgreements(
      regularPermission,
      additionalPermission,
      educationSector,
      parties,
      customTerms
    )

    ItemDrmDetails(agreements = agreements)
  }
}
