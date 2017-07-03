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

package com.tle.web.viewitem.summary.section;

import java.util.Map;

import javax.inject.Inject;

import com.thoughtworks.xstream.io.StreamException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.xml.service.XmlService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class TitleAndDescriptionSection
	extends
		AbstractTitleAndDescriptionSection<Item, TitleAndDescriptionSection.Model>
	implements
		ViewableChildInterface,
		DisplaySectionConfiguration
{
	private static String TITLE_LENGTH_KEY = "title";
	private static String DESCRIPTION_LENGTH_KEY = "description";

	@Inject
	private XmlService xmlService;
	private int titleLength;
	private int descLength;

	@Override
	protected ViewableItem<Item> getViewableItem(SectionInfo info)
	{
		return ParentViewItemSectionUtils.getItemInfo(info).getViewableItem();
	}

	@Override
	protected int getMaxTitleLength(SectionInfo info)
	{
		return titleLength;
	}

	@Override
	protected int getMaxDescriptionLength(SectionInfo info)
	{
		return descLength;
	}

	@Nullable
	@Override
	protected String getItemExtensionType()
	{
		return null;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void associateConfiguration(SummarySectionsConfig config)
	{
		String configuration = config.getConfiguration();
		if( !Check.isEmpty(configuration) )
		{
			try
			{
				final Map<String, String> settings = (Map<String, String>) xmlService
					.deserialiseFromXml(getClass().getClassLoader(), configuration);
				final boolean desc = settings.containsKey(DESCRIPTION_LENGTH_KEY);
				if( desc )
				{
					descLength = Integer.valueOf(settings.get(DESCRIPTION_LENGTH_KEY));
				}

				final boolean title = settings.containsKey(TITLE_LENGTH_KEY);
				if( title )
				{
					titleLength = Integer.valueOf(settings.get(TITLE_LENGTH_KEY));
				}
			}
			catch( StreamException e )
			{
				descLength = 0;
				titleLength = 0;
			}
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "basic";
	}

	@Override
	public Model instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model extends AbstractTitleAndDescriptionSection.TitleAndDescriptionModel
	{
		// Nothing specific
	}
}
