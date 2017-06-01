/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.wizard.standard.controls;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.generic.expression.NullExpression;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.AssignableFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.renderers.TextFieldRenderer;
import com.tle.web.wizard.controls.AbstractSimpleWebControl;
import com.tle.web.wizard.controls.EditableCtrl;
import com.tle.web.wizard.controls.SimpleValueControl;

@Bind
public class SimpleWebControl extends AbstractSimpleWebControl implements SimpleValueControl
{
	@Component(stateful = false)
	private TextField field;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		field.setParameterId(getFormName());
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		field.setValue(context, ((EditableCtrl) getWrappedControl()).getValue());
		addDisabler(context, field);
		TextFieldRenderer hidden = (TextFieldRenderer) SectionUtils.renderSectionResult(context, field);
		hidden.setHidden(true);
		return hidden;
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		getWrappedControl().setValues(field.getValue(info));
	}

	@Override
	public JSAssignable createEditFunction()
	{
		return AssignableFunction.get(field.createSetFunction());
	}

	@Override
	public JSAssignable createResetFunction()
	{
		return AssignableFunction.get(field.createResetFunction());
	}

	@Override
	public JSAssignable createTextFunction()
	{
		return new AnonymousFunction(new ReturnStatement(new NullExpression()));
	}

	@Override
	public JSAssignable createValueFunction()
	{
		return new AnonymousFunction(new ReturnStatement(field.createGetExpression()));
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}
}
