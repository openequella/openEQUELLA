package com.tle.upgrade;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

@SuppressWarnings("nls")
public abstract class PropertyFileModifier
{
	public static String MANDATORY_CONFIG = "mandatory-config.properties";
	public static String OPTIONAL_CONFIG = "optional-config.properties";
	public static String HIBERNATE_CONFIG = "hibernate.properties";

	private final File file;
	protected final PropertiesConfiguration props;

	public PropertyFileModifier(File propertiesFile) throws ConfigurationException
	{
		this.file = propertiesFile;
		props = new PropertiesConfiguration();
		props.setLayout(new ExtendedPropertiesLayout(props));
		props.setEncoding("UTF-8");
		props.setDelimiterParsingDisabled(true);

		if( file.exists() )
		{
			props.load(file);
		}
	}

	public void updateProperties() throws IOException
	{
		if( modifyProperties(props) )
		{
			save();
		}
	}

	protected void save() throws IOException
	{
		final File bakFile = getBakFile(file.getParentFile());
		final boolean origExists = file.exists();

		new FileCopier(file, bakFile, false).rename();
		try
		{
			if( !origExists )
			{
				file.getParentFile().mkdirs();
			}
			props.save(file);
			if( origExists )
			{
				bakFile.delete();
			}
		}
		catch( Exception t )
		{
			new FileCopier(bakFile, file, false).rename();
			throw new RuntimeException(t);
		}
	}

	/**
	 * Makes sure a previous backup is never overwritten
	 * 
	 * @param parent
	 * @return
	 */
	private File getBakFile(File parent)
	{
		int num = 1;
		File bak = new File(parent, file.getName() + ".bak"); //$NON-NLS-1$
		while( bak.exists() )
		{
			bak = new File(parent, file.getName() + '.' + num + ".bak"); //$NON-NLS-1$
			num++;
		}
		return bak;
	}

	/**
	 * @param props
	 * @return true if the contents of the file were changed in any way
	 */
	protected abstract boolean modifyProperties(PropertiesConfiguration props);
}
