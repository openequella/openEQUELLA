package com.tle.web.errors;

import com.dytech.edge.exceptions.NotFoundException;
import com.tle.core.guice.Bind;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;

@Bind
public class ExternalErrorSection extends DefaultErrorSection implements ParametersEventListener
{
	@SuppressWarnings("nls")
	@Override
	public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception
	{
		Throwable ex = null;
		String method = event.getParameter("method", false);
		if( method != null )
		{
			if( "notFound".equals(method) )
			{
				ex = new NotFoundException("404", true);
			}
			else if( "accessDenied".equals(method) )
			{
				ex = new AccessDeniedException("403");
			}
			else if( "throwable".equals(method) )
			{
				ex = (Throwable) info.getRequest().getAttribute("javax.servlet.error.exception");
			}
			if( ex != null )
			{
				info.setAttribute(SectionInfo.KEY_ORIGINAL_EXCEPTION, ex);
				info.setAttribute(SectionInfo.KEY_MATCHED_EXCEPTION, ex);
				info.preventGET();
			}
		}
	}
}
