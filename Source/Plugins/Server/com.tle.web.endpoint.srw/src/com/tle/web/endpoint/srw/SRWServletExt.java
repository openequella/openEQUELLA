/*
 * Created on 2/06/2006
 */
package com.tle.web.endpoint.srw;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ORG.oclc.os.SRW.SRWServletInfo;

import com.tle.core.guice.Bind;

@Bind
@Singleton
public class SRWServletExt extends ORG.oclc.os.SRW.SRWServlet
{
	private static final long serialVersionUID = 1L;

	private static final String DB_NAME = "tle"; //$NON-NLS-1$

	@Override
	public void init() throws ServletException
	{
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		final ServletConfig config = super.getServletConfig();
		super.init();

		// We do so we don't have specify a 'database' on the URL path
		// eg. http://localhost:8988/dev/first/srw/
		srwInfo = new SRWServletInfo()
		{
			@Override
			public String getDBName(HttpServletRequest request)
			{
				return DB_NAME;
			}
		};
		srwInfo.init(config);
		srwInfo.getProperties().put("defaultSchema", SRWDatabaseExt.DEFAULT_SCHEMA.getTleId()); //$NON-NLS-1$
		srwInfo.getProperties().put("db." + DB_NAME + ".class", SRWDatabaseExt.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		super.service(req, resp);
	}
}
