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

package com.tle.web.portal.standard.renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.exceptions.InvalidSearchQueryException;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.portal.entity.impl.PortletRecentContrib;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.portal.standard.editor.RssPortletEditorSection;
import com.tle.web.portal.standard.service.PortletStandardWebService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewurl.ViewItemUrlFactory;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class RecentContribPortletRenderer
	extends
		PortletContentRenderer<RecentContribPortletRenderer.RecentContribPortletRendererModel>
{
	private static final int DEFAULT_RESULT_COUNT = 5;

	@PlugKey("recent.invalid.query")
	private static Label LABEL_INVALID;

	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private PortletStandardWebService portletStandardService;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	private Button showMoreButton;
	@Component
	private Button showLessButton;

	@Override
	public SectionRenderable renderHtml(RenderEventContext context) throws Exception
	{
		final RecentContribPortletRendererModel model = getModel(context);

		PortletRecentContrib extra = (PortletRecentContrib) portlet.getExtraData();
		try
		{
			final List<Item> results = portletStandardService.getRecentContributions(extra);
			List<RecentContribResult> resultList = new ArrayList<RecentContribResult>();
			for( Item item : results )
			{
				if( item == null )
				{
					continue;
				}
				final Bookmark titleBookmark = urlFactory.createItemUrl(context,
					new ItemId(item.getUuid(), item.getVersion()), 0);
				final HtmlLinkState linkState = new HtmlLinkState(titleBookmark);
				final LinkRenderer link = new LinkRenderer(linkState);
				final BundleLabel label = new BundleLabel(item.getName(), bundleCache);
				label.setDefaultString(item.getUuid());

				link.setNestedRenderable(new LabelRenderer(label));
				String description = null;
				String titleOnly = portlet.getAttribute(RssPortletEditorSection.KEY_TITLEONLY);
				final boolean showDescription = !(titleOnly != null && titleOnly
					.equals(RssPortletEditorSection.KEY_TITLEONLY));
				if( showDescription )
				{
					description = CurrentLocale.get(item.getDescription(), null);
				}
				resultList.add(new RecentContribResult(link, description, item.getDateModified()));
			}

			if( resultList.size() > DEFAULT_RESULT_COUNT )
			{
				if( !model.isShowMore() )
				{
					resultList = resultList.subList(0, DEFAULT_RESULT_COUNT);
				}
				model.setMoreAvailable(true);
			}

			model.setResults(resultList);
		}
		catch( InvalidSearchQueryException e )
		{
			model.setError(LABEL_INVALID);
		}

		return view.createResult("recentportlet.ftl", context);
	}

	@EventHandlerMethod
	public void showMore(SectionInfo info)
	{
		RecentContribPortletRendererModel model = getModel(info);
		model.setShowMore(true);
	}

	@EventHandlerMethod
	public void showLess(SectionInfo info)
	{
		RecentContribPortletRendererModel model = getModel(info);
		model.setShowMore(false);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		UpdateDomFunction showMoreFunc = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("showMore"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), id + "recentPortlet");
		showMoreButton.setClickHandler(new OverrideHandler(showMoreFunc));

		UpdateDomFunction showLessFunc = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("showLess"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), id + "recentPortlet");
		showLessButton.setClickHandler(new OverrideHandler(showLessFunc));
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "prc";
	}

	@Override
	public Class<RecentContribPortletRendererModel> getModelClass()
	{
		return RecentContribPortletRendererModel.class;
	}

	public Button getShowMoreButton()
	{
		return showMoreButton;
	}

	public Button getShowLessButton()
	{
		return showLessButton;
	}

	public static class RecentContribPortletRendererModel
	{
		@Bookmarked(name = "m")
		private boolean showMore;
		private boolean moreAvailable;
		private List<RecentContribResult> results;

		private Label error;
		private boolean hasError = false;

		public boolean isHasError()
		{
			return hasError;
		}

		public boolean isShowMore()
		{
			return showMore;
		}

		public void setShowMore(boolean showMore)
		{
			this.showMore = showMore;
		}

		public boolean isMoreAvailable()
		{
			return moreAvailable;
		}

		public void setMoreAvailable(boolean moreAvailable)
		{
			this.moreAvailable = moreAvailable;
		}

		public List<RecentContribResult> getResults()
		{
			return results;
		}

		public void setResults(List<RecentContribResult> results)
		{
			this.results = results;
		}

		public Label getError()
		{
			return error;
		}

		public void setError(Label message)
		{
			hasError = true;
			error = message;
		}
	}

	public static class RecentContribResult
	{
		private final LinkRenderer title;
		private final String description;
		private final Date date;

		public RecentContribResult(LinkRenderer title, String description, Date date)
		{
			this.title = title;
			this.description = description;
			this.date = date;
		}

		public LinkRenderer getTitle()
		{
			return title;
		}

		public String getDescription()
		{
			return description;
		}

		public Date getDate()
		{
			return date;
		}
	}
}
