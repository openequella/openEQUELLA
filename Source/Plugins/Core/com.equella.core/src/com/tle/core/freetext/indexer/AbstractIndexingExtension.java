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

package com.tle.core.freetext.indexer;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.hibernate.Hibernate;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.item.ItemSelect;
import com.tle.common.searching.DateFilter;
import com.tle.common.searching.DateFilter.Format;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;

public abstract class AbstractIndexingExtension implements IndexingExtension
{
	@Override
	public void prepareForLoad(ItemSelect select)
	{
		// nothing by default
	}

	public void addAllFields(Document doc, List<Fieldable> fields)
	{
		for( Fieldable field : fields )
		{
			doc.add(field);
		}
	}

	protected static String getSortableDate(Date date, DateFilter.Format format, Long defaultTime)
	{
		return new UtcDate(date).format(Dates.ISO);
	}

	public static Field addDateField(Document doc, String name, Date date, DateFilter.Format format, Long defaultTime)
	{
		if( date == null && defaultTime == null )
		{
			return null;
		}
		String val;
		if( format == Format.ISO )
		{
			if( date == null )
			{
				date = new Date(defaultTime);
			}
			val = new UtcDate(date).format(Dates.ISO);
		}
		else
		{
			if( date != null )
			{
				val = Long.toString(date.getTime());
			}
			else
			{
				val = defaultTime.toString();
			}
		}
		Field field = new Field(name, val, Field.Store.NO, Field.Index.NOT_ANALYZED);
		doc.add(field);
		return field;
	}

	public static Field indexed(String name, String value)
	{
		return new Field(name, value, Field.Store.NO, Field.Index.NOT_ANALYZED);
	}

	public static Field keyword(String name, String value)
	{
		return new Field(name, value, Field.Store.YES, Field.Index.NOT_ANALYZED);
	}

	public static Field keyword(String name, TokenStream value)
	{
		return new Field(name, value);
	}

	public static Field unstored(String name, String value)
	{
		return new Field(name, value, Field.Store.NO, Field.Index.ANALYZED);
	}

	public static Field unstoredAndVectored(String name, String value)
	{
		return new Field(name, value, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES);
	}

	protected StringBuilder gatherLanguageBundles(LanguageBundle... bundles)
	{
		StringBuilder builder = new StringBuilder();
		for( LanguageBundle bundle : bundles )
		{
			if( bundle != null )
			{
				Map<String, LanguageString> strings = bundle.getStrings();
				if( strings != null )
				{
					for( LanguageString langstring : bundle.getStrings().values() )
					{
						builder.append(langstring.getText());
						builder.append(' ');
					}
				}
			}
		}
		return builder;
	}

	protected void initBundle(LanguageBundle bundle)
	{
		if( bundle != null )
		{
			Hibernate.initialize(bundle.getStrings());
		}
	}

}
