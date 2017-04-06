package com.tle.web.controls.universal.handlers.fileupload.details;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Check;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.services.FileSystemService;
import com.tle.web.controls.universal.UniversalControlState;
import com.tle.web.controls.universal.handlers.FileUploadHandler;
import com.tle.web.controls.universal.handlers.fileupload.TypeDetails;
import com.tle.web.controls.universal.handlers.fileupload.UploadedFile;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractDetailsEditor<M extends AbstractDetailsEditor.Model> extends AbstractPrototypeSection<M>
	implements
		TypeDetails
{
	private static final String KEY_FIELD_DISPLAYNAME = "displayName";

	@PlugKey("handlers.abstract.error.blank")
	private static Label LABEL_ERROR_BLANK;

	@Inject
	private FileSystemService fileSystemService;

	@Component
	protected TextField displayName;
	@Component
	protected Checkbox previewCheckBox;
	@Component
	protected Checkbox restrictCheckbox;

	@EventFactory
	protected EventGenerator events;
	@ViewFactory(fixed = false)
	protected FreemarkerFactory viewFactory;

	/**
	 * It shouldn't be null, but can conceivably be if not registered correctly.
	 */
	@Nullable
	private FileUploadHandler handler;

	public TextField getDisplayName()
	{
		return displayName;
	}

	@Override
	public boolean isShowViewLink()
	{
		return true;
	}

	@Override
	public void onRegister(SectionTree tree, String parentId, FileUploadHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void setupDetailsForEdit(SectionInfo info, UploadedFile uploadedFile)
	{
		Attachment attachment = uploadedFile.getAttachment();
		displayName.setValue(info, attachment.getDescription());
		previewCheckBox.setChecked(info, attachment.isPreview());
		restrictCheckbox.setChecked(info, attachment.isRestricted());
	}

	public void saveDetailsToAttachment(SectionInfo info, UploadedFile uploadedFile, Attachment attachment)
	{
		attachment.setDescription(displayName.getValue(info));
		attachment.setPreview(getFileUploadHandler().getFileSettings().isAllowPreviews()
			&& previewCheckBox.isChecked(info));
		attachment.setRestricted(restrictCheckbox.isChecked(info));
	}

	@Override
	public boolean validateDetails(SectionInfo info, UploadedFile uploadedFile)
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
	public void removeAttachment(SectionInfo info, Attachment attachment, boolean willBeReplaced)
	{
		UniversalControlState dialogState = getFileUploadHandler().getDialogState();
		dialogState.removeAttachment(info, attachment);
		if( !willBeReplaced )
		{
			dialogState.removeMetadataUuid(info, attachment.getUuid());
		}
	}

	@Override
	public void commitNew(SectionInfo info, UploadedFile uploadedFile, @Nullable String replacementUuid)
	{
		UniversalControlState dialogState = getFileUploadHandler().getDialogState();
		Attachment realAttach = uploadedFile.getAttachment();
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

	@Override
	public FileUploadHandler getFileUploadHandler()
	{
		if( handler == null )
		{
			throw new Error("onRegister needs to be called on " + getClass().getName());
		}
		return handler;
	}

	protected void move(StagingFile staging, String filepath, String destpath)
	{
		move(staging, filepath, staging, destpath);
	}

	protected void move(StagingFile staging, String filepath, StagingFile stagingDest, String destpath)
	{
		if( !fileSystemService.move(staging, filepath, stagingDest, destpath) )
		{
			throw new RuntimeException("Couldn't move file from '" + filepath + "' to '" + destpath + "'");
		}
	}

	@NonNullByDefault(false)
	public static class Model
	{
		private final Map<String, Label> errors = Maps.newHashMap();
		private Label editTitle;
		private boolean showViewers;
		private boolean showPreview;
		private boolean showRestrict;

		public void addError(String key, Label errorMessage)
		{
			errors.put(key, errorMessage);
		}

		public Label getEditTitle()
		{
			return editTitle;
		}

		public void setEditTitle(Label editTitle)
		{
			this.editTitle = editTitle;
		}

		public boolean isShowViewers()
		{
			return showViewers;
		}

		public void setShowViewers(boolean showViewers)
		{
			this.showViewers = showViewers;
		}

		public boolean isShowPreview()
		{
			return showPreview;
		}

		public void setShowPreview(boolean showPreview)
		{
			this.showPreview = showPreview;
		}

		public boolean isShowRestrict()
		{
			return showRestrict;
		}

		public void setShowRestrict(boolean showRestrict)
		{
			this.showRestrict = showRestrict;
		}

		public Map<String, Label> getErrors()
		{
			return errors;
		}
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
