package com.tle.web.sections.standard.renderers;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.model.HtmlComponentState;

@NonNullByDefault
public class DivRenderer extends TagRenderer
{
	public DivRenderer(Object innerContent)
	{
		this((String) null, innerContent);
	}

	public DivRenderer(HtmlComponentState tagState)
	{
		this((TagState) tagState);
	}

	public DivRenderer(TagState tagState)
	{
		super("div", tagState); //$NON-NLS-1$
	}

	public DivRenderer(TagState tagState, @Nullable Object innerContent)
	{
		super("div", tagState, SectionUtils.convertToRenderer(innerContent)); //$NON-NLS-1$
	}

	public DivRenderer(@Nullable String styleClass, @Nullable Object innerContent)
	{
		this(new TagState(), "div", styleClass, innerContent); //$NON-NLS-1$
	}

	public DivRenderer(TagState tagState, @Nullable String styleClass, @Nullable Object innerContent)
	{
		this(tagState, "div", styleClass, innerContent); //$NON-NLS-1$
	}

	public DivRenderer(String tag, @Nullable String styleClass, @Nullable Object innerContent)
	{
		this(new TagState(), tag, styleClass, innerContent);
	}

	public DivRenderer(TagState tagState, String tag, @Nullable String styleClass, @Nullable Object innerContent)
	{
		super(tag, tagState);
		if( !Check.isEmpty(styleClass) )
		{
			tagState.addClass(styleClass);
		}
		setNestedRenderable(SectionUtils.convertToRenderer(innerContent));
	}

	@Override
	public SectionRenderable getNestedRenderable()
	{
		if( nestedRenderable != null )
		{
			return nestedRenderable;
		}

		if( tagState instanceof HtmlComponentState )
		{
			HtmlComponentState state = (HtmlComponentState) tagState;
			if( state.getLabel() != null )
			{
				nestedRenderable = state.createLabelRenderer();
			}
		}

		return nestedRenderable;
	}
}
