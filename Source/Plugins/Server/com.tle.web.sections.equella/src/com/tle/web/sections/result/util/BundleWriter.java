package com.tle.web.sections.result.util;

import java.util.Collection;

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.web.TextBundle;
import com.tle.web.i18n.BundleCache;

public class BundleWriter
{
	private final String pluginKeyBase;
	private final BundleCache bundleCache;

	public BundleWriter(String pluginId, BundleCache bundleCache)
	{
		this.pluginKeyBase = pluginId + '.';
		this.bundleCache = bundleCache;
	}

	public boolean isEmpty(LanguageBundle bundle)
	{
		return LangUtils.isEmpty(bundle);
	}

	public String bundle(Object bundle)
	{
		return bundle(bundle, "??no_strings_in_bundle??"); //$NON-NLS-1$
	}

	public String bundle(Object bundle, String defString)
	{
		if( bundle == null || (bundle instanceof Number && ((Number) bundle).intValue() == 0) )
		{
			return defString;
		}
		return TextBundle.getLocalString(bundle, bundleCache, null, defString);
	}

	public String key(String key)
	{
		return gkey(this.pluginKeyBase + key);
	}

	public String gkey(String key)
	{
		return CurrentLocale.get(key);
	}

	public String key(String key, Object... values)
	{
		return gkey(this.pluginKeyBase + key, values);
	}

	public String gkey(String key, Object... values)
	{
		return CurrentLocale.get(key, values);
	}

	public String key(String key, Collection<Object> vals)
	{
		return key(key, vals.toArray(new Object[vals.size()]));
	}

	public String gkey(String key, Collection<Object> vals)
	{
		return gkey(key, vals.toArray(new Object[vals.size()]));
	}
}
