package com.tle.web.qti;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.qti.QtiConstants;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.controls.universal.handlers.fileupload.UploadedFile;
import com.tle.web.controls.universal.handlers.fileupload.details.AbstractDetailsEditor;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;

@Bind
@NonNullByDefault
public class QtiTestAttachmentDetails
	extends
		AbstractDetailsEditor<QtiTestAttachmentDetails.QTITestAttachmentDetailsModel>
{
	@Override
	public SectionRenderable renderDetailsEditor(RenderContext context, DialogRenderOptions renderOptions,
		UploadedFile uploadedFile)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialiseFromUpload(SectionInfo info, UploadedFile uploadedFile, boolean resolved)
	{
		CustomAttachment qti = new CustomAttachment();
		qti.setType(QtiConstants.TEST_CUSTOM_ATTACHMENT_TYPE);

		String filepath = uploadedFile.getFilepath();
		qti.setUrl(filepath);
		qti.setMd5sum(uploadedFile.getMd5());

		// IMS details...
		qti.setDescription(uploadedFile.getDescription());

		// if( resolved )
		// {
		// String unzippedPath = FileUploadHandler.getUploadFilepath("_pkg");
		// String pkgInfoPath = uploadedFile.getFilepath();
		// WebRepository repo = handler.getDialogState().getRepository();
		// try
		// {
		// repo.unzipFile(uploadedFile.getFilepath(), unzippedPath, false);
		// uploadedFile.setExtractedPath(unzippedPath);
		// pkgInfoPath = unzippedPath;
		// }
		// catch( Exception e )
		// {
		// // This is bad.. Why unzip something that's not a zip?
		// }
		// PackageInfo pkgInfo = repo.readPackageInfo(pkgInfoPath);
		// final String name = pkgInfo.getTitle();
		// if( name != null )
		// {
		// attachment.setDescription(name);
		// }
		// if( type == FileType.SCORM )
		// {
		// attachment.setData(KEY_SCORM_VERSION, pkgInfo.getScormVersion());
		// }
		// }
		uploadedFile.setAttachment(qti);
	}

	@Override
	public void prepareForEdit(SectionInfo info, UploadedFile uploadedFile)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup(SectionInfo info, UploadedFile uploadedFile)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void commitEdit(SectionInfo info, UploadedFile ua, Attachment attachment)
	{
		// TODO Auto-generated method stub

	}

	@NonNullByDefault(false)
	public static class QTITestAttachmentDetailsModel extends AbstractDetailsEditor.Model
	{
	}
}
