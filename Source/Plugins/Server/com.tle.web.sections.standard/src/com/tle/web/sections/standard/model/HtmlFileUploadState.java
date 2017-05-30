package com.tle.web.sections.standard.model;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;

/**
 * @author Aaron, Doolse
 */
public class HtmlFileUploadState extends HtmlComponentState
{
	private Bookmark ajaxUploadUrl;
	private JSAssignable validateFile;

	public Bookmark getAjaxUploadUrl()
	{
		return ajaxUploadUrl;
	}

	public void setAjaxUploadUrl(Bookmark ajaxUploadUrl)
	{
		this.ajaxUploadUrl = ajaxUploadUrl;
	}

	public JSAssignable getValidateFile()
	{
		return validateFile;
	}

	public void setValidateFile(JSAssignable validateFile)
	{
		this.validateFile = validateFile;
	}

}
