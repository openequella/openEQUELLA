package com.tle.web;

import java.util.Collection;
import java.util.Map;

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.i18n.BundleCache;

public final class TextBundle
{
	private TextBundle()
	{
		throw new Error();
	}

	public static String getLocalString(Object origObj, BundleCache bundleCache, Collection<?> values,
		String defaultString)
	{
		String output;
		Object obj = origObj;
		if( obj instanceof LanguageBundle )
		{
			obj = ((LanguageBundle) obj).getId();
		}
		if( obj instanceof Long )
		{
			Map<Long, String> bundleMap = bundleCache.getBundleMap();
			output = bundleMap.get(obj);
			if( output == null )
			{
				if( bundleMap.containsKey(obj) )
				{
					// This indicates that the string is empty
					return defaultString;
				}
				else if( origObj instanceof LanguageBundle )
				{
					output = CurrentLocale.get((LanguageBundle) origObj, defaultString);
				}
				else
				{
					return "???no_cached_string for " + origObj + "???"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		else
		{
			if( obj == null )
			{
				return defaultString;
			}
			output = CurrentLocale.get(obj.toString(), toArray(values));
		}
		return output;
	}

	private static Object[] toArray(Collection<?> collection)
	{
		return collection == null ? new Object[0] : collection.toArray();
	}

}
