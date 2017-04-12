package com.dytech.edge.installer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import com.dytech.common.text.Substitution;
import com.dytech.devlib.PropBagEx;
import com.dytech.installer.ForeignCommand;
import com.dytech.installer.InstallerException;
import com.dytech.installer.XpathResolver;

public class DatasourceConfig extends ForeignCommand
{
	private final String localPath;
	private final String database;
	private final String installPath;

	public DatasourceConfig(PropBagEx commandBag, PropBagEx resultBag) throws InstallerException
	{
		super(commandBag, resultBag);

		database = resultBag.getNode("datasource/dbtype"); //$NON-NLS-1$
		localPath = resultBag.getNode("installer/local"); //$NON-NLS-1$
		installPath = resultBag.getNode("install.path"); //$NON-NLS-1$
	}

	/**
	 * When this is called, we actually do all the work that needs doing. In
	 * this case, we're populating the database.
	 */
	@Override
	public void execute() throws InstallerException
	{
		propogateTaskStarted(2);

		String source = localPath + "/learningedge-config/hibernate.properties." + database; //$NON-NLS-1$
		String destination = installPath + "/learningedge-config/hibernate.properties"; //$NON-NLS-1$
		copy(source, destination);

		propogateSubtaskCompleted();

		propogateTaskCompleted();
	}

	private void copy(String source, String destination)
	{
		try( BufferedReader in = new BufferedReader(new FileReader(source));
			BufferedWriter out = new BufferedWriter(new FileWriter(destination)) )
		{
			Substitution sub = new Substitution(new XpathResolver(resultBag), "${ }"); //$NON-NLS-1$
			sub.resolve(in, out);
			propogateSubtaskCompleted();
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			throw new InstallerException("Problem copying datasources.xml");
		}
	}

	/**
	 * Return a nice string for the progress box to show.
	 */
	@Override
	public String toString()
	{
		return "Updating data sources XML...";
	}
}
