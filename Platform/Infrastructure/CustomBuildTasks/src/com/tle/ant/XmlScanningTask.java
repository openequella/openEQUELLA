package com.tle.ant;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author aholland
 */
public abstract class XmlScanningTask extends Task
{
	protected static final String PLUGIN_XML = "plugin-jpf.xml"; //$NON-NLS-1$
	protected static final String SPRING_XML = "spring.xml"; //$NON-NLS-1$

	protected static final String PLUGINS_PROJECT_NAME = "TLE Plugins"; //$NON-NLS-1$

	protected Path scanPath;
	protected File scanFolder;
	protected String lookFor;
	protected boolean verbose;

	@Override
	public void execute()
	{
		setup();
		try
		{
			scanFiles(scanFolder);
		}
		catch( Exception e )
		{
			throw new BuildException(e.getMessage(), e);
		}
	}

	protected void scanFiles(File folder) throws JDOMException, IOException
	{
		if( scanPath != null )
		{
			for( String includedFile : scanPath.list() )
			{
				File file = new File(includedFile);
				if( !file.isDirectory() )
				{
					doFile(file);
				}
			}
		}
		else
		{
			File file = new File(folder, lookFor);
			if( file.exists() )
			{
				doFile(file);
			}
			else
			{
				for( File child : folder.listFiles() )
				{
					if( child.isDirectory() )
					{
						scanFiles(child);
					}
				}
			}
		}
	}

	protected void setup()
	{
		if( lookFor == null )
		{
			lookFor = defaultLookFor();
		}
		if( scanFolder == null )
		{
			scanFolder = defaultScanFolder();
		}
	}

	protected abstract String defaultLookFor();

	protected File defaultScanFolder()
	{
		return Helper.getBaseDirForProjectName(getProject(), PLUGINS_PROJECT_NAME, false);
	}

	protected abstract void doFile(File file) throws JDOMException, IOException;

	@SuppressWarnings("nls")
	protected String getJPFParamValue(Element extensionXml, String paramId)
	{
		@SuppressWarnings("unchecked")
		List<Element> params = extensionXml.getChildren("parameter");
		for( Element param : params )
		{
			if( param.getAttributeValue("id").equals(paramId) )
			{
				return param.getAttributeValue("value");
			}
		}
		return null;
	}

	public File getScanFolder()
	{
		return scanFolder;
	}

	public void setScanFolder(File scanFolder)
	{
		this.scanFolder = scanFolder;
	}

	public String getLookFor()
	{
		return lookFor;
	}

	public void setLookFor(String lookFor)
	{
		this.lookFor = lookFor;
	}

	public void addPath(Path path)
	{
		scanPath = path;
	}

	public boolean isVerbose()
	{
		return verbose;
	}

	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}
}
