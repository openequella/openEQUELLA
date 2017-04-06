package com.tle.web.sections.ajax.exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.tle.common.Utils;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.errors.SectionsExceptionHandler;
import com.tle.web.sections.events.SectionEvent;

/**
 * @author aholland
 */
@Bind
@Singleton
public class AjaxExceptionHandler implements SectionsExceptionHandler
{
	private static Log LOGGER = LogFactory.getLog(AjaxExceptionHandler.class);
	private static ObjectMapper mapper;

	static
	{
		mapper = new ObjectMapper();
	}

	@Override
	public boolean canHandle(SectionInfo info, Throwable ex, SectionEvent<?> event)
	{
		return (ex instanceof AjaxException);
	}

	@SuppressWarnings("nls")
	@Override
	public void handle(Throwable exception, SectionInfo info, SectionsController controller, SectionEvent<?> event)
	{
		LOGGER.error("Error during ajax request", exception);
		info.setRendered();
		final HttpServletResponse response = info.getResponse();
		if( !response.isCommitted() )
		{
			response.reset();
			response.setStatus(500);
			response.setHeader("Content-Type", "application/json");
			try
			{
				final Map<String, Object> message = new HashMap<String, Object>();
				final Throwable rootCause = Throwables.getRootCause(exception);
				final String errorMessage = Utils.coalesce(rootCause.getMessage(), rootCause.getClass()
					.getCanonicalName());
				message.put("message", errorMessage);
				mapper.writeValue(response.getWriter(), message);
			}
			catch( IOException e )
			{
				throw new SectionsRuntimeException(e);
			}
		}
	}
}
