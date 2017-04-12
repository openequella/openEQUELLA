package com.tle.core.taxonomy.scripting.types.impl;

import com.tle.core.taxonomy.TaxonomyService;
import com.tle.core.taxonomy.TermResult;
import com.tle.core.taxonomy.scripting.types.TermScriptType;

/**
 * @author aholland
 */
public class TermScriptTypeImpl implements TermScriptType
{
	private final TaxonomyService taxonomyService;
	private final String taxonomyUuid;
	private final TermResult wrapped;

	public TermScriptTypeImpl(TaxonomyService taxonomyService, String taxonomyUuid, TermResult wrapped)
	{
		this.taxonomyService = taxonomyService;
		this.taxonomyUuid = taxonomyUuid;
		this.wrapped = wrapped;
	}

	@Override
	public String getFullPath()
	{
		return wrapped.getFullTerm();
	}

	@Override
	public String getTerm()
	{
		return wrapped.getTerm();
	}

	@Override
	public boolean isLeaf()
	{
		return wrapped.isLeaf();
	}

	@Override
	public String getData(String attributeKey)
	{
		return taxonomyService.getDataForTerm(taxonomyUuid, getFullPath(), attributeKey);
	}
}
