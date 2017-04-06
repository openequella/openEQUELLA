package com.tle.reporting.oda;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDriver;
import org.eclipse.datatools.connectivity.oda.LogConfiguration;
import org.eclipse.datatools.connectivity.oda.OdaException;

public class Driver implements IDriver
{
	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.datatools.connectivity.oda.IDriver#getConnection(java.lang
	 * .String)
	 */
	public IConnection getConnection(String datasourceId) throws OdaException
	{
		return new Connection();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDriver#getMaxConnections()
	 */
	public int getMaxConnections() throws OdaException
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.datatools.connectivity.oda.IDriver#setAppContext(java.lang
	 * .Object)
	 */
	public void setAppContext(Object appContext) throws OdaException
	{
		// We don't care about the context
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.datatools.connectivity.oda.IDriver#setLogConfiguration(org
	 * .eclipse.datatools.connectivity.oda.LogConfiguration)
	 */
	public void setLogConfiguration(LogConfiguration config) throws OdaException
	{
		// We don't care about the logger configuration
	}
}
