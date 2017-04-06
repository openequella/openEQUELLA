package com.tle.mypages.web.model;

import com.tle.web.sections.annotations.Bookmarked;

/*
 * @author aholland
 */
public class MyPagesTitleModel
{
	@Bookmarked
	private boolean error;

	public boolean getError()
	{
		return error;
	}

	public void setError(boolean error)
	{
		this.error = error;
	}
}
