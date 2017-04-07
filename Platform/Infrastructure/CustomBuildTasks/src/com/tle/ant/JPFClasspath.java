package com.tle.ant;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;

@SuppressWarnings("nls")
public class JPFClasspath extends CachedBaseJpfTask
{
	private String pathId;
	private String pathIdRef;
	private String pluginId;
	private boolean generateOnly = false;
	private boolean clearCache;

	/**
	 * @param value the path ID to set
	 */
	public void setPathId(final String value)
	{
		pathId = value;
	}

	/**
	 * @param value the path ID reference to set
	 */
	public void setPathIdRef(final String value)
	{
		pathIdRef = value;
	}

	/**
	 * @param value the plug-in ID to set
	 */
	public void setPluginId(final String value)
	{
		pluginId = value;
	}

	@Override
	public void execute() throws BuildException
	{
		if( clearCache )
		{
			System.clearProperty("cache.created");
			return;
		}
		if( generateOnly )
		{
			getJpfMap();
			return;
		}

		if( (pathId == null) && (pathIdRef == null) )
		{
			throw new BuildException("pathid or pathidref attribute must be set!", //$NON-NLS-1$
				getLocation());
		}

		if( pluginId == null || pluginId.isEmpty() )
		{
			throw new BuildException("pluginId attribute must be set!", //$NON-NLS-1$
				getLocation());
		}

		Map<String, Set<File>> results = getJpfMap();

		Path path;
		if( pathIdRef != null )
		{
			Object ref = getProject().getReference(pathIdRef);
			if( !(ref instanceof Path) )
			{
				throw new BuildException("invalid reference " + pathIdRef //$NON-NLS-1$
					+ ", expected " + Path.class.getName() //$NON-NLS-1$
					+ ", found " + ref, //$NON-NLS-1$
					getLocation());
			}
			path = (Path) ref;
		}
		else
		{
			path = new Path(getProject());
			getProject().addReference(pathId, path);
		}

		Set<String> ids = pluginIds();
		for( String id : ids )
		{
			// if( !results.containsKey(id) )
			// {
			// for( PluginFragment fragment : getRegistry().getPluginFragments()
			// )
			// {
			// if( !id.equals(fragment.getId()) )
			// {
			// continue;
			// }
			// if( getVerbose() )
			// {
			// log("Found id " + fragment.getPluginId() + " for fragment " +
			// id);
			// }
			// id = fragment.getPluginId();
			// break;
			// }
			// }
			if( !results.containsKey(id) )
			{
				throw new BuildException("pluginId '" + id + "' not found in the registery!");
			}

			Set<File> result = results.get(id);
			for( File file : result )
			{
				path.setLocation(file);
				if( getVerbose() )
				{
					log("Added " + file.getAbsolutePath());
				}
			}
			if( getVerbose() )
			{
				log("Collected path entries: " + result.size()); //$NON-NLS-1$
			}
		}
	}

	private Set<String> pluginIds()
	{
		HashSet<String> result = new HashSet<String>();

		if( pluginId != null )
		{
			for( StringTokenizer st = new StringTokenizer(pluginId, ",", false); //$NON-NLS-1$
			st.hasMoreTokens(); )
			{
				result.add(st.nextToken());
			}
		}

		return result;
	}

	public boolean isGenerateOnly()
	{
		return generateOnly;
	}

	public void setGenerateOnly(boolean generateOnly)
	{
		this.generateOnly = generateOnly;
	}

	public boolean isClearCache()
	{
		return clearCache;
	}

	public void setClearCache(boolean clearCache)
	{
		this.clearCache = clearCache;
	}
}
