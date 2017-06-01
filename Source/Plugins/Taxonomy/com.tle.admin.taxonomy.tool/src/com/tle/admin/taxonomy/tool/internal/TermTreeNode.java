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

package com.tle.admin.taxonomy.tool.internal;

import java.util.Enumeration;

import com.tle.common.LazyTreeNode;
import com.tle.common.taxonomy.TaxonomyConstants;

public class TermTreeNode extends LazyTreeNode
{
	private String fullPath;

	public String getFullPath()
	{
		return fullPath;
	}

	public void setFullPath(String fullPath)
	{
		this.fullPath = fullPath;
	}

	public void updateFullPath(TermTreeNode parent)
	{
		final String pfp = parent == null ? null : parent.getFullPath();
		fullPath = pfp == null ? getName() : pfp + TaxonomyConstants.TERM_SEPARATOR + getName();

		if( getChildrenState() == ChildrenState.LOADED )
		{
			for( Enumeration<?> e = children(); e.hasMoreElements(); )
			{
				TermTreeNode cttn = (TermTreeNode) e.nextElement();
				cttn.updateFullPath(this);
			}
		}
	}
}
