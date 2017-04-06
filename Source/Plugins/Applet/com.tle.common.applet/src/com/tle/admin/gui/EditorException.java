package com.tle.admin.gui;

/**
 * @author Nicholas Read
 */
public class EditorException extends Exception
{
	private static final long serialVersionUID = 1L;
	private boolean isFatal;
	private String category;

	public EditorException(String message)
	{
		super(message);
		isFatal = false;
	}

	public EditorException(String errorCategory, Throwable th)
	{
		super(th);
		this.category = errorCategory;
		isFatal = true;
	}

	public String getCategory()
	{
		return category;
	}

	public boolean isFatal()
	{
		return isFatal;
	}
}
