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

package com.tle.web.selection.section;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.AbstractRootModalSessionSection;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.component.NavBar;
import com.tle.web.sections.equella.component.NavBarBuilder;
import com.tle.web.sections.equella.impl.ModalErrorSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.ImageButtonRenderer;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.selection.SelectionNavAction;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.viewurl.ViewItemUrl;

@SuppressWarnings("nls")
public class RootSelectionSection extends AbstractRootModalSessionSection<RootSelectionSection.RootSelectionModel>
{
	public static enum Layout
	{
		NORMAL("selection/layout.ftl"), SKINNY("selection/skinny-layout.ftl"), COURSE("selection/course-layout.ftl");

		private final String ftl;

		private Layout(String ftl)
		{
			this.ftl = ftl;
		}

		public String getFtl()
		{
			return ftl;
		}
	}

	@PlugURL("images/close.png")
	private static String CLOSE_PNG_URL;
	@PlugKey("button.close")
	private static Label CLOSE_BUTTON_LABEL;
	@PlugKey("header.product")
	private static Label LABEL_PRODUCT;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Inject
	private SelectionService selectionService;

	@TreeLookup
	private ModalErrorSection errorSection;

	@Inject
	@Component(name = "n")
	private NavBar navBar;
	@Component(name = "a", parameter = "a", ignoreForContext = ViewItemUrl.VIEWONLY_CONTEXT)
	private SingleSelectionList<SelectionNavAction> navActions;
	@Inject
	private PluginTracker<SelectionNavAction> navActionTracker;

	@Override
	protected void setupModelForRender(SectionInfo info, RootSelectionModel model)
	{
		try
		{
			final SelectionSession session = selectionService.getCurrentSession(info);
			final Decorations decorations = Decorations.getDecorations(info);
			final Layout layout = session.getLayout();

			model.setLayout(layout);
			model.setDecorations(decorations);
			model.setSelectItems(session.isSelectItem());
			model.setSelectAttachments(session.isSelectAttachments());

			NavBarBuilder buildMiddle = navBar.getState(info).buildMiddle();

			List<Option<SelectionNavAction>> actions = navActions.getListModel().getOptions(info);
			if( !actions.isEmpty() )
			{
				for( Iterator<Option<SelectionNavAction>> iter = actions.iterator(); iter.hasNext(); )
				{
					SelectionNavAction navAction = iter.next().getObject();
					HtmlLinkState actionLink = new HtmlLinkState(navAction.getLabelForNavAction(info));
					actionLink.setClickHandler(new OverrideHandler(events.getNamedHandler("navActionsSelected",
						navAction.getActionType())));

					buildMiddle.action("link", actionLink);

					if( iter.hasNext() )
					{
						buildMiddle.divider();
					}
				}
			}

			if( !session.isCancelDisabled() )
			{
				final HtmlLinkState close = new HtmlLinkState(CLOSE_BUTTON_LABEL, events.getNamedHandler("cancelled"));
				final ImageButtonRenderer renderer = new ImageButtonRenderer(close);
				renderer.setNestedRenderable(new LabelRenderer(CLOSE_BUTTON_LABEL));
				renderer.setSource(CLOSE_PNG_URL);
				renderer.addClass("close");
				NavBarBuilder buildRight = navBar.getState(info).buildRight();
				buildRight.content(renderer);
			}

			SelectionNavAction selectedValue = navActions.getSelectedValue(info);
			if( selectedValue != null )
			{
				if( selectedValue.isShowBreadcrumbs() )
				{
					model.setBreadcrumbs(Breadcrumbs.get(info));
				}
			}
			else if( actions.size() == 0 )
			{
				model.setHideDividers("no-dividers");
			}

			navBar.getState(info).addClass("navbar-equella-selection " + layout.toString().toLowerCase());
		}
		catch( Exception e )
		{
			throw new RootSelectionException(e);
		}
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		navActions.setAlwaysSelect(true);
		navActions.setListModel(new NavActionsListModel());

		NavBarBuilder navLeft = navBar.buildLeft();
		navLeft.content(new SpanRenderer("logo", LABEL_PRODUCT));
	}

	@EventHandlerMethod
	public void cancelled(SectionInfo info)
	{
		selectionService.getCurrentSession(info).clearResources();
		selectionService.returnFromSession(info);
	}

	@EventHandlerMethod
	public void navActionsSelected(SectionInfo info, String actionType)
	{
		List<Option<SelectionNavAction>> actions = navActions.getListModel().getOptions(info);
		for( Option<SelectionNavAction> action : actions )
		{
			SelectionNavAction selectedValue = action.getObject();
			if( selectedValue.getActionType().equals(actionType) )
			{
				final SectionInfo fwd = selectedValue.createForwardForNavAction(info,
					selectionService.getCurrentSession(info));
				navActions.setSelectedValue(fwd, selectedValue);
				info.forwardAsBookmark(fwd);
				return;
			}
		}
	}

	@Override
	protected SectionResult getFinalRenderable(RenderEventContext context, RootSelectionModel model)
	{
		if( !model.isNoTemplate() )
		{
			return viewFactory.createTemplateResult(model.getLayout().getFtl(), context);
		}
		return model.getParts().getNamedResult(context, "body");
	}

	@Override
	public void forwardCreated(SectionInfo info, SectionInfo forward)
	{
		super.forwardCreated(info, forward);
		final SelectionSession session = selectionService.getCurrentSession(info);
		if( session != null )
		{
			// Preserve the selected action
			navActions.setSelectedValue(forward, navActions.getSelectedValue(info));
		}
	}

	@Override
	protected SectionId getErrorSection()
	{
		return errorSection;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "_sl";
	}

	@Override
	protected Object getSessionKey()
	{
		return SelectionSession.class;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new RootSelectionModel(info, this);
	}

	public NavBar getNavBar()
	{
		return navBar;
	}

	public class NavActionsListModel extends DynamicHtmlListModel<SelectionNavAction>
	{
		protected NavActionsListModel()
		{
			setSort(false);
		}

		@Override
		public String getDefaultValue(SectionInfo info)
		{
			final SelectionSession session = selectionService.getCurrentSession(info);
			final String homeSelectable = session.getHomeSelectable();
			if( homeSelectable != null )
			{
				return homeSelectable;
			}
			return super.getDefaultValue(info);
		}

		@Override
		protected Iterable<SelectionNavAction> populateModel(SectionInfo info)
		{
			final SelectionSession session = selectionService.getCurrentSession(info);
			List<SelectionNavAction> actions = Lists.newArrayList();
			for( SelectionNavAction navAction : navActionTracker.getBeanMap().values() )
			{
				if( navAction.isActionAvailable(info, session) )
				{
					actions.add(navAction);
				}
			}

			actions = reorderList(actions);
			return actions;
		}

		@Override
		protected Option<SelectionNavAction> convertToOption(SectionInfo info, SelectionNavAction action)
		{
			return new LabelOption<SelectionNavAction>(action.getLabelForNavAction(info), action.getActionType(),
				action);
		}

		private List<SelectionNavAction> reorderList(List<SelectionNavAction> actions)
		{
			List<SelectionNavAction> navActionList = new ArrayList<>();
			boolean found = false;
			for( SelectionNavAction action : actions )
			{
				if( action.getActionType().equals("home") )
				{
					navActionList.add(action);
					actions.remove(action);
					found = true;
					break;
				}
			}

			if( !found )
			{
				for( SelectionNavAction action : actions )
				{
					if( action.getActionType().equals("coursesearch") )
					{
						navActionList.add(action);
						actions.remove(action);
						break;
					}
				}

				for( SelectionNavAction action : actions )
				{
					if( action.getActionType().equals("skinnybrowse") )
					{
						navActionList.add(action);
						actions.remove(action);
						break;
					}
				}

				for( SelectionNavAction action : actions )
				{
					if( action.getActionType().equals("skinnyfavourites") )
					{
						navActionList.add(action);
						actions.remove(action);
						break;
					}
				}

				for( SelectionNavAction action : actions )
				{
					if( action.getActionType().equals("myresources") )
					{
						navActionList.add(action);
						actions.remove(action);
						break;
					}
				}
			}

			navActionList.addAll(actions);
			return navActionList;
		}
	}

	public static class RootSelectionModel extends AbstractRootModalSessionSection.RootModalSessionModel
	{
		private Decorations decorations;
		private Breadcrumbs breadcrumbs;
		private Layout layout;
		private boolean selectItems;
		private boolean selectAttachments;
		private String hideDividers;

		public String getHideDividers()
		{
			return hideDividers;
		}

		public void setHideDividers(String hideDividers)
		{
			this.hideDividers = hideDividers;
		}

		public RootSelectionModel(SectionInfo info, AbstractRootModalSessionSection<?> section)
		{
			super(info, section);
		}

		public Decorations getDecorations()
		{
			return decorations;
		}

		public void setDecorations(Decorations decorations)
		{
			this.decorations = decorations;
		}

		public Layout getLayout()
		{
			return layout;
		}

		public void setLayout(Layout layout)
		{
			this.layout = layout;
		}

		public boolean isSelectItems()
		{
			return selectItems;
		}

		public void setSelectItems(boolean selectItems)
		{
			this.selectItems = selectItems;
		}

		public boolean isSelectAttachments()
		{
			return selectAttachments;
		}

		public void setSelectAttachments(boolean selectAttachments)
		{
			this.selectAttachments = selectAttachments;
		}

		public Breadcrumbs getBreadcrumbs()
		{
			return breadcrumbs;
		}

		public void setBreadcrumbs(Breadcrumbs breadcrumbs)
		{
			this.breadcrumbs = breadcrumbs;
		}

	}
}
