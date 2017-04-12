package com.tle.core.taxonomy.datasource.internal;

import java.util.List;
import java.util.Map;

import com.dytech.edge.exceptions.InvalidDataException;
import com.tle.common.Pair;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.TaxonomyConstants;
import com.tle.core.taxonomy.TermResult;
import com.tle.core.taxonomy.TermService;
import com.tle.core.taxonomy.datasource.TaxonomyDataSource;

public class InternalTaxonomyDataSource implements TaxonomyDataSource
{
	private final Taxonomy taxonomy;
	private final TermService termService;
	private final boolean supportsTermAddition;

	InternalTaxonomyDataSource(Taxonomy taxonomy, TermService termService)
	{
		this.taxonomy = taxonomy;
		this.termService = termService;
		this.supportsTermAddition = taxonomy.getAttribute(TaxonomyConstants.TERM_ALLOW_ADDITION, false);
	}

	@Override
	public TermResult getTerm(String fullTermPath)
	{
		return termService.getTermResult(taxonomy, fullTermPath);
	}

	@Override
	public List<TermResult> getChildTerms(String parentFullPath)
	{
		return termService.listTermResults(taxonomy, parentFullPath);
	}

	@Override
	public String getDataForTerm(String fullTermPath, String key)
	{
		return termService.getData(taxonomy, fullTermPath, key);
	}

	@Override
	public Pair<Long, List<TermResult>> searchTerms(String query, SelectionRestriction restriction, int limit,
		boolean searchFullTerm)
	{
		return termService.searchTerms(taxonomy, query, restriction, limit, searchFullTerm);
	}

	@Override
	public TermResult addTerm(String parentFullPath, String termValue, boolean createHierarchy)
	{
		if( supportsTermAddition() )
		{
			return termService.addTerm(taxonomy, parentFullPath, termValue, createHierarchy);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public void validateTerm(String parentFullTermPath, String termValue, boolean requireParent)
		throws InvalidDataException
	{
		termService.validateTerm(taxonomy, parentFullTermPath, termValue, requireParent);
	}

	@Override
	public boolean supportsTermAddition()
	{
		return supportsTermAddition;
	}

	@Override
	public boolean supportsTermBrowsing()
	{
		return true;
	}

	@Override
	public boolean supportsTermSearching()
	{
		return true;
	}

	@Override
	public boolean isReadonly()
	{
		return false;
	}

	@Override
	public TermResult getTermByUuid(String termUuid)
	{
		return termService.getTermResultByUuid(taxonomy, termUuid);
	}

	@Override
	public String getDataByTermUuid(String termUuid, String dataKey)
	{
		return termService.getDataByTermUuid(taxonomy, termUuid, dataKey);
	}

	@Override
	public Map<String, String> getAllDataByTermUuid(String termUuid)
	{
		return termService.getAllDataByTermUuid(taxonomy, termUuid);
	}
}
