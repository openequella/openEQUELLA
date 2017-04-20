package com.tle.core.remoterepo.merlot.syndication;

import org.jdom2.Namespace;

import com.rometools.rome.feed.module.Module;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public interface MerlotModule extends Module
{
	String URI = "http://www.merlot.org/merlot/materials-rest";
	Namespace NAMESPACE = Namespace.getNamespace(URI);

	String getTitle();

	void setTitle(String title);

	String getUrl();

	void setUrl(String url);
}
