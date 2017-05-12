package com.tle.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.java.plugin.registry.IntegrityCheckReport;
import org.java.plugin.registry.IntegrityCheckReport.Error;
import org.java.plugin.registry.IntegrityCheckReport.ReportItem;
import org.java.plugin.registry.IntegrityCheckReport.Severity;
import org.java.plugin.registry.Library;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginFragment;
import org.java.plugin.registry.PluginPrerequisite;
import org.java.plugin.registry.UniqueIdentity;
import org.java.plugin.util.IoUtil;

@SuppressWarnings("nls")
public abstract class CachedBaseJpfTask extends ExtendedBaseJpfTask
{
	protected boolean followExports = true;
	protected boolean useCache = true;
	protected boolean fullClasspath = false;
	protected File cache;

	protected Map<String, Set<File>> getJpfMap()
	{
		Map<String, Set<File>> results;
		if( useCache && "true".equals(System.getProperty("cache.created")) && cache != null && cache.exists() )
		{
			results = readCache();
		}
		else
		{
			initRegistry(true);
			IntegrityCheckReport integrityReport = getRegistry().checkIntegrity(getPathResolver());
			if( integrityReport.countErrors() > 0 )
			{
				boolean abort = false;
				for( ReportItem reportItem : integrityReport.getItems() )
				{
					if( reportItem.getSeverity() == Severity.ERROR && reportItem.getCode() != Error.BAD_LIBRARY )
					{
						log(reportItem.getMessage());
						abort = true;
					}
				}
				if( abort )
				{
					throw new BuildException("JPF registry has errors");
				}
			}
			Set<UniqueIdentity> allIds = collectTargetIds();
			Map<String, Set<File>> regMap = new HashMap<String, Set<File>>();
			for( UniqueIdentity id : allIds )
			{
				Set<String> processedIds = new HashSet<String>();
				Set<File> tempResult = new HashSet<File>();
				if( id instanceof PluginFragment )
				{
					String pluginId = ((PluginFragment) id).getPluginId();
					processDescriptor(tempResult, processedIds, getRegistry().getPluginDescriptor(pluginId), true,
						id.getId());
				}
				else
				{
					processDescriptor(tempResult, processedIds, getRegistry().getPluginDescriptor(id.getId()), true,
						id.getId());
				}
				regMap.put(id.getId(), tempResult);
			}

			writeCache(regMap);
			System.setProperty("cache.created", "true");
			results = regMap;
		}
		return results;
	}

	private void processDescriptor(final Set<File> result, final Set<String> processedIds,
		final PluginDescriptor descr, final boolean includePrivate, String id)
	{
		if( followExports && !includePrivate && processedIds.contains(id) )
		{
			return;
		}
		processedIds.add(id);
		for( Library lib : descr.getLibraries() )
		{
			if( followExports && !includePrivate && lib.getExports().isEmpty() )
			{
				continue;
			}
			URL url = getPathResolver().resolvePath(lib, lib.getPath());
			File file = IoUtil.url2file(url);
			if( file != null )
			{
				result.add(file);
				if( getVerbose() )
				{
					log("Collected file " + file //$NON-NLS-1$
						+ " from library " + lib.getUniqueId()); //$NON-NLS-1$
				}
			}
			else
			{
				log("Ignoring non-local URL " + url //$NON-NLS-1$
					+ " found in library " + lib.getUniqueId()); //$NON-NLS-1$
			}
		}
		for( PluginPrerequisite pre : descr.getPrerequisites() )
		{
			if( !pre.matches() )
			{
				continue;
			}
			if( includePrivate || pre.isExported() )
			{
				processDescriptor(result, processedIds, getRegistry().getPluginDescriptor(pre.getPluginId()),
					fullClasspath, pre.getPluginId());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Set<File>> readCache()
	{
		Map<String, Set<File>> regMap = null;
		try
		{
			FileInputStream fin = new FileInputStream(cache);
			ObjectInputStream ois = new ObjectInputStream(fin);
			try
			{
				regMap = (Map<String, Set<File>>) ois.readObject();
			}
			finally
			{
				ois.close();
			}
		}
		catch( IOException ex )
		{
			throw new BuildException("Could not read the cache", ex); //$NON-NLS-1$
		}
		catch( ClassNotFoundException ex )
		{
			throw new BuildException("Could not find the class", ex); //$NON-NLS-1$
		}

		return regMap;
	}

	private void writeCache(Map<String, Set<File>> regMap)
	{
		if( cache != null )
		{
			cache.getParentFile().mkdirs();
			try
			{
				FileOutputStream fout = new FileOutputStream(cache);
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				try
				{
					oos.writeObject(regMap);
				}
				finally
				{
					oos.close();
				}
			}
			catch( IOException ex )
			{
				throw new BuildException("Could not write the cache", ex); //$NON-NLS-1$
			}
		}
	}

	private Set<UniqueIdentity> collectTargetIds()
	{
		HashSet<UniqueIdentity> result = new HashSet<UniqueIdentity>();
		Collection<PluginDescriptor> pluginDescriptors = getRegistry().getPluginDescriptors();
		for( PluginDescriptor pluginDescriptor : pluginDescriptors )
		{
			result.add(pluginDescriptor);
		}
		Collection<PluginFragment> pluginFragments = getRegistry().getPluginFragments();
		for( PluginFragment pluginFragment : pluginFragments )
		{
			result.add(pluginFragment);
		}
		return result;
	}

	public File getCache()
	{
		return cache;
	}

	public void setCache(File cache)
	{
		this.cache = cache;
	}

	public boolean isFollowExports()
	{
		return followExports;
	}

	public void setFollowExports(boolean followExports)
	{
		this.followExports = followExports;
	}

	public boolean isUseCache()
	{
		return useCache;
	}

	public void setUseCache(boolean useCache)
	{
		this.useCache = useCache;
	}

	public boolean isFullClasspath()
	{
		return fullClasspath;
	}

	public void setFullClasspath(boolean fullClasspath)
	{
		this.fullClasspath = fullClasspath;
	}
}
