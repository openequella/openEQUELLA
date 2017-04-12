package com.tle.core.harvester.search;

import com.tle.common.search.DefaultSearch;

public class DownloadItemSearch extends DefaultSearch
{
	private static final long serialVersionUID = 1L;

	public DownloadItemSearch()
	{
		super();
	}

	@Override
	public String getPrivilege()
	{
		return "DOWNLOAD_ITEM"; //$NON-NLS-1$
	}
}
