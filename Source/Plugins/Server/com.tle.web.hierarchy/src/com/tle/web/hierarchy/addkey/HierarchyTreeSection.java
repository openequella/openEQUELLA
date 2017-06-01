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

package com.tle.web.hierarchy.addkey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.common.URLUtils;
import com.tle.common.hierarchy.VirtualTopicUtils;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.security.TLEAclManager;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.hierarchy.TopicUtils;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryTreeView;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.Tree;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlTreeModel;
import com.tle.web.sections.standard.model.HtmlTreeNode;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
@SuppressWarnings("nls")
public class HierarchyTreeSection extends AbstractContentSection<Object>
{
	private static final ExternallyDefinedFunction FUNCTION_CLICKABLE_LINES = new ExternallyDefinedFunction(
		"makeClickable", 1, new IncludeFile(ResourcesService.getResourceHelper(HierarchyTreeSection.class).url(
			"scripts/htree.js"), JQueryTreeView.PRERENDER));

	@PlugKey("aftercontribution.receipt")
	private static String RECEIPT_SINGLE_KEY;
	@PlugKey("aftercontribution.title")
	private static Label LABEL_TITLE;

	@Inject
	private HierarchyService hierarchyService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private TLEAclManager aclManager;

	@Component(name = "krs", stateful = false)
	private MappedBooleans topicSelections;

	@Component
	@PlugKey("button.add")
	private Button addButton;

	@Component
	private Tree topicTree;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(context);
		addDefaultBreadcrumbs(context, iinfo, LABEL_TITLE);
		return view.createResult("hierarchytree.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		topicTree.setModel(new TopicTreeModel());
		topicTree.setLazyLoad(true);
		topicTree.setAllowMultipleOpenBranches(true);
		topicTree.addReadyStatements(Js.call_s(FUNCTION_CLICKABLE_LINES, Jq.$(topicTree)));
		addButton.addClickStatements(events.getNamedHandler("addToHierarchy"));
	}

	@EventHandlerMethod
	public void addToHierarchy(SectionInfo info)
	{
		final ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(info);

		Set<String> selectedTopics = topicSelections.getCheckedSet(info);

		// delete all the key resources then save all the checked hierarchy
		// topics
		hierarchyService.deleteKeyResources(iinfo.getItem());

		for( String topicId : selectedTopics )
		{
			hierarchyService.addKeyResource(topicId, iinfo.getItemId());
		}

		receiptService.setReceipt(new KeyLabel(RECEIPT_SINGLE_KEY));
	}

	public Tree getTopicTree()
	{
		return topicTree;
	}

	public Button getAddButton()
	{
		return addButton;
	}

	public class TopicTreeModel implements HtmlTreeModel
	{
		@Override
		public List<HtmlTreeNode> getChildNodes(SectionInfo info, String id)
		{
			HierarchyTopic t = null;

			Map<String, String> values = Maps.newHashMap();
			// Id will be null if it is the mother of it all...
			if( id != null )
			{
				for( String uuidValue : Splitter.on(',').omitEmptyStrings().split(id) )
				{
					final String[] uv = uuidValue.split(":", 2);

					if( t == null )
					{
						t = hierarchyService.getHierarchyTopicByUuid(uv[0]);
						if( t == null )
						{
							throw new IllegalArgumentException("Could not find topic " + uv[0]);
						}
					}

					if( uv.length > 1 )
					{
						values.put(uv[0], URLUtils.basicUrlDecode(uv[1]));
					}
				}
			}

			List<HierarchyTopic> allTopics = new ArrayList<HierarchyTopic>(hierarchyService.getChildTopics(t));
			Collection<HierarchyTopic> allowedTopics = aclManager.filterNonGrantedObjects(
				Collections.singleton("MODIFY_KEY_RESOURCE"), allTopics);

			final List<VirtualisableAndValue<HierarchyTopic>> topicValues = hierarchyService.expandVirtualisedTopics(
				(List<HierarchyTopic>) allowedTopics, null, null);

			Set<String> topicWithKeyResource = Sets.newHashSet(hierarchyService
				.getTopicIdsWithKeyResource(ParentViewItemSectionUtils.getItemInfo(info).getItem()));

			List<HtmlTreeNode> childNodes = Lists.newArrayList();
			for( VirtualisableAndValue<HierarchyTopic> p : topicValues )
			{
				HierarchyTopic topic = p.getVt();
				Label topicName = new BundleLabel(topic.getName(), bundleCache);
				String dynamicValue = p.getVirtualisedValue();
				if( dynamicValue != null )
				{
					topicName = TopicUtils.labelForValue(topicName, dynamicValue);
				}

				boolean leaf = hierarchyService.getChildTopics(topic).isEmpty();

				String topicId = VirtualTopicUtils.buildTopicId(topic, dynamicValue, values);
				childNodes.add(new TopicTreeNode(info, topicName, topicId, allowedTopics.contains(topic), leaf));

				topicSelections.setValue(info, topicId, topicWithKeyResource.contains(topicId));
			}

			return childNodes;
		}
	}

	public class TopicTreeNode implements HtmlTreeNode
	{
		private final SectionInfo info;
		private final Label topicName;
		private final String id;
		private final boolean canAddKeyResources;
		private boolean leaf;

		protected TopicTreeNode(SectionInfo info, Label topicName, String dynamicId, boolean canAddKeyResources,
			boolean leaf)
		{
			this.info = info;
			this.topicName = topicName;
			this.id = dynamicId;
			this.canAddKeyResources = canAddKeyResources;
			this.leaf = leaf;
		}

		@Override
		public String getId()
		{
			return id;
		}

		@Override
		public SectionRenderable getRenderer()
		{
			return view.createResultWithModel("tree/topicline.ftl", this);
		}

		@Override
		public Label getLabel()
		{
			return topicName;
		}

		@Override
		public boolean isLeaf()
		{
			return leaf;
		}

		public HtmlBooleanState getCheck()
		{
			HtmlBooleanState hbs = topicSelections.getBooleanState(info, getId());
			if( !canAddKeyResources )
			{
				hbs.setDisabled(false);
				hbs.setDisplayed(false);
			}
			return hbs;
		}
	}
}