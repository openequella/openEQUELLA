/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.i18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.dytech.devlib.PropBagEx;
import com.google.common.collect.Maps;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.Check;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.i18n.beans.LanguageStringBean;

/**
 * @author Nicholas Read
 */
public final class LangUtils
{
	private static final String IEEE_LOM_LANG_NODE = "string"; //$NON-NLS-1$
	private static final String IEEE_LOM_LANG_ATTRIBUTE = "@language"; //$NON-NLS-1$

	/**
	 * Sets the text for the given locale.
	 */
	public static void setString(final LanguageBundle bundle, Locale locale, final String text)
	{
		LanguageString string = null;

		Map<String, LanguageString> strings = bundle.getStrings();
		if( strings != null )
		{
			string = strings.get(LocaleUtils.getExactKey(locale));
		}

		if( string == null )
		{
			createLanguageString(bundle, locale, text);
		}
		else
		{
			string.setText(text);
		}
	}

	public static String getString(final LanguageBundle bundle)
	{
		return getString(bundle, CurrentLocale.getLocale());
	}

	public static String getString(final LanguageBundle bundle, final String defaultResult)
	{
		return getString(bundle, CurrentLocale.getLocale(), defaultResult);
	}

	/**
	 * Gets the text for the closet matching locale.
	 */
	public static String getString(final LanguageBundle bundle, final Locale locale)
	{
		return getString(bundle, locale, "???no_strings_in_bundle???"); //$NON-NLS-1$
	}

	/**
	 * Gets the text for the closet matching locale, else returns the default
	 * result.
	 */
	public static String getString(final LanguageBundle bundle, final Locale locale, final String defaultResult)
	{
		if( bundle != null && bundle.getStrings() != null )
		{
			LanguageString closest = getClosestObjectForLocale(bundle.getStrings(), locale);
			if( closest != null && !Check.isEmpty(closest.getText()) )
			{
				return closest.getText();
			}
		}
		return defaultResult;
	}

	/**
	 * Extracts a bunch of languages from either the root node, the old IMS or
	 * the IEEE LOM standards, eg:
	 * <ul>
	 * <li>Some String</li>
	 * <li>&lt;langstring lang="en"&gt;Some string&lt;/langstring&gt;</li>
	 * <li>&lt;string language="en"&gt;Some string&lt;/string&gt;</li>
	 * </ul>
	 * The XML is expected to be rooted at the parent of the set of
	 * langstring|string elements.
	 */
	public static LanguageBundle getBundleFromXml(PropBagEx xml)
	{
		return getBundleFromXml(xml, CurrentLocale.getLocale());
	}

	public static LanguageBundle getBundleFromXmlString(String xmlString)
	{
		if( !Check.isEmpty(xmlString) )
		{
			try
			{
				return getBundleFromXml(new PropBagEx(xmlString));
			}
			catch( RuntimeException ex )
			{
				// Ignore and fall through
			}
		}
		return null;
	}

	/**
	 * Extracts a bunch of languages from either the root node, the old IMS or
	 * the IEEE LOM standards, eg:
	 * <ul>
	 * <li>Some String</li>
	 * <li>&lt;langstring lang="en"&gt;Some string&lt;/langstring&gt;</li>
	 * <li>&lt;string language="en"&gt;Some string&lt;/string&gt;</li>
	 * </ul>
	 * The XML is expected to be rooted at the parent of the set of
	 * langstring|string elements.
	 */
	public static LanguageBundle getBundleFromXml(PropBagEx xml, Locale defaultLocale)
	{
		if( xml == null )
		{
			return null;
		}

		LanguageBundle result = new LanguageBundle();

		if( xml.nodeExists("*") ) //$NON-NLS-1$
		{
			// IEEE LOM standard
			String nodeName = IEEE_LOM_LANG_NODE;
			String attribute = IEEE_LOM_LANG_ATTRIBUTE;

			// If no IEEE nodes exists...
			if( !xml.nodeExists(nodeName) )
			{
				// ...try the old IMS standard
				nodeName = "langstring"; //$NON-NLS-1$
				attribute = "@lang"; //$NON-NLS-1$
			}

			populateLanguageStringsFromXml(result, xml, nodeName, attribute);
		}
		else
		{
			String val = xml.getNode();
			if( !Check.isEmpty(val) )
			{
				// No child nodes - just get the value as the default locale
				createLanguageString(result, defaultLocale, val);
			}
		}
		result.ensureStrings();
		return result;
	}

	/**
	 * Builds up a map of LanguageStrings from and XML document.
	 */
	private static void populateLanguageStringsFromXml(LanguageBundle bundle, PropBagEx xml, String nodeName,
		String attribute)
	{
		for( PropBagEx node : xml.iterateAll(nodeName) )
		{
			Locale locale = LocaleUtils.parseLocale(node.getNode(attribute));
			String str = node.getNode();
			if( !Check.isEmpty(str) )
			{
				createLanguageString(bundle, locale, str);
			}
		}
	}

	/**
	 * Saves a LanguageBundle to an XML document in IEEE LOM format.
	 */
	public static void setBundleToXml(LanguageBundle bundle, PropBagEx xml)
	{
		// Clear existing nodes
		xml.deleteAll(IEEE_LOM_LANG_NODE);

		if( bundle != null )
		{
			Map<String, LanguageString> strings = bundle.getStrings();
			if( strings != null )
			{
				for( LanguageString langstring : bundle.getStrings().values() )
				{
					PropBagEx subXml = xml.newSubtree(IEEE_LOM_LANG_NODE);
					subXml.setNode(IEEE_LOM_LANG_ATTRIBUTE, langstring.getLocale());
					subXml.setNode("", langstring.getText()); //$NON-NLS-1$
				}
			}
		}
	}

	public static String getBundleAsXmlString(LanguageBundle bundle)
	{
		// if( bundle == null )
		// {
		// return null;
		// }

		PropBagEx xml = new PropBagEx();
		setBundleToXml(bundle, xml);
		return xml.toString();
	}

	/**
	 * Retrieves the closest matching value from a map of Locale strings to
	 * values of some type.
	 */
	public static <T> T getClosestObjectForLocale(Map<String, T> values, Locale locale)
	{
		Stack<String> keys = LocaleUtils.getAllPossibleKeys(locale);
		while( !keys.isEmpty() )
		{
			String key = keys.pop();
			T value = values.get(key);
			if( value != null )
			{
				return value;
			}
		}
		String langOnly = locale.getLanguage() + '_';
		String moreSpecific = langOnly + locale.getCountry();

		for( Map.Entry<String, T> entry : values.entrySet() )
		{
			String key = entry.getKey();
			if( key.startsWith(moreSpecific) || key.startsWith(langOnly) )
			{
				return entry.getValue();
			}
		}

		Collection<T> vs = values.values();
		return vs.isEmpty() ? null : vs.iterator().next();
	}

	public static LanguageBundle createTempLangugageBundle(String key, Object... values)
	{
		String text = key == null ? (String) values[0] : CurrentLocale.get(key, values);
		LanguageString langstring = createLanguageString(null, CurrentLocale.getLocale(), text);

		LanguageBundle bundle = new LanguageBundle();
		langstring.setBundle(bundle);
		bundle.ensureStrings().put(langstring.getLocale(), langstring);
		// bundle.setStrings(Collections.singletonMap(langstring.getLocale(),
		// langstring));
		return bundle;
	}

	public static LanguageBundle createTextTempLangugageBundle(Map<String, String> localeMap)
	{
		final LanguageBundle bundle = new LanguageBundle();
		final Map<String, LanguageString> stringMap = Maps.newHashMap();
		if( localeMap != null )
		{
			for( Entry<String, String> entry : localeMap.entrySet() )
			{
				final Locale locale = new Locale(entry.getKey());
				final LanguageString langstring = createLanguageString(null, locale, entry.getValue());
				langstring.setBundle(bundle);
				stringMap.put(locale.toString(), langstring);
			}
		}
		bundle.setStrings(stringMap);
		return bundle;
	}

	public static LanguageBundle createTextTempLangugageBundle(String text)
	{
		return createTextTempLangugageBundle(text, CurrentLocale.getLocale());
	}

	public static LanguageBundle createTextTempLangugageBundle(String text, Locale locale)
	{
		LanguageString langstring = createLanguageString(null, locale, text);

		// We want to set the bundle on the langstring separately so we can use
		// SingletonMap.
		LanguageBundle bundle = new LanguageBundle();
		langstring.setBundle(bundle);
		bundle.setStrings(Collections.singletonMap(langstring.getLocale(), langstring));
		return bundle;
	}

	/**
	 * Creates a new LanguageString.
	 */
	public static LanguageString createLanguageString(final LanguageBundle bundle, final Locale locale,
		final String text)
	{
		LanguageString string = new LanguageString();
		string.setBundle(bundle);
		string.setLocale(LocaleUtils.getExactKey(locale));
		string.setPriority(LocaleUtils.getPriorityForLocale(locale));
		string.setText(text);

		if( bundle != null )
		{
			bundle.ensureStrings().put(string.getLocale(), string);
		}

		return string;
	}

	public static boolean isEmpty(LanguageBundle bundle)
	{
		return bundle == null || bundle.isEmpty();
	}

	public static boolean isEmpty(LanguageBundleBean bean)
	{
		return bean == null || bean.isEmpty();
	}

	public static List<Long> convertToBundleIds(List<? extends BundleReference> defs)
	{
		List<Long> ids = new ArrayList<Long>(defs.size());
		for( BundleReference bent : defs )
		{
			ids.add(bent.getBundleId());
		}
		return ids;
	}

	public static LanguageBundleBean convertBundleToBean(LanguageBundle bundle)
	{
		if( bundle == null )
		{
			return null;
		}
		final LanguageBundleBean bean = new LanguageBundleBean();
		bean.setId(bundle.getId());
		final Map<String, LanguageStringBean> strings = Maps.newHashMap();
		final Map<String, LanguageString> bundleStrings = bundle.getStrings();
		for( LanguageString bundleString : bundleStrings.values() )
		{
			final LanguageStringBean stringBean = new LanguageStringBean();
			stringBean.setId(bundleString.getId());
			stringBean.setLocale(bundleString.getLocale());
			stringBean.setPriority(bundleString.getPriority());
			stringBean.setText(bundleString.getText());
			strings.put(bundleString.getLocale(), stringBean);
		}
		bean.setStrings(strings);
		return bean;
	}

	public static LanguageBundle convertBeanToBundle(LanguageBundleBean bean)
	{
		if( bean == null )
		{
			return null;
		}
		final LanguageBundle entity = new LanguageBundle();
		entity.setId(bean.getId());
		final Map<String, LanguageString> strings = Maps.newHashMap();
		final Map<String, LanguageStringBean> beanStrings = bean.getStrings();
		for( LanguageStringBean beanString : beanStrings.values() )
		{
			final LanguageString string = new LanguageString();
			string.setId(beanString.getId());
			string.setLocale(beanString.getLocale());
			string.setPriority(beanString.getPriority());
			string.setText(beanString.getText());
			strings.put(string.getLocale(), string);
			string.setBundle(entity);
		}
		entity.setStrings(strings);
		return entity;
	}

	private LangUtils()
	{
		throw new Error();
	}
}
