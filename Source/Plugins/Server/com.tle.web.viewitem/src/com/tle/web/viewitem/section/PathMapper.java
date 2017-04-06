package com.tle.web.viewitem.section;

import java.util.HashMap;
import java.util.Map;

public class PathMapper<T>
{
	public enum Type
	{
		FULL, FILENAME, EXTENSION, MIME, ALWAYS
	}

	private Map<String, T> fullpathMappings;
	private Map<String, T> mimeMappings;
	private Map<String, T> filenameMappings;
	private Map<String, T> extensionMappings;

	public PathMapper()
	{
		fullpathMappings = new HashMap<String, T>();
		mimeMappings = new HashMap<String, T>();
		filenameMappings = new HashMap<String, T>();
		extensionMappings = new HashMap<String, T>();
	}

	public void addMapping(Type type, String path, T object)
	{
		path = path.toLowerCase();
		switch( type )
		{
			case FULL:
				fullpathMappings.put(path, object);
				break;
			case FILENAME:
				filenameMappings.put(path, object);
				break;
			case EXTENSION:
				extensionMappings.put(path, object);
				break;
			case MIME:
				mimeMappings.put(path, object);
				break;

			default:
				break;
		}
	}

	public T getMapping(String path, String mimeType)
	{
		path = path.toLowerCase();
		if( path.contains("viewcontent/") )
		{
			path = "viewcontent";
		}

		T single = fullpathMappings.get(path);
		if( single != null )
		{
			return single;
		}
		String filename = path.substring(path.lastIndexOf('/') + 1);
		single = filenameMappings.get(filename);
		if( single != null )
		{
			return single;
		}
		single = mimeMappings.get(mimeType);
		if( single != null )
		{
			return single;
		}
		int extIndex = filename.lastIndexOf('.');
		if( extIndex != -1 )
		{
			String ext = filename.substring(extIndex + 1);
			single = extensionMappings.get(ext);
			if( single != null )
			{
				return single;
			}
		}
		return null;
	}
}
