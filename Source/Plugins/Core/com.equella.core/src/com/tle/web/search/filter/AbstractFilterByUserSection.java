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

package com.tle.web.search.filter;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.equella.utils.SelectUserDialog;
import com.tle.web.sections.equella.utils.SelectedUser;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.HiddenState;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;

@NonNullByDefault
@SuppressWarnings("nls")
public abstract class AbstractFilterByUserSection<SE extends AbstractSearchEvent<SE>>
	extends
		AbstractPrototypeSection<AbstractFilterByUserSection.Model>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<SE>
{
	@PlugKey("filter.byowner.dialog.title")
	private static Label LABEL_DIALOG_TITLE;
	@PlugKey("filter.byowner.title")
	private static Label LABEL_TITLE;

	@Inject
	private UserLinkService userLinkService;
	protected UserLinkSection userLinkSection;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;

	@TreeLookup
	protected AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Component(name = "so")
	@Inject
	protected SelectUserDialog selOwner;
	@Component(name = "r")
	@PlugKey("filter.byowner.remove")
	private Link remove;

	@Component(supported = true)
	protected HiddenState hidden;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		userLinkSection = userLinkService.register(tree, id);

		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
		selOwner.setTitle(getDialogTitle());
		hidden.setParameterId(getPublicParam());
	}

	protected abstract String getPublicParam();

	public Label getDialogTitle()
	{
		return LABEL_DIALOG_TITLE;
	}

	public Label getTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		selOwner.setOkCallback(searchResults.getResultsUpdater(tree, events.getEventHandler("ownerSelected"),
			getAjaxDiv()));
		remove.setClickHandler(new OverrideHandler(searchResults.getResultsUpdater(tree,
			events.getEventHandler("ownerRemoved"), getAjaxDiv())));
	}

	public abstract String getAjaxDiv();

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		String ownerUuid = hidden.getValue(context);
		if( ownerUuid != null )
		{
			getModel(context).setOwner(userLinkSection.createLink(context, ownerUuid));
		}
		return viewFactory.createResult("filter/filterbyowner.ftl", context);
	}

	@EventHandlerMethod
	public void ownerSelected(SectionInfo info, String user)
	{
		SelectedUser selectedUser = SelectUserDialog.userFromJsonString(user);

		if( selectedUser != null )
		{
			hidden.setValue(info, selectedUser.getUuid());
		}
		else
		{
			hidden.setValue(info, null);
		}
	}

	protected String getSelectedUserId(SectionInfo info)
	{
		return hidden.getValue(info);
	}

	@Override
	public void reset(SectionInfo info)
	{
		hidden.setValue(info, null);
	}

	@EventHandlerMethod
	public void ownerRemoved(SectionInfo info)
	{
		reset(info);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public SelectUserDialog getSelectOwner()
	{
		return selOwner;
	}

	public static class Model
	{
		private HtmlLinkState owner;

		public HtmlLinkState getOwner()
		{
			return owner;
		}

		public void setOwner(HtmlLinkState owner)
		{
			this.owner = owner;
		}
	}

	public Link getRemove()
	{
		return remove;
	}

	public boolean isShowOrphaned()
	{
		return false;
	}
}
