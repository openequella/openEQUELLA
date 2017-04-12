/**
 * 
 */
package com.tle.core.remoterepo.sru.service.impl;

import com.dytech.devlib.PropBagEx;
import com.tle.core.fedsearch.RemoteRepoSearchResult;

/**
 * @author larry
 */
public class SruSearchResult extends RemoteRepoSearchResult
{
	private static final long serialVersionUID = 1L;

	private PropBagEx importXml;

	public SruSearchResult(int index)
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
