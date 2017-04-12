package com.tle.core.dao;

import java.util.Collection;
import java.util.Map;

import com.tle.beans.Language;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

public interface LanguageDao extends GenericInstitutionalDao<Language, Long>
{
	Map<Long, String> getNames(Collection<Long> bundleRef);

	void deleteBundles(Collection<Long> bundles);
}
