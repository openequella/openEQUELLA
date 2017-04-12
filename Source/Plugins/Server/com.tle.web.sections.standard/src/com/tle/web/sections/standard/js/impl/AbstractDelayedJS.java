package com.tle.web.sections.standard.js.impl;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererCallback;
import com.tle.web.sections.standard.js.DelayedRenderer;

@NonNullByDefault
public abstract class AbstractDelayedJS<T> implements RendererCallback, DelayedRenderer<T>
{
	protected ElementId id;

	public AbstractDelayedJS(ElementId id)
	{
		this.id = id;
	}

	@Override
	public final void rendererSelected(RenderContext info, SectionRenderable renderer)
	{
		info.setAttribute(this, renderer);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public T getSelectedRenderer(RenderContext info)
	{
		return (T) info.getAttribute(this);
	}
}
