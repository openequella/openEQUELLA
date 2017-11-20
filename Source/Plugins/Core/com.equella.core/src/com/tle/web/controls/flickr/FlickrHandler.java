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

package com.tle.web.controls.flickr;

import java.util.List;

import javax.inject.Inject;

import com.flickr4java.flickr.photos.Photo;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.NameValueExtra;
import com.tle.common.Pair;
import com.tle.common.wizard.controls.universal.handlers.FlickrSettings;
import com.tle.core.guice.Bind;
import com.tle.web.controls.universal.AbstractDetailsAttachmentHandler;
import com.tle.web.controls.universal.AttachmentHandlerLabel;
import com.tle.web.controls.universal.BasicAbstractAttachmentHandler;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.RegistrationController;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionNode;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.events.InfoEventListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.DefaultSectionTree;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

/**
 * A class to handle Flickr pages. First cut is to present the entire
 * www.flickr.com url (the image's home page on Flickr) rather than isolate the
 * image.
 * 
 * @author Larry. Based on the YouTube plugin.
 */
@SuppressWarnings("nls")
@Bind
@NonNullByDefault
public class FlickrHandler extends BasicAbstractAttachmentHandler<FlickrHandler.FlickrHandlerModel>
	implements
		InfoEventListener
{
	@PlugKey("flickr.name")
	private static Label NAME_LABEL;
	@PlugKey("flickr.description")
	private static Label DESCRIPTION_LABEL;
	@PlugKey("flickr.add.title")
	private static Label ADD_TITLE_LABEL;
	@PlugKey("flickr.edit.title")
	private static Label EDIT_TITLE_LABEL;

	@PlugKey("flickr.details.viewlink")
	private static Label VIEW_LINK_LABEL;

	@Inject
	private RegistrationController controller;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private SectionNode flickrRoot;

	private DefaultSectionTree flickrTree;
	private FlickrLayoutSection flickrLayoutSection;
	private FlickrQuerySection flickrQuerySection;
	private FlickrSearchResultsSection flickrSearchResultsSection;

	public FlickrQuerySection getFlickrQuerySection()
	{
		return flickrQuerySection;
	}

	public FlickrSearchResultsSection getFlickrSearchResultsSection()
	{
		return flickrSearchResultsSection;
	}

	@Override
	public String getHandlerId()
	{
		return "flickrHandler";
	}

	@Override
	public void registered(final String id, SectionTree tree)
	{
		super.registered(id, tree);

		String rootId = id + "fl";
		flickrTree = new DefaultSectionTree(controller, new SectionNode(rootId));
		flickrTree.registerSections(flickrRoot, rootId);

		flickrLayoutSection = flickrTree.lookupSection(FlickrLayoutSection.class, null);
		flickrQuerySection = flickrTree.lookupSection(FlickrQuerySection.class, flickrLayoutSection);

		flickrSearchResultsSection = flickrTree.lookupSection(FlickrSearchResultsSection.class, flickrLayoutSection);
		flickrSearchResultsSection.setFlickrHandler(this);
		flickrSearchResultsSection.setUpdateHandler(new StatementHandler(dialogState.getDialog().getFooterUpdate(tree,
			events.getEventHandler("updateButtons"))));
		flickrQuerySection.setDialogFooterId(dialogState.getDialog().getFooterId());

		flickrTree.treeFinished();
	}

	@Override
	public AttachmentHandlerLabel getLabel()
	{
		return new AttachmentHandlerLabel(NAME_LABEL, DESCRIPTION_LABEL);
	}

	@Override
	public boolean supports(IAttachment attachment)
	{
		if( attachment instanceof CustomAttachment )
		{
			CustomAttachment ca = (CustomAttachment) attachment;
			return FlickrUtils.ATTACHMENT_TYPE.equals(ca.getType());
		}
		return false;
	}

	@Override
	public Label getTitleLabel(RenderContext context, boolean editing)
	{
		return editing ? EDIT_TITLE_LABEL : ADD_TITLE_LABEL;
	}

	@Override
	protected SectionRenderable renderAdd(RenderContext context, DialogRenderOptions renderOptions)
	{
		TemplateResult tr = renderToTemplate(context, flickrLayoutSection.getSectionId());
		renderOptions.setShowSave(!Check.isEmpty(flickrSearchResultsSection.getResults().getSelectedValuesAsStrings(
			context)));
		return tr.getNamedResult(context, OneColumnLayout.BODY);
	}

	@Override
	protected List<Attachment> createAttachments(SectionInfo info)
	{
		List<Attachment> attachments = Lists.newArrayList();
		List<Photo> photos = flickrSearchResultsSection.getResults().getSelectedValues(info);

		for( Photo photo : photos )
		{

			CustomAttachment a = new CustomAttachment();
			a.setType(FlickrUtils.ATTACHMENT_TYPE);
			a.setData(FlickrUtils.PROPERTY_THUMB_URL, photo.getThumbnailUrl());

			a.setDescription(photo.getTitle());
			a.setData(FlickrUtils.PROPERTY_ID, photo.getId());

			String hw = FlickrUtils.describePhotoSize(photo);
			if( !Check.isEmpty(hw) )
			{
				a.setData(FlickrUtils.PROPERTY_IMAGE_SIZE, hw);
			}
			a.setData(FlickrUtils.PROPERTY_SHOW_URL, photo.getUrl());

			// there may or may not be a 'real' name, but there should always be
			// a
			// username
			String ownerName = photo.getOwner() != null ? photo.getOwner().getRealName() : "";
			if( !Check.isEmpty(ownerName) )
			{
				ownerName += "(" + photo.getOwner().getUsername() + ")";
			}
			else
			{
				ownerName = photo.getOwner().getUsername();
			}

			if( !Check.isEmpty(ownerName) )
			{
				a.setData(FlickrUtils.PROPERTY_AUTHOR, ownerName);
			}

			a.setData(FlickrUtils.PROPERTY_DATE_POSTED, photo.getDatePosted());
			a.setData(FlickrUtils.PROPERTY_DATE_TAKEN, photo.getDateTaken());

			String mediumUrl = photo.getMediumUrl();
			if( !Check.isEmpty(mediumUrl) )
			{
				a.setData(FlickrUtils.PROPERTY_MEDIUM_URL, mediumUrl);
			}

			String rawPhotoLicence = photo.getLicense();
			if( !Check.isEmpty(rawPhotoLicence) )
			{
				a.setData(FlickrUtils.PROPERTY_LICENCE_KEY, rawPhotoLicence);
				for( NameValueExtra nve : flickrSearchResultsSection.getAllLicenceValues() )
				{
					if( rawPhotoLicence.equals(nve.getValue()) )
					{
						a.setData(FlickrUtils.PROPERTY_LICENCE_CODE, nve.getName());
						a.setData(FlickrUtils.PROPERTY_LICENCE_NAME, nve.getExtra());
						break; // from .. for (NameValueExtra nve ...)
					}
				}
			}
			attachments.add(a);
		}
		return attachments;
	}

	@Override
	protected SectionRenderable renderDetails(RenderContext context, DialogRenderOptions renderOptions)
	{
		final FlickrHandlerModel model = getModel(context);
		// Common Details
		final Attachment a = getDetailsAttachment(context);
		ItemSectionInfo itemInfo = context.getAttributeForClass(ItemSectionInfo.class);
		ViewableResource resource = attachmentResourceService.getViewableResource(context, itemInfo.getViewableItem(),
			a);
		addAttachmentDetails(context, resource.getCommonAttachmentDetails());

		// Additional Details
		String embedUrl = (String) a.getData(FlickrUtils.PROPERTY_MEDIUM_URL);
		// Haven't yet seen a case where Medium URL didn't exist, but there's
		// always
		// a first time ...
		if( Check.isEmpty(embedUrl) )
		{
			embedUrl = (String) a.getData(FlickrUtils.PROPERTY_THUMB_URL);
		}

		if( embedUrl != null && embedUrl.length() > 0 )
		{
			String embedStr = "<iframe class='preview' src='" + embedUrl
				+ "' frameborder='0' allowfullscreen></iframe>";
			model.addSpecificDetail("embed", new Pair<Label, Object>(null, embedStr));
		}

		String descr = a.getDescription();
		if( !Check.isEmpty(descr) )
		{
			descr = FlickrUtils.formatToSize(descr);
			model.addSpecificDetail("description", new Pair<Label, Object>(new TextLabel(""), descr));
		}

		// View link
		HtmlLinkState linkState = new HtmlLinkState(VIEW_LINK_LABEL, new SimpleBookmark(
			(String) a.getData(FlickrUtils.PROPERTY_SHOW_URL)));
		linkState.setTarget(HtmlLinkState.TARGET_BLANK);
		model.setViewlink(new LinkRenderer(linkState));
		return viewFactory.createResult("edit-flickr.ftl", this);
	}

	public FlickrSettings getFlickrSettings()
	{
		return new FlickrSettings(getSettings());
	}

	@Override
	public Class<FlickrHandlerModel> getModelClass()
	{
		return FlickrHandlerModel.class;
	}

	@Override
	public void handleInfoEvent(MutableSectionInfo info, boolean removed, boolean processParameters)
	{
		if( !removed )
		{
			info.getAttributeForClass(MutableSectionInfo.class).addTreeToBottom(flickrTree, processParameters);
		}
	}

	public static class FlickrHandlerModel extends AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel
	{
		/**
		 * Provide for a warning message for soft errors, specifically, user not
		 * found
		 */
		private String warningMsg;
		private Boolean noResult;

		public String getWarningMsg()
		{
			return warningMsg;
		}

		public void setWarningMsg(String warningMsg)
		{
			this.warningMsg = warningMsg;
		}

		public Boolean getNoResult()
		{
			return noResult;
		}

		public void setNoResult(boolean noResult)
		{
			this.noResult = noResult;
		}
	}

	@Override
	protected boolean validateAddPage(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getMimeType(SectionInfo info)
	{
		return FlickrUtils.MIME_TYPE;
	}
}
