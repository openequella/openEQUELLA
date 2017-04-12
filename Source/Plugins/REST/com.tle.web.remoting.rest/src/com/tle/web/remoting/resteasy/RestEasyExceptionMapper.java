package com.tle.web.remoting.resteasy;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.edge.common.LockedException;
import com.dytech.edge.exceptions.InvalidDataException;
import com.dytech.edge.exceptions.WebException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tle.beans.item.ItemEditingException;
import com.tle.exceptions.AccessDeniedException;

@Provider
public class RestEasyExceptionMapper implements ExceptionMapper<Throwable>
{
	private static final Log LOGGER = LogFactory.getLog(RestEasyExceptionMapper.class);

	@Context
	protected HttpHeaders httpHeaders;

	@SuppressWarnings("nls")
	@Override
	public Response toResponse(Throwable t)
	{
		final WebApplicationException webAppException = mapException(t);
		Response response = webAppException.getResponse();
		final int status = response.getStatus();
		if( status >= 500 )
		{
			LOGGER.error("REST API error", t);
		}
		if( response.getEntity() == null )
		{
			ResponseBuilder builder = Response.fromResponse(response);
			response = builder.entity(convertExceptionToJsonResponse(response.getStatus(), webAppException))
				.type(MediaType.APPLICATION_JSON).build();
		}

		return response;
	}

	public static WebApplicationException mapException(Throwable t)
	{
		final WebApplicationException webAppException;
		if( t instanceof WebApplicationException )
		{
			webAppException = (WebApplicationException) t;
		}
		else if( t instanceof WebException )
		{
			final WebException w = (WebException) t;
			webAppException = new WebApplicationException(w, w.getCode());
		}
		else if( t instanceof ItemEditingException || t instanceof InvalidDataException )
		{
			webAppException = new WebApplicationException(t, Status.BAD_REQUEST);
		}
		else if( t instanceof AccessDeniedException )
		{
			webAppException = new WebApplicationException(t, Status.FORBIDDEN);
		}
		else if( t instanceof LockedException )
		{
			webAppException = new WebApplicationException(t, Status.CONFLICT);
		}
		else if( t instanceof NotFoundException || t instanceof com.dytech.edge.exceptions.NotFoundException )
		{
			webAppException = new WebApplicationException(t, Status.NOT_FOUND);
		}
		else
		{
			webAppException = new WebApplicationException(t);
		}
		return webAppException;
	}

	public static ErrorResponse convertExceptionToJsonResponse(int status, WebApplicationException webAppException)
	{
		final ErrorResponse err = new ErrorResponse();
		err.setCode(status);
		String msg = webAppException.getMessage();
		// if we happen to be using an 'unofficial' HTTP code, just repeat the
		// exception message
		String statusCodeErr = Status.fromStatusCode(status) != null ? Status.fromStatusCode(status).getReasonPhrase()
			: msg;
		err.setError(statusCodeErr);
		if( webAppException.getCause() != null )
		{
			msg = webAppException.getCause().getMessage();
		}
		err.setErrorDescription(msg);
		return err;
	}

	public static class ErrorResponse
	{
		private int code;
		private String error;
		private String errorDescription;

		@JsonProperty("code")
		public int getCode()
		{
			return code;
		}

		public void setCode(int code)
		{
			this.code = code;
		}

		@JsonProperty("error")
		public String getError()
		{
			return error;
		}

		public void setError(String error)
		{
			this.error = error;
		}

		@JsonProperty("error_description")
		public String getErrorDescription()
		{
			return errorDescription;
		}

		public void setErrorDescription(String errorDescription)
		{
			this.errorDescription = errorDescription;
		}
	}
}
