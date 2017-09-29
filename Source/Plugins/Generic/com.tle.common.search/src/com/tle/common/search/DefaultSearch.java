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

package com.tle.common.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.collect.Lists;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemSelect;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.common.searching.DateFilter;
import com.tle.common.searching.Field;
import com.tle.common.searching.SortField;
import com.tle.common.searching.VeryBasicSearch;

@SuppressWarnings("nls")
public class DefaultSearch extends VeryBasicSearch
{
	private static final long serialVersionUID = 1L;

	public static final String PRIV_DISCOVER_ITEM = "DISCOVER_ITEM";
	public static final String PRIV_DOWNLOAD_ITEM = "DOWNLOAD_ITEM";

	protected Collection<ItemDefinition> itemDefinitions;
	protected Collection<String> collectionUuids;
	protected Collection<Schema> schemas;
	protected Collection<CourseInfo> courses;
	protected String activationStatus;
	protected List<ItemStatus> itemStatuses;
	protected List<ItemStatus> notItemStatuses;
	protected Date[] dateRange;
	protected SortField[] sortFields;
	protected boolean sortReversed;
	protected String privilege = PRIV_DISCOVER_ITEM;
	protected String privilegePrefix;
	protected String searchType = "item"; //$NON-NLS-1$
	protected boolean moderating;
	protected String owner;
	private DefaultSearch toplevel;
	protected ItemSelect select;
	protected Collection<String> mimeTypes;
	private String privilegeToCollect;
	protected Collection<DateFilter> dateFilters = Lists.newArrayList();
	private final List<List<Field>> musts = new ArrayList<List<Field>>();
	private final List<List<Field>> mustNots = new ArrayList<List<Field>>();

	public DefaultSearch()
	{
		super();
		toplevel = this;
	}

	@Override
	public Date[] getDateRange()
	{
		return dateRange;
	}

	public void setDateRange(Date[] dateRange)
	{
		this.dateRange = dateRange;
	}

	public void setCollectionUuids(@Nullable Collection<String> uuids)
	{
		this.collectionUuids = (uuids == null ? null : new HashSet<>(uuids));
	}

	public Collection<String> getCollectionUuids()
	{
		return collectionUuids;
	}

	public Collection<Schema> getSchemas()
	{
		return schemas;
	}

	public void setSchemas(@Nullable Collection<Schema> schemas)
	{
		this.schemas = (schemas == null ? null : new HashSet<>(schemas));
	}

	public void setCourses(@Nullable Collection<CourseInfo> courses)
	{
		this.courses = (courses == null ? null : new HashSet<>(courses));
	}

	public Collection<CourseInfo> getCourses()
	{
		return courses;
	}

	public String getActivationStatus()
	{
		return activationStatus;
	}

	public void setActivationStatus(String activationStatus)
	{
		this.activationStatus = activationStatus;
	}

	public List<ItemStatus> getItemStatuses()
	{
		return itemStatuses;
	}

	public void setItemStatuses(@Nullable List<ItemStatus> itemStatuses)
	{
		this.itemStatuses = (itemStatuses == null ? null : new ArrayList<>(itemStatuses));
	}

	public void setItemStatuses(ItemStatus... itemStatuses)
	{
		setItemStatuses(Arrays.asList(itemStatuses));
	}

	public List<ItemStatus> getNotItemStatuses()
	{
		return notItemStatuses;
	}

	public void setNotItemStatuses(ItemStatus... notItemStatuses)
	{
		this.notItemStatuses = Arrays.asList(notItemStatuses);
	}

	public void setSortType(String deprecatedSortType)
	{
		SortType sortType = null;

		if( deprecatedSortType != null )
		{
			try
			{
				sortType = SortType.valueOf(deprecatedSortType);
			}
			catch( Exception e )
			{
				int isortType = Integer.valueOf(deprecatedSortType);
				switch( isortType )
				{
					case 0:
						sortType = SortType.RANK;
						break;
					case 1:
						sortType = SortType.DATEMODIFIED;
						break;
					case 2:
						sortType = SortType.NAME;
						break;
					case 4:
						sortType = SortType.RATING;
						break;
					case 5:
						sortType = SortType.DATECREATED;
						break;
					default:
						break;
				}
			}
		}
		if( sortType == null )
		{
			sortType = SortType.RANK;
		}
		setSortType(sortType);
	}

	public void setSortType(SortType sortType)
	{
		setSortFields(sortType.getSortField());
	}

	@Override
	public String getPrivilege()
	{
		return privilege;
	}

	public void setPrivilege(String privilege)
	{
		this.privilege = privilege;
	}

	@Override
	public String getPrivilegeToCollect()
	{
		return privilegeToCollect;
	}

	protected List<Field> createFields(String field, String... values)
	{
		return createFields(field, Arrays.asList(values));
	}

	protected List<Field> createFields(String field, Collection<String> values)
	{
		List<Field> namevalues = new ArrayList<Field>();
		for( String value : values )
		{
			namevalues.add(new Field(field, value));
		}
		return namevalues;
	}

	protected void addExtraMusts(List<List<Field>> musts)
	{
		// nothing by default
	}

	protected void addExtraMustNots(List<List<Field>> mustNots)
	{
		// nothing by default
	}

	public void addMust(String field, String value)
	{
		musts.add(Arrays.asList(new Field(field, value)));
	}

	public void addMust(String field, Collection<String> values)
	{
		musts.add(createFields(field, values));
	}

	public void addMustNot(String field, String value)
	{
		mustNots.add(Arrays.asList(new Field(field, value)));
	}

	public void addMustNot(String field, Collection<String> values)
	{
		mustNots.add(createFields(field, values));
	}

	@Override
	public List<List<Field>> getMust()
	{
		List<List<Field>> locMusts = new ArrayList<List<Field>>(this.musts);

		Collection<String> locCollectionUuids = getCollectionUuids();
		Collection<Schema> locSchemas = getSchemas();
		Collection<CourseInfo> locCourses = getCourses();
		List<ItemStatus> locStatuses = getItemStatuses();
		String locActivationStatus = getActivationStatus();

		if( anyEmpty(locCollectionUuids, locSchemas, locCourses, locStatuses) )
		{
			return impossibleSearch();
		}

		if( !Check.isEmpty(locCollectionUuids) )
		{
			locMusts.add(createFields(FreeTextQuery.FIELD_ITEMDEFID, locCollectionUuids));
		}

		if( !Check.isEmpty(locSchemas) )
		{
			locMusts.add(createFields(FreeTextQuery.FIELD_SCHEMAID, convertBaseEntities(locSchemas)));
		}

		if( !Check.isEmpty(locCourses) )
		{
			locMusts.add(createFields(FreeTextQuery.FIELD_COURSE_ID, convertBaseEntities(locCourses)));
		}

		if( !Check.isEmpty(locActivationStatus) )
		{
			locMusts.add(createFields(FreeTextQuery.FIELD_ACTIVATION_STATUS,
				Collections.singletonList(locActivationStatus)));
		}

		if( !Check.isEmpty(locStatuses) )
		{
			locMusts.add(createFields(FreeTextQuery.FIELD_ITEMSTATUS, convertStatuses(locStatuses)));
		}

		String theOwner = getOwner();
		if( theOwner != null )
		{
			locMusts.add(createFields(FreeTextQuery.FIELD_OWNER, Collections.singletonList(theOwner)));
		}

		if( isModerating() )
		{
			locMusts.add(Collections.singletonList(new Field(FreeTextQuery.FIELD_MODERATING, "true"))); //$NON-NLS-1$
		}

		addMimeTypes(locMusts);

		addExtraMusts(locMusts);

		return locMusts;
	}

	protected void addMimeTypes(List<List<Field>> musts)
	{
		Collection<String> mimes = getMimeTypes();
		if( mimes != null )
		{
			musts.add(createFields(FreeTextQuery.FIELD_ATTACHMENT_MIME_TYPES, mimes));
		}
	}

	@Override
	public List<List<Field>> getMustNot()
	{
		List<List<Field>> locMustNots = new ArrayList<List<Field>>(this.mustNots);

		List<ItemStatus> notStatuses = getNotItemStatuses();
		if( notStatuses != null && !notStatuses.isEmpty() )
		{
			locMustNots.add(createFields(FreeTextQuery.FIELD_ITEMSTATUS, convertStatuses(notStatuses)));
		}

		addExtraMustNots(locMustNots);
		return locMustNots;
	}

	private List<String> convertBaseEntities(Collection<? extends BaseEntity> itemdefs)
	{
		List<String> retDefs = new ArrayList<String>();
		for( BaseEntity definition : itemdefs )
		{
			retDefs.add(definition.getUuid());
		}
		return retDefs;
	}

	protected List<String> convertStatuses(Collection<ItemStatus> statuses)
	{
		List<String> retStatuses = new ArrayList<String>();
		for( ItemStatus status : statuses )
		{
			retStatuses.add(status.toString());
		}
		return retStatuses;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public boolean isModerating()
	{
		return moderating;
	}

	public void setModerating(boolean moderating)
	{
		this.moderating = moderating;
	}

	public DefaultSearch getToplevel()
	{
		return toplevel;
	}

	public void setToplevel(DefaultSearch toplevel)
	{
		this.toplevel = toplevel;
	}

	@Override
	public ItemSelect getSelect()
	{
		return select;
	}

	public void setSelect(ItemSelect select)
	{
		this.select = select;
	}

	public Collection<String> getMimeTypes()
	{
		return mimeTypes;
	}

	public void setMimeTypes(Collection<String> mimeTypes)
	{
		if( this.mimeTypes != null )
		{
			this.mimeTypes.addAll(mimeTypes);
		}
		else
		{
			this.mimeTypes = (mimeTypes == null ? null : new HashSet<>(mimeTypes));
		}
	}

	@Override
	public List<Field> getMatrixFields()
	{
		return null;
	}

	@Override
	public String getSearchType()
	{
		return searchType;
	}

	public void setSearchType(String searchType)
	{
		this.searchType = searchType;
	}

	public void setPrivilegeToCollect(String privilegeToCollect)
	{
		this.privilegeToCollect = privilegeToCollect;
	}

	private boolean anyEmpty(Collection<?>... collections)
	{
		for( Collection<?> collection : collections )
		{
			if( collection != null && collection.isEmpty() )
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Used for when there is a condition such that no results could possibly
	 * ever be returned. Eg. The list of allowed searchable collections is not
	 * null but is empty
	 * 
	 * @return
	 */
	private List<List<Field>> impossibleSearch()
	{
		List<List<Field>> locMusts = new ArrayList<List<Field>>();

		locMusts.add(createFields(FreeTextQuery.FIELD_ITEMSTATUS, Collections.singleton("$NORESULTS$"))); //$NON-NLS-1$
		return locMusts;
	}

	public static class QueryParser
	{
		private final List<QueryToken> tokens;
		private final List<String> hilight;

		public QueryParser(String query)
		{
			hilight = new ArrayList<String>();
			tokens = new ArrayList<QueryToken>();
			parseQuery(query);
		}

		protected void parseQuery(final String query)
		{
			final List<QueryToken> newTokens = QueryToken.tokenize(query);
			tokens.addAll(newTokens);

			for( int i = 0; i < newTokens.size(); i++ )
			{
				final QueryToken qtok = newTokens.get(i);
				final String token = qtok.token;

				boolean isFullWord = qtok.isaword;
				isFullWord = isFullWord && !token.equalsIgnoreCase("AND"); //$NON-NLS-1$
				isFullWord = isFullWord && !token.equalsIgnoreCase("OR"); //$NON-NLS-1$
				isFullWord = isFullWord && !token.equalsIgnoreCase("NOT"); //$NON-NLS-1$
				isFullWord = isFullWord && !token.startsWith("?"); //$NON-NLS-1$
				isFullWord = isFullWord && !token.startsWith("*"); //$NON-NLS-1$

				if( isFullWord )
				{
					hilight.add(token);
				}

				if( !qtok.isaword && token.charAt(0) == '"' && token.length() > 2 )
				{
					hilight.add(token.substring(1, token.length() - 1));
				}
			}
		}

		public Collection<String> getHilightedList()
		{
			return hilight;
		}
	}

	private static class QueryToken
	{
		private String token;
		private boolean isaword;

		public QueryToken(String token, boolean isaword)
		{
			this.token = token;
			this.isaword = isaword;
		}

		public static List<QueryToken> tokenize(String str)
		{
			List<QueryToken> retvec = new ArrayList<QueryToken>();
			if( str == null )
			{
				return retvec;
			}

			boolean intext = false;
			boolean inspace = false;
			boolean inquot = false;
			StringBuilder curbuf = new StringBuilder();
			int upto = 0;
			int len = str.length();
			while( upto < len )
			{
				char ch = str.charAt(upto);
				if( inquot )
				{
					if( ch == '"' )
					{
						curbuf.append('"');
						finish(curbuf, retvec, false);
						inquot = false;
						intext = false;
						upto++;
						continue;
					}
				}
				else
				{
					if( ch == '"' )
					{
						inquot = true;
						finish(curbuf, retvec, intext);
					}
					else
					{
						if( intext )
						{
							if( !isJavaIdentifierPart(ch) )
							{
								finish(curbuf, retvec, true);
								intext = false;
							}
						}
						else
						{
							if( isJavaIdentifierPart(ch) )
							{
								finish(curbuf, retvec, false);
								intext = true;
								inspace = false;
							}
							else if( inspace )
							{
								if( !Character.isSpaceChar(ch) )
								{
									finish(curbuf, retvec, false);
									inspace = false;
								}
							}
							else if( Character.isSpaceChar(ch) )
							{
								finish(curbuf, retvec, false);
								inspace = true;
							}
						}
					}
				}
				curbuf.append(ch);
				upto++;
			}
			finish(curbuf, retvec, intext);
			return retvec;
		}

		private static boolean isJavaIdentifierPart(char ch)
		{
			return Character.isJavaIdentifierPart(ch) || ch == '?' || ch == '~' || ch == '*';
		}

		private static void finish(StringBuilder sbuf, List<QueryToken> retvec, boolean isword)
		{
			if( sbuf.length() > 0 )
			{
				QueryToken token = new QueryToken(sbuf.toString(), isword);
				retvec.add(token);
				sbuf.setLength(0);
			}
		}

		@Override
		public String toString()
		{
			return token;
		}
	}

	public void addDateFilter(DateFilter filter)
	{
		dateFilters.add(filter);
	}

	@Override
	public Collection<DateFilter> getDateFilters()
	{
		return dateFilters;
	}

	@Override
	public SortField[] getSortFields()
	{
		return sortFields;
	}

	public void setSortFields(SortField... sortFields)
	{
		this.sortFields = sortFields;
	}

	// Used by a couple of API resource classes at least. Parking it here to
	// save duplication.

	public static SortType getOrderType(String order, String q)
	{
		if( order != null )
		{
			// allowed values are relevance, modified, name, rating
			if( order.equals("relevance") )
			{
				return SortType.RANK;
			}
			else if( order.equals("modified") )
			{
				return SortType.DATEMODIFIED;
			}
			else if( order.equals("name") )
			{
				return SortType.NAME;
			}
			else if( order.equals("rating") )
			{
				return SortType.RATING;
			}
			else if( order.equals("created") )
			{
				return SortType.DATECREATED;
			}
		}

		// default is 'modified' for a blank query and 'relevance' for anything
		// else (as is the case for the UI)
		if( Check.isEmpty(q) )
		{
			return SortType.DATEMODIFIED;
		}
		return SortType.RANK;
	}

	public boolean isSortReversed()
	{
		return sortReversed;
	}

	public void setSortReversed(boolean sortReverse)
	{
		this.sortReversed = sortReverse;
	}
}
