package com.tle.web.selection;

import java.io.Serializable;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.TreeLookupCallback;

/**
 * @author aholland
 */
public class TreeLookupSelectionCallback extends TreeLookupCallback implements SelectionsMadeCallback
{
	private static final long serialVersionUID = 1L;

	public TreeLookupSelectionCallback(Serializable treeKey)
	{
		super(treeKey);
	}

	@Override
	public boolean executeSelectionsMade(SectionInfo info, SelectionSession session)
	{
		return ((SelectionsMadeCallback) info.getTreeAttribute(treeKey)).executeSelectionsMade(info, session);
	}
}
