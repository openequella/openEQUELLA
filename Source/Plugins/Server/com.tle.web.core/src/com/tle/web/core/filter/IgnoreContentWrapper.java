package com.tle.web.core.filter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class IgnoreContentWrapper extends HttpServletResponseWrapper
{
	public IgnoreContentWrapper(HttpServletResponse resp)
	{
		super(resp);
	}

	@Override
	public void addHeader(String header, String val)
	{
		if( header.equals("Content-Location") ) //$NON-NLS-1$
		{
			return;
		}

		if( header.equals("X-Content-Location") ) //$NON-NLS-1$
		{
			header = "Content-Location"; //$NON-NLS-1$
		}

		super.addHeader(header, val);
	}

	@Override
	public void setHeader(String header, String val)
	{
		if( header.equals("Content-Location") ) //$NON-NLS-1$
		{
			return;
		}

		if( header.equals("X-Content-Location") ) //$NON-NLS-1$
		{
			header = "Content-Location"; //$NON-NLS-1$
		}

		super.setHeader(header, val);
	}

}
