package com.tle.core.util;

import java.io.StringWriter;

import org.ccil.cowan.tagsoup.XMLWriter;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public abstract class AbstractHtmlContentHandler extends XMLWriter implements HtmlContentHandler
{
	protected final StringWriter w;
	protected boolean outputNamespaces;

	protected AbstractHtmlContentHandler(StringWriter w)
	{
		super(w);

		this.w = w;
		setOutputProperty(XMLWriter.METHOD, "html");
		setOutputProperty(XMLWriter.OMIT_XML_DECLARATION, "yes");
		setOutputProperty(XMLWriter.ENCODING, "UTF-8");
	}

	@Override
	public String getOutput()
	{
		return w.toString();
	}

	@Override
	public boolean isOutputNamespaces()
	{
		return outputNamespaces;
	}

	@Override
	public void setOutputNamespaces(boolean outputNamespaces)
	{
		this.outputNamespaces = outputNamespaces;
	}
}
