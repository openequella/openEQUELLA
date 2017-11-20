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

/**
 * A super-interface for entity types. E.g. collection, schema, taxonomy...
 * 
 * @author Aaron
 */
public interface BaseEntityScriptType extends Serializable
{
	/**
	 * @return The UUID of the entity
	 */
	String getUniqueID();

	/**
	 * @return This function is an alias for getUniqueID()
	 */
	String getUuid();

	/**
	 * @return The name of the entity in the current user's language
	 */
	String getName();

	/**
	 * @return The name of the entity in the current user's language
	 */
	String getDescription();
}
