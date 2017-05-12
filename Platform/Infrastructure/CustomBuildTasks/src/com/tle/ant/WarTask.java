package com.tle.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ZipFileSet;

public class WarTask extends Task
{
	private String pathrefid;
	private File dir;
	private File destfile;

	@SuppressWarnings("nls")
	@Override
	public void execute() throws BuildException
	{
		Path path = (Path) getProject().getReference(pathrefid);
		if( path == null )
		{
			throw new BuildException("Path '" + pathrefid + "' doesn't exist");
		}
		Zip zip = new Zip();
		zip.bindToOwner(this);

		String[] list = path.list();
		for( String entry : list )
		{
			File file = new File(entry);
			if( file.isDirectory() )
			{
				ZipFileSet classSet = new ZipFileSet();
				classSet.setPrefix("WEB-INF/classes");
				classSet.setDir(file);
				zip.addZipfileset(classSet);
			}
			else
			{
				ZipFileSet jarSet = new ZipFileSet();
				jarSet.setFile(file);
				jarSet.setPrefix("WEB-INF/lib");
				zip.addZipfileset(jarSet);
			}
		}
		FileSet publicHtml = new FileSet();
		publicHtml.setDir(dir);
		zip.addFileset(publicHtml);
		zip.setDestFile(destfile);
		zip.init();
		zip.execute();
	}

	public String getPathrefid()
	{
		return pathrefid;
	}

	public void setPathrefid(String pathrefid)
	{
		this.pathrefid = pathrefid;
	}

	public File getDir()
	{
		return dir;
	}

	public void setDir(File dir)
	{
		this.dir = dir;
	}

	public File getDestfile()
	{
		return destfile;
	}

	public void setDestfile(File destfile)
	{
		this.destfile = destfile;
	}
}
