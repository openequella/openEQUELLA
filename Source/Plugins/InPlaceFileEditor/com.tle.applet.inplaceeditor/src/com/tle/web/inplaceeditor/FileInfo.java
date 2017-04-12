package com.tle.web.inplaceeditor;

import java.io.Serializable;

import com.tle.common.Check;

public class FileInfo implements Serializable
{
	private static final long serialVersionUID = -1L;

	private String name;
	private String path;
	private boolean directory;
	private long size;
	private long lastModified;
	private boolean markAsAttachment;

	public FileInfo()
	{
		super();
	}

	public FileInfo(String path)
	{
		if( !Check.isEmpty(path) )
		{
			int ind = path.lastIndexOf('/');

			setDirectory(true);
			if( ind > 0 )
			{
				setName(path.substring(ind + 1));
				setPath(path.substring(0, ind));
			}
			else
			{
				setName(path);
			}
		}
	}

	public FileInfo(FileInfo parent, String name)
	{
		this.path = parent == null ? "" : parent.getFullPath(); //$NON-NLS-1$
		this.name = name;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public boolean isDirectory()
	{
		return directory;
	}

	public void setDirectory(boolean directory)
	{
		this.directory = directory;
	}

	public long getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(long lastModified)
	{
		this.lastModified = lastModified;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	public boolean isMarkAsAttachment()
	{
		return markAsAttachment;
	}

	public void setMarkAsAttachment(boolean markAsAttachment)
	{
		this.markAsAttachment = markAsAttachment;
	}

	public String getFullPath()
	{
		if( Check.isEmpty(path) )
		{
			return Check.nullToEmpty(name);
		}
		else
		{
			return path + '/' + Check.nullToEmpty(name);
		}
	}

	public FileInfo getParentFileInfo()
	{
		return Check.isEmpty(path) ? null : new FileInfo(path);
	}

	public boolean isRoot()
	{
		return Check.isEmpty(path) && Check.isEmpty(name);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}
		if( obj == null )
		{
			return false;
		}
		if( getClass() != obj.getClass() )
		{
			return false;
		}
		final FileInfo other = (FileInfo) obj;
		if( name == null )
		{
			if( other.name != null )
			{
				return false;
			}
		}
		else if( !name.equals(other.name) )
		{
			return false;
		}
		if( path == null )
		{
			if( other.path != null )
			{
				return false;
			}
		}
		else if( !path.equals(other.path) )
		{
			return false;
		}
		return true;
	}
}
