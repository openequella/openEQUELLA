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

package com.tle.web.viewitem.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.viewurl.ItemSectionInfo;

@NonNullByDefault
public abstract class AbstractParentViewItemSection<M> extends AbstractPrototypeSection<M>
	implements
		ViewableChildInterface,
		HtmlRenderer
{
	@ViewFactory(fixed = false, optional = true)
	protected FreemarkerFactory viewFactory;

	public static ItemSectionInfo getItemInfo(SectionInfo info)
	{
		return ParentViewItemSectionUtils.getItemInfo(info);
	}

	protected boolean canViewChildren(SectionInfo info)
	{
		return SectionUtils.canViewChildren(info, this);
	}

	protected boolean isForPreview(SectionInfo info)
	{
		return ParentViewItemSectionUtils.isForPreview(info);
	}

	protected boolean isInIntegration(SectionInfo info)
	{
		return ParentViewItemSectionUtils.isInIntegration(info);
	}
}
