package com.tle.web.sections.standard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.model.HtmlFileUploadState;

public class FileUpload extends AbstractDisablerComponent<HtmlFileUploadState>
{
	public FileUpload()
	{
		super(RendererConstants.FILE);
	}

	@Override
	public Class<HtmlFileUploadState> getModelClass()
	{
		return HtmlFileUploadState.class;
	}

	public long getFileSize(SectionInfo info)
	{
		MultipartFile file = getMultipartFile(info);
		if( file != null )
		{
			return file.getSize();
		}
		return -1;
	}

	public InputStream getInputStream(SectionInfo info)
	{
		MultipartFile file = getMultipartFile(info);
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

	public String getMimeType(SectionInfo info)
	{
		MultipartFile file = getMultipartFile(info);
		if( file != null )
		{
			return file.getContentType();
		}
		return null;
	}

	public String getFullFilename(SectionInfo info)
	{
		MultipartFile file = getMultipartFile(info);
		if( file != null )
		{
			return file.getOriginalFilename();
		}
		return null;
	}

	public String getFilename(SectionInfo info)
	{
		MultipartFile file = getMultipartFile(info);
		if( file != null )
		{
			return new File(file.getOriginalFilename()).getName();
		}
		return null;
	}

	private MultipartFile getMultipartFile(SectionInfo info)
	{
		HttpServletRequest request = info.getRequest();
		if( request instanceof MultipartHttpServletRequest )
		{
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			return multiRequest.getFile(getSectionId());
		}
		return null;
	}

	/**
	 * @param info
	 * @param ajaxUploadUrl Typically generated thusly: BookmarkAndModify
	 *            ajaxUploadUrl = new BookmarkAndModify(context,
	 *            events.getNamedModifier("processUpload"));
	 */
	public void setAjaxUploadUrl(SectionInfo info, Bookmark ajaxUploadUrl)
	{
		getState(info).setAjaxUploadUrl(ajaxUploadUrl);
	}

	public void setAjaxBeforeUpload(SectionInfo info, JSAssignable beforeFunc)
	{
		getState(info).setAjaxBeforeUpload(beforeFunc);
	}

	public void setAjaxAfterUpload(SectionInfo info, JSAssignable afterFunc)
	{
		getState(info).setAjaxAfterUpload(afterFunc);
	}

	public void setUploadId(SectionInfo info, String id)
	{
		getState(info).setUploadId(id);
	}

	public void setMaxFilesize(SectionInfo info, int maxSize)
	{
		getState(info).setMaxFilesize(maxSize);
	}

	public void setErrorCallback(SectionInfo info, JSCallable errorFunc)
	{
		getState(info).setErrorCallback(errorFunc);
	}

}
