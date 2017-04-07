package com.tle.web.sections.render;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.tle.web.sections.SectionWriter;

@SuppressWarnings("nls")
public class ListTagRenderer extends TagRenderer
{
	private final Iterable<ListElementState> elements;

	public ListTagRenderer(TagState state, boolean ordered, Iterable<ListElementState> elements)
	{
		super(ordered ? "ol" : "ul", state);
		this.elements = elements;
	}

	public ListTagRenderer(List<?> elements)
	{
		this(new TagState(), false, convertToStates(elements));
	}

	private static Iterable<ListElementState> convertToStates(List<?> elements)
	{
		List<ListElementState> elems = Lists.newArrayList();
		for( Object object : elements )
		{
			ListElementState listElem;
			if( object instanceof ListElementState )
			{
				listElem = (ListElementState) object;
			}
			else
			{
				listElem = new ListElementState(object);
			}
			elems.add(listElem);
		}
		return elems;
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		for( ListElementState listElem : elements )
		{
			writer.render(new TagRenderer("li", listElem, listElem.getContent()));
		}
	}
}
