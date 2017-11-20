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

package com.tle.web.cloud.guice;

import com.tle.core.config.guice.PropertiesModule;
import com.tle.web.cloud.view.CloudViewItemSelectionSummarySection;
import com.tle.web.cloud.view.actions.CloudSelectItemSummarySection;
import com.tle.web.cloud.view.actions.CloudShareWithOthersLinkSection;
import com.tle.web.cloud.view.actions.CloudUnselectItemSummarySection;
import com.tle.web.cloud.view.section.CloudAttachmentsSection;
import com.tle.web.cloud.view.section.CloudDisplayNodesSection;
import com.tle.web.cloud.view.section.CloudItemSummaryContentSection;
import com.tle.web.cloud.view.section.CloudItemSummarySection;
import com.tle.web.cloud.view.section.CloudTitleAndDescriptionSection;
import com.tle.web.cloud.view.section.RootCloudViewItemSection;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.viewitem.summary.section.ItemSummarySidebarSection;
import com.tle.web.viewitem.summary.sidebar.MajorActionsGroupSection;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class CloudViewItemModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bindNamed("/cloud/viewitem", cloudViewItemTree());
		install(new CloudViewItemPropsModule());
	}

	private NodeProvider cloudViewItemTree()
	{
		final NodeProvider root = node(RootCloudViewItemSection.class);

		final NodeProvider summary = node(CloudItemSummarySection.class);
		root.child(summary);

		final NodeProvider summaryContent = node(CloudItemSummaryContentSection.class);
		summaryContent.child(CloudTitleAndDescriptionSection.class);
		summaryContent.child(CloudDisplayNodesSection.class);
		summaryContent.child(CloudAttachmentsSection.class);
		summary.child(summaryContent);

		final NodeProvider summarySidebar = node(ItemSummarySidebarSection.class).placeHolder(
			"com.tle.web.cloud.summary.SUMMARY_SIDEBAR");
		summarySidebar.child(majorActions());
		summarySidebar.child(CloudViewItemSelectionSummarySection.class);
		summary.child(summarySidebar);

		return root;
	}

	private NodeProvider majorActions()
	{
		NodeProvider node = node(MajorActionsGroupSection.class).placeHolder(
			"com.tle.web.cloud.summary.sidebar.MAJOR_ACTIONS");
		node.child(CloudShareWithOthersLinkSection.class);
		node.child(CloudSelectItemSummarySection.class);
		node.child(CloudUnselectItemSummarySection.class);
		return node;
	}

	/**
	 * Hijacks the viewitem properties (we don't want double configuration)
	 */
	private static class CloudViewItemPropsModule extends PropertiesModule
	{
		@Override
		protected void configure()
		{
			bindProp("audit.level", "NONE");
		}

		@Override
		protected String getFilename()
		{
			return "/plugins/com.tle.web.viewitem/mandatory.properties";
		}
	}
}
