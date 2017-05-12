package com.tle.ant;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Path.PathElement;

public class RemovePathEntries extends Task
{
	private String in;
	private String remove;
	private String out;

	@Override
	public void execute() throws BuildException
	{
		if( in == null || out == null || remove == null )
		{
			throw new BuildException("Must specify path refs for 'in','remove' and 'out'"); //$NON-NLS-1$
		}
		Project project = getProject();
		Path inPath = (Path) project.getReference(in);
		Path removePath = (Path) project.getReference(remove);
		Path outPath = new Path(project);
		Set<String> entries = new HashSet<String>(Arrays.asList(inPath.list()));
		entries.removeAll(Arrays.asList(removePath.list()));
		for( String filePath : entries )
		{
			PathElement element = outPath.createPathElement();
			element.setLocation(new File(filePath));
		}
		project.addReference(out, outPath);
	}

	public String getIn()
	{
		return in;
	}

	public void setIn(String in)
	{
		this.in = in;
	}

	public String getRemove()
	{
		return remove;
	}

	public void setRemove(String remove)
	{
		this.remove = remove;
	}

	public String getOut()
	{
		return out;
	}

	public void setOut(String out)
	{
		this.out = out;
	}
}
