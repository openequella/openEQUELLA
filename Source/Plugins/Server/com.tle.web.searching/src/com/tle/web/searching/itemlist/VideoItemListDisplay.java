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

package com.tle.web.searching.itemlist;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.collection.AttachmentConfigConstants;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemResolver;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.TLEAclManager;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.resources.ResourcesService;
import com.tle.web.searching.VideoPreviewRenderer;
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
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;
import com.tle.web.viewitem.FilestoreContentFilter;
import com.tle.web.viewitem.service.FileFilterService;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class VideoItemListDisplay extends AbstractPrototypeSection<VideoItemListDisplay.VideoItemListDisplayModel>
	implements
		ItemlikeListEntryExtension<Item, StandardItemListEntry>,
		HtmlRenderer
{
	private static final IncludeFile INCLUDE = new IncludeFile(ResourcesService.getResourceHelper(
		VideoItemListDisplay.class).url("scripts/videoresults.js"));
	private static final JSCallAndReference VIDEO_CLASS = new ExternallyDefinedFunction("VideoResults", INCLUDE);

	private static final ExternallyDefinedFunction SETUP_HOVER_FUNCTION = new ExternallyDefinedFunction(VIDEO_CLASS,
		"setupHover", 2, INCLUDE);
	private static final ExternallyDefinedFunction AJAX_SUCCESS_FUNCTION = new ExternallyDefinedFunction(VIDEO_CLASS,
		"videoAjaxSuccess", 1, INCLUDE);

	@PlugKey("preview.not.available")
	private static Label NOT_AVAILABLE;
	@PlugKey("preview.loading")
	private static Label LOADING_PREVIEW;

	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ItemResolver itemResolver;
	@Inject
	private ViewableItemResolver viewableItemResolver;
	@Inject
	private PluginTracker<VideoPreviewRenderer> previewRenderers;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private FileFilterService filters;
	@AjaxFactory
	private AjaxGenerator ajax;
	@EventFactory
	private EventGenerator events;
	private UpdateDomEvent updateEvent;

	@Nullable
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		VideoItemListDisplayModel model = getModel(context);
		String itemUuid = model.getItemUuid();
		int itemVersion = model.getItemVersion();
		if( itemUuid != null )
		{
			Item item = itemResolver.getItem(new ItemId(itemUuid, model.getItemVersion()), null);
			ViewableItem<?> vitem = viewableItemResolver.createViewableItem(item, null);
			IAttachment attachment = vitem.getAttachmentByUuid(model.getAttachmentUuid());
			SectionRenderable videoPreview = addVideoPreview(context, (Attachment) attachment, vitem);
			if( videoPreview != null && canUserViewAttachment(item, attachment) )
			{
				return new AjaxCaptureRenderer("preview" + itemUuid + itemVersion, videoPreview);
			}

			ViewableResource viewableResource = attachmentResourceService.getViewableResource(context, vitem,
				attachment);

			ImageRenderer thumb = viewableResource.createGalleryThumbnailRenderer(NOT_AVAILABLE);
			DivRenderer textDiv = new DivRenderer("info", NOT_AVAILABLE);
			DivRenderer tag = new DivRenderer("temp-thumb", null);
			tag.setNestedRenderable(CombinedRenderer.combineMultipleResults(thumb, textDiv));

			return new AjaxCaptureRenderer("preview" + itemUuid + itemVersion, playerTagRenderer(tag));

		}
		return null;
	}

	private boolean canUserViewAttachment(Item item, IAttachment attach)
	{
		boolean canView = true;

		for( FilestoreContentFilter filter : filters.getFilters() )
		{
			canView = filter.canView(item, attach);
			if( !canView )
			{
				break;
			}
		}
		if( attach.isRestricted()
			&& aclManager.filterNonGrantedPrivileges(item,
				Collections.singleton(AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS)).isEmpty() )
		{
			canView = false;
		}
		return canView;
	}

	@Override
	public ProcessEntryCallback<Item, StandardItemListEntry> processEntries(RenderContext context,
		List<StandardItemListEntry> entries, ListSettings<StandardItemListEntry> listSettings)
	{
		// Don't highlight matching key words after searching.
		listSettings.setHilightedWords(null);

		// Maybe don't prerender unused players? It's quicker (as in, quicker
		// for the server
		// to run) to do it this way though.
		final List<VideoPreviewRenderer> renderers = previewRenderers.getBeanList();
		for( VideoPreviewRenderer renderer : renderers )
		{
			renderer.preRender(context.getPreRenderContext());
		}

		return new ProcessEntryCallback<Item, StandardItemListEntry>()
		{
			@Override
			public void processEntry(StandardItemListEntry entry)
			{
				addVideoThumbs(context, entry);
			}
		};
	}

	private void addVideoThumbs(RenderContext context, StandardItemListEntry entry)
	{
		Item item = entry.getItem();
		for( ViewableResource resource : entry.getViewableResources() )
		{
			Attachment attachment = (Attachment) resource.getAttachment();
			String mimeType = mimeTypeService.getMimeEntryForAttachment(attachment);
			boolean videoType = isVideoType(mimeType);
			if( videoType )
			{
				TagState tag = thumbnailHoverTag(item, attachment.getUuid());
				entry.addThumbnail(resource.createVideoThumbnailRenderer(entry.getTitleLabel(), tag));

				ImageRenderer thumb = resource.createGalleryThumbnailRenderer(LOADING_PREVIEW);
				DivRenderer textDiv = new DivRenderer("info", LOADING_PREVIEW);
				DivRenderer tempThumbDiv = new DivRenderer("temp-thumb", null);
				tempThumbDiv.setNestedRenderable(CombinedRenderer.combineMultipleResults(thumb, textDiv));
				entry.addExtras(tempThumbDiv);
				break;
			}
		}
	}

	private boolean isVideoType(String mimeType)
	{
		List<VideoPreviewRenderer> renderers = previewRenderers.getBeanList();
		for( VideoPreviewRenderer renderer : renderers )
		{
			if( renderer.supports(mimeType) )
			{
				return true;
			}
		}
		return false;
	}

	private TagState thumbnailHoverTag(Item item, String thumbUuid)
	{
		TagState tag = new HtmlComponentState();
		JQuerySelector tagSelector = Jq.$(tag);

		JSAssignable ajaxSuccess = Js.function(Js.call_s(AJAX_SUCCESS_FUNCTION, tagSelector));
		final JSCallAndReference successCarf = CallAndReferenceFunction.get(ajaxSuccess, tag);

		final UpdateDomFunction updateFunc = new UpdateDomFunction(updateEvent, "preview" + item.getUuid()
			+ item.getVersion(), ajax.getEffectFunction(EffectType.ACTIVITY, AjaxGenerator.URL_SPINNER_LOADING),
			successCarf);
		final JSCallAndReference updateCarf = CallAndReferenceFunction.get(updateFunc, tag);

		JSAssignable loadPlayer = Js.function(Js.call_s(updateCarf, item.getUuid(), item.getVersion(), thumbUuid));
		tag.addReadyStatements(Js.statement(Js.call(SETUP_HOVER_FUNCTION, tagSelector, loadPlayer)));

		return tag;
	}

	@Nullable
	private SectionRenderable addVideoPreview(RenderContext context, Attachment attachment, ViewableItem<?> vitem)
	{
		String mimeType = mimeTypeService.getMimeEntryForAttachment(attachment);
		List<VideoPreviewRenderer> renderers = previewRenderers.getBeanList();
		for( VideoPreviewRenderer renderer : renderers )
		{
			SectionRenderable result = renderer.renderPreview(context, attachment, vitem, mimeType);
			if( result != null )
			{
				return playerTagRenderer(result);
			}
		}
		return null;
	}

	private TagRenderer playerTagRenderer(SectionRenderable tag)
	{
		DivRenderer div = new DivRenderer("video-player", null);
		div.setNestedRenderable(tag);
		return div;
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
		updateEvent = UpdateDomEvent.register(tree, this, events.getEventHandler("loadPlayer"), "");
	}

	@EventHandlerMethod
	public void loadPlayer(SectionInfo info, String itemUuid, int itemVersion, String attachmentUuid)
	{
		final AjaxRenderContext context = info.getAttributeForClass(AjaxRenderContext.class);
		if( context != null )
		{
			context.addAjaxDivs("preview" + itemUuid + itemVersion);
		}
		VideoItemListDisplayModel model = getModel(info);
		model.setItemUuid(itemUuid);
		model.setItemVersion(itemVersion);
		model.setAttachmentUuid(attachmentUuid);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new VideoItemListDisplayModel();
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}

	@NonNullByDefault(false)
	public static class VideoItemListDisplayModel
	{
		private String itemUuid;
		private int itemVersion;
		private String attachmentUuid;

		public String getItemUuid()
		{
			return itemUuid;
		}

		public void setItemUuid(String itemUuid)
		{
			this.itemUuid = itemUuid;
		}

		public void setAttachmentUuid(String attachmentUuid)
		{
			this.attachmentUuid = attachmentUuid;
		}

		public String getAttachmentUuid()
		{
			return attachmentUuid;
		}

		public void setItemVersion(int itemVersion)
		{
			this.itemVersion = itemVersion;
		}

		public int getItemVersion()
		{
			return itemVersion;
		}
	}
}
