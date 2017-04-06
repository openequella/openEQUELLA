package com.tle.common.taxonomy;

import com.tle.core.remoting.RemoteAbstractEntityService;

public interface RemoteTaxonomyService extends RemoteAbstractEntityService<Taxonomy>
{
	String ENTITY_TYPE = "TAXONOMY"; //$NON-NLS-1$

	boolean supportsTermAddition(String taxonomyUuid);

	boolean supportsTermSearching(String taxonomyUuid);

	boolean supportsTermBrowsing(String taxonomyUuid);
}
