package com.tle.ant;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.FileSet;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * @author Nicholas Read
 */
public class JarAntTask extends Jar
{
	private static final String ECLIPSE_CLASSPATH_FILENAME = ".classpath";
	private boolean verbose;
	private File staging;
	private Set<File> libraries = new HashSet<File>();
	private String mainClass;
	private FileSet fileSet2;
	private String classPath;
	private SAXBuilder sax = Helper.createSAXBuilder();

	public JarAntTask()
	{
		super();
	}

	@Override
	public void addFileset(FileSet fileSet)
	{
		fileSet2 = fileSet;
	}

	@Override
	public void execute() throws BuildException
	{
		libraries.clear();

		processClasspath(getProject().getBaseDir());

		try
		{
			Manifest manifest = new Manifest();
			if( mainClass != null )
			{
				manifest.addConfiguredAttribute(new Manifest.Attribute("Main-Class", mainClass));
			}

			if( classPath != null )
			{
				manifest.addConfiguredAttribute(new Manifest.Attribute("Class-Path", classPath));
			}

			addConfiguredManifest(manifest);
		}
		catch( ManifestException e )
		{
			log(e.getMessage());
		}

		if( fileSet2 == null )
		{
			fileSet2 = new FileSet();
		}
		fileSet2.setDir(getStaging());
		super.addFileset(fileSet2);

		super.execute();
	}

	private void processClasspath(File baseDir)
	{
		if( isVerbose() )
		{
			log("Processing classpath in " + baseDir.getAbsolutePath());
		}

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
				if( isExported(element) )
				{
					processLib(path);
				}
				else
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

	private boolean isExported(Element element)
	{
		Attribute attr = element.getAttribute("exported");
		return attr != null && attr.getValue().equals("true");
	}

	private void processLib(String libraryPath)
	{
		// Remove leading '/'
		libraryPath = libraryPath.substring(1);

		// Split out the project name
		int slashIndex = libraryPath.indexOf('/');
		String projectName = libraryPath.substring(0, slashIndex);
		libraryPath = libraryPath.substring(slashIndex + 1);

		// Get the file
		File file = getBaseDirForProjectName(projectName);
		file = new File(file, libraryPath);

		if( !file.exists() )
		{
			throw new BuildException("Could not find library '" + file.getAbsolutePath() + "'\n");
		}

		if( !file.isAbsolute() )
		{
			throw new BuildException("File must be an absolute path\n");
		}

		if( !libraries.add(file) )
		{
			if( isVerbose() )
			{
				log("Library already exists " + libraryPath + "\n");
			}
		}
		else
		{
			if( isVerbose() )
			{
				log("Unzipping library to classpath '" + libraryPath + "'\n");
			}

			Expand unzip = new Expand();
			unzip.setOverwrite(true);
			unzip.setProject(getProject());
			unzip.setDest(staging);
			unzip.setSrc(file);
			unzip.execute();
		}

	}

	private void processOutput(File baseDir, String folder)
	{
		// Get the file
		File file = new File(baseDir, folder);

		if( !file.exists() )
		{
			throw new BuildException("Could not find output directory '" + file.getAbsolutePath() + "'\n");
		}

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
			log("Copying library to classpath '" + file.getAbsolutePath() + "'\n");
		}

		FileSet set = new FileSet();
		set.setProject(getProject());
		set.setDir(file);
		super.addFileset(set);
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
		File projectFile = getBaseDirForProjectName(projectName);

		if( isVerbose() )
		{
			log("Importing classpath for project '" + projectName + "'\n");
		}

		processClasspath(projectFile);
	}

	private File getBaseDirForProjectName(String projectName)
	{
		String projectBase = getProject().getProperty(projectName + ".base");
		if( projectBase == null )
		{
			throw new BuildException("No base property defined for project '" + projectName + "'\n");
		}

		File projectFile = new File(projectBase);
		if( !projectFile.exists() || !projectFile.isDirectory() )
		{
			throw new BuildException("Base directory does not exist for project '" + projectName + "'\n");
		}

		return projectFile;
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

	public File getStaging()
	{
		return staging;
	}

	public void setStaging(File staging)
	{
		this.staging = staging;
	}

	public String getMainClass()
	{
		return mainClass;
	}

	public void setMainClass(String mainClass)
	{
		this.mainClass = mainClass;
	}

	public void setClassPath(String classPath)
	{
		this.classPath = classPath;
	}

	public String getClassPath()
	{
		return classPath;
	}
}
