package com.tle.web.sections.standard;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.js.impl.DelayedJSValueComponent;
import com.tle.web.sections.standard.model.HtmlComponentState;

@NonNullByDefault
public abstract class AbstractValueComponent<S extends HtmlComponentState, VC extends JSValueComponent>
	extends
		AbstractDisablerComponent<S> implements JSValueComponent
{
	private DelayedJSValueComponent<VC> delayedValues;

	public AbstractValueComponent(String defaultRenderer)
	{
		super(defaultRenderer);
	}

	@Override
	public void rendererSelected(RenderContext info, SectionRenderable renderer)
	{
		delayedValues.rendererSelected(info, renderer);
		super.rendererSelected(info, renderer);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		delayedValues = createDelayedJS(this);
	}

	protected DelayedJSValueComponent<VC> createDelayedJS(ElementId id)
	{
		return new DelayedJSValueComponent<VC>(id);
	}

	@Override
	public JSExpression createGetExpression()
	{
		return delayedValues.createGetExpression();
	}

	@Override
	public JSCallable createSetFunction()
	{
		return delayedValues.createSetFunction();
	}

	@Override
	public JSCallable createResetFunction()
	{
		return delayedValues.createResetFunction();
	}

}
