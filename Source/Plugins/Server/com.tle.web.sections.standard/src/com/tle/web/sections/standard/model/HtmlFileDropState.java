package com.tle.web.sections.standard.model;

import java.util.Collection;

import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;

public class HtmlFileDropState extends HtmlFileUploadState
{
	private int maxFiles;

	public void setMaxFiles(int maxFiles)
	{
		this.maxFiles = maxFiles;
	}

	public int getMaxFiles()
	{
		return maxFiles;
	}
}
