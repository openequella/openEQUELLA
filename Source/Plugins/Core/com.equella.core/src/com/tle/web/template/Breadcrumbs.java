/*
 * Copyright 2019 Apereo
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

package com.tle.web.template;

import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;

public class Breadcrumbs
{
	public static final String KEY = "BREADCRUMBS_KEY"; //$NON-NLS-1$

	private final List<TagState> crumbs = new ArrayList<TagState>();
	private Label forcedLastCrumb;

	public List<TagState> getLinks()
	{
		return crumbs;
	}

	public Label getForcedLastCrumb()
	{
		return forcedLastCrumb;
	}

	public void setForcedLastCrumb(Label forcedLastCrumb)
	{
		this.forcedLastCrumb = forcedLastCrumb;
	}

	public void add(TagState crumb)
	{
		crumbs.add(crumb);
	}

	public void addToStart(TagState crumb)
	{
		crumbs.add(0, crumb);
	}

	public static Breadcrumbs get(SectionInfo info)
	{
		Breadcrumbs crumbs = info.getAttribute(KEY);
		if( crumbs == null )
		{
			crumbs = new Breadcrumbs();
			info.setAttribute(KEY, crumbs);
		}
		return crumbs;
	}
}
