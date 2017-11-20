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

package com.tle.web.controls.flickr;

import java.util.List;

import com.tle.web.search.base.AbstractRootSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;

/**
 * @author larry
 */
public class FlickrLayoutSection extends AbstractRootSearchSection<AbstractRootSearchSection.Model>
{
	@PlugURL("css/flickr.css")
	private static String FLICKR_CSS;

	@Override
	public Label getTitle(SectionInfo info)
	{
		return new TextLabel(this.getClass().getCanonicalName());
	}

	@Override
	protected void createCssIncludes(List<CssInclude> includes)
	{
		super.createCssIncludes(includes);
		includes.add(CssInclude.include(FLICKR_CSS).hasRtl().make());
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.ONE_COLUMN;
	}
}
