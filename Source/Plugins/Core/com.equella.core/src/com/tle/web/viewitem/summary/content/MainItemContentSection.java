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

package com.tle.web.viewitem.summary.content;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.java.plugin.registry.Extension;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.RegistrationController;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionNode;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.DefaultSectionTree;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.section.DisplaySectionConfiguration;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewItemServiceImpl.CachedTree;

@SuppressWarnings("nls")
public class MainItemContentSection extends AbstractContentSection<MainItemContentSection.SummaryPageModel>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private RegistrationController registrationController;
	@Inject
	private ViewItemService viewItemService;

	private PluginTracker<DisplaySectionConfiguration> extensionTracker;

	@Override
	public SectionResult renderHtml(RenderEventContext info)
	{
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(info);
		final ResultListCollector results = new ResultListCollector(true);

		addDefaultBreadcrumbs(info, itemInfo, null);

		SectionTree tree = getModel(info).getTree();
		List<SectionId> children = tree.getChildIds(tree.getRootId());

		SectionUtils.renderSectionIds(info, children, results);
		SectionUtils.renderSectionIds(info, info.getChildIds(info), results);

		SectionRenderable sr = results.getFirstResult();
		return sr;
	}

	public void ensureTree(SectionInfo info, ViewItemResource resource)
	{
		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(info);
		ItemDefinition collection = itemInfo.getItemdef();
		Date dateModified = collection.getDateModified();

		CachedTree cachedTree = viewItemService.getCachedTree(collection);
		if( cachedTree == null || cachedTree.getLastModified().before(dateModified) )
		{
			DefaultSectionTree sectionTree = new DefaultSectionTree(registrationController, new SectionNode("sc_"));
			for( SummarySectionsConfig displaySection : collection.getItemSummaryDisplayTemplate().getConfigList() )
			{
				Extension extension = extensionTracker.getExtension(displaySection.getValue());
				// extension could be null from previous configurations
				if( extension != null )
				{
					DisplaySectionConfiguration renderer = extensionTracker.getNewBeanByExtension(extension);
					sectionTree.registerSections(renderer, null);
					renderer.associateConfiguration(displaySection);
				}
			}
			sectionTree.treeFinished();
			cachedTree = new CachedTree(dateModified, sectionTree);
			viewItemService.putCachedTree(collection, cachedTree);
		}

		SectionTree tree = cachedTree.getTree();
		MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
		minfo.addTreeToBottom(tree, true);
		getModel(info).setTree(tree);
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		if( !isCourseSelectionSession(context) )
		{
			return viewFactory.createResult("viewitem/summary/help/summary.ftl", this);
		}
		return null;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "summSections";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new SummaryPageModel();
	}

	public static class SummaryPageModel
	{
		private List<SummarySectionsConfig> sections;
		private SectionTree tree;

		public void setSections(List<SummarySectionsConfig> sections)
		{
			this.sections = sections;
		}

		public List<SummarySectionsConfig> getSections()
		{
			return sections;
		}

		public SectionTree getTree()
		{
			return tree;
		}

		public void setTree(SectionTree tree)
		{
			this.tree = tree;
		}
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		extensionTracker = new PluginTracker<DisplaySectionConfiguration>(pluginService, "com.tle.web.viewitem.summary",
			"summaryTabExtension", "id").setBeanKey("class");
	}
}