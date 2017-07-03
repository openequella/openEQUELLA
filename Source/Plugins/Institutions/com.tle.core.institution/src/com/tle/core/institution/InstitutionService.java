package com.tle.core.institution;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.Multimap;
import com.tle.beans.Institution;
import com.tle.core.migration.SchemaInfo;

public interface InstitutionService
{
	Collection<InstitutionStatus> getAllInstitutions();

	InstitutionStatus getInstitutionStatus(long institutionId);

	Collection<Institution> enumerateAvailable();

	long getSchemaIdForInstitution(Institution institution);

	Institution getInstitution(long institutionId);

	void update(Institution institution);

	void setEnabled(long instId, boolean enabled);

	void deleteInstitution(Institution institution);

	Institution createInstitution(Institution newInstitution, long schemaId);

	boolean canAddInstitution();

	Multimap<Long, Institution> getAvailableMap();

	List<InstitutionValidationError> validate(Institution institution);

	<T> InstitutionCache<T> newInstitutionAwareCache(CacheLoader<Institution, T> loader);

	List<SchemaInfo> getAllSchemaInfos();

	URL getInstitutionUrl();

	URI getInstitutionUri();

	URL getInstitutionUrl(Institution institution);

	String institutionalise(String url);

	/**
	 * Does not check to see if the url is actually an institution URL in the
	 * first place. Take care.
	 * 
	 * @param url
	 * @return
	 */
	String removeInstitution(String url);

	boolean isInstitutionUrl(String url);
}
