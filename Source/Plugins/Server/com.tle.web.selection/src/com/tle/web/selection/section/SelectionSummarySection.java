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

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.core.item.service.ItemService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.component.Box;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.SelectAttachmentHandler;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.event.AttachmentSelectorEvent;
import com.tle.web.selection.event.AttachmentSelectorEventListener;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;

@SuppressWarnings("nls")
@TreeIndexed
public class SelectionSummarySection extends AbstractPrototypeSection<SelectionSummarySection.SelectionSessionModel>
	implements
		HtmlRenderer,
		ViewableChildInterface,
		AttachmentSelectorEventListener
{
	static
	{
		PluginResourceHandler.init(SelectionSummarySection.class);
	}

	private static final String DIVID_SELECTBOX = "selection-summary";

	@PlugKey("selectionsbox.title")
	private static String KEY_BOXTITLE;

	@Inject
	private SelectionService selectionService;
	@Inject
	private ItemService itemService;
	@Inject
	private ViewableItemResolver viewableItemResolver;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	private Box box;
	@Component
	@PlugKey("selectionsbox.viewselected")
	private Link viewSelectedLink;
	@Component
	@PlugKey("selectionsbox.unselectall")
	private Link unselectAllLink;
	@Component
	@PlugKey("returnselections")
	private Button finishedButton;

	private boolean finishedInBox;
	private boolean followWithHr;
	private String layout = AbstractSearchActionsSection.AREA_SELECT;

	// private JSCallable selectAttachmentFunction;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		SubmitValuesHandler checkoutFunc = events.getNamedHandler("checkout");

		box.setNoMinMaxOnHeader(true);

		viewSelectedLink.setClickHandler(checkoutFunc);
		unselectAllLink.setClickHandler(events.getNamedHandler("unselectAll"));
		finishedButton.setClickHandler(checkoutFunc);

		if( !finishedInBox )
		{
			finishedButton.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
			finishedButton.setStyleClass("execute-action");
		}

		if( !Check.isEmpty(layout) )
		{
			tree.setLayout(id, layout);
		}

		tree.addListener(null, AttachmentSelectorEventListener.class, this);

		// selectAttachmentFunction =
		// events.getSubmitValuesFunction("addAttachment");
	}

	public JSCallable getUpdateSelection(SectionTree tree, ParameterizedEvent event)
	{
		return ajax.getAjaxUpdateDomFunction(tree, null, event, ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE),
			DIVID_SELECTBOX);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return selectionService.getCurrentSession(info) != null;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final SelectionSession ss = selectionService.getCurrentSession(context);
		if( ss == null )
		{
			return null;
		}

		getModel(context).setSession(ss);

		box.setLabel(context, new KeyLabel(KEY_BOXTITLE, ss.getSelectedResources().size()));
		return viewFactory.createNamedResult("ss", "selection/selectionsummary.ftl", context);
	}

	@EventHandlerMethod
	public void unselectAll(SectionContext context)
	{
		selectionService.getCurrentSession(context).clearResources();
	}

	@EventHandlerMethod
	public void checkout(SectionContext context)
	{
		// TODO: Do show a message here if SelectionSession.isCancelDisabled and
		// there are no selections? Alternatively, do we now show the checkout
		// button until there are selections if isCancelDisabled is true?

		selectionService.forwardToCheckout(context);
	}

	@EventHandlerMethod
	public void addAttachment(SectionInfo info, String uuid, int version, String attachUuid)
	{
		ItemId itemId = new ItemId(uuid, version);
		Item item = itemService.get(itemId);
		ViewableItem<?> vitem = viewableItemResolver.createViewableItem(item, null);

		final SelectAttachmentHandler selectAttachmentHandler = selectionService.getSelectAttachmentHandler(info,
			vitem, attachUuid);
		if( selectAttachmentHandler != null )
		{
			Attachment attachment = UnmodifiableAttachments.convertToMapUuid(item.getAttachmentsUnmodifiable()).get(
				attachUuid);
			selectAttachmentHandler.handleAttachmentSelection(info, itemId, attachment, null);
		}
	}

	@EventHandlerMethod
	public void addSimpleSelection(SectionInfo info, String uuid, int version, String path, String title)
	{
		ItemId itemId = new ItemId(uuid, version);
		selectionService.addSelectedPath(info, itemId, path, title, null, null, null);
	}

	@EventHandlerMethod
	public void addPath(SectionInfo info, String uuid, int version, String path, String title)
	{
		ItemId itemId = new ItemId(uuid, version);
		selectionService.addSelectedPath(info, itemId, path, title, null, null, null);
	}

	@EventHandlerMethod
	public void addItem(SectionInfo info, String uuid, int version)
	{
		selectionService.addSelectedItem(info, itemService.get(new ItemId(uuid, version)), null, null);
	}

	@Override
	public void supplyFunction(SectionInfo info, AttachmentSelectorEvent event)
	{
		event.setHandler(this);
	}

	@Override
	public void handleAttachmentSelection(SectionInfo info, ItemId itemId, IAttachment attachment, String extensionType)
	{
		selectionService.addSelectedResource(info,
			selectionService.createAttachmentSelection(info, itemId, attachment, null, extensionType), true);
	}

	public boolean isFinishedInBox()
	{
		return finishedInBox;
	}

	public void setFinishedInBox(boolean finishedInBox)
	{
		this.finishedInBox = finishedInBox;
	}

	public boolean isFollowWithHr()
	{
		return followWithHr;
	}

	public void setFollowWithHr(boolean followWithHr)
	{
		this.followWithHr = followWithHr;
	}

	public void setLayout(String layout)
	{
		this.layout = layout;
	}

	public Box getBox()
	{
		return box;
	}

	public Link getViewSelectedLink()
	{
		return viewSelectedLink;
	}

	public Link getUnselectAllLink()
	{
		return unselectAllLink;
	}

	public Button getFinishedButton()
	{
		return finishedButton;
	}

	/**
	 * Only called by the item XSLT extension. Thankfully.
	 * 
	 * @param info
	 * @param functionName
	 * @param sampleResource
	 * @return
	 */
	public JSCallable getSelectFunction(SectionInfo info, String functionName, SelectedResource sampleResource)
	{
		char type = sampleResource.getType();
		String uuid = sampleResource.getUuid();
		int version = sampleResource.getVersion();
		if( type == SelectedResource.TYPE_ATTACHMENT )
		{
			SubmitValuesFunction addAttach = events.getSubmitValuesFunction("addAttachment");
			ScriptVariable uuidVar = new ScriptVariable("uuid");
			return new SimpleFunction(functionName, new ReturnStatement(new FunctionCallExpression(addAttach, uuid,
				version, uuidVar)), uuidVar);
		}
		else if( type == SelectedResource.TYPE_PATH )
		{
			if( sampleResource.getUrl().length() == 0 )
			{
				return new SimpleFunction(functionName, new ReturnStatement(new FunctionCallExpression(
					events.getSubmitValuesFunction("addItem"), uuid, version)));
			}
			SubmitValuesFunction addPath = events.getSubmitValuesFunction("addPath");
			ScriptVariable pathVar = new ScriptVariable("path");
			ScriptVariable titleVar = new ScriptVariable("title");
			return new SimpleFunction(functionName, new ReturnStatement(new FunctionCallExpression(addPath, uuid,
				version, pathVar, titleVar)), pathVar, titleVar);

		}
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<SelectionSessionModel> getModelClass()
	{
		return SelectionSessionModel.class;
	}

	public static class SelectionSessionModel
	{
		private SelectionSession session;

		public SelectionSession getSession()
		{
			return session;
		}

		public void setSession(SelectionSession session)
		{
			this.session = session;
		}
	}
}
