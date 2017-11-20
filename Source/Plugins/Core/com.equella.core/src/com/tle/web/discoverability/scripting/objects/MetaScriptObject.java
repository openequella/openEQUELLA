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

package com.tle.web.discoverability.scripting.objects;

import com.tle.common.scripting.ScriptObject;

/**
 * Referenced by the 'meta' variable in scripting
 * 
 * @author wbowling
 */
public interface MetaScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "meta"; //$NON-NLS-1$

	/**
	 * Add a new meta tag to the head element of the item summary page. So
	 * <code>meta.add("citation_title", "Etiam aliquet massa et lorem")</code>
	 * will add the following meta tag:
	 * 
	 * <pre>
	 * {@code
	 * <meta name="citation_title" content="Etiam aliquet massa et lorem">
	 * }
	 * </pre>
	 * 
	 * @param name The name attribute of the meta tag
	 * @param content The content attribute of the meta tag
	 */
	void add(String name, String content);

}
