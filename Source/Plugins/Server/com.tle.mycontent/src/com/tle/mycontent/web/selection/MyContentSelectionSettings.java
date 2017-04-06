package com.tle.mycontent.web.selection;

import java.io.Serializable;
import java.util.Collection;

public class MyContentSelectionSettings implements Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean rawFilesOnly;
	private Collection<String> restrictToHandlerTypes;

	public boolean isRawFilesOnly()
	{
		return rawFilesOnly;
	}

	public void setRawFilesOnly(boolean rawFilesOnly)
	{
		this.rawFilesOnly = rawFilesOnly;
	}

	public Collection<String> getRestrictToHandlerTypes()
	{
		return restrictToHandlerTypes;
	}

	public void setRestrictToHandlerTypes(Collection<String> restrictToHandlerTypes)
	{
		this.restrictToHandlerTypes = restrictToHandlerTypes;
	}
}
