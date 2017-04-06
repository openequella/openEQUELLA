package com.tle.qti.data;

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