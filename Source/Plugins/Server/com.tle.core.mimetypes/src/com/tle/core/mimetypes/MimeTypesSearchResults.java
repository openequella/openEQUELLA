package com.tle.core.mimetypes;

import java.util.List;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.searching.SimpleSearchResults;

/**
 * @author aholland
 */
public class MimeTypesSearchResults extends SimpleSearchResults<MimeEntry>
{
	private static final long serialVersionUID = 1L;

	public MimeTypesSearchResults(List<MimeEntry> results, int offset, int available)
	{
		super(results, results.size(), offset, available);
	}
}
