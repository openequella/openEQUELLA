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

package com.tle.mypages.web.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.common.Check;
import com.tle.mypages.MyPagesConstants;
import com.tle.mypages.service.MyPagesService;
import com.tle.mypages.web.MyPagesPageFilter;
import com.tle.mypages.web.event.ChangePageEvent;
import com.tle.mypages.web.event.ChangePageEventListener;
import com.tle.mypages.web.event.LoadItemEvent;
import com.tle.mypages.web.event.LoadItemEventListener;
import com.tle.mypages.web.model.MyPagesContributeModel;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.component.model.SelectionsTableState;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class AbstractMyPagesPageActionsSection
	extends
		AbstractMyPagesSection<AbstractMyPagesPageActionsSection.MyPagesPageActionsModel>
	implements
		HtmlRenderer,
		LoadItemEventListener,
		ChangePageEventListener
{
	@PlugKey("pagetitle.untitled")
	private static Label LABEL_PAGE_UNTITLED;
	@PlugKey("button.deletepage")
	private static Label LABEL_DELETE_PAGE;
	@PlugKey("nopages")
	private static Label LABEL_NO_PAGES;

	@Inject
	private MyPagesService myPagesService;

	@Component(name = "p")
	private SelectionsTable pagesTable;
	@PlugKey("button.addpage")
	@Component(name = "a")
	protected Link addPage;
	@TreeLookup
	protected MyPagesContributeSection contribSection;
	@ViewFactory(fixed = true)
	protected FreemarkerFactory fixedFactory;

	private SectionTree mytree;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final MyPagesPageActionsModel model = getModel(context);
		if( !model.isDisabled() )
		{
			return fixedFactory.createResult("mypagespageactions.ftl", context);
		}
		return null;
	}

	@EventHandlerMethod
	public void addPage(SectionInfo info)
	{
		if( !getModel(info).isNoAdd() )
		{
			final MyPagesContributeModel model2 = contribSection.getModel(info);
			myPagesService.savePage(info, mytree, model2.getSession(), model2.getPageUuid());
			createPage(info);
		}
	}

	public HtmlAttachment createPage(SectionInfo info)
	{
		final MyPagesContributeModel model = contribSection.getModel(info);
		final HtmlAttachment newPage = myPagesService.newPage(info, model.getSession(), LABEL_PAGE_UNTITLED.getText());
		setPageUuid(info, newPage.getUuid());
		return newPage;
	}

	@EventHandlerMethod
	public void deletePage(SectionInfo info, String pageUuid)
	{
		final MyPagesContributeModel model = contribSection.getModel(info);
		myPagesService.deletePage(info, model.getSession(), pageUuid);
		final String currentPageUuid = model.getPageUuid();
		if( currentPageUuid != null && currentPageUuid.equals(pageUuid) )
		{
			final HtmlAttachment nextPage = myPagesService.findNextAvailablePage(info, model.getSession(), pageUuid);
			final String newPageUuid = (nextPage == null ? null : nextPage.getUuid());
			setPageUuid(info, newPageUuid);
		}
	}

	@Override
	public void changePage(SectionInfo info, ChangePageEvent event)
	{
		changePage(info, event.getNewPageUuid());
	}

	@EventHandlerMethod
	public void changePage(SectionInfo info, String pageUuid)
	{
		MyPagesContributeModel model = contribSection.getModel(info);
		myPagesService.savePage(info, mytree, model.getSession(), model.getPageUuid());
		setPageUuid(info, pageUuid);
	}

	@Override
	public void doLoadItemEvent(SectionInfo info, LoadItemEvent event)
	{
		// pick the first page (if a uuid is not specified)
		List<HtmlAttachment> allOfThem = myPagesService.getPageAttachments(info, event.getSessionId(), null);

		String pageUuid = event.getPageUuid();
		if( Check.isEmpty(pageUuid) )
		{
			if( allOfThem.size() > 0 )
			{
				setPageUuid(info, allOfThem.get(0).getUuid());
			}
			else
			{
				setPageUuid(info, null);
			}
		}
		else
		{
			for( HtmlAttachment page : allOfThem )
			{
				if( page.getUuid().equals(pageUuid) )
				{
					setPageUuid(info, page.getUuid());
					return;
				}
			}
			throw new RuntimeException("Cannot find the page with UUID " + pageUuid);
		}
	}

	private List<HtmlAttachment> getPageAttachments(SectionInfo info, String sessionId, String itemId)
	{
		final List<HtmlAttachment> nonDeletedAttachments = myPagesService.getNonDeletedPageAttachments(info, sessionId,
			itemId);
		final MyPagesPageFilter filter = getModel(info).getPageFilter();
		if( filter != null )
		{
			return filter.filterPages(nonDeletedAttachments);
		}
		return nonDeletedAttachments;
	}

	public void setDisabled(SectionInfo info, boolean disabled)
	{
		MyPagesPageActionsModel model = getModel(info);
		model.setDisabled(disabled);
	}

	public void setDisallowAdd(SectionInfo info, boolean disallow)
	{
		MyPagesPageActionsModel model = getModel(info);
		model.setNoAdd(disallow);
	}

	public void setScrapbookCommand(SectionInfo info, HtmlComponentState scrapbook)
	{
		MyPagesPageActionsModel model = getModel(info);
		model.setScrapbook(scrapbook);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setupAddHandler(tree);
		pagesTable.setNothingSelectedText(LABEL_NO_PAGES);
		pagesTable.setSelectionsModel(new MyPagesModel());

	}

	protected abstract void setupAddHandler(SectionTree tree);

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		this.mytree = tree;
	}

	private void setPageUuid(SectionInfo info, String pageUuid)
	{
		MyPagesContributeModel model = contribSection.getModel(info);
		model.setPageUuid(pageUuid);
	}

	public String getCurrentPageUuid(SectionInfo info)
	{
		return contribSection.getModel(info).getPageUuid();
	}

	public void setPageFilter(SectionInfo info, MyPagesPageFilter pageFilter)
	{
		getModel(info).setPageFilter(pageFilter);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new MyPagesPageActionsModel();
	}

	@Override
	public String getDefaultPropertyName()
	{
		return MyPagesConstants.SECTION_PAGE_ACTIONS;
	}

	public Link getAddPageLink()
	{
		return addPage;
	}

	public SelectionsTable getPagesTable()
	{
		return pagesTable;
	}

	private class MyPagesModel extends DynamicSelectionsTableModel<HtmlAttachment>
	{
		@Override
		protected List<HtmlAttachment> getSourceList(SectionInfo info)
		{
			final MyPagesContributeModel contribModel = contribSection.getModel(info);
			return getPageAttachments(info, contribModel.getSession(), contribModel.getItemId());
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, HtmlAttachment page,
			List<SectionRenderable> actions, int index)
		{
			final String pageName = page.getDescription();
			final HtmlLinkState view = new HtmlLinkState(events.getNamedHandler("changePage", page.getUuid()));
			final LinkRenderer viewLink = new LinkRenderer(view);
			viewLink.setLabel(new TextLabel(pageName));
			viewLink.addClass("focus");
			selection.setViewAction(viewLink);

			final JSHandler deleteHandler = events.getNamedHandler("deletePage", page.getUuid());
			deleteHandler.addValidator(new Confirm(new KeyLabel(RESOURCES.key("delete.confirm"), pageName)));
			actions.add(makeRemoveAction(LABEL_DELETE_PAGE, deleteHandler));
		}
	}

	public static class MyPagesPageActionsModel
	{
		@Bookmarked(name = "d")
		private boolean disabled;
		@Bookmarked(name = "n")
		private boolean noAdd;
		private SelectionsTableState pages;
		private HtmlComponentState scrapbook;
		private MyPagesPageFilter pageFilter;

		public boolean isDisabled()
		{
			return disabled;
		}

		public void setDisabled(boolean disabled)
		{
			this.disabled = disabled;
		}

		public boolean isNoAdd()
		{
			return noAdd;
		}

		public void setNoAdd(boolean noAdd)
		{
			this.noAdd = noAdd;
		}

		public SelectionsTableState getPages()
		{
			return pages;
		}

		public void setPages(SelectionsTableState pages)
		{
			this.pages = pages;
		}

		public HtmlComponentState getScrapbook()
		{
			return scrapbook;
		}

		public void setScrapbook(HtmlComponentState scrapbook)
		{
			this.scrapbook = scrapbook;
		}

		public MyPagesPageFilter getPageFilter()
		{
			return pageFilter;
		}

		public void setPageFilter(MyPagesPageFilter pageFilter)
		{
			this.pageFilter = pageFilter;
		}
	}
}
