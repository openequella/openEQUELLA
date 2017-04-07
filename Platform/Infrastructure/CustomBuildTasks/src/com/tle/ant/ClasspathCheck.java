package com.tle.ant;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class ClasspathCheck extends Task
{
	private static final String ECLIPSE_CLASSPATH_FILENAME = ".classpath";

	private static final Set<String> IGNORE = new HashSet<String>(Arrays.asList("com.tle.core.spring",
		"com.tle.common.i18n"));

	private boolean verbose;
	private Set<String> exportInJpf = new HashSet<String>();
	private Set<String> exportInEclipse = new HashSet<String>();
	private Set<String> extraInJpf = new HashSet<String>();
	private SAXBuilder sax = Helper.createSAXBuilder();

	@Override
	public void execute() throws BuildException
	{
		Set<String> eclipseLocals = new HashSet<String>();
		Set<String> eclipseExports = new HashSet<String>();

		processEclipseClasspaths(eclipseLocals, eclipseExports);
		processPluginDependencies(eclipseLocals, eclipseExports);

		// Add them all together an see if there is anything left over
		eclipseLocals.addAll(eclipseExports);
		boolean errors = false;
		if( !eclipseLocals.isEmpty() )
		{
			log("Eclipse has the following dependencies that are not in JPF: " + eclipseLocals, Project.MSG_ERR);
			errors = true;
		}
		extraInJpf.removeAll(IGNORE);
		if( !extraInJpf.isEmpty() )
		{
			log("JPF has a dependencies on " + extraInJpf
				+ " but Eclipse does not.  Remove from one, or add to the other.", Project.MSG_ERR);
			errors = true;
		}
		if( !exportInEclipse.isEmpty() )
		{
			log("Eclipse exports " + exportInEclipse + " but JPF does not.  Make them the same!", Project.MSG_ERR);
			errors = true;
		}
		if( !exportInJpf.isEmpty() )
		{
			log("JPF exports " + exportInJpf + " but Eclipse does not.  Make them the same!", Project.MSG_ERR);
			errors = true;
		}
		if( errors )
		{
			throw new BuildException("Please fix the eclipse/jpf classpath errors in " + getProject().getBaseDir());
		}
	}

	private void processEclipseClasspaths(Set<String> local, Set<String> exported)
	{
		File startFile = new File(getProject().getBaseDir(), ECLIPSE_CLASSPATH_FILENAME);
		Document document = parseFile(startFile);
		Element root = document.getRootElement();

		@SuppressWarnings("unchecked")
		final List<Element> children = root.getChildren("classpathentry");
		for( Element element : children )
		{
			// Plug-ins path contain periods
			final String path = element.getAttributeValue("path");
			if( path != null && path.contains(".") )
			{
				// Only check src paths
				final String kind = element.getAttributeValue("kind");
				if( "src".equals(kind) )
				{
					(Boolean.parseBoolean(element.getAttributeValue("exported")) ? exported : local).add(path
						.substring(1));
				}
			}
		}
	}

	private void processPluginDependencies(Set<String> eclipseLocals, Set<String> eclipseExports)
	{
		File startFile = new File(getProject().getBaseDir(), "plugin-jpf.xml");
		Document document = parseFile(startFile);
		Element root = document.getRootElement();

		Element requires = root.getChild("requires");
		if( requires == null )
		{
			return;
		}
		@SuppressWarnings("unchecked")
		List<Element> imports = requires.getChildren("import");
		for( Element i : imports )
		{
			String pluginId = i.getAttributeValue("plugin-id");
			boolean exported = Boolean.parseBoolean(i.getAttributeValue("exported"));
			boolean removed = (exported ? eclipseExports : eclipseLocals).remove(pluginId);

			if( !removed )
			{
				if( exported && eclipseLocals.contains(pluginId) )
				{
					exportInJpf.add(pluginId);
					eclipseLocals.remove(pluginId);
				}
				else if( !exported && eclipseExports.contains(pluginId) )
				{
					exportInEclipse.add(pluginId);
					eclipseExports.remove(pluginId);
				}
				else
				{
					extraInJpf.add(pluginId);
				}
			}
		}
	}

	private Document parseFile(File file)
	{
		if( !file.exists() )
		{
			throw new BuildException("Eclipse classpath file does not exist: " + file.getAbsolutePath());
		}

		try
		{
			return sax.build(file);
		}
		catch( Exception ex )
		{
			throw new BuildException("Error parsing file: " + file.getAbsolutePath(), ex);
		}
	}

	public boolean isVerbose()
	{
		return verbose;
	}

	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}

	public static void main(String[] args) throws IOException
	{
		for( File dir : new File("../../Source/Plugins").listFiles() )
		{
			if( !dir.isDirectory() )
			{
				continue;
			}

			for( File pluginDir : dir.listFiles() )
			{
				if( !pluginDir.isDirectory() || !new File(pluginDir, "plugin-jpf.xml").exists() )
				{
					continue;
				}

				ClasspathCheck task = new ClasspathCheck();
				Project project = new Project();
				project.setBaseDir(pluginDir);
				project.setName(pluginDir.getName());
				task.setProject(project);
				DefaultLogger consoleLogger = new DefaultLogger();
				consoleLogger.setErrorPrintStream(System.err);
				consoleLogger.setOutputPrintStream(System.out);
				consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
				project.addBuildListener(consoleLogger);
				task.execute();
			}
		}
	}
}
