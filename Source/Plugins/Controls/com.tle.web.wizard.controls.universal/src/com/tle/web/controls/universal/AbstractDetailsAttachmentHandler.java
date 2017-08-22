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

package com.tle.web.controls.universal;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.Pair;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.mimetypes.section.FakeMimeTypeResource;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

/**
 * An abstract {@link AttachmentHandler} which has an "add" page and a "details"
 * page. Multiple attachments can be created by the "add" page, however if only
 * 1 is created the handler will go to the "details" page.
 * <p>
 * Attachments themselves are represented by a generic type UA extends
 * {@link UniversalAttachment}, which can have more state than just the
 * {@link Attachment} object itself. Most handlers don't need the extra state
 * ability and should extend {@link BasicAbstractAttachmentHandler}.
 * 
 * @author jolz
 * @param <M>
 * @param <UA>
 */
@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractDetailsAttachmentHandler<M extends AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel, UA extends UniversalAttachment>
	extends
		AbstractAttachmentHandler<M>
{
	private static final String KEY_FIELD_DISPLAYNAME = "displayName";

	@PlugKey("handlers.abstract.error.blank")
	private static Label LABEL_ERROR_BLANK;

	@PlugKey("handlers.abstract.viewer.default")
	private static String DEFAULT_VIEWER;

	@PlugKey("details.type")
	private static Label ATTACHMENT_TYPE;

	@Component
	private TextField displayName;
	@Component
	private Checkbox previewCheckBox;
	@Component
	private Checkbox restrictCheckbox;
	@Component
	private SingleSelectionList<NameValue> viewers;
	@Component(name = "d")
	private Table detailTable;

	@Inject
	private ViewItemService viewItemService;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	/**
	 * Get the attachment type for the attachment being handled. Used by viewer
	 * list
	 *
	 * @param info TODO
	 * @return the mime type of the current attachment.
	 */
	protected abstract String getMimeType(SectionInfo info);

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		viewers.setListModel(new ViewersListModel());
	}

	@Override
	public void createNew(SectionInfo info)
	{
		displayName.setValue(info, null);
		viewers.setSelectedStringValue(info, null);
		getModel(info).setEditDetails(false);
	}

	@Override
	public void loadForEdit(SectionInfo info, Attachment attachment)
	{
		dialogState.setAttribute(info, this,
			Collections.singletonList(createUniversalAttachmentForEdit(info, (Attachment) attachment.clone())));
		setupDetailEditing(info);
	}

	/**
	 * Create a {@link UniversalAttachment} for the given attachment which is
	 * about to be edited.
	 * 
	 * @param info
	 * @param attachment
	 * @return
	 */
	protected abstract UA createUniversalAttachmentForEdit(SectionInfo info, Attachment attachment);

	/**
	 * Render the "details" page. <br>
	 * Before calling this method the {@code showSave} flag of the
	 * {@code DialogRenderOptions} is set to true.
	 * 
	 * @param context
	 * @param renderOptions
	 * @return
	 */
	protected abstract SectionRenderable renderDetails(RenderContext context, DialogRenderOptions renderOptions);

	/**
	 * Render the "add" page. <br>
	 * If {@link #isOnePageAdd()} returns true, the showSave and showAddReplace
	 * flags are set to true on the {@code DialogRenderOptions}.
	 * 
	 * @param context
	 * @param renderOptions
	 * @return
	 */
	protected abstract SectionRenderable renderAdd(RenderContext context, DialogRenderOptions renderOptions);

	/**
	 * Create a list of new {@link UniversalAttachment} objects after validating
	 * the values on the "add" page. <br>
	 * If only 1 {@code UniversalAttachment} is returned from this method the
	 * handler will go to "details editing" page to edit the details for the
	 * single {@code UniversalAttachment}.
	 * 
	 * @param info
	 * @return
	 */
	protected abstract List<UA> createUniversalAttachments(SectionInfo info);

	/**
	 * Override this method if you need to delete other resources such as files.
	 */
	@Override
	public void remove(SectionInfo info, Attachment attachment, boolean willBeReplaced)
	{
		dialogState.removeAttachment(info, attachment);
		if( !willBeReplaced )
		{
			dialogState.removeMetadataUuid(info, attachment.getUuid());
		}
	}

	@Override
	public SectionRenderable render(RenderContext context, DialogRenderOptions renderOptions)
	{
		M model = getModel(context);
		if( model.isEditDetails() )
		{
			renderOptions.setShowSave(true);
			prepareEditModel(context);
			return renderDetails(context, renderOptions);
		}
		if( isOnePageAdd() )
		{
			renderOptions.setShowSave(true);
			renderOptions.setShowAddReplace(true);
		}

		return renderAdd(context, renderOptions);
	}

	/**
	 * Returns true if the handler has a single "add" page which always needs
	 * the add/replace button to show.
	 * 
	 * @return
	 */
	protected boolean isOnePageAdd()
	{
		return true;
	}

	/**
	 * Return true if the values on the "add" page
	 * 
	 * @param info
	 * @return
	 */
	protected boolean validateAddPage(SectionInfo info)
	{
		return true;
	}

	/**
	 * Return true the values on the "details" page are valid.
	 * 
	 * @param info
	 * @return
	 */
	protected boolean validateDetailsPage(SectionInfo info)
	{
		M model = getModel(info);
		if( Check.isEmpty(displayName.getValue(info)) )
		{
			model.addError(KEY_FIELD_DISPLAYNAME, LABEL_ERROR_BLANK);
			return false;
		}
		return true;
	}

	@Override
	public boolean validate(SectionInfo info)
	{
		M model = getModel(info);
		if( model.isEditDetails() )
		{
			return validateDetailsPage(info);
		}
		else if( validateAddPage(info) )
		{
			List<UA> attachments = createUniversalAttachments(info);
			storeUniversalAttachments(info, attachments);
			if( attachments.size() == 1 )
			{
				setupDetailEditing(info);
				return false;
			}
			if( !isMultipleAllowed(info) && attachments.size() > 1 )
			{
				throw new Error("Weren't meant to allow multiple attachments");
			}
			return true;
		}
		return false;
	}

	/**
	 * Override this if your "details" page needs other UI components to be set
	 * up.
	 * 
	 * @param info
	 */
	protected void setupDetailEditing(SectionInfo info)
	{
		getModel(info).setEditDetails(true);
		Attachment attachment = getDetailsAttachment(info);
		displayName.setValue(info, attachment.getDescription());
		viewers.setSelectedStringValue(info, attachment.getViewer());
		previewCheckBox.setChecked(info, attachment.isPreview());
		restrictCheckbox.setChecked(info, attachment.isRestricted());
	}

	@Override
	public void saveChanges(SectionInfo info, String replacementUuid)
	{
		if( getModel(info).isEditDetails() )
		{
			UA ua = getDetailsUniversalAttachment(info);
			saveUniversalDetails(info, ua, ua.getAttachment());
			addOrReplace(info, ua, replacementUuid);
		}
		else
		{
			List<UA> attachments = getStoredUniversalAttachments(info);
			for( UA attachment : attachments )
			{
				addOrReplace(info, attachment, replacementUuid);
				replacementUuid = null;
			}
		}
	}

	protected void saveUniversalDetails(SectionInfo info, UA ua, Attachment attachment)
	{
		saveDetailsToAttachment(info, attachment);
	}

	@Override
	public void saveEdited(SectionInfo info, Attachment attachment)
	{
		saveUniversalDetails(info, getDetailsUniversalAttachment(info), attachment);
	}

	/**
	 * Save the values from the "details" page into the given attachment.
	 * 
	 * @param info
	 * @param attachment
	 */
	protected void saveDetailsToAttachment(SectionInfo info, Attachment attachment)
	{
		attachment.setDescription(displayName.getValue(info));
		attachment.setPreview(getSettings().isAllowPreviews() && previewCheckBox.isChecked(info));
		attachment.setViewer(viewers.getSelectedValueAsString(info));
		attachment.setRestricted(restrictCheckbox.isChecked(info));

		ViewableResource viewableResource = attachmentResourceService.getViewableResource(info,
			dialogState.getViewableItem(info), attachment);
		if( viewableResource.isCustomThumb() )
		{
			// TODO: temporary fixed
			URL url = viewableResource.getThumbnailReference(info, null).getUrl();
			if( url != null )
			{
				attachment.setThumbnail(url.toString());
			}
		}
	}

	/**
	 * Modify the item and itemxml with the new {@link UniversalAttachment}.
	 * Only needs to be overridden if the Attachment has to make other changes
	 * beside the {@code Attachment} and UUID in the item xml.
	 * 
	 * @param info
	 * @param attachment
	 * @param replacementUuid
	 */
	protected void addOrReplace(SectionInfo info, UA attachment, @Nullable String replacementUuid)
	{
		Attachment realAttach = attachment.getAttachment();
		if( replacementUuid != null )
		{
			realAttach.setUuid(replacementUuid);
		}
		else
		{
			dialogState.addMetadataUuid(info, realAttach.getUuid());
		}
		dialogState.addAttachment(info, realAttach);
	}

	@EventHandlerMethod
	public void updateButtons(SectionInfo info)
	{
		getModel(info).setButtonUpdate(true);
	}

	public TextField getDisplayName()
	{
		return displayName;
	}

	protected UA getDetailsUniversalAttachment(SectionInfo info)
	{
		List<UA> attachments = getStoredUniversalAttachments(info);
		return attachments.get(0);
	}

	protected List<UA> getStoredUniversalAttachments(SectionInfo info)
	{
		return dialogState.getAttribute(info, this);
	}

	protected void storeUniversalAttachments(SectionInfo info, List<UA> attachments)
	{
		dialogState.setAttribute(info, this, attachments);
	}

	protected Attachment getDetailsAttachment(SectionInfo info)
	{
		return getDetailsUniversalAttachment(info).getAttachment();
	}

	public SectionRenderable getThumbnailUrlForAttachment(SectionInfo info, Attachment attachment)
	{
		return attachmentResourceService.getViewableResource(info, dialogState.getViewableItem(info), attachment)
			.createStandardThumbnailRenderer(new TextLabel(attachment.getDescription())).addClass("file-thumbnail");
	}

	public SectionRenderable getThumbnailUrlForFile(SectionInfo info, String filename, String mimeType)
	{
		return attachmentResourceService
			.createPathResource(info, dialogState.getViewableItem(info), filename, filename, mimeType, null)
			.createStandardThumbnailRenderer(new TextLabel(mimeType)).addClass("file-thumbnail");
	}

	/**
	 * Prepare the model for rendering the "details" page. Called before
	 * {@link #renderDetails(RenderContext, DialogRenderOptions)}.
	 * 
	 * @param info
	 */
	protected void prepareEditModel(SectionInfo info)
	{
		M model = getModel(info);
		// make sure the details table cannot be filtered
		detailTable.getState(info).setFilterable(false);
		model.setEditTitle(displayName.getValue(info));
		final Attachment attachment = getDetailsAttachment(info);
		model.setThumbnail(getThumbnailUrlForAttachment(info, attachment));
		model.setShowViewers(!(viewers.getListModel().getOptions(info).size() == 2));
		model.setShowPreview(settings.isAllowPreviews());
		model.setShowRestrict(canRestrictAttachments());
	}

	protected void addAttachmentDetail(SectionInfo info, Label label, Object detail)
	{
		TableState state = detailTable.getState(info);
		addRow(state, label, detail);
	}

	protected void addAttachmentDetails(SectionInfo info, List<AttachmentDetail> details)
	{
		TableState state = detailTable.getState(info);
		for( AttachmentDetail detail : details )
		{
			addRow(state, detail.getName(), detail.getDescription());
		}
	}

	private void addRow(TableState state, Label label, Object detail)
	{
		TableCell labelCell = new TableCell(label);
		labelCell.addClass("label");
		state.addRow(labelCell, detail);
	}

	public SingleSelectionList<NameValue> getViewers()
	{
		return viewers;
	}

	public UniversalSettings getSettings()
	{
		return settings;
	}

	public Table getDetailTable()
	{
		return detailTable;
	}

	public static class AbstractAttachmentHandlerModel
	{
		private final Map<String, Label> errors = Maps.newHashMap();
		@Bookmarked(name = "ed")
		private boolean editDetails;
		private String editTitle;
		private SectionRenderable thumbnail;
		private SectionRenderable viewlink;
		private String mimeType;
		private boolean showViewers;
		private boolean showPreview;
		private boolean showRestrict;
		private boolean buttonUpdate;
		private final Map<String, Pair<Label, Object>> specificDetail = new HashMap<String, Pair<Label, Object>>();

		public void addError(String key, Label errorMessage)
		{
			errors.put(key, errorMessage);
		}

		public String getMimeType()
		{
			return mimeType;
		}

		public void setMimeType(String mimeType)
		{
			this.mimeType = mimeType;
		}

		public String getEditTitle()
		{
			return editTitle;
		}

		public void setEditTitle(String editTitle)
		{
			this.editTitle = editTitle;
		}

		public SectionRenderable getThumbnail()
		{
			return thumbnail;
		}

		public void setThumbnail(SectionRenderable thumbnail)
		{
			this.thumbnail = thumbnail;
		}

		public SectionRenderable getViewlink()
		{
			return viewlink;
		}

		public void setViewlink(SectionRenderable viewlink)
		{
			this.viewlink = viewlink;
		}

		public void setShowViewers(boolean showViewers)
		{
			this.showViewers = showViewers;
		}

		public boolean isShowViewers()
		{
			return showViewers;
		}

		public Map<String, Pair<Label, Object>> getSpecificDetail()
		{
			return specificDetail;
		}

		public void addSpecificDetail(String key, Pair<Label, Object> detail)
		{
			specificDetail.put(key, detail);
		}

		public boolean isEditDetails()
		{
			return editDetails;
		}

		public void setEditDetails(boolean editDetails)
		{
			this.editDetails = editDetails;
		}

		public boolean isShowPreview()
		{
			return showPreview;
		}

		public void setShowPreview(boolean showPreview)
		{
			this.showPreview = showPreview;
		}

		public Map<String, Label> getErrors()
		{
			return errors;
		}

		public boolean isButtonUpdate()
		{
			return buttonUpdate;
		}

		public void setButtonUpdate(boolean buttonUpdate)
		{
			this.buttonUpdate = buttonUpdate;
		}

		public boolean isShowRestrict()
		{
			return showRestrict;
		}

		public void setShowRestrict(boolean showRestrict)
		{
			this.showRestrict = showRestrict;
		}
	}

	private class ViewersListModel extends DynamicHtmlListModel<NameValue>
	{
		@Override
		protected Iterable<NameValue> populateModel(SectionInfo info)
		{
			final String mimeType = getMimeType(info);
			return viewItemService.getEnabledViewers(info, new FakeMimeTypeResource(mimeType));
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		protected Option<NameValue> getTopOption()
		{
			return new NameValueOption(new BundleNameValue(DEFAULT_VIEWER, Constants.BLANK), null);
		}
	}

	@Override
	public void cancelled(SectionInfo info)
	{
		// no cleanup necessary usually
	}

	public Checkbox getPreviewCheckBox()
	{
		return previewCheckBox;
	}

	public Checkbox getRestrictCheckbox()
	{
		return restrictCheckbox;
	}
}
