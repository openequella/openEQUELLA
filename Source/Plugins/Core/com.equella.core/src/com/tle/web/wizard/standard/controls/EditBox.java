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
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.AssignableFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.AbstractSimpleWebControl;
import com.tle.web.wizard.controls.CEditBox;
import com.tle.web.wizard.controls.SimpleValueControl;

/**
 * @author jmaginnis
 */
@Bind
public class EditBox extends AbstractSimpleWebControl implements SimpleValueControl
{
	@ViewFactory(name="wizardFreemarkerFactory")
	private FreemarkerFactory viewFactory;
	@Component(register = false, stateful = false)
	private TextField field;
	private CEditBox box;

	@Override
	public void registered(String id, SectionTree tree)
	{
		field.setParameterId(getFormName());
		tree.registerInnerSection(field, id);
		super.registered(id, tree);
	}

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		super.setWrappedControl(control);
		this.box = (CEditBox) control;
		if( control.getSize1() == 0 )
		{
			control.setSize1(70);
		}
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		String value = field.getValue(info);
		HTMLControl ctrl = getWrappedControl();
		if( value == null )
		{
			ctrl.setValues();
		}
		else
		{
			ctrl.setValues(value);
		}
	}

	@Override
	@SuppressWarnings("nls")
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		field.setValue(context, box.getValue());
		if( getSize2() > 1 )
		{
			field.setEventHandler(context, "keyup", new OverrideHandler(new ScriptStatement(
				"if(this.value.length > 8192) this.value = this.value.slice(0, 8192);")));
		}
		addDisabler(context, field);
		return viewFactory.createResult("editbox.ftl", context);
	}

	public TextField getField()
	{
		return field;
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
		return new AnonymousFunction(new ReturnStatement(field.createGetExpression()));
	}

	@Override
	public JSAssignable createValueFunction()
	{
		return new AnonymousFunction(new ReturnStatement(field.createGetExpression()));
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return field;
	}
}
