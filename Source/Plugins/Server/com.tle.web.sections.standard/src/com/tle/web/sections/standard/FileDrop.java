package com.tle.web.sections.standard;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.model.HtmlFileDropState;

import static com.tle.web.sections.standard.FileUpload.isMultipartRequest;

/**
 * @author Dongsheng Cai
 */
@NonNullByDefault
public class FileDrop extends AbstractEventOnlyComponent<HtmlFileDropState>
{
	public FileDrop()
	{
		super(RendererConstants.DIV);
	}

	@Override
	public Class<HtmlFileDropState> getModelClass()
	{
		return HtmlFileDropState.class;
	}

	public void setFallbackFormId(SectionInfo info, String id)
	{
		getState(info).setFallbackFormId(id);
	}

	public void setFallbackHiddenIds(SectionInfo info, Collection<String> ids)
	{
		getState(info).setFallbackHiddenIds(ids);
	}

	public void setMaxFiles(SectionInfo info, int maxFiles)
	{
		getState(info).setMaxFiles(maxFiles);
	}

	public void setAjaxBeforeUpload(SectionInfo info, JSAssignable ajaxBeforeFunc)
	{
		getState(info).setAjaxBeforeUpload(ajaxBeforeFunc);
	}

	public void setUploadFinishedCallback(SectionInfo info, JSAssignable uploadFinishedCallback)
	{
		getState(info).setUploadFinishedCallback(uploadFinishedCallback);
	}

	public void setAjaxAfterUpload(SectionInfo info, JSAssignable ajaxAfterUpload)
	{
		getState(info).setAjaxAfterUpload(ajaxAfterUpload);
	}

	/**
	 * @param info
	 * @param js Must be 2 param method: uuid, filename
	 */
	public void setAjaxMethod(SectionInfo info, JSCallable js)
	{
		getState(info).setAjaxMethod(js);
	}

	public void setUploadId(SectionInfo info, String uploadId)
	{
		getState(info).setUploadId(uploadId);
	}

	public void setBanned(SectionInfo info, Collection<String> banned)
	{
		getState(info).setBanned(banned);
	}

	public void setAllowedMimetypes(SectionInfo info, Collection<String> allowedMimes)
	{
		getState(info).setAllowedMimetypes(allowedMimes);
	}

	public void setMimetypeErrorMessage(SectionInfo info, String message)
	{
		getState(info).setMimetypeErrorMessage(message);
	}

	public void setMaxFilesizeErrorMessage(SectionInfo info, String message)
	{
		getState(info).setMaxFilesizeErrorMessage(message);
	}

	public void setMaxFilesize(SectionInfo info, int maxSize)
	{
		getState(info).setMaxFilesize(maxSize);
	}

	public void setProgressAreaId(SectionInfo info, @Nullable String id)
	{
		getState(info).setProgressAreaId(id);
	}

	@Nullable
	public Part getMultipartFile(SectionInfo info) {
		HttpServletRequest request = info.getRequest();
		if (!isMultipartRequest(request))
		{
			return null;
		}
		try {
			return request.getPart("files[]");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ServletException e) {
			throw new RuntimeException(e);
		}
	}

	public long getFileSize(SectionInfo info)
	{
		Part file = getMultipartFile(info);
		if( file != null )
		{
			return file.getSize();
		}
		return -1;
	}

	@Nullable
	public InputStream getInputStream(SectionInfo info)
	{
		Part file = getMultipartFile(info);
		if( file != null )
		{
			try
			{
				return file.getInputStream();
			}
			catch( IOException e )
			{
				SectionUtils.throwRuntime(e);
			}
		}
		return null;
	}
}
