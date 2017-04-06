package com.tle.core.remoting;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.beans.Language;

/**
 * @author Nicholas Read
 */
public interface RemoteLanguageService
{
	Map<Long, String> getNames(Collection<Long> bundleRefs);

	List<Language> getLanguages();
}