package com.tle.common.interfaces.equella;

import java.util.Collection;

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.CurrentLocale;

public final class TextBundle
{
	private TextBundle()
	{
		throw new Error();
	}

	public static String getLocalString(Object origObj, Collection<?> values, String defaultString)
	{
		String output;
		Object obj = origObj;
		if( obj == null )
		{
			return defaultString;
		}
		LanguageBundle bundle;
		// TODO:???
		// if( origObj instanceof BundleReference )
		// {
		// bundle = ((BundleReference) origObj).getBundle();
		// }
		// else
		// {
		bundle = (LanguageBundle) origObj;
		// }
		output = CurrentLocale.get(bundle);
		return output;
	}
}
