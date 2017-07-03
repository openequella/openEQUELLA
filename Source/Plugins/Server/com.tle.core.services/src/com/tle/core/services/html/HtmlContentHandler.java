package com.tle.core.services.html;

import org.xml.sax.ContentHandler;

/**
 * @author Aaron
 */
public interface HtmlContentHandler extends ContentHandler
{
	String getOutput();

	boolean isOutputNamespaces();

	void setOutputNamespaces(boolean outputNamespaces);
}
