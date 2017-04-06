package com.tle.core.remoterepo.srw.service.impl;

import com.dytech.devlib.PropBagEx;
import com.tle.core.fedsearch.RemoteRepoSearchResult;

/**
 * @author aholland
 */
public class SrwSearchResult extends RemoteRepoSearchResult
{
	private static final long serialVersionUID = 1L;

	private PropBagEx importXml;

	public SrwSearchResult(int index)
	{
		super(index);
	}

	public PropBagEx getImportXml()
	{
		return importXml;
	}

	public void setImportXml(PropBagEx importXml)
	{
		this.importXml = importXml;
	}
}
