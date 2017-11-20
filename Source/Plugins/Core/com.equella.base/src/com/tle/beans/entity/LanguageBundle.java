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

package com.tle.beans.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NamedNativeQueries;
import org.hibernate.annotations.NamedNativeQuery;

import com.tle.beans.IdCloneable;
import com.tle.common.Check;
import com.tle.common.i18n.LangUtils;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.i18n.beans.LanguageStringBean;

@Entity
@AccessType("property")
@SqlResultSetMapping(name = "resultMap", columns = {@ColumnResult(name = "bundle_id"), @ColumnResult(name = "text")})
@NamedNativeQueries({
		@NamedNativeQuery(name = "getClosest", readOnly = true, resultSetMapping = "resultMap", query = ""
			+ "SELECT s2.bundle_id as bundle_id, s2.text as text                  "
			+ "FROM (                                                             "
			+ "    SELECT bundle_id, max(priority) AS priority                    "
			+ "    FROM language_string                                           "
			+ "    WHERE bundle_id IN (:bundles) AND locale IN (:locales)         "
			+ "    GROUP BY bundle_id                                             "
			+ ") s1                                                               "
			+ "INNER JOIN language_string s2                                      "
			+ "    ON (s1.bundle_id = s2.bundle_id AND s1.priority = s2.priority) "
			+ "WHERE s2.locale IN (:locales)                                      "),
		@NamedNativeQuery(name = "getMoreSpecific", readOnly = true, resultSetMapping = "resultMap", query = ""
			+ "SELECT s2.bundle_id as bundle_id, s2.text as text                  "
			+ "FROM (                                                             "
			+ "    SELECT bundle_id, max(priority) AS priority                    "
			+ "    FROM language_string                                           "
			+ "    WHERE bundle_id IN (:bundles) AND (locale LIKE :locale OR       "
			+ "                                   locale LIKE :locale2)            "
			+ "    GROUP BY bundle_id                                             "
			+ ") s1                                                               "
			+ "INNER JOIN language_string s2                                      "
			+ "    ON (s1.bundle_id = s2.bundle_id AND s1.priority = s2.priority) "
			+ "WHERE locale LIKE :locale                                          "),
		@NamedNativeQuery(name = "getLeastSpecific", readOnly = true, resultSetMapping = "resultMap", query = ""
			+ "SELECT s2.bundle_id as bundle_id, s2.text as text                  "
			+ "FROM (                                                             "
			+ "    SELECT bundle_id, min(priority) AS priority                    "
			+ "    FROM language_string                                           "
			+ "    WHERE bundle_id IN (:bundles)                                  "
			+ "    GROUP BY bundle_id                                             "
			+ ") s1                                                               "
			+ "INNER JOIN language_string s2                                      "
			+ "    ON (s1.bundle_id = s2.bundle_id AND s1.priority = s2.priority) ")})
public class LanguageBundle implements Serializable, IdCloneable
{
	private static final long serialVersionUID = 1L;

	private long id;
	private Map<String, LanguageString> strings;

	public LanguageBundle()
	{
		super();
	}

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "bundle")
	@Fetch(value = FetchMode.SELECT)
	@MapKey(name = "locale")
	public Map<String, LanguageString> getStrings()
	{
		return strings;
	}

	public void setStrings(Map<String, LanguageString> strings)
	{
		this.strings = strings;
	}

	public synchronized Map<String, LanguageString> ensureStrings()
	{
		if( strings == null )
		{
			strings = new HashMap<String, LanguageString>();
		}
		return strings;
	}

	@Transient
	public boolean isEmpty()
	{
		if( strings != null )
		{
			for( LanguageString string : strings.values() )
			{
				if( !Check.isEmpty(string.getText()) )
				{
					return false;
				}
			}
		}
		return true;
	}

	public static LanguageBundle clone(LanguageBundle bundle)
	{
		if( bundle == null )
		{
			return null;
		}
		LanguageBundle newBundle = new LanguageBundle();
		HashMap<String, LanguageString> langStrings = new HashMap<String, LanguageString>();
		Map<String, LanguageString> oldStrings = bundle.getStrings();
		for( LanguageString langString : oldStrings.values() )
		{
			LanguageString newString = new LanguageString();
			newString.setBundle(newBundle);
			newString.setText(langString.getText());
			newString.setLocale(langString.getLocale());
			newString.setPriority(langString.getPriority());
			langStrings.put(newString.getLocale(), newString);
		}
		newBundle.setStrings(langStrings);
		return newBundle;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			// Reflexitivity
			return true;
		}
		else if( obj == null )
		{
			// Non-null
			return false;
		}
		else if( this.getClass() != obj.getClass() )
		{
			// Symmetry
			return false;
		}
		else
		{
			return Objects.equals(((LanguageBundle) obj).getStrings(), this.getStrings());
		}
	}

	@Override
	public int hashCode()
	{
		return Check.getHashCode(strings);
	}

	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		for( Map.Entry<String, LanguageString> entry : getStrings().entrySet() )
		{
			str.append(entry.getValue().getText());
		}
		// easily detectable if accidently printed directly to an FTL
		return "LB[[" + str.toString() + "]]";
	}

	public static LanguageBundle edit(LanguageBundle orig, LanguageBundle newBundle, DeleteHandler deleteHandler)
	{
		if( orig == null )
		{
			if( newBundle != null )
			{
				newBundle.setId(0);
			}
			return newBundle;
		}
		if( newBundle == null || Check.isEmpty(newBundle.getStrings()) )
		{
			deleteHandler.deleteBundleObject(orig);
			return null;
		}

		Map<String, LanguageString> oldStrings = orig.getStrings();
		Set<String> keys = new HashSet<String>(oldStrings.keySet());
		Map<String, LanguageString> newStrings = newBundle.getStrings();

		// It used to use a values iterator, but Hibernate is known to have issues with it and remove.
		// https://hibernate.atlassian.net/browse/HHH-5742
		for( String key : keys )
		{
			LanguageString oldString = oldStrings.get(key);
			//locale *should* be the same as the key
			String locale = oldString.getLocale();
			LanguageString newString = newStrings.get(locale);
			if( newString == null )
			{
				oldStrings.remove(key);
				deleteHandler.deleteBundleObject(oldString);
			}
			else
			{
				oldString.setText(newString.getText());
			}
		}
		for( LanguageString newString : newStrings.values() )
		{
			if( !oldStrings.containsKey(newString.getLocale()) )
			{
				newString.setBundle(orig);
				newString.setId(0);
				oldStrings.put(newString.getLocale(), newString);
			}
		}
		return orig;
	}

	// TODO: maybe refactor...
	public static LanguageBundle edit(LanguageBundle orig, LanguageBundleBean newBundle, DeleteHandler deleteHandler)
	{
		if( orig == null )
		{
			if( newBundle != null )
			{
				newBundle.setId(0);
			}
			return LangUtils.convertBeanToBundle(newBundle);
		}
		if( newBundle == null || Check.isEmpty(newBundle.getStrings()) )
		{
			deleteHandler.deleteBundleObject(orig);
			return null;
		}
		Map<String, LanguageString> oldStrings = orig.getStrings();
		Iterator<LanguageString> iter = oldStrings.values().iterator();
		Map<String, LanguageStringBean> newStrings = newBundle.getStrings();
		while( iter.hasNext() )
		{
			LanguageString oldString = iter.next();
			String locale = oldString.getLocale();
			LanguageStringBean newString = newStrings.get(locale);
			if( newString == null )
			{
				iter.remove();
				deleteHandler.deleteBundleObject(oldString);
			}
			else
			{
				oldString.setText(newString.getText());
			}
		}
		for( LanguageStringBean newString : newStrings.values() )
		{
			if( !oldStrings.containsKey(newString.getLocale()) )
			{
				LanguageString dbString = new LanguageString();
				dbString.setBundle(orig);
				dbString.setId(0);
				dbString.setLocale(newString.getLocale());
				dbString.setPriority(newString.getPriority());
				dbString.setText(newString.getText());
				oldStrings.put(dbString.getLocale(), dbString);
			}
		}
		return orig;
	}

	public interface DeleteHandler
	{
		void deleteBundleObject(Object obj);
	}
}
