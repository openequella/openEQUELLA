package com.tle.core.htmleditor.service;

/**
 * @author Aaron
 */
public class InvalidHtmlEditorPluginException extends Exception
{
	public InvalidHtmlEditorPluginException(String reason)
	{
		super(reason);
	}

	public InvalidHtmlEditorPluginException(String reason, Exception cause)
	{
		super(reason, cause);
	}
}
