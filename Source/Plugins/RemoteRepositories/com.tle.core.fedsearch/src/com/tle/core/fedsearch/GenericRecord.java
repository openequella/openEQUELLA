package com.tle.core.fedsearch;

import java.util.Collection;

import com.dytech.devlib.PropBagEx;

/**
 * @author aholland
 */
public interface GenericRecord
{
	String getTitle();

	String getDescription();

	String getIsbn();

	String getIssn();

	String getLccn();

	String getUri();

	String getUrl();

	Collection<String> getAuthors();

	PropBagEx getXml();

	String getPhysicalDescription();

	/**
	 * @return MODS or MARCXML etc
	 */
	String getType();
}
