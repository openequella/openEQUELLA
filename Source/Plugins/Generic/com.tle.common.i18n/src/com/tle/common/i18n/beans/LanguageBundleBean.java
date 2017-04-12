package com.tle.common.i18n.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.tle.common.Check;

/**
 * @author Aaron
 */
public class LanguageBundleBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private long id;
	private Map<String, LanguageStringBean> strings;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Map<String, LanguageStringBean> getStrings()
	{
		return strings;
	}

	public void setStrings(Map<String, LanguageStringBean> strings)
	{
		this.strings = strings;
	}

	public boolean isEmpty()
	{
		if( strings != null )
		{
			for( LanguageStringBean string : strings.values() )
			{
				if( !Check.isEmpty(string.getText()) )
				{
					return false;
				}
			}
		}
		return true;
	}

	public static LanguageBundleBean clone(LanguageBundleBean bundle)
	{
		if( bundle == null )
		{
			return null;
		}
		final LanguageBundleBean newBundle = new LanguageBundleBean();
		final HashMap<String, LanguageStringBean> langStrings = new HashMap<String, LanguageStringBean>();
		final Map<String, LanguageStringBean> oldStrings = bundle.getStrings();
		for( LanguageStringBean langString : oldStrings.values() )
		{
			final LanguageStringBean newString = new LanguageStringBean();
			newString.setText(langString.getText());
			newString.setLocale(langString.getLocale());
			newString.setPriority(langString.getPriority());
			langStrings.put(newString.getLocale(), newString);
		}
		newBundle.setStrings(langStrings);
		return newBundle;
	}
}
