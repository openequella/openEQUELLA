package com.tle.ant;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * @author Nicholas Read
 */
public class EclipseClasspath extends Task
{
	private static final String ECLIPSE_CLASSPATH_FILENAME = ".classpath";
	private static final String DEFAULT_PATH_ID = "eclipse.classpath";

	private boolean verbose;
	private String pathId;
	private File basedir;

	private Path classPath;
	private Set<File> visitedProjects = new HashSet<File>();
	private static Map<String, File> pluginMap = Collections.synchronizedMap(new HashMap<String, File>());
	private SAXBuilder sax = Helper.createSAXBuilder();

	public EclipseClasspath()
	{
		super();
	}

	@Override
	public void execute() throws BuildException
	{
		// This is the Ant Path-like structure
		classPath = new Path(getProject());

		// Start off with classpath in current directory
		if( basedir == null )
		{
			basedir = getProject().getBaseDir();
		}
		processClasspath(basedir, false);

		// Set the new path into the project
		if( pathId == null || pathId.trim().length() == 0 )
		{
			pathId = DEFAULT_PATH_ID;
		}

		getProject().addReference(pathId, classPath);
		if( isVerbose() )
		{
			log(pathId + ": " + classPath.toString());
		}
	}

	private void processClasspath(File baseDir, boolean exportedOnly)
	{
		if( visitedProjects.contains(baseDir) )
		{
			return;
		}
		if( isVerbose() )
		{
			log("Processing classpath in " + baseDir.getAbsolutePath());
		}
		visitedProjects.add(baseDir);
		File startFile = new File(baseDir, ECLIPSE_CLASSPATH_FILENAME);
		Document document = parseFile(startFile);
		Element root = document.getRootElement();

		// Get all the elements that relate to the classpath and walk them
		List<?> children = root.getChildren("classpathentry");
		if( children == null )
		{
			return;
		}

		for( Iterator<?> i = children.iterator(); i.hasNext(); )
		{
			Element element = (Element) i.next();

			String kind = element.getAttribute("kind").getValue();
			String path = element.getAttribute("path").getValue();

			if( kind.equals("src") )
			{
				processSrc(path);
			}
			else if( kind.equals("output") )
			{
				processOutput(baseDir, path);
			}
			else if( kind.equals("lib") )
			{
				if( !exportedOnly || isExported(element, kind) )
				{
					processLib(baseDir, path);
				}
				else if( isVerbose() )
				{
					log("Library skipped since not exported: " + path);
				}
			}
			else
			{
				if( isVerbose() )
				{
					log("Skipping entry of type '" + kind + "'\n");
				}
			}
		}

		if( isVerbose() )
		{
			log("Finished processing classpath in " + baseDir.getAbsolutePath());
		}
	}

	private boolean isExported(Element element, String kind)
	{
		Attribute attr = element.getAttribute("exported");
		return attr != null && attr.getValue().equals("true");
	}

	private void processLib(File baseDir, String libraryPath)
	{
		File file;
		if( libraryPath.charAt(0) != '/' )
		{
			file = new File(baseDir, libraryPath);
		}
		else
		{
			// Remove leading '/'
			libraryPath = libraryPath.substring(1);

			// Split out the project name
			int slashIndex = libraryPath.indexOf('/');
			String projectName = libraryPath.substring(0, slashIndex);
			libraryPath = libraryPath.substring(slashIndex + 1);

			// Get the file
			file = getProjectBaseDir(projectName);
			file = new File(file, libraryPath);
		}

		if( !file.exists() )
		{
			throw new BuildException("Could not find library '" + file.getAbsolutePath() + "'\n");
		}

		if( !file.isAbsolute() )
		{
			throw new BuildException("File must be an absolute path\n");
		}

		if( isVerbose() )
		{
			log("Adding library to classpath '" + libraryPath + "'\n");
		}

		Path.PathElement e = classPath.createPathElement();
		e.setLocation(file);
	}

	private void processOutput(File baseDir, String folder)
	{
		// Get the file
		File file = new File(baseDir, folder);

		if( file.exists() )
		{
			if( !file.isAbsolute() )
			{
				throw new BuildException("Directory must be an absolute path\n");
			}

			if( !file.isDirectory() )
			{
				throw new BuildException("Output directory must be a directory!\n");
			}

			if( isVerbose() )
			{
				log("Adding library to classpath '" + file.getAbsolutePath() + "'\n");
			}

			Path.PathElement e = classPath.createPathElement();
			e.setLocation(file);
		}
	}

	private void processSrc(String projectName)
	{
		// Do nothing if it is one of the current project's source folders.
		if( projectName.charAt(0) != '/' )
		{
			return;
		}

		// Remove the leading slash
		projectName = projectName.substring(1);

		File projectBaseDir = getProjectBaseDir(projectName);
		processClasspath(projectBaseDir, true);
	}

	@SuppressWarnings("nls")
	private File getProjectBaseDir(String projectName)
	{
		File projectFile = Helper.getBaseDirForProjectName(getProject(), projectName, true);

		if( projectFile != null )
		{
			return projectFile;
		}
		else
		{
			String projectBase = getProject().getProperty("TLE Plugins.base"); //$NON-NLS-1$
			if( projectBase != null )
			{
				File pluginProject = getPluginProject(projectBase, projectName);
				if( pluginProject != null )
				{
					return pluginProject;
				}
			}
		}
		throw new BuildException("No project found for '" + projectName + "'\n");
	}

	@SuppressWarnings("nls")
	private File getPluginProject(String projectBase, String projectName)
	{
		if( pluginMap.containsKey(projectName) )
		{
			return pluginMap.get(projectName);
		}
		File pluginsDir = new File(projectBase);
		String pluginFilename = projectName + "/plugin-jpf.xml";
		for( File subDir : pluginsDir.listFiles() )
		{
			if( subDir.isDirectory() )
			{
				File pluginFile = new File(subDir, pluginFilename);
				if( pluginFile.exists() )
				{
					File pluginDir = pluginFile.getParentFile();
					pluginMap.put(projectName, pluginDir);
					return pluginDir;
				}
			}
		}
		return null;
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

	public File getBasedir()
	{
		return basedir;
	}

	public void setBasedir(File basedir)
	{
		this.basedir = basedir;
	}

	public void setPathId(String pathId)
	{
		this.pathId = pathId;
	}
}
