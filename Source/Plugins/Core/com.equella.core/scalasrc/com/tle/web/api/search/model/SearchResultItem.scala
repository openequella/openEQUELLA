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

package com.tle.web.api.search.model

import com.tle.common.interfaces.I18NString
import com.tle.web.api.item.equella.interfaces.beans.{DisplayField, DisplayOptions}

import java.util.Date

/**
  * This model class includes an item's information required in the new Search page.
  *
  * @param uuid Item's unique ID.
  * @param name Item's name which can be null.
  * @param description Item's description which can be null.
  * @param status Item's status.
  * @param createdDate The date when an item is created.
  * @param modifiedDate The last date when an item is modified.
  * @param collectionId The ID of a collection which the item belongs to.
  * @param commentCount The count of an item's comments.
  * @param starRatings The star ratings of an Item.
  * @param attachmentCount The total number of attachments for this item.
  * @param attachments A list of Item's attachments.
  * @param thumbnail Item's thumbnail type.
  * @param thumbnailDetails Details for displaying a thumbnail for an item.
  * @param displayFields A list of Item's displayFields.
  * @param displayOptions Item's displayOptions which can be null.
  * @param keywordFoundInAttachment Indicates if a search term has been found inside attachment content
  * @param links Item's links.
  * @param bookmarkId ID of Bookmark linking to this Item.
  * @param isLatestVersion True if this version is the latest version.
  * @param drmStatus Status of Item's DRM, consisting if terms accepted and if authorised, absent if item not DRM controlled.
  */
case class SearchResultItem(
    uuid: String,
    version: Int,
    name: Option[I18NString],
    description: Option[I18NString],
    status: String,
    createdDate: Date,
    modifiedDate: Date,
    collectionId: String,
    commentCount: Option[Integer],
    starRatings: Float,
    attachmentCount: Int,
    attachments: Option[List[SearchResultAttachment]],
    thumbnail: String,
    thumbnailDetails: Option[ThumbnailDetails],
    displayFields: List[DisplayField],
    displayOptions: Option[DisplayOptions],
    keywordFoundInAttachment: Boolean,
    links: java.util.Map[String, String],
    bookmarkId: Option[Long],
    isLatestVersion: Boolean,
    drmStatus: Option[DrmStatus],
    moderationDetails: Option[ModerationDetails],
)

/**
  * This model class provides general information of an attachment.
  * @param attachmentType Attachment's type.
  * @param id The unique ID of an attachment.
  * @param description The description of an attachment.
  * @param brokenAttachment If true, this attachment is broken or inaccessible.
  *                          For file attachments, this means that it is not accessible from the filestore.
  *                          For resource selector attachments, this means that the linked attachment or item summary does not exist.
  * @param preview If an attachment can be previewed or not.
  * @param mimeType Mime Type of file based attachments
  * @param hasGeneratedThumb Indicates if file based attachments have a generated thumbnail store in filestore
  * @param links Attachment's links.
  * @param filePath If a file attachment, the path for the represented file
  */
case class SearchResultAttachment(
    attachmentType: String,
    id: String,
    description: Option[String],
    brokenAttachment: Boolean,
    preview: Boolean,
    mimeType: Option[String],
    hasGeneratedThumb: Option[Boolean],
    links: java.util.Map[String, String],
    filePath: Option[String]
)

/**
  * Model class providing DRM related status.
  *
  * @param termsAccepted Whether terms have been accepted or not.
  * @param isAuthorised Whether user is authorised to access Item or accept DRM.
  * @param isAllowSummary Whether user can view the Item summary page without accepting the terms.
  */
case class DrmStatus(termsAccepted: Boolean, isAuthorised: Boolean, isAllowSummary: Boolean)

/**
  * Provides details to assist with displaying a thumbnail for a search result, based on the
  * attachment that is designated to be used as the basis for the thumbnail of this item (typically
  * the first attachment).
  *
  * @param attachmentType The broad indicator of attachment type which which drives the content of
  *                       the other properties. Example values are `file`, `link`, `custom/xyz`.
  * @param mimeType Mostly used when `attachmentType` is `file` but also when `custom/resource`.
  * @param link If the server has generated a specific thumbnail for this item, then this will
  *             provide the URL for it.
  */
case class ThumbnailDetails(attachmentType: String, mimeType: Option[String], link: Option[String])

/**
  * Model class to provide an Item's moderation details.
  *
  * @param lastActionDate When was the last moderation action performed.
  * @param submittedDate When was the Item submitted to moderation.
  * @param rejectionMessage Message for why the Item was rejected.
  */
case class ModerationDetails(lastActionDate: Date,
                             submittedDate: Date,
                             rejectionMessage: Option[String])
