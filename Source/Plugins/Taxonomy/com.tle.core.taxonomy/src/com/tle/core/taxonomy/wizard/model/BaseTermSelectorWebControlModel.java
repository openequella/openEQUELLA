package com.tle.core.taxonomy.wizard.model;

import java.util.Set;

public class BaseTermSelectorWebControlModel
{
	/**
	 * A short lived holder for terms just selected
	 */
	private Set<String> selectedTerms;

	public Set<String> getSelectedTerms()
	{
		return selectedTerms;
	}

	public void setSelectedTerms(Set<String> selectedTerms)
	{
		this.selectedTerms = selectedTerms;
	}
}