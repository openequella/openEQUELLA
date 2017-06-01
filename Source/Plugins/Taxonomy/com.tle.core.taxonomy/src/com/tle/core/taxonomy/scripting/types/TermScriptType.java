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

package com.tle.core.taxonomy.scripting.types;

/**
 * These are retrieved and manipulated with the TaxonomyScriptType
 * 
 * @author aholland
 */
public interface TermScriptType
{
	/**
	 * @return The node name of the term (i.e. not the full path)
	 */
	String getTerm();

	/**
	 * @return The full path to this term.
	 */
	String getFullPath();

	/**
	 * @return true if this node has no child nodes.
	 */
	boolean isLeaf();

	/**
	 * @param attributeKey The key of the custom data field.
	 * @return The value of the custom data (if any)
	 */
	String getData(String attributeKey);
}
