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

package com.tle.core.remoterepo.z3950.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.jafer.exception.JaferException;
import org.jafer.interfaces.QueryBuilder;
import org.jafer.record.DataObject;
import org.jafer.record.Field;
import org.jafer.record.RecordFactory;
import org.jafer.zclient.ZClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.Z3950Settings;
import com.tle.beans.search.Z3950Settings.AttributeProfile;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.core.fedsearch.GenericRecord;
import com.tle.core.fedsearch.impl.BasicRecord;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.remoterepo.parser.mods.impl.loose.LooseModsRecord;
import com.tle.core.remoterepo.z3950.AdvancedSearchOptions;
import com.tle.core.remoterepo.z3950.AdvancedSearchOptions.ExtraQuery;
import com.tle.core.remoterepo.z3950.Z3950Constants.Accuracy;
import com.tle.core.remoterepo.z3950.Z3950Constants.Operator;
import com.tle.core.remoterepo.z3950.Z3950Constants.Use;
import com.tle.core.remoterepo.z3950.Z3950SearchResult;
import com.tle.core.remoterepo.z3950.Z3950SearchResults;
import com.tle.core.remoterepo.z3950.service.Z3950Service;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind(Z3950Service.class)
@Singleton
public class Z3950ServiceImpl implements Z3950Service
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(Z3950Service.class);
	/**
	 * z3950 is inherently dodgy, don't log full stack traces or you will end up
	 * with massive log files.
	 */
	private static final Logger LOGGER = Logger.getLogger(Z3950Service.class);

	private static final Z3950SearchResults NO_RESULTS = new Z3950SearchResults(new ArrayList<Z3950SearchResult>(), 0,
		0, 0);

	// Use, Relation, Position, Structure, Truncation, Completeness
	// ANY, EQUALS, ANY, ?, NONE, INCOMPLETE
	private static final String QUERY_CONNECTION = "1016.3.3.2.100.1";

	@SuppressWarnings("null")
	@Override
	public Z3950SearchResults search(FederatedSearch z3950Search, String qs, int offset, int perpage,
		AdvancedSearchOptions advanced)
	{
		if( Check.isEmpty(qs) )
		{
			if( advanced != null )
			{
				List<ExtraQuery> extra = advanced.getExtra();
				if( extra != null && extra.size() > 0 )
				{
					if( Check.isEmpty(extra.get(0).getTerm()) )
					{
						return NO_RESULTS;
					}
				}
			}
			else
			{
				return NO_RESULTS;
			}
		}

		WrappedClient wc = null;
		try
		{
			// qs can conceivably be null here for an advanced search EQ-200
			if( qs != null )
			{
				qs = qs.trim();
			}
			wc = makeClientForQuery(z3950Search, perpage, qs, advanced, false);

			// NOTE: The first result is 1 NOT 0
			final int start = offset + 1;
			int end = start + perpage - 1;
			if( end > wc.numResults )
			{
				end = wc.numResults;
			}
			int count = end - start + 1;
			if( count < 0 )
			{
				count = 0;
			}

			final List<Z3950SearchResult> results = new ArrayList<Z3950SearchResult>(count);
			for( int i = start; i <= end; i++ )
			{
				wc.client.setRecordCursor(i);
				try
				{
					final Z3950SearchResult result = convertFieldToResult(wc.client.getCurrentRecord(), i - 1);
					results.add(result);
				}
				catch( NullPointerException n ) // NOSONAR
				{
					// this is actually required because BEREncoding sometimes
					// throws null pointers when it can't
					// read from the stream it's given...
					LOGGER.warn("Error getting search result for " + CurrentLocale.get(z3950Search.getName())
						+ " (Index: " + (i - 1) + ")");
				}
			}

			return new Z3950SearchResults(results, count, offset, wc.numResults);
		}
		catch( Exception e )
		{
			LOGGER.debug("Error executing search", e);
			throw new RuntimeException(
				"Error executing search " + CurrentLocale.get(z3950Search.getName()) + ": " + e.getMessage(), e);
		}
		finally
		{
			if( wc != null )
			{
				try
				{
					wc.client.close();
				}
				catch( Exception e )
				{
					// Ignore
				}
			}
		}
	}

	@Override
	public GenericRecord getRecord(FederatedSearch z3950Search, String qs, int index, AdvancedSearchOptions advanced,
		boolean useImportSchema)
	{
		WrappedClient wc;
		try
		{
			wc = makeClientForQuery(z3950Search, 1, qs, advanced, useImportSchema);
			wc.client.setRecordCursor(index + 1);
			return convertFieldToFullRecord(wc.client.getCurrentRecord(), index);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	protected WrappedClient makeClientForQuery(FederatedSearch z3950Search, int fetchSize, String query,
		AdvancedSearchOptions advanced, boolean useConfiguredImportSchema) throws JaferException
	{
		Z3950Settings settings = new Z3950Settings();
		settings.load(z3950Search);
		String attributes;

		if( Check.isEmpty(settings.getStandardAttributes()) )
		{
			attributes = QUERY_CONNECTION;
		}
		else
		{
			attributes = settings.getStandardAttributes();
		}

		final QueryBuilder qb = new org.jafer.query.QueryBuilder(attributes);

		Node searchNode = null;

		// Convert basic queries into advanced queries
		if( advanced == null && !Check.isEmpty(query) )
		{
			advanced = new AdvancedSearchOptions();

			for( String q : query.split("\\s+") )
			{
				advanced.addExtra(attributes, q, Operator.AND);
			}
		}

		if( advanced != null )
		{
			for( ExtraQuery extra : advanced.getExtra() )
			{
				String t = extra.getTerm();
				if( Check.isEmpty(t) )
				{
					break;
				}
				t = t.trim();

				int[] atts = new int[6];
				final String a = extra.getAttributes();
				int index = 0;
				for( String att : a.split("\\.") )
				{
					atts[index] = Integer.parseInt(att);
					index++;
				}

				final Operator op = extra.getOperator();

				switch( op )
				{
					case OR:
						searchNode = qb.or(searchNode, qb.getNode(atts, t));
						break;

					case ANDNOT:
						searchNode = qb.and(searchNode, qb.not(qb.getNode(atts, t)));
						break;

					default:
						if( searchNode == null )
						{
							searchNode = qb.getNode(atts, t);
						}
						else
						{
							searchNode = qb.and(searchNode, qb.getNode(atts, t));
						}
				}
			}
		}

		final WrappedClient wc = new WrappedClient();
		wc.client = new ZClient();
		wc.settings = new Z3950Settings();
		wc.settings.load(z3950Search);
		wc.client.setHost(wc.settings.getHost());
		wc.client.setPort(wc.settings.getPort());
		wc.client.setDatabases(wc.settings.getDatabase());
		wc.client.setUsername(wc.settings.getUsername());
		wc.client.setPassword(wc.settings.getPassword());
		wc.client.setGroup(wc.settings.getGroup());
		wc.client.setFetchSize(fetchSize);
		wc.client.setCheckRecordFormat(true);
		wc.client.setRecordSchema(
			useConfiguredImportSchema ? wc.settings.getImportRecordSchema() : "http://www.loc.gov/mods/");
		wc.numResults = wc.client.submitQuery(searchNode);
		return wc;
	}

	protected static class WrappedClient
	{
		ZClient client;
		Z3950Settings settings;
		int numResults;
	}

	protected Z3950SearchResult convertFieldToResult(Field field, int resultIndex)
	{
		try
		{
			final Z3950SearchResult result = new Z3950SearchResult(resultIndex);

			// result.setUuid(field.getFirst("recordIdentifier").getValue());
			result.setTitle(field.getFirst("title").getValue());
			result.setDescription(getAuthor(field));

			final PropBagEx xml = new PropBagEx(field.getXML());

			String publisher = xml.getNode("publication/publisher");
			result.setPublisher(publisher);

			List<Element> dateNodes = xml.getNodesByAttribute("type", "published");
			dateNodes.addAll(xml.getNodesByAttribute("type", "issued"));
			if( !dateNodes.isEmpty() )
			{
				for( Element date : dateNodes )
				{
					if( date.getNodeName().equals("date") )
					{
						result.setPublishDate(date.getTextContent());
					}
				}
			}

			result.setEdition(xml.getNode("publication/edition"));

			for( PropBagEx ident : xml.iterateAll("identifier") )
			{
				final String type = ident.getNode("@type");
				final String id = ident.getNode();
				if( type.equals("uri") )
				{
					result.setUrl(id);
				}
				else if( type.equals("isbn") )
				{
					result.setIsbn(tidyIsbn(id, result.getIsbn()));
				}
				else if( type.equals("issn") )
				{
					result.setIssn(id);
				}
				else if( type.equals("lccn") )
				{
					result.setLccn(id);
				}
			}

			return result;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	protected GenericRecord convertFieldToFullRecord(Field field, int resultIndex)
	{
		try
		{
			// It pretty much has to. We request MODS in the search params
			final PropBagEx xml = new PropBagEx(field.getXML());
			if( field.getRecordSchema().startsWith("http://www.loc.gov/mods") )
			{
				final LooseModsRecord result = new LooseModsRecord(xml);

				// TODO: Move to ModsRecord
				for( PropBagEx ident : xml.iterateAll("identifier") )
				{
					final String type = ident.getNode("@type");
					final String id = ident.getNode();

					if( type.equals("isbn") )
					{
						result.setIdentifier(type, tidyIsbn(id, result.getIsbn()));
					}
					else
					{
						result.setIdentifier(type, id);
					}
				}
				return result;
			}

			final BasicRecord basic = new BasicRecord();
			basic.setXml(xml);
			return basic;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	private String tidyIsbn(String isbn, String currentIsbnValue)
	{
		// Trim the junk off the ISBN. A real ISBN can be at most 13 digits long
		// Get the longest fully digit version of the ISBN
		if( !Check.isEmpty(isbn) )
		{
			// all 13 digit ISBNs start with 978 or 979
			int length = (isbn.startsWith("978") || isbn.startsWith("979") ? Math.min(13, isbn.length())
				: Math.min(10, isbn.length()));
			if( Check.isEmpty(currentIsbnValue) || length > currentIsbnValue.length() )
			{
				return isbn.substring(0, length);
			}
		}
		return currentIsbnValue;
	}

	protected PropBagEx convertFieldToXml(Field field, WrappedClient wc) throws JaferException
	{
		// final String displaySchema = wc.settings.getDisplayRecordSchema();
		final String importSchema = wc.settings.getImportRecordSchema();

		final DataObject dataObject = wc.client.getCurrentDataObject();
		final String schema = wc.client.getRecordSchema();

		PropBagEx xml = new PropBagEx(field.getXML());

		// PropBagEx displayXml = xml;
		// if( !schema.equals(displaySchema) )
		// {
		// displayXml = getXmlInSchema(dataObject, wc.client, displaySchema);
		// }
		// result.setExtrameta(displayXml);

		PropBagEx importXml = xml;
		if( !schema.equals(importSchema) )
		{
			importXml = getXmlInSchema(dataObject, wc.client, importSchema);
		}
		return importXml;
	}

	/**
	 * If there is no direct "author" then a number of different fields are used
	 * instead
	 * 
	 * @param field
	 * @return
	 */
	protected String getAuthor(Field field)
	{
		String currentAuthor = field.getFirst("name", "role", "creator").getFirst("displayForm").getValue();
		if( currentAuthor.length() == 0 )
		{
			currentAuthor = field.getFirst("name", "role", "creator").getValue();
		}
		if( currentAuthor.length() == 0 )
		{
			currentAuthor = field.getFirst("name").getValue();
		}
		if( currentAuthor.length() == 0 )
		{
			currentAuthor = field.getFirst("title", "type", "uniform").getValue();
		}
		if( currentAuthor.length() == 0 )
		{
			currentAuthor = field.getFirst("title", "type", "alternate").getValue();
		}
		return currentAuthor;
	}

	private PropBagEx getXmlInSchema(DataObject dataObject, ZClient client, String displaySchema) throws JaferException
	{
		return new PropBagEx(
			((Node) new RecordFactory().getXML(dataObject, client.getDocument(), displaySchema, 1)).getFirstChild());
	}

	private List<NameValue> listEquellaDefaultFields()
	{
		List<NameValue> equellaDefaults = new ArrayList<NameValue>();
		equellaDefaults.add(keywordAttribute("field.anywhere", Use.ANY));
		equellaDefaults.add(keywordAttribute("field.name.personal", Use.NAME_PERSONAL));
		equellaDefaults.add(keywordAttribute("field.name.corporate", Use.NAME_CORPORATE));
		equellaDefaults.add(keywordAttribute("field.name.conference", Use.NAME_CONFERENCE));
		equellaDefaults.add(keywordAttribute("field.title", Use.TITLE));
		equellaDefaults.add(keywordAttribute("field.title.series", Use.TITLE_SERIES));
		equellaDefaults.add(keywordAttribute("field.title.uniform", Use.TITLE_UNIFORM));
		equellaDefaults.add(keywordAttribute("field.title.key", Use.TITLE_KEY));
		equellaDefaults.add(keywordAttribute("field.isbn", Use.ISBN));
		equellaDefaults.add(keywordAttribute("field.issn", Use.ISSN));
		equellaDefaults.add(keywordAttribute("field.lccn", Use.LCCN));
		equellaDefaults.add(keywordAttribute("field.lccn2", Use.LCCN2));
		equellaDefaults.add(keywordAttribute("field.dewey", Use.DEWEY));
		equellaDefaults.add(keywordAttribute("field.subject", Use.SUBJECT));
		equellaDefaults.add(keywordAttribute("field.subject.authorized", Use.SUBJECT_LC));
		equellaDefaults.add(keywordAttribute("field.subject.personal", Use.SUBJECT_PERSONAL));
		equellaDefaults.add(keywordAttribute("field.date", Use.PUBLISHED_DATE));
		equellaDefaults.add(keywordAttribute("field.publisher", Use.PUBLISHER));
		equellaDefaults.add(keywordAttribute("field.geographicname", Use.GEOGRAPHIC_NAME));
		equellaDefaults.add(keywordAttribute("field.note", Use.NOTE));
		equellaDefaults.add(keywordAttribute("field.author", Use.AUTHOR));
		return equellaDefaults;
	}

	private static NameValue keywordAttribute(String key, Use use)
	{
		return new NameValue(CurrentLocale.get(resources.key("search." + key)),
			use.value() + Accuracy.KEYWORD.getAttributes());
	}

	private List<NameValue> listBathZeroDefaultFields()
	{
		List<NameValue> bathZero = new ArrayList<NameValue>();
		bathZero.add(attribute("field.anywhere", Use.ANY, Accuracy.KEYWORD));
		bathZero.add(attribute("field.author", Use.AUTHOR, Accuracy.PRECISION));
		bathZero.add(attribute("field.title", Use.TITLE, Accuracy.KEYWORD));
		bathZero.add(attribute("field.subject", Use.SUBJECT, Accuracy.KEYWORD));
		return bathZero;
	}

	private List<NameValue> listBathOneDefaultFields()
	{
		List<NameValue> bathOne = new ArrayList<NameValue>(listBathZeroDefaultFields());
		bathOne.add(attribute("field.author", Use.AUTHOR, Accuracy.PRECISION_RIGHT_TRUNCATION));
		bathOne.add(attribute("field.author", Use.AUTHOR, Accuracy.KEYWORD));
		bathOne.add(attribute("field.author", Use.AUTHOR, Accuracy.KEYWORD_RIGHT_TRUNCATION));
		bathOne.add(attribute("field.author", Use.AUTHOR, Accuracy.EXACT));
		bathOne.add(attribute("field.title", Use.TITLE, Accuracy.KEYWORD_RIGHT_TRUNCATION));
		bathOne.add(attribute("field.title", Use.TITLE, Accuracy.EXACT));
		bathOne.add(attribute("field.title", Use.TITLE, Accuracy.FIRST_WORD));
		bathOne.add(attribute("field.title", Use.TITLE, Accuracy.FIRST_CHARACTER));
		bathOne.add(attribute("field.subject", Use.SUBJECT, Accuracy.KEYWORD_RIGHT_TRUNCATION));
		bathOne.add(attribute("field.subject", Use.SUBJECT, Accuracy.EXACT));
		bathOne.add(attribute("field.subject", Use.SUBJECT, Accuracy.FIRST_WORD));
		bathOne.add(attribute("field.subject", Use.SUBJECT, Accuracy.FIRST_CHARACTER));
		bathOne.add(attribute("field.anywhere", Use.ANY, Accuracy.KEYWORD_RIGHT_TRUNCATION));
		bathOne.add(attribute("field.identifier", Use.STANDARD_IDENTIFIER, Accuracy.FIRST_WORD));
		bathOne.addAll(publicationDatesFields());
		return bathOne;
	}

	private static NameValue attribute(String key, Use use, Accuracy acc)
	{
		String name = CurrentLocale.get(resources.key("search." + key));
		name += " " + CurrentLocale.get(resources.key("search." + acc.getLangKey()));
		return new NameValue(name, use.value() + acc.getAttributes());
	}

	private List<NameValue> publicationDatesFields()
	{
		List<NameValue> fields = new ArrayList<NameValue>();
		for( int rel = 1; rel <= 5; rel++ )
		{
			String name = CurrentLocale.get(resources.key("search.field.date"));
			name += " " + CurrentLocale.get(resources.key("relation." + rel));
			fields.add(new NameValue(name, Use.PUBLISHED_DATE.value() + "." + rel + ".1.4.100.1"));
		}
		return fields;
	}

	@Override
	public List<NameValue> convertAdvancedFieldsXml(String xml, BundleCache cache)
	{
		List<NameValue> fields = new ArrayList<NameValue>();
		if( !Check.isEmpty(xml) )
		{
			PropBagEx fieldsXml = new PropBagEx(xml);
			PropBagIterator entryIterator = fieldsXml.iterator("entry");
			while( entryIterator.hasNext() )
			{
				PropBagEx entryXml = entryIterator.next();
				BundleNameValue field = new BundleNameValue(LangUtils.getBundleFromXml(entryXml.getSubtree("name")),
					entryXml.getNode("value"), cache);
				fields.add(field);
			}
		}
		return fields;
	}

	@Override
	public List<NameValue> listDefaultFields(AttributeProfile profile)
	{
		switch( profile )
		{
			case EQUELLA:
				return listEquellaDefaultFields();
			case BATH_0:
				return listBathZeroDefaultFields();
			case BATH_1:
				return listBathOneDefaultFields();
			default:
				return listEquellaDefaultFields();
		}
	}
}
