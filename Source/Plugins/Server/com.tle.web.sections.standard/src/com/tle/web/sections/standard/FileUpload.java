package com.tle.web.sections.standard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import com.google.common.base.Charsets;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.model.HtmlFileUploadState;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;

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
		Part file = getMultipartFile(info);
		if( file != null )
		{
			return file.getSize();
		}
		return -1;
	}

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

	public String getMimeType(SectionInfo info)
	{
		Part file = getMultipartFile(info);
		if( file != null )
		{
			return file.getContentType();
		}
		return null;
	}

	public String getFullFilename(SectionInfo info)
	{
		Part file = getMultipartFile(info);
		if( file != null )
		{
			return file.getSubmittedFileName();
		}
		return null;
	}

	public String getFilename(SectionInfo info)
	{
		Part file = getMultipartFile(info);
		if( file != null )
		{
			return new File(file.getSubmittedFileName()).getName();
		}
		return null;
	}

	public static boolean isMultipartRequest(HttpServletRequest request)
	{
		String contentType = request.getContentType();
		return contentType != null && contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/");
	}

	public Part getMultipartFile(SectionInfo info)
	{
		HttpServletRequest request = info.getRequest();
		if (!isMultipartRequest(request))
		{
			if (request.getHeader("X_FILENAME") != null) {
				return new Part() {

					@Override
					public InputStream getInputStream() throws IOException {
						return request.getInputStream();
					}

					@Override
					public String getContentType() {
						return request.getContentType();
					}

					@Override
					public String getName() {
						String base64 = request.getHeader("X_FILENAME");
						return new String(Base64.getDecoder().decode(base64), Charsets.UTF_8);
					}

					@Override
					public String getSubmittedFileName() {
						return getName();
					}

					@Override
					public long getSize() {
						return request.getContentLength();
					}

					@Override
					public void write(String fileName) throws IOException {

					}

					@Override
					public void delete() throws IOException {

					}

					@Override
					public String getHeader(String name) {
						return null;
					}

					@Override
					public Collection<String> getHeaders(String name) {
						return Collections.emptyList();
					}

					@Override
					public Collection<String> getHeaderNames() {
						return Collections.emptyList();
					}
				};
			}
			return null;
		}
		try {
			return request.getPart(getSectionId());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ServletException e) {
			throw new RuntimeException(e);
		}
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

	public void setValidateFile(SectionInfo info, JSAssignable validateFunc)
	{
		getState(info).setValidateFile(validateFunc);
	}

	public void setAjaxAfterUpload(RenderEventContext context, JSAssignable function) {
		throw new Error("FIXME");
	}
}
