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

package com.tle.beans.hierarchy;

import com.tle.common.LazyTreeNode;

/**
 * @author Nicholas Read
 */
public class HierarchyTreeNode extends LazyTreeNode
{
	private static final long serialVersionUID = 1L;

	private long id;
	private boolean grantedEditTopic;

	public HierarchyTreeNode()
	{
		super();
	}

	public HierarchyTreeNode(long id)
	{
		this.id = id;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public boolean isGrantedEditTopic()
	{
		return grantedEditTopic;
	}

	public void setGrantedEditTopic(boolean grantedEditTopic)
	{
		this.grantedEditTopic = grantedEditTopic;
	}
}
