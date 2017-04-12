package com.tle.web.remoting.resteasy;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.DefaultOptionsMethodException;

/**
 * @author Aaron
 *
 */
@Provider
public class DefaultOptionsExceptionMapper implements ExceptionMapper<DefaultOptionsMethodException>
{
	private static final String ALL_METHODS = "GET,HEAD,POST,PUT,DELETE,OPTIONS";

	@Context
	protected HttpHeaders httpHeaders;

	@Override
	public Response toResponse(DefaultOptionsMethodException arg0)
	{
		final ResponseBuilder response = Response.ok();
		response.header("Access-Control-Allow-Origin", "*");
		response.header("Access-Control-Allow-Methods", ALL_METHODS);
		response.header("Allow", ALL_METHODS);
		response.header("Access-Control-Allow-Headers", "X-Authorization, Content-Type");
		response.header("Access-Control-Max-Age", TimeUnit.DAYS.toSeconds(1));
		response.header("Access-Control-Expose-Headers", "Location");
		return response.build();
	}
}
