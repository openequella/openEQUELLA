package com.tle.web.sections.equella;

import java.io.Serializable;

import com.tle.web.sections.SectionInfo;

public class TreeLookupCallback implements ModalSessionCallback
{
	private static final long serialVersionUID = 1L;

	protected final Serializable treeKey;

	public TreeLookupCallback(Serializable treeKey)
	{
		this.treeKey = treeKey;
	}

	@Override
	public void executeModalFinished(SectionInfo info, ModalSession session)
	{
		((ModalSessionCallback) info.getTreeAttribute(treeKey)).executeModalFinished(info, session);
	}
}
