package com.tle.web.sections.standard.model;

import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.renderers.TextAreaRenderer;
import com.tle.web.sections.standard.renderers.TextFieldRenderer;

/**
 * The State for single text value {@code Section}s and {@code Renderers}.
 * <p>
 * Along with the text string, we also store whether or not it is editable.
 * 
 * @see TextFieldRenderer
 * @see TextAreaRenderer
 * @author jmaginnis
 */
public class HtmlValueState extends HtmlComponentState
{
	private String value;
	private String placeholderText;

	public HtmlValueState()
	{
		super(RendererConstants.TEXTFIELD);
	}

	public HtmlValueState(String defaultRenderer)
	{
		super(defaultRenderer);
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getPlaceholderText()
	{
		return placeholderText;
	}

	public void setPlaceholderText(String placeholderText)
	{
		this.placeholderText = placeholderText;
	}
}
