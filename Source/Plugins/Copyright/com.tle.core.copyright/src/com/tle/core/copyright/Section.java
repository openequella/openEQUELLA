package com.tle.core.copyright;

/**
 * Absolutely nothing to do with Sections
 * 
 * @author Aaron
 */
public interface Section
{
	boolean isIllustration();

	long getId();

	String getRange();

	String getCopyrightStatus();

	String getAttachment();

	Portion getPortion();
}
