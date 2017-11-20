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

package com.tle.web.hierarchy.section;

import static com.tle.common.hierarchy.VirtualTopicUtils.buildTopicId;
import static com.tle.web.hierarchy.TopicUtils.labelForValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.common.GeneralConstants;
import com.google.common.base.Splitter;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.PresetSearch;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.service.ItemService;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.hierarchy.model.TopicDisplayModel;
import com.tle.web.hierarchy.model.TopicDisplayModel.DisplayHierarchyNode;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.search.actions.SkinnySearchActionsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.search.filter.ResetFiltersParent;
import com.tle.web.search.filter.ResetFiltersSection;
import com.tle.web.searching.section.SearchQuerySection;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.PagingSection;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.Breadcrumbs;

@SuppressWarnings("nls")
public class TopicDisplaySection extends AbstractPrototypeSection<TopicDisplaySection.ExtendedTopicDisplayModel>
	implements
		HtmlRenderer,
		ResetFiltersParent
{
	private static final String SEARCH_PRIV = "SEARCH_POWER_SEARCH";
	private static final String ROOT_TOPICS = "ALL";

	@PlugKey("page.title")
	private static Label rootTitle;

	@Inject
	private HierarchyService hierarchyService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private FreeTextService freeTextService;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private SelectionService selectionService;
	@Inject
	private UserSessionService sessionService;
	@Inject
	private AuditLogService auditLogService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private PagingSection<?, ?> pager;
	@TreeLookup
	private HierarchyResultsSection searchResults;
	@Inject
	private ResetFiltersSection<?> resetFiltersSection;

	/**
	 * Applicable to skinny session, not otherwise
	 */
	@TreeLookup(mandatory = false)
	private SkinnySearchActionsSection skinnySearchActions;

	@Inject
	private ItemService itemService;

	@Component
	private Button advancedSearch;

	protected final List<SectionId> queryActionSections = new ArrayList<SectionId>();

	@Override
	public Class<ExtendedTopicDisplayModel> getModelClass()
	{
		return ExtendedTopicDisplayModel.class;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(resetFiltersSection, id);
		advancedSearch.setClickHandler(events.getNamedHandler("doAdvanced"));
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		pager.getPager().setContexts(Collections.singleton(BookmarkEvent.CONTEXT_BROWSERURL));
		queryActionSections.addAll(tree.getChildIds(id));
	}

	@Override
	public ResetFiltersSection<?> getResetFiltersSection()
	{
		return resetFiltersSection;
	}

	@Override
	public void addResetDiv(SectionTree tree, List<String> ajaxList)
	{
		resetFiltersSection.addAjaxDiv(ajaxList);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final ExtendedTopicDisplayModel model = getModel(context);
		final HierarchyTopic topic = model.getTopic();
		final String searchPriv = selectionService.getSearchPrivilege(context);

		Map<String, String> values = model.getValues();

		String topicVirtValue = null;
		if( topic != null && !Check.isEmpty(values) )
		{
			topicVirtValue = values.get(topic.getUuid());
		}

		boolean hideZero = false;
		if( topic != null )
		{
			hierarchyService.assertViewAccess(topic);

			model.setDescription(
				labelForValue(new BundleLabel(topic.getLongDescription(), bundleCache).setHtml(true), topicVirtValue));
			model.setSubtopicName(
				labelForValue(new BundleLabel(topic.getSubtopicsSectionName(), bundleCache), topicVirtValue));

			PowerSearch powerSearch = topic.getAdvancedSearch();
			model.setShowAdvanced(powerSearch != null
				&& !aclManager.filterNonGrantedPrivileges(powerSearch, Arrays.asList(SEARCH_PRIV)).isEmpty());

			hideZero = topic.isHideSubtopicsWithNoResults();
		}
		model.setName(getLabelForTopic(topic, topicVirtValue));

		// We now need the current topic and value to be in the "values" map so
		// that the counts and URLs for the child topics are generated
		// correctly.
		if( topic != null && !Check.isEmpty(topicVirtValue) )
		{
			values = new ExtraTopicMap(values, topic, topicVirtValue);
		}

		final Collection<String> collectionUuids = getSelectionSessionCollections(context);
		// Go through the child topics and get item counts for them, either as
		// calculated (if by Contributed virtualiser) or as a one-at-a-time
		// search (if manual virtualiser).
		final List<VirtualisableAndValue<HierarchyTopic>> childTopics = hierarchyService
			.expandVirtualisedTopics(hierarchyService.getChildTopics(topic), values, collectionUuids);
		for( VirtualisableAndValue<HierarchyTopic> p : childTopics )
		{
			HierarchyTopic childTopic = p.getVt();
			String childValue = p.getVirtualisedValue();

			if( p.getCount() == GeneralConstants.UNCALCULATED || Check.isEmpty(p.getVirtualisedValue()) )
			{
				PresetSearch search = new PresetSearch(hierarchyService.getFullFreetextQuery(childTopic),
					hierarchyService.getSearchClause(childTopic, new ExtraTopicMap(values, childTopic, childValue)),
					true);
				search.setPrivilege(searchPriv);
				filterSearchCollections(search, collectionUuids);
				p.setCount(freeTextService.countsFromFilters(Collections.singletonList(search))[0]);
			}
		}

		// Generate links for the child topics, filtering out any that shouldn't
		// show if zero results, etc...
		final List<DisplayHierarchyNode> subNodes = new ArrayList<DisplayHierarchyNode>();
		final Collection<String> keyResPrivs = Collections.singleton(searchPriv);
		for( VirtualisableAndValue<HierarchyTopic> p : childTopics )
		{
			HierarchyTopic childTopic = p.getVt();
			String childValue = p.getVirtualisedValue();

			String dynamicHierarchyId = buildTopicId(childTopic, childValue, values);

			HtmlLinkState link = new HtmlLinkState(events.getNamedHandler("changeTopic", dynamicHierarchyId));

			List<HierarchyTopicDynamicKeyResources> dynamicKeyResources = hierarchyService
				.getDynamicKeyResource(dynamicHierarchyId);

			int searchCount = p.getCount();
			if( dynamicKeyResources != null )
			{
				searchCount += dynamicKeyResources.size();
			}

			DisplayHierarchyNode node = new DisplayHierarchyNode(childTopic, childValue, link, searchCount, bundleCache,
				aclManager, keyResPrivs);

			if( !hideZero || node.getResultCountInt() > 0 )
			{
				subNodes.add(node);
			}
		}
		model.setSubTopics(subNodes);

		// for the benefit of skinny sessions ...
		if( skinnySearchActions != null )
		{
			// If there's no topic, we're showing the topic display and so don't
			// want to see the search actions (hiding the search results is done
			// elsewhere), but if there is a topic then we're in viewing results
			// mode, so show the search actions.
			boolean searchDisabled = !isShowingResults(context);
			skinnySearchActions.getModel(context).setSearchDisabled(searchDisabled);
		}
		getModel(context).setQueryActions(SectionUtils.renderSectionIds(context, queryActionSections));

		return viewFactory.createResult("topic.ftl", this);
	}

	public boolean isShowingResults(SectionInfo info)
	{
		HierarchyTopic topic = getModel(info).getTopic();
		if( topic == null )
		{
			return false;
		}
		return topic.isShowResults();
	}

	@EventHandlerMethod
	public void doAdvanced(SectionInfo info)
	{
		HierarchyTopic topic = getModel(info).getTopic();
		PowerSearch search = topic.getAdvancedSearch();
		if( skinnySearchActions != null )
		{
			// in a skinny session we call on skinny power *(ensures single
			// column presentation, search actions etc
			SearchQuerySection.skinnyPowerSearch(info, search.getUuid());
		}
		else
		{
			// normal power search mode
			SearchQuerySection.powerSearch(info, search.getUuid());
		}
	}

	@EventHandlerMethod
	public void changeTopic(SectionInfo info, String topicUuid)
	{
		getModel(info).setTopicId(topicUuid);
		pager.resetToFirst(info);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ExtendedTopicDisplayModel();
	}

	public class ExtendedTopicDisplayModel extends TopicDisplayModel
	{
		@Bookmarked(parameter = "topic", contexts = BookmarkEvent.CONTEXT_BROWSERURL, supported = true)
		private String topicId;

		private HierarchyTopic topic;
		private String topicValue;
		private Map<String, String> values;

		public HierarchyTopic getTopic()
		{
			ensureParsed();
			return topic;
		}

		public String getTopicValue()
		{
			ensureParsed();
			return topicValue;
		}

		public Map<String, String> getValues()
		{
			ensureParsed();
			return values;
		}

		public void ensureParsed()
		{
			if( topic != null || Check.isEmpty(topicId) || ROOT_TOPICS.equals(topicId) )
			{
				return;
			}

			for( String uuidValue : Splitter.on(',').omitEmptyStrings().split(topicId) )
			{
				final String[] uv = uuidValue.split(":", 2);

				// First UUID is the topic to view
				if( topic == null )
				{
					topic = hierarchyService.getHierarchyTopicByUuid(uv[0]);
					topicValue = uv.length > 1 ? URLUtils.basicUrlDecode(uv[1]) : null;
					if( topic == null )
					{
						throw new IllegalArgumentException("Could not find topic " + uv[0]);
					}
				}

				if( uv.length > 1 )
				{
					if( values == null )
					{
						values = Maps.newHashMap();
					}
					values.put(uv[0], URLUtils.basicUrlDecode(uv[1]));
				}
			}
		}

		/**
		 * V = virtualised. NV = non-virtualised. enc = URL encoded
		 * <ul>
		 * <li>NV topic with NV parents: UUID
		 * <li>V topic with NV parents: UUID:enc(value)
		 * <li>NV topic with V parents: UUID,PUUID:enc(value),...
		 * <li>V topic with V parents: UUID:enc(value),PUUID:enc(value),...
		 * </ul>
		 * 
		 * @param topicId See above for format.
		 */
		public void setTopicId(String topicId)
		{
			this.topicId = topicId;

			topic = null;
			values = null;
		}
	}

	public Label getResultsTitle(SectionInfo context, Label normalLabel)
	{
		ExtendedTopicDisplayModel model = getModel(context);
		HierarchyTopic topic = model.getTopic();
		Map<String, String> values = model.getValues();
		String topicVirtValue = null;

		// BundleLabel can cope with a null LanguageBundle, if it must ...
		if( topic != null && !Check.isEmpty(values) )
		{
			topicVirtValue = values.get(topic.getUuid());
		}

		// sanity check to avoid null (and avoid 'potential null' warning)
		LanguageBundle topicNameBndl = topic != null ? topic.getResultsSectionName() : null;

		return labelForValue(new BundleLabel(topicNameBndl, normalLabel, bundleCache), topicVirtValue);
	}

	public FreetextSearchEvent createFreetextSearchEvent(SectionInfo info)
	{
		ExtendedTopicDisplayModel model = getModel(info);
		HierarchyTopic topic = model.getTopic();
		Map<String, String> values = model.getValues();
		String query = hierarchyService.getFullFreetextQuery(topic);

		FreeTextBooleanQuery searchClause = hierarchyService.getSearchClause(topic, values);

		final PresetSearch search = new PresetSearch(query, searchClause, true);
		search.setPrivilege(selectionService.getSearchPrivilege(info));

		final PresetSearch unfiltered = new PresetSearch(query, searchClause, true);
		unfiltered.setPrivilege(selectionService.getSearchPrivilege(info));

		final Collection<String> collectionUuids = getSelectionSessionCollections(info);
		filterSearchCollections(search, collectionUuids);
		filterSearchCollections(unfiltered, collectionUuids);

		return new FreetextSearchEvent(search, unfiltered);
	}

	private Collection<String> getSelectionSessionCollections(SectionInfo info)
	{
		final SelectionSession session = selectionService.getCurrentSession(info);
		if( session != null && !session.isAllCollections() )
		{
			return session.getCollectionUuids();
		}
		return null;
	}

	private void filterSearchCollections(DefaultSearch search, Collection<String> collectionUuids)
	{
		final Collection<String> existing = search.getCollectionUuids();
		if( existing == null )
		{
			search.setCollectionUuids(collectionUuids);
		}
		else
		{
			existing.retainAll(collectionUuids);
		}
	}

	public Button getAdvancedSearch()
	{
		return advancedSearch;
	}

	public void addCrumbs(SectionInfo info, Breadcrumbs crumbs)
	{
		ExtendedTopicDisplayModel model = getModel(info);
		HierarchyTopic topic = model.getTopic();
		Map<String, String> values = model.getValues();

		while( topic != null )
		{
			topic = topic.getParent();

			String value = null;
			String topicId = ROOT_TOPICS;
			if( topic != null )
			{
				value = Check.isEmpty(values) ? null : values.get(topic.getUuid());
				topicId = buildTopicId(topic, value, values);
			}

			crumbs.addToStart(
				new HtmlLinkState(getLabelForTopic(topic, value), events.getNamedHandler("changeTopic", topicId)));
		}
	}

	private Label getLabelForTopic(HierarchyTopic topic, String value)
	{
		if( topic == null )
		{
			return rootTitle;
		}
		Label label = new BundleLabel(topic.getName(), bundleCache);

		return labelForValue(label, value);
	}

	public Label getPageTitle(SectionInfo info)
	{
		ExtendedTopicDisplayModel model = getModel(info);
		return getLabelForTopic(model.getTopic(), model.getTopicValue());
	}

	/**
	 * Invoked from HierarchyResultsSection.processFreetextResults
	 * 
	 * @param info
	 * @param searchEvent
	 * @return
	 */
	public FreetextSearchResults<? extends FreetextResult> processFreetextResults(SectionInfo info,
		FreetextSearchEvent searchEvent)
	{
		List<Item> keyResources = (List<Item>) aclManager.filterNonGrantedObjects(
			Collections.singleton(selectionService.getSearchPrivilege(info)),
			getModel(info).getTopic().getKeyResources());

		if( getDynamicKeyResourceItems(info) != null )
		{
			keyResources.addAll(getDynamicKeyResourceItems(info));
		}

		int offset = searchEvent.getOffset() - keyResources.size();
		if( offset < 0 )
		{
			offset = 0;
		}

		FreetextSearchResults<FreetextResult> results = freeTextService.search(searchEvent.getFinalSearch(), offset,
			searchEvent.getCount());
		return new KeyResourceAddedResults<FreetextResult>(results, keyResources, searchEvent);
	}

	public List<Item> getDynamicKeyResourceItems(SectionInfo info)
	{
		List<Item> keyResources = new ArrayList<Item>();

		ExtendedTopicDisplayModel model = getModel(info);
		HierarchyTopic topic = model.getTopic();
		Map<String, String> values = model.getValues();
		String value = Check.isEmpty(values) ? null : values.get(topic.getUuid());
		String topicId = buildTopicId(topic, value, values);

		List<HierarchyTopicDynamicKeyResources> dynamicTopicItems = hierarchyService.getDynamicKeyResource(topicId);

		if( dynamicTopicItems != null )
		{
			for( HierarchyTopicDynamicKeyResources h : dynamicTopicItems )
			{
				String uuid = h.getUuid();
				int version = h.getVersion();
				ItemId id = new ItemId(uuid, version);
				Map<String, Object> itemObject = itemService.getItemInfo(id);

				if( itemObject != null )
				{
					keyResources.add(itemService.get(id));
				}
				else
				{
					// if item is deleted, then remove the reference from
					// dynamic key resource
					hierarchyService.removeDeletedItemReference(uuid, version);
				}
			}
		}
		return keyResources;
	}

	/**
	 * Invoked from HierarchyResultsSection.processResults
	 * 
	 * @param info
	 * @param event
	 * @param itemList
	 */
	public void processResults(SectionInfo info, FreetextSearchResultEvent event, HierarchyItemList itemList)
	{
		FreetextSearchResults<? extends FreetextResult> results = event.getResults();
		Collection<String> words = event.getSearchEvent().getFinalSearch().getTokenisedQuery();
		ListSettings<StandardItemListEntry> settings = itemList.getListSettings(info);
		settings.setHilightedWords(words);
		int keySize = getModel(info).getTopic().getKeyResources().size();
		if( getDynamicKeyResourceItems(info) != null )
		{
			keySize += getDynamicKeyResourceItems(info).size();
		}
		int count = results.getCount();
		int offset = results.getOffset();
		for( int i = 0; i < count; i++ )
		{
			Item item = results.getItem(i);

			StandardItemListEntry itemEntry = itemList.addItem(info, item, results.getResultData(i));
			if( (i + offset) < keySize )
			{
				itemEntry.setHilighted(true);
			}
		}

		FreetextSearchEvent searchEvent = event.getSearchEvent();
		final boolean loggable = searchEvent.isLoggable();
		if( loggable )
		{
			logFilterByKeyword(info, searchEvent, results.getAvailable());
		}
	}

	private void logFilterByKeyword(SectionInfo info, FreetextSearchEvent searchEvent, long resultCount)
	{
		final String query = searchEvent.getSearchedText();
		String type = "HIERARCHY";
		if( !isAlreadySearched(type, query, null) )
		{
			auditLogService.logSearch(type, query, null, resultCount);
			registerSearched(type, query, null);
		}
	}

	private boolean isAlreadySearched(String type, String query, @Nullable String in)
	{
		Boolean val = sessionService.getAttribute(key(type, query, in));
		return (val != null && val);
	}

	private void registerSearched(String type, String query, @Nullable String in)
	{
		sessionService.setAttribute(key(type, query, in), Boolean.TRUE);
	}

	private String key(String type, String query, @Nullable String in)
	{
		return "$SEARCHED$-" + type + in + query;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "hier";
	}

	public static class KeyResourceAddedResults<T extends FreetextResult> implements FreetextSearchResults<T>
	{
		private static final long serialVersionUID = 1L;

		private final FreetextSearchResults<? extends FreetextResult> wrapped;
		private final List<Item> keyResources;
		private final int keyResourcesSize;
		private final int count;
		private final int offset;
		private final int mixedEnd;

		public KeyResourceAddedResults(FreetextSearchResults<? extends FreetextResult> results, List<Item> keyResources,
			FreetextSearchEvent searchEvent)
		{
			this.wrapped = results;
			this.keyResources = keyResources;
			this.offset = searchEvent.getOffset();
			this.keyResourcesSize = keyResources.size();

			final int perpage = searchEvent.getCount();
			this.count = Math.min(perpage, wrapped.getCount() + Math.max(0, keyResourcesSize - offset));
			this.mixedEnd = (keyResourcesSize == 0 || keyResourcesSize % perpage == 0 ? 0
				: (keyResourcesSize / perpage) * perpage + perpage);
		}

		@Override
		public T getResultData(int index)
		{
			return null;
		}

		@Override
		public int getAvailable()
		{
			return wrapped.getAvailable() + keyResources.size();
		}

		@Override
		public int getCount()
		{
			return count;
		}

		@Override
		public int getOffset()
		{
			return offset;
		}

		@Override
		public List<Item> getResults()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String getErrorMessage()
		{
			return null;
		}

		@Override
		public void setErrorMessage(String errorMessage)
		{
			// do nothing
		}

		@Override
		public Item getItem(int index)
		{
			int i = offset + index;
			if( i < keyResourcesSize )
			{
				return keyResources.get(i);
			}
			else if( i < mixedEnd )
			{
				i = i - keyResourcesSize;
			}
			else
			{
				i = index;
			}
			return wrapped.getItem(i);
		}

		@Override
		public int getKeyResourcesSize()
		{
			return keyResourcesSize;
		}
	}

	public String getWithinTopic(SectionInfo info)
	{
		ExtendedTopicDisplayModel model = getModel(info);
		return getLabelForTopic(model.getTopic(), model.getTopicValue()).getText();
	}

	public HierarchyResultsSection getSearchResults()
	{
		return searchResults;
	}

	/**
	 * Takes an existing uuid/value map and works as if it has an extra
	 * uuid/value. Only implemented for "get" right now - iterators will not
	 * work, etc...
	 * 
	 * @author nick
	 */
	public static class ExtraTopicMap extends ForwardingMap<String, String>
	{
		private final Map<String, String> delegate;
		private final String topic;
		private final String value;

		public ExtraTopicMap(Map<String, String> delegate, HierarchyTopic topic, String value)
		{
			this.delegate = delegate;
			this.topic = topic.getUuid();
			this.value = value;
		}

		@Override
		public String get(Object key)
		{
			return topic.equals(key) ? value : delegate == null ? null : super.get(key);
		}

		@Override
		public boolean isEmpty()
		{
			return value == null && Check.isEmpty(delegate);
		}

		@Override
		protected Map<String, String> delegate()
		{
			return delegate;
		}
	}
}
