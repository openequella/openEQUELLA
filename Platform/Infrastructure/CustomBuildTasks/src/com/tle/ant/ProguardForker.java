package com.tle.ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.Redirector;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;

/**
 * http://java.sun.com/j2se/1.4.2/docs/guide/jws/downloadservletguide.html
 */
public class ProguardForker extends Task
{
	private final CommandlineJava commandLine = new CommandlineJava();
	private final Redirector redirector = new Redirector(this);

	private File proguard;
	private File config;
	private String maxMemory;

	public ProguardForker()
	{
		super();
	}

	@Override
	public void execute() throws BuildException
	{
		if( getProguard() == null )
		{
			throw new BuildException("You must specify the ProGuard Jar location");
		}
		else if( !getProguard().exists() )
		{
			throw new BuildException("Given ProGuard Jar location does not exist");
		}
		else if( getConfig() == null )
		{
			throw new BuildException("You must specify the configuration location");
		}
		else if( !getConfig().exists() )
		{
			throw new BuildException("Given configuration location does not exist");
		}

		commandLine.setJar(getProguard().getAbsolutePath());
		commandLine.createArgument().setValue("@" + getConfig());

		String mm = getMaxMemory();
		if( mm != null && mm.trim().length() > 0 )
		{
			commandLine.setMaxmemory(mm);
		}

		fork(commandLine.getCommandline());
	}

	private int fork(String[] command) throws BuildException
	{
		Execute exe = new Execute(redirector.createHandler(), null);
		exe.setAntRun(getProject());
		exe.setWorkingDirectory(getProject().getBaseDir());
		exe.setCommandline(command);

		try
		{
			int rc = exe.execute();
			redirector.complete();
			if( exe.killedProcess() )
			{
				throw new BuildException("Timeout: killed the sub-process");
			}
			return rc;
		}
		catch( IOException e )
		{
			throw new BuildException(e, getLocation());
		}
	}

	public File getProguard()
	{
		return proguard;
	}

	public void setProguard(File proguard)
	{
		this.proguard = proguard;
	}

	public File getConfig()
	{
		return config;
	}

	public void setConfig(File config)
	{
		this.config = config;
	}

	public void addSysproperty(Environment.Variable sysp)
	{
		commandLine.addSysproperty(sysp);
	}

	public String getMaxMemory()
	{
		return maxMemory;
	}

	public void setMaxMemory(String maxMemory)
	{
		this.maxMemory = maxMemory;
	}
}
