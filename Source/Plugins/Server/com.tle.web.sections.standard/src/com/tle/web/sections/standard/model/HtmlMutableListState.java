package com.tle.web.sections.standard.model;

import java.util.List;

import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.renderers.list.DropDownRenderer;

/**
 * The State class for mutable lists.
 * <p>
 * The difference between a {@code HtmlMutableListState} and
 * {@link HtmlListState}, is that the mutable list is designed for modifying the
 * values of the list, where as the {@code HtmlListState} is about selecting
 * values from the pre-defined list.
 * 
 * @see HtmlListState
 * @see DropDownRenderer
 * @author jmaginnis
 */
public class HtmlMutableListState extends HtmlComponentState
{
	private List<Option<?>> options;
	private boolean grouped;

	public HtmlMutableListState()
	{
		super(RendererConstants.DROPDOWN);
	}

	public List<Option<?>> getOptions()
	{
		return options;
	}

	public void setOptions(List<Option<?>> options)
	{
		this.options = options;
	}

	public boolean isGrouped()
	{
		return grouped;
	}

	public void setGrouped(boolean grouped)
	{
		this.grouped = grouped;
	}
}
