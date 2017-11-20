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

package com.tle.web.searching;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.DynaCollection;
import com.tle.beans.entity.FederatedSearch;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.fedsearch.FederatedSearchService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.powersearch.PowerSearchService;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.OptionNameComparator;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;

@SuppressWarnings("nls")
@Bind
@Singleton
public class SearchWhereModel extends DynamicHtmlListModel<SearchWhereModel.WhereEntry>
{
	@PlugKey("within.collections")
	private static Label COLLECTION_GROUP;
	@PlugKey("within.powersearches")
	private static Label POWERSEARCH_GROUP;
	@PlugKey("within.remoterepositories")
	private static Label REMOTEREPO_GROUP;
	@PlugKey("query.collection.all")
	private static String ALL_KEY;

	@Inject
	private FederatedSearchService fedService;
	private static final String ALL_VAL = "all";

	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private PowerSearchService powerSearchService;
	@Inject
	private DynaCollectionService dynaCollectionService;
	@Inject
	private FederatedSearchService federatedSearchService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private BundleCache bundleCache;

	/**
	 * Short-lived cache of item defs, power searches, dyna collections and
	 * remote repos for display in the "where" box.
	 */
	private InstitutionCache<LoadingCache<String, CachedWhereModelEntries>> whereModelCache;

	public SearchWhereModel()
	{
		setSort(true);
		setComparator(new OptionNameComparator()
		{
			private static final long serialVersionUID = 1L;

			/*
			 * Order should be Collections/Dynamic, Advanced Searches,
			 * RemoteRepos c2 C P R C s - - c1 P + s - R + + s
			 */

			@Override
			public int compare(Option<?> o1, Option<?> o2)
			{
				char c1 = o1.getValue().charAt(0);
				char c2 = o2.getValue().charAt(0);

				if( c1 == c2 )
				{
					return super.compare(o1, o2);
				}

				if( (c1 == 'C' || c1 == 'D') && (c2 == 'C' || c2 == 'D') )
				{
					return super.compare(o1, o2);
				}

				if( c1 == 'C' || c1 == 'D' )
				{
					return -1;
				}

				if( c1 == 'R' )
				{
					return 1;
				}

				return c2 == 'R' ? -1 : 1;
			}
		});
	}

	@Inject
	public void setInstitutionService(InstitutionService service)
	{
		whereModelCache = service
			.newInstitutionAwareCache(new CacheLoader<Institution, LoadingCache<String, CachedWhereModelEntries>>()
			{
				@Override
				public LoadingCache<String, CachedWhereModelEntries> load(Institution key)
				{
					return CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
						.build(new CacheLoader<String, CachedWhereModelEntries>()
					{
						@Override
						public CachedWhereModelEntries load(String key)
						{
							List<VirtualisableAndValue<DynaCollection>> dynaCollections = newArrayList(
								filter(dynaCollectionService.enumerateExpanded("searchUsage"),
									new Predicate<VirtualisableAndValue<DynaCollection>>()
							{
								final Set<String> dynaCollectionUuids = newHashSet(transform(
									dynaCollectionService.listSearchable(), new Function<BaseEntityLabel, String>()
								{
									@Override
									public String apply(BaseEntityLabel input)
									{
										return input.getUuid();
									}
								}));

								@Override
								public boolean apply(@Nullable VirtualisableAndValue<DynaCollection> input)
								{
									return dynaCollectionUuids.contains(input.getVt().getUuid());
								}
							}));

							return new CachedWhereModelEntries(itemDefinitionService.listSearchable(),
								powerSearchService.listSearchable(), federatedSearchService.listEnabledSearchable(),
								dynaCollections);
						}
					});
				}
			});
	}

	@Nullable
	@Override
	public WhereEntry getValue(SectionInfo info, @Nullable String value)
	{
		if( value == null || ALL_VAL.equals(value) )
		{
			return null;
		}
		return new WhereEntry(value);
	}

	@Override
	protected Option<WhereEntry> getTopOption()
	{
		return new KeyOption<WhereEntry>(ALL_KEY, ALL_VAL, null)
		{
			@Override
			public String getGroupName()
			{
				return COLLECTION_GROUP.getText();
			}
		};
	}

	@Override
	protected Iterable<WhereEntry> populateModel(SectionInfo info)
	{
		List<WhereEntry> collectionOptions = new ArrayList<WhereEntry>();

		CachedWhereModelEntries cached = whereModelCache.getCache().getUnchecked(CurrentUser.getUserID());
		Iterable<BaseEntityLabel> collections = cached.collections;
		Iterable<BaseEntityLabel> powerSearches = cached.powerSearches;
		Iterable<BaseEntityLabel> remoteRepos = cached.remoteRepos;
		Iterable<VirtualisableAndValue<DynaCollection>> dynaCollections = cached.virtualisedDynaCollections;

		final SelectionSession session = selectionService.getCurrentSession(info);
		if( session != null )
		{
			if( !session.isAllCollections() )
			{
				collections = filterByUuids(collections, session.getCollectionUuids());
			}
			if( !session.isAllPowerSearches() )
			{
				powerSearches = filterByUuids(powerSearches, session.getPowerSearchIds());
			}

			if( !session.isAllRemoteRepositories() )
			{
				remoteRepos = filterByUuids(remoteRepos, session.getRemoteRepositoryIds());
			}
			if( !session.isAllContributionCollections() )
			{
				Set<String> contributionCollectionIds = session.getContributionCollectionIds();

				if( !contributionCollectionIds.isEmpty() )
				{
					for( BaseEntityLabel repo : remoteRepos )
					{
						FederatedSearch fedSearch = fedService.getByUuid(repo.getUuid());
						String collectionUuid = fedSearch.getCollectionUuid();
						if( !contributionCollectionIds.contains(collectionUuid) )
						{
							remoteRepos = removeFromCollection(remoteRepos, repo.getUuid());
						}
					}
				}
				else
				{
					remoteRepos = Collections.emptySet();
				}
			}
			if( !session.isAllDynamicCollections() )
			{
				dynaCollections = filter(dynaCollections, new Predicate<VirtualisableAndValue<DynaCollection>>()
				{
					@Override
					public boolean apply(@Nullable VirtualisableAndValue<DynaCollection> input)
					{
						return session.getDynamicCollectionIds().contains(input.getVt().getUuid());
					}
				});
			}
		}

		for( BaseEntityLabel bel : collections )
		{
			collectionOptions.add(new WhereEntry(bel, WithinType.COLLECTION));
		}

		for( VirtualisableAndValue<DynaCollection> vdc : dynaCollections )
		{
			DynaCollection dc = vdc.getVt();
			collectionOptions.add(new WhereEntry(CurrentLocale.get(dc.getName()), dc.getUuid(),
				Strings.nullToEmpty(vdc.getVirtualisedValue())));
		}

		for( BaseEntityLabel bel : powerSearches )
		{
			collectionOptions.add(new WhereEntry(bel, WithinType.POWER));
		}

		for( BaseEntityLabel bel : remoteRepos )
		{
			collectionOptions.add(new WhereEntry(bel, WithinType.REMOTE));
		}

		return collectionOptions;
	}

	private Iterable<BaseEntityLabel> filterByUuids(Iterable<BaseEntityLabel> entities, final Set<String> uuids)
	{
		return filter(entities, new Predicate<BaseEntityLabel>()
		{
			@Override
			public boolean apply(@Nullable BaseEntityLabel label)
			{
				return uuids.contains(label.getUuid());
			}
		});
	}

	private Iterable<BaseEntityLabel> removeFromCollection(Iterable<BaseEntityLabel> entities, final String uuid)
	{
		return filter(entities, new Predicate<BaseEntityLabel>()
		{
			@Override
			public boolean apply(@Nullable BaseEntityLabel label)
			{
				return label.getUuid() != uuid;
			}
		});
	}

	public WhereEntry createWhere(String uuid, WithinType type)
	{
		return new WhereEntry(uuid, type);
	}

	@Override
	protected Option<WhereEntry> convertToOption(SectionInfo info, WhereEntry obj)
	{
		return obj.convert();
	}

	public class WhereEntry
	{
		private final WithinType type;
		private final String uuid;
		private final String value;
		private NameValue name;
		private String virt;
		private BaseEntity entity;
		private boolean disabled = false;
		private boolean html = false;

		public WhereEntry(String fromString)
		{
			this.value = fromString;
			char strType = fromString.charAt(0);
			switch( strType )
			{
				case 'C':
					this.type = WithinType.COLLECTION;
					break;
				case 'D':
					this.type = WithinType.DYNAMIC;
					break;
				case 'P':
					this.type = WithinType.POWER;
					break;
				case 'R':
					this.type = WithinType.REMOTE;
					break;
				case 'E':
					this.type = WithinType.EXTERNAL;
					break;
				default:
					this.type = null;
			}
			if( type == WithinType.DYNAMIC )
			{
				int ind = fromString.indexOf(':');
				uuid = fromString.substring(1, ind);
				virt = fromString.substring(ind + 1);
			}
			else
			{
				uuid = fromString.substring(1);
			}
		}

		public WhereEntry(BaseEntityLabel bel, WithinType type)
		{
			this(bel.getUuid(), type);
			this.name = new BundleNameValue(bel.getBundleId(), value, bundleCache);
		}

		public WhereEntry(String uuid, WithinType type)
		{
			this.type = type;
			this.uuid = uuid;
			this.value = type.name().charAt(0) + uuid;
		}

		public WhereEntry(String name, String uuid, String virt)
		{
			this.type = WithinType.DYNAMIC;
			this.uuid = uuid;
			this.value = 'D' + uuid + ':' + virt;
			this.name = new NameValue(name, value);
			this.virt = virt;
		}

		public WhereEntry(String uuid, String name, WithinType type)
		{
			this.type = type;
			this.uuid = uuid;
			this.value = type.name().charAt(0) + uuid;
			this.name = new NameValue(name, value);
		}

		public Option<WhereEntry> convert()
		{
			HtmlNameValueOption<WhereEntry> nameValueOption = new HtmlNameValueOption<WhereEntry>(getName(), this, html,
				getGroupName());
			nameValueOption.setDisabled(disabled);
			return nameValueOption;
		}

		private String getGroupName()
		{
			switch( type )
			{
				case COLLECTION:
				case DYNAMIC:
					return COLLECTION_GROUP.getText();
				case POWER:
					return POWERSEARCH_GROUP.getText();
				case REMOTE:
					return REMOTEREPO_GROUP.getText();
				default:
					return "";
			}
		}

		public NameValue getName()
		{
			if( name == null )
			{
				name = new BundleNameValue(getEntity().getName(), value, bundleCache);
			}
			return name;
		}

		public WithinType getType()
		{
			return type;
		}

		@SuppressWarnings("unchecked")
		public <T extends BaseEntity> T getEntity()
		{
			if( entity == null )
			{
				switch( type )
				{
					case COLLECTION:
						entity = itemDefinitionService.getByUuid(uuid);
						break;
					case POWER:
						entity = powerSearchService.getByUuid(uuid);
						break;
					case DYNAMIC:
						entity = dynaCollectionService.getByUuid(uuid);
						break;

					default:
						break;
				}
			}
			return (T) entity;
		}

		public String getVirt()
		{
			return virt;
		}

		public String getValue()
		{
			return value;
		}

		public String getUuid()
		{
			return uuid;
		}

		public void setDisabled(boolean disabled)
		{
			this.disabled = disabled;
		}

		public boolean isDisabled()
		{
			return disabled;
		}

		public void setHtml(boolean html)
		{
			this.html = html;
		}

		public boolean isHtml()
		{
			return html;
		}
	}

	private static class HtmlNameValueOption<T> extends NameValueOption<T>
	{
		private final boolean html;
		private final String groupName;

		public HtmlNameValueOption(NameValue nv, T object, String groupName)
		{
			super(nv, object);
			this.html = false;
			this.groupName = groupName;
		}

		public HtmlNameValueOption(NameValue nv, T object, boolean html, String groupName)
		{
			super(nv, object);
			this.html = html;
			this.groupName = groupName;
		}

		@Override
		public boolean isNameHtml()
		{
			return html;
		}

		@Override
		public String getGroupName()
		{
			return groupName;
		}
	}

	private static class CachedWhereModelEntries
	{
		final List<BaseEntityLabel> collections;
		final List<BaseEntityLabel> powerSearches;
		final List<BaseEntityLabel> remoteRepos;
		final List<VirtualisableAndValue<DynaCollection>> virtualisedDynaCollections;

		public CachedWhereModelEntries(List<BaseEntityLabel> collections, List<BaseEntityLabel> powerSearches,
			List<BaseEntityLabel> remoteRepos, List<VirtualisableAndValue<DynaCollection>> virtualisedDynaCollections)
		{
			super();
			this.collections = collections;
			this.powerSearches = powerSearches;
			this.remoteRepos = remoteRepos;
			this.virtualisedDynaCollections = virtualisedDynaCollections;
		}
	}
}