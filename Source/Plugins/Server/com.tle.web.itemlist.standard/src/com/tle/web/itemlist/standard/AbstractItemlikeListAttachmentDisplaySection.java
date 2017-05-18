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

package com.tle.web.itemlist.standard;

import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.AttachmentDisplay;
import com.tle.beans.entity.itemdef.SearchDetails;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.item.ItemlikeListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxCaptureRenderer;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomEvent;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.selection.SelectAttachmentHandler;
import com.tle.web.selection.SelectedResourceKey;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.event.AttachmentSelectorEvent;
import com.tle.web.selection.event.AttachmentSelectorEventListener;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.summary.attachment.service.ViewAttachmentWebService;
import com.tle.web.viewitem.summary.attachment.service.ViewAttachmentWebService.AttachmentRowDisplay;

@NonNullByDefault
@SuppressWarnings("nls")
public abstract class AbstractItemlikeListAttachmentDisplaySection<I extends IItem<?>, LE extends ItemlikeListEntry<I>>
	extends
		AbstractPrototypeSection<AbstractItemlikeListAttachmentDisplaySection.AttachmentDisplayModel<I>>
	implements
		ItemlikeListEntryExtension<I, LE>,
		HtmlRenderer,
		AttachmentSelectorEventListener
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(AbstractItemlikeListAttachmentDisplaySection.class);
	private static final IncludeFile JS = new IncludeFile(resources.url("scripts/itemlistattachments.js"));
	private static final JSCallAndReference ATTACHMENT_LIST_CLASS = new ExternallyDefinedFunction(
		"ItemListAttachments", JS);
	private static final JSCallable TOGGLE = new ExternallyDefinedFunction(ATTACHMENT_LIST_CLASS, "toggle", 5);
	private static final JSCallable END_TOGGLE = new ExternallyDefinedFunction(ATTACHMENT_LIST_CLASS, "endToggle", 0);

	private static final String TOGGLE_ID_PREFIX = "toggle_";
	private static final String ATTACHMENTS_ID_PREFIX = "attachments_";

	@PlugKey("showattachments.")
	private static String ATTACHMENT_LABEL_PREFIX;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;
	@EventFactory
	protected EventGenerator events;

	@Inject
	private ViewAttachmentWebService viewAttachmentWebService;
	@Inject
	private SelectionService selectionService;

	private UpdateDomEvent updateEvent;
	private JSCallable selectCall;
	private JSCallable selectCallOne;

	protected abstract boolean canSeeAttachments(I item);

	@Nullable
	protected abstract SearchDetails getSearchDetails(I item);

	protected abstract I getItem(LE entry);

	protected abstract I getItem(ItemId itemId);

	protected abstract ViewableItem<I> getViewableItem(I item);

	@Nullable
	@Override
	public ProcessEntryCallback<I, LE> processEntries(final RenderContext context, List<LE> entries,
		ListSettings<LE> listSettings)
	{
		for( LE entry : entries )
		{
			if( !Check.isEmpty(entry.getAttachments().getList())
				&& !entry.isFlagSet("com.tle.web.itemlist.standard.DontShowAttachments") )
			{
				final I item = getItem(entry);
				final String itemUuid = item.getUuid();
				final int itemVersion = item.getVersion();
				final String itemUuidAndVersion = itemUuid + itemVersion;

				final SimpleElementId attId = new SimpleElementId(ATTACHMENTS_ID_PREFIX + itemUuidAndVersion);
				attId.registerUse();

				if( canSeeAttachments(item) )
				{
					boolean defaultOpen = false;
					final AttachmentDisplayModel<I> model = getModel(context);

					AttachmentDisplay attdisplay = null;
					SearchDetails searchDetails = getSearchDetails(item);
					if( searchDetails != null )
					{
						defaultOpen = selectionService.getCurrentSession(context) != null ? searchDetails
							.isIntegrationOpen() : searchDetails.isStandardOpen();

						String attDisplay = searchDetails.getAttDisplay();
						attdisplay = (Strings.isNullOrEmpty(attDisplay) ? AttachmentDisplay.STRUCTURED
							: AttachmentDisplay.valueOf(attDisplay));
					}

					boolean structured = true;
					if( attdisplay == AttachmentDisplay.THUMBNAILS )
					{
						structured = false;
					}

					if( defaultOpen )
					{
						entry.addExtras(createAttachmentsList(context, item, structured, attId, itemUuid, itemVersion,
							defaultOpen));
					}
					else
					{
						if( model.getItemId() != null && model.getItemId().equals(item.getItemId()) )
						{
							entry.addExtras(createAttachmentsList(context, item, structured, attId, itemUuid,
								itemVersion, true));
						}
						else
						{
							entry.addExtras(new DivRenderer(new TagState(attId)));
						}
					}

					entry.setToggle(createToggler(context, itemUuid, itemVersion, attId, defaultOpen));
				}
			}
		}
		return null;
	}

	private SectionRenderable createToggler(RenderContext context, String itemUuid, int itemVersion,
		SimpleElementId attId, boolean defaultOpen)
	{
		final String toggleIdStr = getToggleId(itemUuid, itemVersion);
		final SimpleElementId toggleId = new SimpleElementId(toggleIdStr);
		toggleId.registerUse();

		final TagState divToggle = new TagState(toggleId);
		final UpdateDomFunction updateFunc = new UpdateDomFunction(updateEvent, toggleIdStr, ajax.getEffectFunction(
			EffectType.ACTIVITY, AjaxGenerator.URL_SPINNER_INLINE), END_TOGGLE);
		final JSCallAndReference updateCarf = CallAndReferenceFunction.get(updateFunc, toggleId);
		divToggle.setClickHandler(new OverrideHandler(TOGGLE, Jq.$(divToggle), Jq.$(attId), updateCarf, itemUuid,
			itemVersion));

		final Map<Object, Object> model = Maps.newHashMap();
		model.put("iconClass", (defaultOpen ? "icon-circle-arrow-up" : "icon-circle-arrow-down"));
		model.put("linkLabel", CurrentLocale.get(ATTACHMENT_LABEL_PREFIX + (defaultOpen ? "hide" : "show")));
		model.put("divToggle", new DivRenderer(divToggle));

		return viewFactory.createResultWithModel("attachmentlisttoggle.ftl", model);
	}

	private SectionRenderable createAttachmentsList(RenderContext context, I item, boolean structured,
		SimpleElementId attId, final String itemUuid, final int itemVersion, boolean defaultOpen)
	{
		final TagState attDivState = createAttDivState(attId);
		final TagState captureDiv = createCaptureDiv(context, item, attId, itemUuid, itemVersion);

		// Build Attachments
		final List<AttachmentRowDisplay> attachments = buildAttachmentRowDisplay(context, item, structured, attId);

		final Map<Object, Object> model = Maps.newHashMap();
		model.put("attachmentRows", attachments);
		model.put("structured", structured);
		model.put("itemId", new ItemId(itemUuid, itemVersion));
		model.put("show", defaultOpen);

		DivRenderer atts = new DivRenderer(attDivState, new AjaxCaptureRenderer(attId.getElementId(context),
			new DivRenderer(captureDiv, viewFactory.createResultWithModel("attachmentdisplay.ftl", model)), defaultOpen
				? null : getEffectMap()));

		atts.addClass("opened");
		return atts;
	}

	private List<AttachmentRowDisplay> buildAttachmentRowDisplay(RenderContext context, I item, boolean structured,
		SimpleElementId attId)
	{
		final List<AttachmentRowDisplay> attachments = viewAttachmentWebService.createViewsForItem(context,
			getViewableItem(item), attId, true, false, !structured, false);
		viewAttachmentWebService.filterAttachmentDisplays(context, attachments);
		return attachments;
	}

	private TagState createCaptureDiv(RenderContext context, I item, SimpleElementId attId, final String itemUuid,
		final int itemVersion)
	{
		final TagState captureDiv = new TagState();

		final ViewableItem<I> viewableItem = getViewableItem(item);
		final JSCallable selectFunction = selectionService.getSelectAttachmentFunction(context, viewableItem);
		if( selectFunction != null )
		{
			final String itemExtensionType = getItemExtensionType();
			captureDiv.addReadyStatements(viewAttachmentWebService.setupSelectButtonsFunction(selectFunction,
				new ItemId(itemUuid, itemVersion), itemExtensionType, "#" + attId.getElementId(context)));
		}

		if( selectionService.canSelectAttachment(context, viewableItem, null) )
		{
			captureDiv.addClass("selectable");
		}
		return captureDiv;
	}

	private TagState createAttDivState(final SimpleElementId attId)
	{
		final TagState attDivState = new TagState(attId);
		attDivState.addPreRenderable(JQueryUIEffects.getEffectLibrary("blind"));
		attDivState.addPreRenderable(JQueryUIEffects.getEffectLibrary("transfer"));
		return attDivState;
	}

	private Map<String, Object> getEffectMap()
	{
		final Map<String, Object> params = Maps.newHashMap();
		params.put("showEffect", "blind");
		params.put("hideEffect", "blind");
		return params;
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		updateEvent = UpdateDomEvent.register(tree, this, events.getEventHandler("toggleAttachments"), "");
		selectCallOne = events.getSubmitValuesFunction("selectAttachment");

		final JSCallable effectFunction = ajax.getEffectFunction(EffectType.ACTIVITY, AjaxGenerator.URL_SPINNER_INLINE);
		selectCall = ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("selectAttachment"),
			effectFunction, "placeholder1");

		tree.addListener(null, AttachmentSelectorEventListener.class, this);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final AttachmentDisplayModel<I> model = getModel(context);
		return renderAttachments(context, getCachedItem(context, model.getItemId()), true);
	}

	protected SectionRenderable renderAttachments(RenderContext context, I item, boolean structured)
	{
		final AttachmentDisplayModel<I> model = getModel(context);

		final String itemUuid = item.getUuid();
		final int itemVersion = item.getVersion();
		final String itemUuidAndVersion = itemUuid + itemVersion;

		final SimpleElementId attId = new SimpleElementId(ATTACHMENTS_ID_PREFIX + itemUuidAndVersion);
		attId.registerUse();

		final TagState attDivState = createAttDivState(attId);
		final TagState captureDiv = createCaptureDiv(context, item, attId, itemUuid, itemVersion);

		final List<AttachmentRowDisplay> attachments = buildAttachmentRowDisplay(context, item, structured, attId);
		model.setAttachmentRows(attachments);
		model.setStructured(structured);

		// Build Renderables
		SectionRenderable toggle = createToggler(context, itemUuid, itemVersion, attId, model.isShow());
		DivRenderer atts = new DivRenderer(attDivState,
			new AjaxCaptureRenderer(attId.getElementId(context), new DivRenderer(captureDiv, model.isShow()
				? viewFactory.createResult("attachmentdisplay.ftl", this) : null), getEffectMap()));

		return CombinedRenderer.combineResults(toggle, atts);
	}

	@EventHandlerMethod
	public void toggleAttachments(SectionInfo info, String itemUuid, int itemVersion, boolean opened)
	{
		final AjaxRenderContext context = info.getAttributeForClass(AjaxRenderContext.class);
		context.addAjaxDivs(ATTACHMENTS_ID_PREFIX + itemUuid + itemVersion);
		context.addAjaxDivs(getToggleId(itemUuid, itemVersion));

		final AttachmentDisplayModel<I> model = getModel(info);
		model.setShow(opened);
		model.setItemId(new ItemId(itemUuid, itemVersion));
	}

	@EventHandlerMethod
	public void selectAttachment(SectionInfo info, String attachmentUuid, ItemId itemId, String extensionType)
	{
		final AjaxRenderContext context = info.getAttributeForClass(AjaxRenderContext.class);
		if( context != null )
		{
			final String itemUuidAndVersion = itemId.getUuid() + itemId.getVersion();
			context.addAjaxDivs(ATTACHMENTS_ID_PREFIX + itemUuidAndVersion);
			// Dodge-o
			context.addAjaxDivs("selection-summary");
		}

		final AttachmentDisplayModel<I> model = getModel(info);
		model.setShow(true);
		model.setItemId(itemId);

		final SelectedResourceKey key = new SelectedResourceKey(itemId, attachmentUuid, extensionType);
		final SelectionSession ss = selectionService.getCurrentSession(info);
		if( ss == null )
		{
			throw new Error("No selection session");
		}
		if( ss.containsResource(key, false) )
		{
			selectionService.removeSelectedResource(info, key);
		}
		else
		{
			final IAttachment attachment = new UnmodifiableAttachments(getCachedItem(info, itemId))
				.getAttachmentByUuid(attachmentUuid);
			if( attachment != null )
			{
				final ViewableItem<I> viewableItem = getViewableItem(getItem(itemId));
				final SelectAttachmentHandler selectAttachmentHandler = selectionService.getSelectAttachmentHandler(
					info, viewableItem, attachmentUuid);
				if( selectAttachmentHandler != null )
				{
					selectAttachmentHandler.handleAttachmentSelection(info, itemId, attachment, extensionType);
				}
			}
		}

	}

	private String getToggleId(String itemUuid, int itemVersion)
	{
		return (TOGGLE_ID_PREFIX + itemUuid + itemVersion).replaceAll("\\-", "");
	}

	protected I getCachedItem(SectionInfo info, ItemId itemId)
	{
		final AttachmentDisplayModel<I> model = getModel(info);
		I item = model.getItem();
		if( item == null )
		{
			item = getItem(itemId);
			model.setItem(item);
		}
		return item;
	}

	@Override
	public void supplyFunction(SectionInfo info, AttachmentSelectorEvent event)
	{
		event.setFunction(event.getSession().isSelectMultiple() ? selectCall : selectCallOne);
		event.setHandler(this);
	}

	@Override
	public void handleAttachmentSelection(SectionInfo info, ItemId itemId, IAttachment attachment, String extensionType)
	{
		selectionService.addSelectedResource(info,
			selectionService.createAttachmentSelection(info, itemId, attachment, null, extensionType), true);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new AttachmentDisplayModel<I>();
	}

	@NonNullByDefault(false)
	public static class AttachmentDisplayModel<I extends IItem<?>>
	{
		private boolean show;
		private boolean structured;
		private ItemId itemId;
		private List<AttachmentRowDisplay> attachmentRows;
		// Cached
		private I item;

		public boolean isShow()
		{
			return show;
		}

		public void setShow(boolean show)
		{
			this.show = show;
		}

		public ItemId getItemId()
		{
			return itemId;
		}

		public void setItemId(ItemId itemId)
		{
			this.itemId = itemId;
		}

		public List<AttachmentRowDisplay> getAttachmentRows()
		{
			return attachmentRows;
		}

		public void setAttachmentRows(List<AttachmentRowDisplay> attachmentRows)
		{
			this.attachmentRows = attachmentRows;
		}

		public boolean isStructured()
		{
			return structured;
		}

		public void setStructured(boolean structured)
		{
			this.structured = structured;
		}

		public I getItem()
		{
			return item;
		}

		public void setItem(I item)
		{
			this.item = item;
		}
	}
}
