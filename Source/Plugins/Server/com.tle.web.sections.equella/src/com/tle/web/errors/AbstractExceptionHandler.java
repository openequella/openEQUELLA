package com.tle.web.errors;

import javax.servlet.http.HttpServletRequest;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.errors.SectionsExceptionHandler;
import com.tle.web.sections.events.SectionEvent;

public abstract class AbstractExceptionHandler implements SectionsExceptionHandler
{
	protected Throwable getFirstCause(Throwable ex)
	{
		if( ex == null )
		{
			return null;
		}
		if( ex.getCause() == null )
		{
			return ex;
		}
		return getFirstCause(ex.getCause());
	}

	@Override
	public boolean canHandle(SectionInfo info, Throwable ex, SectionEvent<?> event)
	{
		return !hasAlreadyBeenHandled(info, ex, event);
	}

	protected boolean hasAlreadyBeenHandled(SectionInfo info, Throwable ex, SectionEvent<?> event)
	{
		HttpServletRequest request = info.getRequest();
		if( request == null )
		{
			return true;
		}
		else
		{
			return (request.getAttribute(getClass().getName()) != null);
		}
	}

	protected void markHandled(SectionInfo info)
	{
		info.getRequest().setAttribute(getClass().getName(), true);
	}
}
