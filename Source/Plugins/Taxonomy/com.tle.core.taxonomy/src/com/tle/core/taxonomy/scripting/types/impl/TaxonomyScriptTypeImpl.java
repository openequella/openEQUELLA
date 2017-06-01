/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.taxonomy.scripting.types.impl;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.TaxonomyConstants;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.core.taxonomy.TermResult;
import com.tle.core.taxonomy.scripting.types.TaxonomyScriptType;
import com.tle.core.taxonomy.scripting.types.TermScriptType;

/**
 * @author aholland
 */
public class TaxonomyScriptTypeImpl implements TaxonomyScriptType
{
	private static final long serialVersionUID = 1L;

	protected final TaxonomyService service;
	protected final String taxonomyUuid;

	public TaxonomyScriptTypeImpl(TaxonomyService service, String taxonomyUuid)
	{
		this.service = service;
		this.taxonomyUuid = taxonomyUuid;
	}

	@Override
	public List<TermScriptType> searchTerms(String query)
	{
		return convertTermResults(service.searchTerms(taxonomyUuid, query + '*', SelectionRestriction.UNRESTRICTED, 50,
			false).getSecond());
	}

	@Override
	public TermScriptType insertTerm(String parentFullPath, String termValue)
	{
		service.addTerm(taxonomyUuid, parentFullPath, termValue, true);
		String newPath = parentFullPath + TaxonomyConstants.TERM_SEPARATOR + termValue;
		return new TermScriptTypeImpl(service, taxonomyUuid, service.getTerm(taxonomyUuid, newPath));
	}

	@Override
	public List<TermScriptType> getChildTerms(TermScriptType term)
	{
		final String parentTermPath = (term != null ? term.getFullPath() : null);
		return convertTermResults(service.getChildTerms(taxonomyUuid, parentTermPath));
	}

	@Override
	public TermScriptType getParentTerm(TermScriptType term)
	{
		String termPath = term.getFullPath();
		if( !Check.isEmpty(termPath) )
		{
			int index = termPath.lastIndexOf(TaxonomyConstants.TERM_SEPARATOR);
			if( index > 0 )
			{
				TermResult termResult = service.getTerm(taxonomyUuid, termPath.substring(0, index));
				if( termResult != null )
				{
					return new TermScriptTypeImpl(service, taxonomyUuid, termResult);
				}
			}
		}
		return null;
	}

	@Override
	public TermScriptType getTerm(String fullTermPath)
	{
		final TermResult term = service.getTerm(taxonomyUuid, fullTermPath);
		if( term != null )
		{
			return new TermScriptTypeImpl(service, taxonomyUuid, term);
		}
		return null;
	}

	@Override
	public boolean supportsTermAddition()
	{
		return service.supportsTermAddition(taxonomyUuid);
	}

	@Override
	public boolean supportsTermBrowsing()
	{
		return service.supportsTermBrowsing(taxonomyUuid);
	}

	@Override
	public boolean supportsTermSearching()
	{
		return service.supportsTermSearching(taxonomyUuid);
	}

	private List<TermScriptType> convertTermResults(List<TermResult> terms)
	{
		return Lists.transform(terms, new Function<TermResult, TermScriptType>()
		{
			@Override
			public TermScriptType apply(TermResult term)
			{
				return new TermScriptTypeImpl(service, taxonomyUuid, term);
			}
		});
	}
}
