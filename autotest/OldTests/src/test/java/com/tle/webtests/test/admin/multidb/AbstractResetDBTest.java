package com.tle.webtests.test.admin.multidb;

import org.testng.annotations.BeforeClass;

import com.tle.webtests.test.AbstractTest;

public abstract class AbstractResetDBTest extends AbstractTest
{
	protected PGControl pgControl;
	protected DockerControl dckrControl;
	protected ServerControl serverControl;
	protected String dbPrefix;

	@Override
	protected boolean isInstitutional()
	{
		return false;
	}

	@BeforeClass
	public void cleanDb()
	{
		// This should be handled by Jenkins restarting the Docker container

		// pgControl = new PGControl();
		// pgControl.setHostname(testConfig.getProperty("multidb.dbhost"));
		// pgControl.setUsername(testConfig.getProperty("multidb.dbuser"));
		// pgControl.setPassword(testConfig.getProperty("multidb.dbpass"));
		// pgControl.setVersion(testConfig.getProperty("multidb.version"));
		//
		// dckrControl = new DockerControl();
		// dckrControl.setHostname(testConfig.getProperty("multidb.dbhost"));
		// dckrControl.setUsername(testConfig.getProperty("multidb.dbuser"));
		// dckrControl.setPassword(testConfig.getProperty("multidb.dbpass"));
		// dckrControl.setVersion(testConfig.getProperty("multidb.version"));
		// dbPrefix = testConfig.getProperty("multidb.dbprefix");
		//
		// if( !"true".equals(testConfig.getProperty("multidb.norestore")) )
		// {
		// if( "true".equals(testConfig.getProperty("multidb.eclipse")) )
		// {
		// serverControl = new EclipseServerControl(testConfig);
		// }
		// else
		// {
		// serverControl = new ManagerServerControl(testConfig);
		// }
		// serverControl.stop();
		// try
		// {
		// System.out.println("Resetting database");
		// dckrControl.reset();
		// while( !pgControl.checkDbUp(dbPrefix) )
		// {
		// System.out.println("Database not ready/up trying again in 2 seconds");
		// Thread.sleep(2000);
		// }
		// System.out.println("Database reset complete");
		// }
		// catch( Exception e )
		// {
		// Throwables.propagate(e);
		// }
		// finally
		// {
		// serverControl.start();
		// }
		// }
	}

	protected abstract void setupDatabases();
}
