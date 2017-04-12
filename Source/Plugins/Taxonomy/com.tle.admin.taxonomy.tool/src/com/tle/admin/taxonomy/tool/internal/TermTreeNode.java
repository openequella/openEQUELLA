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
