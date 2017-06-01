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

package com.tle.web.template.section;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.java.plugin.registry.Extension;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ParamFilter;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.navigation.MenuService;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.render.GenericNamedResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.js.modules.TooltipModule;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;
import com.tle.web.template.section.MenuContributor.MenuContribution;

@SuppressWarnings("nls")
public class MenuSection extends AbstractPrototypeSection<MenuSection.MenuModel> implements HtmlRenderer
{
	private static final ParamFilter GUEST_FILTER = new ParamFilter("enabledFor", "guest");
	private static final ParamFilter SERVER_ADMIN_FILTER = new ParamFilter("enabledFor", "serverAdmin");
	private static final ParamFilter LOGGED_IN_FILTER = new ParamFilter("enabledFor", true, "loggedIn");

	private static final String DISPLAY_CLASS_FULL = "menu-full";
	private static final String DISPLAY_CLASS_COLLAPSED = "menu-collapsed";

	@PlugURL("css/nomenu.css")
	private static String HIDDEN_MENU_CSS;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private MenuService menuService;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final Decorations decorations = Decorations.getDecorations(context);
		final MenuMode menuMode = decorations.getMenuMode();

		// Do not render if no navigation
		if( menuMode == MenuMode.HIDDEN )
		{
			return null;
		}

		Multimap<Integer, Pair<SectionRenderable, Integer>> contributions = TreeMultimap.create(Ordering.natural(),
			new Comparator<Pair<SectionRenderable, Integer>>()
			{
				@Override
				public int compare(Pair<SectionRenderable, Integer> o1, Pair<SectionRenderable, Integer> o2)
				{
					return o1.getSecond() < o2.getSecond() ? -1 : 1;
				}
			});

		final PluginTracker<MenuContributor> contributors = menuService.getContributors();
		final ParamFilter filter = CurrentInstitution.get() == null ? SERVER_ADMIN_FILTER : CurrentUser.isGuest()
			? GUEST_FILTER : LOGGED_IN_FILTER;

		for( Extension contributor : contributors.getExtensions(filter) )
		{
			List<MenuContribution> links = contributors.getBeanByExtension(contributor).getMenuContributions(context);
			if( !Check.isEmpty(links) )
			{
				for( MenuContribution link : links )
				{
					HtmlLinkState linkState = link.getLink();
					Label title = linkState.getTitle();
					if( title == null )
					{
						title = linkState.getLabel();
					}

					// Remove the old tooltip and add jquery one
					if( menuMode == MenuMode.COLLAPSED )
					{
						linkState.setLabel(null);
					}
					linkState.setTitle(null);

					LinkRenderer linkRenderer = new LinkRenderer(linkState);

					linkRenderer.registerUse();
					String linkId = linkRenderer.getElementId(context);
					String text = title.getText();
					if( !title.isHtml() )
					{
						text = SectionUtils.ent(text);
					}

					if( menuMode == MenuMode.COLLAPSED )
					{
						linkState.addReadyStatements(TooltipModule.getTooltipStatements(new JQuerySelector(Type.ID,
							linkId), text, menuMode == MenuMode.COLLAPSED ? 1 : 600, true));
					}

					String imagePath = link.getBackgroundImagePath();
					if( !Check.isEmpty(imagePath) )
					{
						linkRenderer.setStyles("background-image: url('" + imagePath + "')", null, null);
					}

					contributions.put(link.getGroupPriority(),
						new Pair<SectionRenderable, Integer>(linkRenderer, link.getLinkPriority()));

				}
			}
		}

		// Nothing to render!
		if( contributions.isEmpty() )
		{
			// If there are no menu entries to show, then we may as well set it
			// to hidden so the content automatically gets more width.
			decorations.setMenuMode(MenuMode.HIDDEN);
			return null;
		}

		final MenuModel model = getModel(context);
		model.setContributions(contributions);
		model.setDisplayClass(menuMode == MenuMode.COLLAPSED ? DISPLAY_CLASS_COLLAPSED : DISPLAY_CLASS_FULL);

		return new GenericNamedResult("menu", viewFactory.createResult("menu.ftl", context));
	}

	public static String getHiddenMenuCSS()
	{
		return HIDDEN_MENU_CSS;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "menu";
	}

	@Override
	public Class<MenuModel> getModelClass()
	{
		return MenuModel.class;
	}

	public static class MenuModel
	{
		private Multimap<Integer, Pair<SectionRenderable, Integer>> contributions;
		private String displayClass = DISPLAY_CLASS_FULL;

		public Multimap<Integer, Pair<SectionRenderable, Integer>> getContributions()
		{
			return contributions;
		}

		public void setContributions(Multimap<Integer, Pair<SectionRenderable, Integer>> contributions)
		{
			this.contributions = contributions;
		}

		public String getDisplayClass()
		{
			return displayClass;
		}

		public void setDisplayClass(String displayClass)
		{
			this.displayClass = displayClass;
		}
	}
}
