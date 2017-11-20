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

package com.tle.core.freetext.reindex;

import com.tle.common.security.PrivilegeTree.Node;

public interface ReindexHandler
{
	/**
	 * Allow the handler to specify the reindex filter that should be used for a
	 * target if privileges are changed. For example, a change to the
	 * DISCOVER_ITEM privilege at the collection level would require all items
	 * in that collection to be reindexed, so an ItemdefFilter object would be
	 * returned.
	 * 
	 * @param target the object with the privileges being modified.
	 * @return the reindex filter to use, or <code>null</code> to prevent any
	 *         reindexing.
	 */
	ReindexFilter getReindexFilter(Node node, Object domainObject);

}
