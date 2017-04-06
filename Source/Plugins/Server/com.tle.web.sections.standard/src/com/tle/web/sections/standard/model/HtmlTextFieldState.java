package com.tle.web.sections.standard.model;

import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.RendererConstants;

public class HtmlTextFieldState extends HtmlValueState
{
	private boolean password;
	private int size;
	private int maxLength;
	private boolean editable = true;
	private boolean autocompleteDisabled;
	private JSCallable callback;

	public HtmlTextFieldState()
	{
		super(RendererConstants.TEXTFIELD);
	}

	public boolean isPassword()
	{
		return password;
	}

	public void setPassword(boolean password)
	{
		this.password = password;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public int getMaxLength()
	{
		return maxLength;
	}

	public void setMaxLength(int maxLength)
	{
		this.maxLength = maxLength;
	}

	public boolean isAutocompleteDisabled()
	{
		return autocompleteDisabled;
	}

	public void setAutocompleteDisabled(boolean autocompleteDisabled)
	{
		this.autocompleteDisabled = autocompleteDisabled;
	}

	public boolean isEditable()
	{
		return editable;
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public JSCallable getAutoCompleteCallback()
	{
		return callback;
	}

	public void setAutoCompleteCallback(JSCallable callback)
	{
		this.callback = callback;
	}
}
