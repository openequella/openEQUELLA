package com.tle.web.i18n;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.BundleReference;
import com.tle.core.guice.Bind;
import com.tle.core.services.language.LanguageService;

@Bind
@Singleton
public final class BundleCache
{
	private final ThreadLocal<Set<Long>> bundleIdsLocal = new ThreadLocal<Set<Long>>();
	private final ThreadLocal<Map<Long, String>> bundleMapLocal = new ThreadLocal<Map<Long, String>>();

	@Inject
	private LanguageService languageService;

	public void reset()
	{
		bundleIdsLocal.remove();
		bundleMapLocal.remove();
	}

	public void addBundle(Object bundle)
	{
		if( bundle != null )
		{
			if( bundle instanceof LanguageBundle )
			{
				addBundleId(((LanguageBundle) bundle).getId());
			}
			else
			{
				addBundleId((Long) bundle);
			}
		}
	}

	public void addBundleId(Long id)
	{
		Map<Long, String> map = bundleMapLocal.get();
		if( map != null && map.containsKey(id) )
		{
			return;
		}
		Set<Long> bundleList = ensureBundleIds();
		if( id != 0 )
		{
			bundleList.add(id);
		}
	}

	public void addBundleIds(Collection<Long> ids)
	{
		Map<Long, String> map = bundleMapLocal.get();
		Set<Long> bundleList = ensureBundleIds();
		for( Long id : ids )
		{
			if( id != null && id != 0 && (map == null || !map.containsKey(id)) )
			{
				bundleList.add(id);
			}
		}
	}

	public void addBundleRefs(Collection<? extends BundleReference> refs)
	{
		Set<Long> bundleList = ensureBundleIds();
		for( BundleReference ref : refs )
		{
			if( ref != null )
			{
				long id = ref.getBundleId();
				if( id != 0 )
				{
					bundleList.add(id);
				}
			}
		}
	}

	private Set<Long> ensureBundleIds()
	{
		Set<Long> bundleList = bundleIdsLocal.get();
		if( bundleList == null )
		{
			bundleList = new HashSet<Long>();
			bundleIdsLocal.set(bundleList);
		}
		return bundleList;
	}

	public Set<Long> getBundleIds()
	{
		return ensureBundleIds();
	}

	public Map<Long, String> getBundleMap()
	{
		Map<Long, String> map = bundleMapLocal.get();
		Set<Long> ids = bundleIdsLocal.get();
		if( ids != null )
		{
			Map<Long, String> langStrings = languageService.getNames(ids);
			if( map != null )
			{
				map.putAll(langStrings);
			}
			else
			{
				map = new HashMap<Long, String>(langStrings);
			}

			// Even if values did not exist for some keys, we still need to
			// indicate that the bundle has been looked up. Put null values in
			// for these IDs.
			if( ids.removeAll(map.keySet()) )
			{
				for( Long id : ids )
				{
					map.put(id, null);
				}
			}

			bundleIdsLocal.remove();
			bundleMapLocal.set(map);
		}
		if( map == null )
		{
			return Collections.emptyMap();
		}
		return map;
	}
}
