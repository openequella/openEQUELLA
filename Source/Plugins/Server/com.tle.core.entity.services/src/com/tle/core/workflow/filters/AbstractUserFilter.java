/*
 * Created on Aug 4, 2004
 */
package com.tle.core.workflow.filters;

import java.util.Map;

@SuppressWarnings("nls")
public abstract class AbstractUserFilter extends BaseFilter
{
	private String userId;

	protected AbstractUserFilter(String userId)
	{
		this.userId = userId;
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("userId", userId);
	}

	@Override
	public String getWhereClause()
	{
		return "i.owner = :userId OR :userId IN ELEMENTS(i.collaborators)";
	}

	public String getUserID()
	{
		return userId;
	}
}
