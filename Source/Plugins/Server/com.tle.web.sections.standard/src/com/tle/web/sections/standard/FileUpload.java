package com.tle.web.sections.standard;

import com.tle.web.sections.standard.model.HtmlFileUploadState;

public class FileUpload extends AbstractFileUpload<HtmlFileUploadState>
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
}
