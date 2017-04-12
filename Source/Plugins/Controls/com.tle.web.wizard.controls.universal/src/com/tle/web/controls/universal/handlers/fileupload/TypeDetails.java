package com.tle.web.controls.universal.handlers.fileupload;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.controls.universal.handlers.FileUploadHandler;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface TypeDetails extends SectionId
{
	void onRegister(SectionTree tree, String parentId, FileUploadHandler handler);

	@Nullable
	SectionRenderable renderDetailsEditor(RenderContext context, DialogRenderOptions renderOptions,
		UploadedFile uploadedFile);

	void initialiseFromUpload(SectionInfo info, UploadedFile uploadedFile, boolean resolved);

	void prepareForEdit(SectionInfo info, UploadedFile uploadedFile);

	void cleanup(SectionInfo info, UploadedFile uploadedFile);

	void removeAttachment(SectionInfo info, Attachment attachment, boolean willBeReplaced);

	void setupDetailsForEdit(SectionInfo info, UploadedFile uploadedFile);

	boolean validateDetails(SectionInfo info, UploadedFile uploadedFile);

	void commitNew(SectionInfo info, UploadedFile uploadedFile, @Nullable String replacementUuid);

	void commitEdit(SectionInfo info, UploadedFile ua, Attachment attachment);

	boolean isShowViewLink();

	FileUploadHandler getFileUploadHandler();

}
