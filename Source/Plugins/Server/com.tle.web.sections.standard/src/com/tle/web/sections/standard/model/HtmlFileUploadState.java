package com.tle.web.sections.standard.model;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;

/**
 * @author Aaron
 */
public class HtmlFileUploadState extends HtmlComponentState
{
	private Bookmark ajaxUploadUrl;
	private JSAssignable ajaxBeforeUpload;
	private JSAssignable ajaxAfterUpload;
	private JSCallable errorCallback;
	private String uploadId;
	private int maxFilesize;

	public Bookmark getAjaxUploadUrl()
	{
		return ajaxUploadUrl;
	}

	public void setAjaxUploadUrl(Bookmark ajaxUploadUrl)
	{
		this.ajaxUploadUrl = ajaxUploadUrl;
	}

	public JSAssignable getAjaxBeforeUpload()
	{
		return ajaxBeforeUpload;
	}

	public void setAjaxBeforeUpload(JSAssignable ajaxBeforeUpload)
	{
		this.ajaxBeforeUpload = ajaxBeforeUpload;
	}

	public JSAssignable getAjaxAfterUpload()
	{
		return ajaxAfterUpload;
	}

	public void setAjaxAfterUpload(JSAssignable ajaxAfterUpload)
	{
		this.ajaxAfterUpload = ajaxAfterUpload;
	}

	public String getUploadId()
	{
		return uploadId;
	}

	public void setUploadId(String uploadId)
	{
		this.uploadId = uploadId;
	}

	public int getMaxFilesize()
	{
		return maxFilesize;
	}

	public void setMaxFilesize(int maxFilesize)
	{
		this.maxFilesize = maxFilesize;
	}

	public JSCallable getErrorCallback()
	{
		return errorCallback;
	}

	public void setErrorCallback(JSCallable errorCallback)
	{
		this.errorCallback = errorCallback;
	}
}
