package com.tle.blackboard.buildingblock.net;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;

import com.tle.blackboard.buildingblock.data.WrappedUser;

// @NonNullByDefault
public class ContextTag extends TagSupport implements TryCatchFinally
{
	private WrappedUser user;

	@Override
	public int doStartTag()
	{
		try
		{
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			user = WrappedUser.getUser(request);
		}
		catch( Exception t )
		{
			// TODO: Can this safely be ignored?
		}
		return Tag.EVAL_BODY_INCLUDE;
	}

	@Override
	public void doCatch(Throwable t) throws Throwable
	{
		// Never happen
	}

	@Override
	public void doFinally()
	{
		if( user != null )
		{
			user.clearContext();
		}
	}
}
