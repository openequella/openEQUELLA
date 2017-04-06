package com.tle.web.bulk.executescript.model;

import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.events.BookmarkEvent;

@Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION)
public class BulkExecuteScriptModel
{
	private boolean validationRan = false;
	@Bookmarked
	private boolean validationErrors;
	@Bookmarked
	private String errorMessage;

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public boolean isValidationErrors()
	{
		return validationErrors;
	}

	public void setValidationErrors(boolean validationErrors)
	{
		this.validationErrors = validationErrors;
	}

	public boolean isValidationRan()
	{
		return validationRan;
	}

	public void setValidationRan(boolean validationRan)
	{
		this.validationRan = validationRan;
	}

}