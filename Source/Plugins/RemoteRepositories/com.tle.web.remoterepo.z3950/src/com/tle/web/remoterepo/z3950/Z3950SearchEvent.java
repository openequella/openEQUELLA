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
