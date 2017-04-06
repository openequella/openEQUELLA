package com.tle.web.mimetypes.model;

import com.tle.web.sections.annotations.Bookmarked;

/*
 * @author aholland
 */
public class MimeTypesEditModel
{
	@Bookmarked
	private long editId;

	// Render time
	private String errorKey;

	public String getErrorKey()
	{
		return errorKey;
	}

	public void setErrorKey(String errorKey)
	{
		this.errorKey = errorKey;
	}

	public long getEditId()
	{
		return editId;
	}

	public void setEditId(long editId)
	{
		this.editId = editId;
	}
}
