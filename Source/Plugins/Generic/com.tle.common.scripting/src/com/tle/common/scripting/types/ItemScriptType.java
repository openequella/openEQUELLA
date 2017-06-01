/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.scripting.types;

import java.io.Serializable;
import java.util.List;

/**
 * Available as 'currentItem' in scripts, but also available from results of
 * calls to methods on 'items' and within search results return by
 * 'utils.search(...)'.
 */
@SuppressWarnings("nls")
public interface ItemScriptType extends Serializable
{
	/**
	 * The variable name of the item being edited (if any)
	 */
	String CURRENT_ITEM = "currentItem";

	/**
	 * @return The system generated UUID of the item
	 */
	String getUuid();

	/**
	 * @return The version number of the item
	 */
	int getVersion();

	/**
	 * @return The name of the item in the current user's language (if language
	 *         pack available).
	 */
	String getName();

	/**
	 * @return The description of the item in the current user's language (if
	 *         language pack available).
	 */
	String getDescription();

	/**
	 * There is generally an 'xml' object available for the current item's xml,
	 * but you can use this for getting xml from OTHER items. Note that this is
	 * READ-ONLY, any changes you make will not be persisted.
	 * 
	 * @return The XML of the item as a PropBagScriptType object
	 */
	XmlScriptType getXml();

	/**
	 * Get the status of the item. One of: DRAFT, LIVE, REJECTED, MODERATING,
	 * ARCHIVED, SUSPENDED, DELETED, REVIEW, PERSONAL
	 * 
	 * @return The status of the item.
	 */
	String getItemStatus();

	/**
	 * @return The unique ID of the owner. This may not be initialised in a
	 *         wizard context as the item does not yet have an owner.
	 */
	String getOwner();

	/**
	 * Can only be used on the currentItem, and depending on the script context
	 * it may still not be saved.
	 * 
	 * @param userUniqueId The unique ID of the new owner.
	 */
	void setOwner(String userUniqueId);

	/**
	 * Can only be used on the current item, and depending on the script context
	 * it may still not be saved.
	 * 
	 * @param userUniqueId The unique ID of the shared owner to add.
	 */
	void addSharedOwner(String userUniqueId);

	/**
	 * Can only be used on the current item, and depending on the script context
	 * it may still not be saved.
	 * 
	 * @param userUniqueId The unique ID of the shared owner to remove.
	 */
	boolean removeSharedOwner(String userUniqueId);

	/**
	 * @return A list of the unique IDs of the current shared owners.
	 */
	List<String> listSharedOwners();

	/**
	 * @return The collection that this item belongs to
	 */
	CollectionScriptType getCollection();

	/**
	 * Sets the thumbnail to show in gallery view and search results.
	 * 
	 * @param thumbnail Either "default", "none" or "custom:[uuid]" where [uuid]
	 *            is the UUID of an attachment.
	 */
	void setThumbnail(String thumbnail);

	/**
	 * @return The thumbnail to show in gallery view and search results.
	 */
	String getThumbnail();
}
