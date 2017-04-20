package com.tle.core.remoterepo.merlot.syndication;


import org.jdom2.Namespace;

import com.rometools.rome.feed.module.Module;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public interface MerlotTopLevelModule extends Module
{
	String URI = "http://www.merlot.org/merlot/materials-rest";
	Namespace NAMESPACE = Namespace.getNamespace(URI);

	int getTotalCount();

	void setTotalCount(int totalCount);

	int getResultCount();

	void setResultCount(int resultCount);

	int getLastRecNumber();

	void setLastRecNumber(int lastRecNumber);
}
