package com.tle.web.sections.standard.model;

import java.util.Collection;

import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;

public class HtmlFileDropState extends HtmlComponentState
{
	private JSAssignable ajaxBeforeUpload;
	private JSAssignable uploadFinishedCallback;
	private JSAssignable ajaxAfterUpload;
	private JSCallable ajaxMethod;
	private String uploadId;
	private String fallbackFormId;
	private String progressAreaId;
	private Collection<String> fallbackHiddenIds;
	private Collection<String> banned;
	private Collection<String> allowedMimes;
	private String mimeErrorMessage;
	private String maxFilesizeErrorMessage;
	private int maxFilesize;

	private int maxFiles;

	public JSAssignable getAjaxAfterUpload()
	{
		return ajaxAfterUpload;
	}

	public void setAjaxAfterUpload(JSAssignable ajaxAfterUpload)
	{
		this.ajaxAfterUpload = ajaxAfterUpload;
	}

	public String getProgressAreaId()
	{
		return progressAreaId;
	}

	public void setProgressAreaId(String progressAreaId)
	{
		this.progressAreaId = progressAreaId;
	}

	public void setMaxFiles(int maxFiles)
	{
		this.maxFiles = maxFiles;
	}

	public int getMaxFiles()
	{
		return maxFiles;
	}

	public void setAjaxMethod(JSCallable js)
	{
		this.ajaxMethod = js;
	}

	public JSCallable getAjaxMethod()
	{
		return this.ajaxMethod;
	}

	public JSAssignable getAjaxBeforeUpload()
	{
		return ajaxBeforeUpload;
	}

	public void setAjaxBeforeUpload(JSAssignable ajaxBeforeUpload)
	{
		this.ajaxBeforeUpload = ajaxBeforeUpload;
	}

	public JSAssignable getUploadFinishedCallback()
	{
		return uploadFinishedCallback;
	}

	public void setUploadFinishedCallback(JSAssignable ajaxAfterUpload)
	{
		this.uploadFinishedCallback = ajaxAfterUpload;
	}

	public String getFallbackFormId()
	{
		return fallbackFormId;
	}

	public void setFallbackFormId(String fallbackFormId)
	{
		this.fallbackFormId = fallbackFormId;
	}

	public Collection<String> getFallbackHiddenIds()
	{
		return fallbackHiddenIds;
	}

	public void setFallbackHiddenIds(Collection<String> fallbackHiddenIds)
	{
		this.fallbackHiddenIds = fallbackHiddenIds;
	}

	public Collection<String> getBanned()
	{
		return banned;
	}

	public void setBanned(Collection<String> banned)
	{
		this.banned = banned;
	}

	public String getUploadId()
	{
		return uploadId;
	}

	public void setUploadId(String uploadId)
	{
		this.uploadId = uploadId;
	}

	public void setAllowedMimetypes(Collection<String> allowedMimes)
	{
		this.allowedMimes = allowedMimes;
	}

	public Collection<String> getAllowedMimetypes()
	{
		return allowedMimes;
	}

	public void setMimetypeErrorMessage(String message)
	{
		this.mimeErrorMessage = message;
	}

	public String getMimetypeErrorMessage()
	{
		return mimeErrorMessage;
	}

	public int getMaxFilesize()
	{
		return maxFilesize;
	}

	public void setMaxFilesize(int maxFilesize)
	{
		this.maxFilesize = maxFilesize;
	}

	public String getMaxFilesizeErrorMessage()
	{
		return maxFilesizeErrorMessage;
	}

	public void setMaxFilesizeErrorMessage(String maxFilesizeErrorMessage)
	{
		this.maxFilesizeErrorMessage = maxFilesizeErrorMessage;
	}
}
