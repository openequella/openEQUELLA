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

package com.tle.core.harvester;

import java.util.Date;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.searching.Search;
import com.tle.core.harvester.old.TLEItem;

public interface LearningEdge
{
	/**
	 * Creates a new item in the supplied collection.
	 * 
	 * @param itemDef The collection to create the item in
	 * @return The xml of the new item
	 */
	PropBagEx newItem(ItemDefinition itemDef) throws Exception;

	/**
	 * Creates a new item in the supplied collection with the given uuid
	 * 
	 * @param uuid The uuid of the new item
	 * @param itemDef The collection to create the item in
	 * @return The xml of the new item
	 */
	PropBagEx newItem(String uuid, ItemDefinition itemDef) throws Exception;

	/**
	 * Creates a new version of the supplied item, archiving the old version
	 * 
	 * @param item The item to create a new version of
	 * @return
	 * @throws Exception
	 */
	PropBagEx newVersion(TLEItem item) throws Exception;

	/**
	 * Uploads an item xml. <br>
	 * You first have to have either created a new item or a new version.
	 * 
	 * @param itemXml The item xml
	 * @param itemDef The collection for it to go in
	 */
	void uploadItem(PropBagEx itemXml, ItemDefinition itemDef) throws Exception;

	/**
	 * Search the institution for an existing item
	 * 
	 * @param request The search request to run
	 * @return The latest version of any found item
	 */
	TLEItem getLatestItem(Search request) throws Exception;

	/**
	 * Saves a harvester profile
	 * 
	 * @param profile The profile to save
	 */
	void updateProfileRunDate(HarvesterProfile profile, Date date);

	/**
	 * Convert an ItemDefinition uuid to the object
	 * 
	 * @param itemdef The uuid of the ItemDefinition
	 * @return The ItemDefinition
	 */
	ItemDefinition getItemDefByUuid(String itemdef);

	/**
	 * Gets the schema for the supplied collection
	 * 
	 * @param itemdef The uuid of the collection
	 * @return The schema id
	 */
	long getSchemaByUuid(String itemdef);

	/**
	 * Transforms the supplied XML with the specified XSLT. <br>
	 * The XSLTs are found in the schema definition
	 * 
	 * @param schema The schema id
	 * @param xml The xml to transform
	 * @param xsltName The name of the xslt
	 * @return The resulting xml as a string
	 */
	String transformSchema(long schema, PropBagEx xml, String xsltName) throws Exception;

	/**
	 * Gets an existing item from the repository
	 * 
	 * @param uuid The uuid of the item
	 * @param newItemDef The collection that you are harvesting too
	 * @return The item
	 * @throws Exception Thrown if the item is in a different collection to the
	 *             one you are harvesting too
	 */
	TLEItem getItem(String uuid, ItemDefinition newItemDef) throws Exception;

	/**
	 * Checks if the item exists in the repository
	 * 
	 * @param uuid The uuid of the item.
	 * @return true or false
	 */
	boolean itemExists(String uuid);

	/**
	 * modify inplace
	 * 
	 * @param item The item to modify
	 * @return
	 * @throws Exception
	 */
	PropBagEx modifyInPlace(TLEItem item) throws Exception;
}
