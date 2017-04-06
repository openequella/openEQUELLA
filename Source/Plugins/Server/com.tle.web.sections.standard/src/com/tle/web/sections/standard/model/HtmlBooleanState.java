package com.tle.web.sections.standard.model;

import com.tle.web.sections.standard.RendererConstants;

/**
 * The State class for Checkbox type {@code Section}s and {@code Renderers}.
 * <p>
 * Keeps track of a value, as well as whether or not it was checked.
 * 
 * @see com.tle.web.sections.standard.MappedBooleans
 * @see com.tle.web.sections.standard.Checkbox
 * @see com.tle.web.sections.standard.renderers.toggle.CheckboxRenderer
 * @see com.tle.web.sections.standard.renderers.toggle.RadioButtonRenderer
 * @author jmaginnis
 */
public class HtmlBooleanState extends HtmlComponentState
{
	private String value;
	private boolean checked;

	public boolean isChecked()
	{
		return checked;
	}

	public void setChecked(boolean checked)
	{
		this.checked = checked;
	}

	public HtmlBooleanState()
	{
		super(RendererConstants.CHECKBOX);
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
