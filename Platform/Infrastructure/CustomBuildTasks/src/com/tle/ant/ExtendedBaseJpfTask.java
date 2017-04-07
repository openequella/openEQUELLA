package com.tle.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.java.plugin.tools.ant.BaseJpfTask;

public abstract class ExtendedBaseJpfTask extends BaseJpfTask
{
	private List<ResourceCollection> collections = new LinkedList<>();

	public void addFileSet(FileSet fileset)
	{
		collections.add(fileset);
	}

	public void add(ResourceCollection col)
	{
		collections.add(col);
	}

	@Override
	protected File[] getIncludedFiles()
	{
		if( collections.isEmpty() )
		{
			return super.getIncludedFiles();
		}
		else
		{
			List<File> newFiles = new ArrayList<File>();
			for( ResourceCollection col : collections )
			{
				Iterator<Resource> iter = col.iterator();
				while( iter.hasNext() )
				{
					Resource res = iter.next();
					if( res instanceof FileResource )
					{
						newFiles.add(((FileResource) res).getFile());
					}
				}
			}
			return newFiles.toArray(new File[newFiles.size()]);
		}
	}

}
