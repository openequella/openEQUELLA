package com.tle.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.jdom.input.SAXBuilder;

public class Helper
{
	public static File getBaseDirForProjectName(Project project, String projectName, boolean ignoreMissing)
	{
		String projectBase = project.getProperty(projectName + ".base");
		if( projectBase == null )
		{
			if( ignoreMissing )
			{
				return null;
			}
			else
			{
				throw new BuildException("No base property defined for project '" + projectName + "'\n");
			}
		}

		File projectFile = new File(projectBase);
		if( !projectFile.exists() || !projectFile.isDirectory() )
		{
			throw new BuildException("Base directory does not exist for project '" + projectName + "'\n");
		}

		return projectFile;
	}

	@SuppressWarnings("nls")
	public static SAXBuilder createSAXBuilder()
	{
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setReuseParser(true);
		builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		return builder;
	}
}
