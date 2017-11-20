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
