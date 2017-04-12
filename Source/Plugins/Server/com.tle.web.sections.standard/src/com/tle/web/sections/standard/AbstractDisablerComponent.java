package com.tle.web.sections.standard;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.impl.DelayedJSDisabler;
import com.tle.web.sections.standard.model.HtmlComponentState;

@NonNullByDefault
public abstract class AbstractDisablerComponent<S extends HtmlComponentState> extends AbstractRenderedComponent<S>
	implements
		JSDisableable
{
	private DelayedJSDisabler delayedDisabler;

	public AbstractDisablerComponent(String defaultRenderer)
	{
		super(defaultRenderer);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		delayedDisabler = new DelayedJSDisabler(this);
	}

	@Override
	public void rendererSelected(RenderContext info, SectionRenderable renderer)
	{
		delayedDisabler.rendererSelected(info, renderer);
		super.rendererSelected(info, renderer);
	}

	@Override
	public JSCallable createDisableFunction()
	{
		return delayedDisabler.createDisableFunction();
	}
}
