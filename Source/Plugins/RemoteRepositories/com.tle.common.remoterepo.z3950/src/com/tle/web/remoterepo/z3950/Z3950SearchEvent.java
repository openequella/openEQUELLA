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

package com.tle.web.remoterepo.z3950;

import com.tle.beans.entity.FederatedSearch;
import com.tle.core.remoterepo.z3950.AdvancedSearchOptions;
import com.tle.core.remoterepo.z3950.Z3950Constants.Operator;
import com.tle.web.remoterepo.event.RemoteRepoSearchEvent;
import com.tle.web.sections.SectionId;

/**
 * @author Aaron
 */
public class Z3950SearchEvent extends RemoteRepoSearchEvent<Z3950SearchEvent>
{
	private AdvancedSearchOptions advanced;

	public Z3950SearchEvent(SectionId sectionId, FederatedSearch search)
	{
		super(sectionId, search);
	}

	public void addExtra(String attributes, String term, Operator operator)
	{
		if( advanced == null )
		{
			advanced = new AdvancedSearchOptions();
		}

		advanced.addExtra(attributes, term, operator);
	}

	public AdvancedSearchOptions getAdvancedOptions()
	{
		return advanced;
	}
}
