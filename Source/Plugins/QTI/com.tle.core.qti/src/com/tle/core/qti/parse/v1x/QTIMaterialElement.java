package com.tle.core.qti.parse.v1x;

/**
 * @author will
 */
public interface QTIMaterialElement
{
	public static enum QTIMaterialMediaType
	{
		IMAGE, EMBED
	}

	String getHtml();
}