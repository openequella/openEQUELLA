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

package com.tle.web.template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.tle.common.Check;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;

@SuppressWarnings("nls")
public class Decorations
{
	public static final String KEY = "DECORATIONS_KEY";

	public static enum MenuMode
	{
		HIDDEN, COLLAPSED, FULL
	}

	public static enum FullScreen
	{
		NO, YES, YES_WITH_TOOLBAR
	}

	private MenuMode menuMode = MenuMode.FULL;
	private Label title;
	private Label bannerTitle;
	private boolean footer = true;
	private boolean banner = true;
	private boolean help = true;
	private boolean options = true;
	private boolean content = true;
	private boolean breadcrumbs = true;
	private boolean forceBreadcrumbsOn = false;
	private FullScreen fullscreen = FullScreen.NO;
	private boolean excludeForm = false;
	private boolean singlePageApp = false;

	private Set<String> contentBodyClasses = new HashSet<String>();

	public void setContentBodyClass(String extraClass)
	{
		contentBodyClasses.clear();
		if( !Check.isEmpty(extraClass) )
		{
			contentBodyClasses.add(extraClass);
		}
	}

	public void addContentBodyClass(String extraClass)
	{
		if( !Check.isEmpty(extraClass) )
		{
			contentBodyClasses.add(extraClass);
		}
	}

	public void addContentBodyClasses(String styleClass)
	{
		String[] classes = styleClass.split("\\s+"); //$NON-NLS-1$
		for( String clazz : classes )
		{
			addContentBodyClass(clazz);
		}
	}

	public void addContentBodyClasses(Set<String> classes)
	{
		for( String clazz : classes )
		{
			addContentBodyClass(clazz);
		}
	}

	public MenuMode getMenuMode()
	{
		return menuMode;
	}

	public String getPageLayoutDisplayClass()
	{
		switch( menuMode )
		{
			case HIDDEN:
				return "page-layout-menu-hidden";
			case COLLAPSED:
				return "page-layout-menu-collapsed";
			default:
				return "page-layout-menu-full";
		}
	}

	public boolean isMenuFull()
	{
		return menuMode == MenuMode.FULL;
	}

	public boolean isMenuCollapsed()
	{
		return menuMode == MenuMode.COLLAPSED;
	}

	public boolean isMenuHidden()
	{
		return menuMode == MenuMode.HIDDEN;
	}

	public void setMenuMode(MenuMode menuMode)
	{
		this.menuMode = menuMode;
	}

	public boolean isBanner()
	{
		return banner;
	}

	public void setBanner(boolean banner)
	{
		this.banner = banner;
	}

	public boolean isHelp()
	{
		return help;
	}

	public void setHelp(boolean help)
	{
		this.help = help;
	}

	public boolean isOptions()
	{
		return options;
	}

	public void setOptions(boolean options)
	{
		this.options = options;
	}

	public static void setDecorations(SectionInfo info, Decorations decorations)
	{
		info.setAttribute(KEY, decorations);
	}

	public static Decorations getDecorations(SectionInfo info)
	{
		Decorations form = info.getAttribute(KEY);
		if( form == null )
		{
			form = new Decorations();
			info.setAttribute(KEY, form);
		}
		return form;
	}

	public static void setTitle(SectionInfo info, Label title)
	{
		getDecorations(info).setTitle(title);
	}

	public void clearAllDecorations()
	{
		menuMode = MenuMode.HIDDEN;
		banner = false;
		help = false;
		options = false;
		content = false;
		breadcrumbs = false;
		footer = false;
	}

	public String getContentBodyAttributes()
	{
		Map<String, String> attrs = new HashMap<String, String>();
		if( !Check.isEmpty(contentBodyClasses) )
		{
			StringBuilder sbuf = new StringBuilder();
			boolean first = true;
			for( String clazz : contentBodyClasses )
			{
				if( !first )
				{
					sbuf.append(' ');
				}
				sbuf.append(clazz);
				first = false;
			}
			attrs.put("class", sbuf.toString());
		}
		return SectionUtils.mapToAttributes(attrs);
	}

	public Label getTitle()
	{
		return title;
	}

	public SectionRenderable getTitleAsRenderable()
	{
		return new LabelRenderer(title);
	}

	public void setTitle(Label title)
	{
		this.title = title;
	}

	public boolean isForceBreadcrumbsOn()
	{
		return forceBreadcrumbsOn;
	}

	public void setForceBreadcrumbsOn(boolean forceBreadcrumbsOn)
	{
		this.forceBreadcrumbsOn = forceBreadcrumbsOn;
	}

	public void setBreadcrumbs(boolean breadcrumbs)
	{
		this.breadcrumbs = breadcrumbs;
	}

	public boolean isBreadcrumbs()
	{
		return breadcrumbs;
	}

	public void setContent(boolean content)
	{
		this.content = content;
	}

	public boolean isContent()
	{
		return content;
	}

	public FullScreen isFullscreen()
	{
		return fullscreen;
	}

	public void setFullscreen(FullScreen fullscreen)
	{
		this.fullscreen = fullscreen;
	}

	public boolean isFooter()
	{
		return footer;
	}

	public void setFooter(boolean footer)
	{
		this.footer = footer;
	}

	public Label getBannerTitle()
	{
		return bannerTitle;
	}

	public void setBannerTitle(Label bannerTitle)
	{
		this.bannerTitle = bannerTitle;
	}

	public Set<String> getContentBodyClasses()
	{
		return contentBodyClasses;
	}

	public void setExcludeForm(boolean excludeForm)
	{
		this.excludeForm = excludeForm;
	}

	public boolean isExcludeForm()
	{
		return excludeForm;
	}
}
