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

package com.tle.web.hierarchy.model;

import static com.tle.web.hierarchy.TopicUtils.labelForValue;

import java.util.Collection;
import java.util.List;

import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.core.i18n.BundleCache;
import com.tle.core.security.TLEAclManager;
import com.tle.web.search.filter.AbstractResetFiltersQuerySection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class TopicDisplayModel extends AbstractResetFiltersQuerySection.AbstractQuerySectionModel
{
	private Label name;
	private Label description;
	private Label subtopicName;
	private List<DisplayHierarchyNode> subTopics;
	private boolean showAdvanced;

	public Label getName()
	{
		return name;
	}

	public void setName(Label name)
	{
		this.name = name;
	}

	public Label getDescription()
	{
		return description;
	}

	public void setDescription(Label description)
	{
		this.description = description;
	}

	public Label getSubtopicName()
	{
		return subtopicName;
	}

	public void setSubtopicName(Label subtopicName)
	{
		this.subtopicName = subtopicName;
	}

	public List<DisplayHierarchyNode> getSubTopics()
	{
		return subTopics;
	}

	public void setSubTopics(List<DisplayHierarchyNode> subTopics)
	{
		this.subTopics = subTopics;
	}

	public boolean isShowAdvanced()
	{
		return showAdvanced;
	}

	public void setShowAdvanced(boolean showAdvanced)
	{
		this.showAdvanced = showAdvanced;
	}

	public static class DisplayHierarchyNode
	{
		private final Label name;
		private final Label description;
		private final HtmlComponentState link;
		private final int resultCount;
		private final boolean showResults;

		public DisplayHierarchyNode(HierarchyTopic topic, String value, HtmlComponentState link, int searchCount,
			BundleCache bundleCache, TLEAclManager aclManager, Collection<String> keyResPrivs)
		{
			name = labelForValue(new BundleLabel(topic.getName(), bundleCache), value);
			description = labelForValue(new BundleLabel(topic.getShortDescription(), bundleCache).setHtml(true), value);

			this.link = link;

			Collection<Item> keyResources = topic.getKeyResources();

			if( !Check.isEmpty(keyResources) )
			{
				keyResources = aclManager.filterNonGrantedObjects(keyResPrivs, keyResources);
				searchCount += keyResources.size();
			}
			resultCount = searchCount;

			showResults = topic.isShowResults();
		}

		public Label getDescription()
		{
			return description;
		}

		public Label getName()
		{
			return name;
		}

		public HtmlComponentState getLink()
		{
			return link;
		}

		public Label getResultCount()
		{
			return new NumberLabel(resultCount);
		}

		public int getResultCountInt()
		{
			return resultCount;
		}

		public boolean isShowResults()
		{
			return showResults;
		}
	}
}
