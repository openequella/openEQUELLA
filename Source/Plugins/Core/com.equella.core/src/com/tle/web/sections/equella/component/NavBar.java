/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.equella.component;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.component.model.NavBarState;
import com.tle.web.sections.equella.component.model.NavBarState.NavBarElement;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.standard.AbstractRenderedComponent;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.RendererFactory;

@Bind
@SuppressWarnings("nls")
public class NavBar extends AbstractRenderedComponent<NavBarState>
{
	@Inject
	private RendererFactory rendererFactory;

	private Link title;
	private List<NavBarElement> left;
	private List<NavBarElement> right;
	private List<NavBarElement> middle;

	public NavBar()
	{
		super("navbar");
	}

	@Override
	protected void prepareModel(RenderContext info)
	{
		final NavBarState model = getModel(info);
		if( model.getTitleLink() == null )
		{
			model.setTitleLink(title);
		}

		if( left != null )
		{
			List<NavBarElement> mleft = model.getLeft();
			if( mleft == null )
			{
				mleft = Lists.newArrayList();
				model.setLeft(mleft);
			}
			mleft.addAll(0, left);
		}
		if( right != null )
		{
			List<NavBarElement> mright = model.getRight();
			if( mright == null )
			{
				mright = Lists.newArrayList();
				model.setRight(mright);
			}
			mright.addAll(0, right);
		}
		if( middle != null )
		{
			List<NavBarElement> mmiddle = model.getMiddle();
			if( mmiddle == null )
			{
				mmiddle = Lists.newArrayList();
				model.setMiddle(mmiddle);
			}
			mmiddle.addAll(0, mmiddle);
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new NavBarState(rendererFactory);
	}

	public void setTitle(Link title)
	{
		ensureBuildingTree();
		this.title = title;
	}

	public NavBarBuilder buildLeft()
	{
		ensureBuildingTree();
		if( left == null )
		{
			left = Lists.newArrayList();
		}
		return new NavBarBuilder(left, rendererFactory);
	}

	public NavBarBuilder buildRight()
	{
		ensureBuildingTree();
		if( right == null )
		{
			right = Lists.newArrayList();
		}
		return new NavBarBuilder(right, rendererFactory);
	}

	public NavBarBuilder buildMiddle()
	{
		ensureBuildingTree();
		if( middle == null )
		{
			middle = Lists.newArrayList();
		}
		return new NavBarBuilder(middle, rendererFactory);
	}

	public Link getTitle()
	{
		return title;
	}

	public List<NavBarElement> getLeft()
	{
		return left;
	}

	public List<NavBarElement> getRight()
	{
		return right;
	}

	public List<NavBarElement> getMiddle()
	{
		return middle;
	}
}
