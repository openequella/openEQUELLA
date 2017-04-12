package com.tle.core.mimetypes.dao;

import java.util.Collection;
import java.util.List;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;
import com.tle.core.mimetypes.MimeTypesSearchResults;

public interface MimeEntryDao extends GenericInstitutionalDao<MimeEntry, Long>
{
	MimeTypesSearchResults searchByMimeType(String mimeType, int offset, int length);

	MimeTypesSearchResults searchAll(String query, int offset, int length);

	List<MimeEntry> getEntriesForExtensions(Collection<String> extensions);
}
