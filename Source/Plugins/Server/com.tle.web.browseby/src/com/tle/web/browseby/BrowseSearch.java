package com.tle.web.browseby;

import java.util.List;

import com.tle.common.search.LiveItemSearch;
import com.tle.common.searching.Field;

public class BrowseSearch extends LiveItemSearch
{
	private static final long serialVersionUID = 1L;

	private List<Field> mustFields;

	public List<Field> getMustFields()
	{
		return mustFields;
	}

	public void setMustFields(List<Field> mustFields)
	{
		this.mustFields = mustFields;
	}

	@Override
	public List<Field> getMatrixFields()
	{
		return mustFields;
	}
}
