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

package com.tle.web.hierarchy.portlet.renderer;

import static com.tle.common.hierarchy.VirtualTopicUtils.buildTopicId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.common.GeneralConstants;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources;
import com.tle.common.Check;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.LiveItemSearch;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.security.TLEAclManager;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.hierarchy.model.TopicDisplayModel;
import com.tle.web.hierarchy.model.TopicDisplayModel.DisplayHierarchyNode;
import com.tle.web.hierarchy.section.TopicDisplaySection;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;

@SuppressWarnings("nls")
@Bind
public class BrowsePortletRenderer extends PortletContentRenderer<TopicDisplayModel>
{
	@ViewFactory
	private FreemarkerFactory view;
	@Inject
	private HierarchyService hierarchyService;
	@Inject
	private FreeTextService freeTextService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private SelectionService selectionService;
	@Inject
	private TLEAclManager aclService;

	// DONTFIXME: There used to be a FIXME message here, but it was deleted
	// because the code from TopicDisplaySection isn't close enough to bother
	// refactoring. The FIXME message just got added back on again, but really,
	// the code in TopicDisplaySection is really, really different and doesn't
	// make sense to try and refactor, so it's a DONTFIXME.

	@Override
	public SectionRenderable renderHtml(RenderEventContext context)
	{
		final TopicDisplayModel model = getModel(context);
		final List<DisplayHierarchyNode> subNodes = new ArrayList<DisplayHierarchyNode>();

		final Collection<String> collectionUuids = getSelectionSessionCollections(context);

		final List<VirtualisableAndValue<HierarchyTopic>> rootTopics = hierarchyService.expandVirtualisedTopics(
			hierarchyService.getChildTopics(null), null, collectionUuids);
		Collection<String> keyResPrivs = Collections.singleton(selectionService.getSearchPrivilege(context));
		if( !Check.isEmpty(rootTopics) )
		{

			for( VirtualisableAndValue<HierarchyTopic> rootTopic : rootTopics )
			{
				HierarchyTopic childTopic = rootTopic.getVt();
				String childValue = rootTopic.getVirtualisedValue();
				// count dynamic hierarchy key resource
				String dynamicHierarchyId = buildTopicId(childTopic, childValue, null);
				List<HierarchyTopicDynamicKeyResources> dynamicKeyResources = hierarchyService
					.getDynamicKeyResource(dynamicHierarchyId);

				int searchCount = rootTopic.getCount();
				if( searchCount == GeneralConstants.UNCALCULATED )
				{
					// It means we haven't conducted the search yet (because we
					// expanded the search set via the ManualVirtualization).
					// Given that we're dealing with a manual (ie finite) range
					// of values, we can afford to conduct searches on a
					// one-value-at-a-time basis.
					LiveItemSearch subSearch = getSubSearch(context, rootTopic, collectionUuids);
					int[] countsOfOne = freeTextService.countsFromFilters(Collections.singletonList(subSearch));
					if( countsOfOne.length > 0 )
					{
						searchCount = countsOfOne[0];
					}
					else
					{
						searchCount = 0;
					}
				}

				if( dynamicKeyResources != null )
				{
					searchCount += dynamicKeyResources.size();
				}

				final SectionInfo fwd = context.createForward("/hierarchy.do");
				final TopicDisplaySection topicDisplay = fwd.lookupSection(TopicDisplaySection.class);
				topicDisplay.changeTopic(fwd, buildTopicId(childTopic, childValue, null));
				final HtmlLinkState link = new HtmlLinkState(new InfoBookmark(fwd));

				subNodes.add(new DisplayHierarchyNode(childTopic, childValue, link, searchCount, bundleCache,
					aclService, keyResPrivs));
			}
		}

		model.setSubTopics(subNodes);

		return view.createResult("portlet/browse.ftl", context);
	}

	private LiveItemSearch getSubSearch(SectionInfo info, VirtualisableAndValue<HierarchyTopic> vTopic,
		Collection<String> collectionUuids)
	{
		HierarchyTopic topic = vTopic.getVt();
		String virtValue = vTopic.getVirtualisedValue();

		LiveItemSearch subSearch = null;
		if( topic.isShowResults() )
		{
			subSearch = new LiveItemSearch();
			subSearch.setPrivilege(selectionService.getSearchPrivilege(info));
			subSearch.setQuery(hierarchyService.getFullFreetextQuery(topic));
			Map<String, String> vs = virtValue == null ? null : Collections.singletonMap(topic.getUuid(), virtValue);
			subSearch.setFreeTextQuery(hierarchyService.getSearchClause(topic, vs));
			filterSearchCollections(subSearch, collectionUuids);
		}
		return subSearch;
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

	@Override
	public boolean canView(SectionInfo info)
	{
		// Always show unless we're in a selection session with zero topics.
		return selectionService.getCurrentSession(info) == null
			|| !hierarchyService.expandVirtualisedTopics(hierarchyService.getChildTopics(null), null, null).isEmpty();
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "pbr";
	}

	@Override
	public Class<TopicDisplayModel> getModelClass()
	{
		return TopicDisplayModel.class;
	}
}
