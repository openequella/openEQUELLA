/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.common.scripting.objects;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.CollectionScriptType;

import java.util.List;

/**
 * Referenced by the 'collection' variable in script
 */
public interface CollectionScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "collection";

	/**
	 * Return a CollectionScriptType object based on the collectionUuid.
	 *
	 * @param collectionUuid The UUID of the collection to locate
	 * @return A CollectionScriptType object or null if not found
	 */
	CollectionScriptType getFromUuid(String collectionUuid);

	/**
	 * List all non-archived collections
	 *
	 * @return A list of CollectionScriptType
	 */
	List<CollectionScriptType> listCollections();
}
