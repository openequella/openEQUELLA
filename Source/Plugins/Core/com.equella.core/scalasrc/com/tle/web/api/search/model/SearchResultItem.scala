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

import java.util.Date
import com.tle.common.interfaces.I18NString
import com.tle.web.api.item.equella.interfaces.beans.{DisplayField, DisplayOptions}

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
  * @param attachments A list of Item's attachments.
  * @param thumbnail Item's thumbnail.
  * @param displayFields A list of Item's displayFields.
  * @param displayOptions Item's displayOptions which can be null.
  * @param links Item's links.
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
    commentCount: Int,
    attachments: List[SearchResultAttachment],
    thumbnail: String,
    displayFields: List[DisplayField],
    displayOptions: Option[DisplayOptions],
    links: java.util.Map[String, String]
)

/**
  * This model class provides general information of an attachment.
  * @param attachmentType Attachment's type.
  * @param id The unique ID of an attachment.
  * @param description The description of an attachment.
  * @param preview If an attachment can be previewed or not.
  * @param mimeType Mime Type of file based attachments
  * @param links Attachment's links.
  */
case class SearchResultAttachment(
    attachmentType: String,
    id: String,
    description: Option[String],
    preview: Boolean,
    mimeType: Option[String],
    isPlaceholderThumb: Boolean,
    links: java.util.Map[String, String]
)
