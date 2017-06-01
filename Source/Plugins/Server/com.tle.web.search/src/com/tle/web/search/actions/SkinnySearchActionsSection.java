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

package com.tle.web.search.actions;

import java.util.List;

import com.tle.common.Check;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;

@SuppressWarnings("nls")
public class SkinnySearchActionsSection
	extends
		AbstractSearchActionsSection<SkinnySearchActionsSection.SkinnySearchActionsModel> implements HtmlRenderer
{
	private static final String AJAX_ID = "sortandfilter";

	public static enum Showing
	{
		NONE, SHARE, SAVE, FILTER, SORT;
	}

	@AjaxFactory
	private AjaxGenerator ajax;
	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	@PlugKey("share.title")
	private Button share;
	@Component
	@PlugKey("save.title")
	private Button save;
	@Component
	@PlugKey("sort.title")
	private Button sort;
	@Component
	@PlugKey("filter.title")
	private Button filter;

	private UpdateDomFunction showHandler;
	private UpdateDomFunction hideHandler;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		showHandler = ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("show"), AJAX_ID);
		hideHandler = ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("hide"), AJAX_ID);

		tree.setAttribute(share, Showing.SHARE);
		tree.setAttribute(save, Showing.SAVE);
		tree.setAttribute(sort, Showing.SORT);
		tree.setAttribute(filter, Showing.FILTER);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final SkinnySearchActionsModel model = getModel(context);
		final Showing showing = model.getShowing();

		// TopicDisplay section can lookup this section, and set disabled where
		// appropriate
		if( model.isSearchDisabled() )
		{
			return null;
		}

		// setupButton(sort, sortSections, showing, context);
		// setupButton(filter, filterSections, showing, context);

		if( !model.isSaveAndShareDisabled() )
		{
			// setupButton(share, shareSections, showing, context);
			setupButton(save, saveSections, showing, context);
		}

		return viewFactory.createResult("skinny-sort-and-filters.ftl", this);
	}

	private void setupButton(Button button, List<SectionId> childSections, Showing mode, RenderEventContext context)
	{
		Showing buttonMode = context.getTree().getAttribute(button);
		HtmlComponentState state = button.getState(context);
		if( mode.equals(buttonMode) )
		{
			button.setClickHandler(context, new OverrideHandler(hideHandler));
			state.setAttribute(Icon.class, Icon.UP);
			state.addClass("active");

			getModel(context).setChildSections(SectionUtils.renderSectionIds(context, childSections));
		}
		else
		{
			button.setClickHandler(context, new OverrideHandler(showHandler, buttonMode.toString()));
			state.setAttribute(Icon.class, Icon.DOWN);
		}
	}

	@EventHandlerMethod
	public void show(SectionInfo info, String mode)
	{
		getModel(info).setShowing(parseShowing(mode));
	}

	@EventHandlerMethod
	public void hide(SectionInfo info)
	{
		getModel(info).setShowing(Showing.NONE);
	}

	private Showing parseShowing(String mode)
	{
		if( !Check.isEmpty(mode) )
		{
			try
			{
				return Showing.valueOf(mode);
			}
			catch( IllegalArgumentException ex )
			{
				// Not matching - that's fine
			}
		}
		return Showing.NONE;
	}

	@Override
	public String[] getResetFilterAjaxIds()
	{
		return new String[]{AJAX_ID};
	}

	public Button getShare()
	{
		return share;
	}

	public Button getSave()
	{
		return save;
	}

	public Button getSort()
	{
		return sort;
	}

	public Button getFilter()
	{
		return filter;
	}

	@Override
	public Class<SkinnySearchActionsModel> getModelClass()
	{
		return SkinnySearchActionsModel.class;
	}

	public static class SkinnySearchActionsModel extends AbstractSearchActionsSection.AbstractSearchActionsModel
	{
		@Bookmarked(stateful = false)
		private Showing showing = Showing.NONE;

		private List<SectionRenderable> childSections;

		public Showing getShowing()
		{
			return showing;
		}

		public void setShowing(Showing showing)
		{
			this.showing = showing;
		}

		public List<SectionRenderable> getChildSections()
		{
			return childSections;
		}

		public void setChildSections(List<SectionRenderable> childSections)
		{
			this.childSections = childSections;
		}
	}
}
