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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.item.service.ItemService;
import com.tle.mycontent.ContentHandlerSection;
import com.tle.mycontent.service.MyContentService;
import com.tle.mypages.MyPagesConstants;
import com.tle.mypages.service.MyPagesService;
import com.tle.mypages.web.MyPagesState;
import com.tle.mypages.web.event.LoadItemEvent;
import com.tle.mypages.web.model.MyPagesContributeModel;
import com.tle.web.DebugSettings;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.ReadyToRespondListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.wizard.WizardStateInterface;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class MyPagesContributeSection extends AbstractMyPagesSection<MyPagesContributeModel>
	implements
		HtmlRenderer,
		ContentHandlerSection,
		ReadyToRespondListener
{
	private static final CssInclude CSS = CssInclude
		.include(ResourcesService.getResourceHelper(MyPagesContributeSection.class).url("css/mypageseditor.css"))
		.hasRtl().make();

	@PlugKey("navigateaway.message")
	private static Label PROMPT_NAVIGATE_AWAY;
	@PlugKey("close.confirm")
	private static Label PROMPT_CLOSE_CONFIRM;
	@PlugKey("button.save")
	private static Label LABEL_SAVE;
	@PlugKey("mypages.button.cancel")
	private static Label LABEL_CANCEL;
	@PlugKey("breadcrumb.link.scrapbook")
	private static Label LABEL_SCRAPBOOK;

	@Inject
	private MyPagesService myPagesService;
	@Inject
	private ItemService itemService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private MyContentService myContentService;

	private SectionTree mytree;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final MyPagesContributeModel model = getModel(context);
		final List<SectionRenderable> renderables = renderChildren(context, new ResultListCollector()).getResultList();
		if( model.isModal() )
		{
			Decorations.getDecorations(context).clearAllDecorations();
		}
		// http://dev.equella.com/issues/5612
		// we can probably remove this if Chrome sorts out the problem
		context.getResponse().addHeader("X-XSS-Protection", "0");

		return new CombinedRenderer(renderables);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		this.mytree = tree;
	}

	public void addCrumbs(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		crumbs.add(getCancelState(LABEL_SCRAPBOOK));
	}

	@Override
	public List<HtmlComponentState> getMajorActions(RenderContext context)
	{
		final List<HtmlComponentState> actions = new ArrayList<HtmlComponentState>();

		final HtmlComponentState save = new HtmlComponentState();
		if( !DebugSettings.isAutoTestMode() )
		{
			save.addEventStatements(JSHandler.EVENT_BEFOREUNLOAD, getUnloadStatements());
		}
		save.setClickHandler(events.getNamedHandler("saveItem"));
		save.addClass("save-scrapbook-page");
		save.addPreRenderable(CSS);
		save.setLabel(LABEL_SAVE);

		actions.add(save);

		return actions;
	}

	@Override
	public List<HtmlComponentState> getMinorActions(RenderContext context)
	{
		final List<HtmlComponentState> actions = new ArrayList<HtmlComponentState>();
		actions.add(getCancelState(LABEL_CANCEL));
		return actions;
	}

	private HtmlComponentState getCancelState(Label label)
	{
		final HtmlComponentState cancel = new HtmlComponentState();
		final JSHandler closeHandler = events.getNamedHandler("close");
		closeHandler.addValidator(getCloseConfirm());
		cancel.setClickHandler(closeHandler);
		cancel.setLabel(label);
		return cancel;
	}

	public String getPageUuid(SectionInfo info)
	{
		return getModel(info).getPageUuid();
	}

	public JSStatements getUnloadStatements()
	{
		return new ReturnStatement(PROMPT_NAVIGATE_AWAY);
	}

	public JSValidator getCloseConfirm()
	{
		return new Confirm(PROMPT_CLOSE_CONFIRM);
	}

	@EventHandlerMethod
	public void saveItem(SectionInfo info)
	{
		saveItem(info, false);
	}

	@EventHandlerMethod
	public void savePages(SectionInfo info)
	{
		saveItem(info, true);
	}

	private void saveItem(SectionInfo info, boolean inWizard)
	{
		final MyPagesContributeModel model = getModel(info);

		// save the page we are on first
		final String pageUuid = model.getPageUuid();
		if( !Check.isEmpty(pageUuid) )
		{
			myPagesService.savePage(info, mytree, model.getSession(), pageUuid);
		}

		// never save the item if modal as this means we are in a contribution
		// wizard!
		if( !inWizard )
		{
			if( !myPagesService.saveItem(info, mytree, model.getSession()) )
			{
				// validation errors
				return;
			}
		}
		else
		{
			myPagesService.commitDraft(info, model.getSession());
		}
		close(info, inWizard);
	}

	@EventHandlerMethod
	public void close(SectionInfo info)
	{
		close(info, false);
	}

	public void close(SectionInfo info, boolean inWizard)
	{
		String session = getModel(info).getSession();
		myPagesService.clearDraft(info, session);
		myPagesService.removeFromSession(info, session);

		if( !inWizard )
		{
			myContentService.returnFromContribute(info);
		}
	}

	/**
	 * Called from MyPagesContentHandler.contribute
	 * 
	 * @param info
	 * @param itemDef
	 */
	public void contribute(SectionInfo info, ItemDefinition itemDef)
	{
		MyPagesState state = myPagesService.newItem(info, itemDef);

		MyPagesContributeModel model = getModel(info);
		model.setSession(state.getWizid());
	}

	public void edit(SectionInfo info, ItemId itemId, boolean readOnly)
	{
		MyPagesContributeModel model = getModel(info);

		if( !readOnly )
		{
			MyPagesState state = myPagesService.loadItem(info, itemId);
			model.setSession(state.getWizid());
		}

		// fire load event
		LoadItemEvent loadEvent = new LoadItemEvent(model.getSession(), itemService.get(itemId), model.getPageUuid());
		info.processEvent(loadEvent);
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void entryPoint(SectionInfo info)
	{
		MyPagesContributeModel model = getModel(info);
		if( model.isLoad() )
		{
			model.setLoad(false);

			String session = model.getSession();
			if( session != null )
			{
				// fire load event
				LoadItemEvent loadEvent = new LoadItemEvent(session, myPagesService.getState(info, session).getItem(),
					model.getPageUuid());
				info.processEvent(loadEvent);
			}
			else if( model.getItemId() != null )
			{
				// call edit, but don't create a session if in selection
				edit(info, new ItemId(model.getItemId()), isSelection(info));
			}
			else
			{
				throw new RuntimeException(CurrentLocale.get(RESOURCES.key("error.nosessionoritem")));
			}
		}
	}

	@Override
	public void readyToRespond(SectionInfo info, boolean redirect)
	{
		final String wizid = getModel(info).getSession();
		final WizardStateInterface state = myPagesService.getState(info, wizid);
		myPagesService.updateSession(info, state);
	}

	protected boolean isSelection(SectionInfo info)
	{
		SelectionSession currentSession = selectionService.getCurrentSession(info);
		return currentSession != null;
	}

	@Override
	public Class<MyPagesContributeModel> getModelClass()
	{
		return MyPagesContributeModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return MyPagesConstants.SECTION_CONTRIBUTE;
	}

	public void setSessionId(SectionInfo info, String sessionId)
	{
		getModel(info).setSession(sessionId);
	}

	public void setPageUuid(SectionInfo info, String uuid)
	{
		getModel(info).setPageUuid(uuid);
	}

	public void saveCurrentEdits(SectionInfo info)
	{
		MyPagesContributeModel model = getModel(info);
		final String pageUuid = model.getPageUuid();
		if( !Check.isEmpty(pageUuid) )
		{
			myPagesService.savePage(info, mytree, model.getSession(), pageUuid);
		}
	}
}
