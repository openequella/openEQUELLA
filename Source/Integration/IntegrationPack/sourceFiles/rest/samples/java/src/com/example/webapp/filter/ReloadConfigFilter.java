package com.example.webapp.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.example.service.Config;

/**
 * Ensures the config file is always up to date
 */
public class ReloadConfigFilter implements Filter
{
	private FilterConfig filterConfig;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
		ServletException
	{
		try
		{
			Config.ensureLoaded(filterConfig.getServletContext());
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig f) throws ServletException
	{
		filterConfig = f;
	}

	@Override
	public void destroy()
	{
		filterConfig = null;
	}
}
