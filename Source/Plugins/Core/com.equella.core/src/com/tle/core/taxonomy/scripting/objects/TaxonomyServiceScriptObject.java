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

package com.tle.core.taxonomy.scripting.objects;

import com.tle.common.scripting.ScriptObject;
import com.tle.core.taxonomy.scripting.types.TaxonomyScriptType;

/**
 * Referenced by the 'data' variable in script
 */
public interface TaxonomyServiceScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "data"; //$NON-NLS-1$

	/**
	 * Find a taxonomy by the UUID of the taxonomy
	 * 
	 * @param uuid The UUID of the taxonomy to find.
	 * @return Will return null if no taxonomy with the given UUID is found.
	 */
	TaxonomyScriptType getTaxonomyByUuid(String uuid);

	/**
	 * Find a taxonomy by the display name of the taxonomy (in the current
	 * user's language). If multiple taxonomies with the same name are found
	 * then a RuntimeException is thrown.
	 * 
	 * @param name The display name of the taxonomy to find.
	 * @return Will return null if no taxonomy by this name is found.
	 */
	TaxonomyScriptType getTaxonomyByName(String name);
}
