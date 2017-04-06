package com.tle.core.search.searchset.virtualisation;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.i18n.LangUtils;
import com.tle.common.search.searchset.SearchSet;
import com.tle.core.search.VirtualisableAndValue;

public abstract class VirtualisationHelper<T>
{
	public abstract SearchSet getSearchSet(T obj);

	public abstract T newFromPrototypeForValue(T obj, String value);

	public VirtualisableAndValue<T> newVirtualisedPathFromPrototypeForValue(T obj, String value, int count)
	{
		return new VirtualisableAndValue<T>(newFromPrototypeForValue(obj, value), value, count);
	}

	public LanguageBundle newLanguageBundleForValue(LanguageBundle bundle, String value)
	{
		bundle = LanguageBundle.clone(bundle);
		if( !LangUtils.isEmpty(bundle) )
		{
			for( LanguageString ls : bundle.getStrings().values() )
			{
				ls.setText(modifyString(ls.getText(), value));
			}
		}
		return bundle;
	}

	protected String modifyString(String text, String value)
	{
		return text;
	}
}
