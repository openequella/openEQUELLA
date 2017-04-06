package com.tle.core.reporting.web;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.tle.core.reporting.ReportPrivileges;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.user.UserService;
import com.tle.core.xstream.XmlService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.reporting.IResultSetExt;
import com.tle.reporting.LearningEdgeOdaDelegate;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;

public class ReportStreamingService extends AbstractPrototypeSection<ReportStreamingService.ReportStreamingModel>
{

	private static final String STATUS_OK = "OK"; //$NON-NLS-1$
	@Inject
	private UserService userService;
	@Inject
	private XmlService xmlService;
	@Inject
	private LearningEdgeOdaDelegate serverDelegate;
	@Inject
	private TLEAclManager aclManager;

	// UGH. What a bunch of bollocks.
	private static Log LOGGER;
	static
	{
		ClassLoader old = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(ReportStreamingService.class.getClassLoader());
			LOGGER = LogFactory.getLog(ReportStreamingService.class);
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(old);
		}
	}

	@Override
	public Class<ReportStreamingModel> getModelClass()
	{
		return ReportStreamingModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return ""; //$NON-NLS-1$
	}

	public void login(SectionInfo info) throws IOException
	{
		ReportStreamingModel model = getModel(info);
		ObjectOutputStream oos = model.getOutputStream();
		userService.login(model.getUsername(), model.getPassword(),
			userService.getWebAuthenticationDetails(info.getRequest()), true);
		checkReportDesigner();
		oos.writeObject(STATUS_OK);
	}

	public void query(SectionInfo info) throws IOException, OdaException
	{
		ReportStreamingModel model = getModel(info);
		ObjectOutputStream oos = model.getOutputStream();
		String type = model.getType();

		int maxRows = model.getMaxRows();
		String query = model.getQuery();
		List<Object> params = xmlService.deserialiseFromXml(getClass().getClassLoader(), model.getParams());
		IResultSetExt resultSet = serverDelegate.executeQuery(type, query, params, maxRows);
		try
		{
			IResultSetMetaData metaData = resultSet.getMetaData();
			int numCols = metaData.getColumnCount();
			oos.writeObject(STATUS_OK);
			oos.writeObject(metaData);
			while( resultSet.next() )
			{
				Object[] row = new Object[numCols];
				for( int i = 1; i <= numCols; i++ )
				{
					row[i - 1] = resultSet.getObject(i);
				}
				oos.writeObject(row);
			}
			oos.writeObject(new Object[]{});
		}
		finally
		{
			resultSet.close();
		}
	}

	public void paramMetadata(SectionInfo info) throws IOException, OdaException
	{
		ReportStreamingModel model = getModel(info);
		ObjectOutputStream oos = model.getOutputStream();
		String type = model.getType();
		String query = model.getQuery();
		List<Object> params = xmlService.deserialiseFromXml(getClass().getClassLoader(), model.getParams());

		IParameterMetaData metadata = serverDelegate.getParamterMetadata(type, query, params);

		oos.writeObject(STATUS_OK);
		oos.writeObject(metadata);
	}

	public void metadata(SectionInfo info) throws IOException, OdaException
	{
		ReportStreamingModel model = getModel(info);
		ObjectOutputStream oos = model.getOutputStream();
		String type = model.getType();

		Map<String, ?> metadata = serverDelegate.getDatasourceMetadata(type);

		oos.writeObject(STATUS_OK);
		oos.writeObject(metadata);
	}

	@DirectEvent
	public void process(SectionInfo info) throws IOException
	{
		info.setRendered();
		HttpServletResponse response = info.getResponse();
		response.setContentType("java/x-serialized"); //$NON-NLS-1$
		ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());
		try
		{
			ReportStreamingModel model = getModel(info);
			model.setOutputStream(oos);
			String method = model.getMethod();
			if( !"login".equals(method) ) //$NON-NLS-1$
			{
				checkReportDesigner();
			}

			Method dispMethod = getClass().getMethod(model.getMethod(), SectionInfo.class);
			dispMethod.invoke(this, info);
		}
		catch( Exception e )
		{
			Throwable cause;
			if( e instanceof InvocationTargetException )
			{
				cause = ((InvocationTargetException) e).getTargetException();
			}
			else
			{
				cause = e;
			}
			LOGGER.error("Error", cause); //$NON-NLS-1$
			oos.writeObject("ERROR"); //$NON-NLS-1$
			oos.writeObject(cause.getMessage());
		}
		oos.close();
	}

	@SuppressWarnings("nls")
	private void checkReportDesigner()
	{
		if( aclManager.filterNonGrantedPrivileges(Collections.singleton(ReportPrivileges.DESIGN_REPORT)).isEmpty() )
		{
			throw new AccessDeniedException("Not a report designer");
		}
	}

	public static class ReportStreamingModel
	{
		@Bookmarked
		private String method;
		@Bookmarked
		private String username;
		@Bookmarked
		private String password;
		@Bookmarked
		private String type;
		@Bookmarked
		private String query;
		@Bookmarked
		private int maxRows;
		@Bookmarked
		private String params;

		private ObjectOutputStream outputStream;

		public String getMethod()
		{
			return method;
		}

		public void setMethod(String method)
		{
			this.method = method;
		}

		public String getUsername()
		{
			return username;
		}

		public void setUsername(String username)
		{
			this.username = username;
		}

		public String getPassword()
		{
			return password;
		}

		public void setPassword(String password)
		{
			this.password = password;
		}

		public ObjectOutputStream getOutputStream()
		{
			return outputStream;
		}

		public void setOutputStream(ObjectOutputStream outputStream)
		{
			this.outputStream = outputStream;
		}

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public String getQuery()
		{
			return query;
		}

		public void setQuery(String query)
		{
			this.query = query;
		}

		public int getMaxRows()
		{
			return maxRows;
		}

		public void setMaxRows(int maxRows)
		{
			this.maxRows = maxRows;
		}

		public String getParams()
		{
			return params;
		}

		public void setParams(String params)
		{
			this.params = params;
		}

	}
}
