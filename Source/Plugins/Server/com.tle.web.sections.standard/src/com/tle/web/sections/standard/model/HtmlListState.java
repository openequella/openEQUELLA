package com.tle.web.sections.standard.model;

import java.util.Set;

import com.google.common.collect.Sets;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.SingleSelectionList;

/**
 * The State class for selectable lists.
 * <p>
 * Allows for single/multiple selection and keeps a set of selected values. Thus
 * everything in the list must be uniquely identifiable.
 * 
 * @see SingleSelectionList
 * @see MultiSelectionList
 * @author jmaginnis
 */
public class HtmlListState extends HtmlMutableListState
{
	private boolean disallowMultiple;
	private boolean multiple;
	private Set<String> selectedValues;

	public boolean isDisallowMultiple()
	{
		return disallowMultiple;
	}

	public void setDisallowMultiple(boolean disallowMultiple)
	{
		this.disallowMultiple = disallowMultiple;
	}

	public boolean isMultiple()
	{
		return multiple;
	}

	public void setMultiple(boolean multiple)
	{
		this.multiple = multiple;
	}

	public Set<String> getSelectedValues()
	{
		return selectedValues;
	}

	public void setSelectedValues(Set<String> selectedValues)
	{
		this.selectedValues = selectedValues;
	}

	public void setSelectedValues(String... selectedValues)
	{
		this.selectedValues = Sets.newHashSet(selectedValues);
	}
}