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

package com.tle.web.sections.standard.renderers.list;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.base.Throwables;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.ListOption;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.JSListComponent;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.HtmlMutableListState;
import com.tle.web.sections.standard.model.Option;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class BooleanListDirective extends AbstractListDirective
{
	@Override
	protected TagRenderer createTagRenderer(HtmlMutableListState state, Environment env,
		@SuppressWarnings("rawtypes") Map params, TemplateDirectiveBody body, TemplateModel[] loopVars)
	{
		return new FreemarkerCheckListRenderer(new BooleanListRenderer((HtmlListState) state), env, body, loopVars);
	}

	public static class FreemarkerCheckListRenderer extends TagRenderer implements JSDisableable, JSListComponent
	{
		private final TemplateDirectiveBody body;
		private final TemplateModel[] loopVars;
		private final BeansWrapper wrapper;
		private final BooleanListRenderer renderer;

		public FreemarkerCheckListRenderer(BooleanListRenderer renderer, Environment env, TemplateDirectiveBody body,
			TemplateModel[] loopVars)
		{
			super("ul", renderer.getListState()); //$NON-NLS-1$
			this.wrapper = (BeansWrapper) env.getObjectWrapper();
			this.body = body;
			this.loopVars = loopVars;
			this.renderer = renderer;
		}

		@Override
		protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException
		{
			// nothing
		}

		@Override
		protected void writeEnd(SectionWriter writer) throws IOException
		{
			// nothing
		}

		@Override
		protected void writeMiddle(SectionWriter writer) throws IOException
		{
			List<ListOption<Object>> optionList = renderer.renderOptionList(writer);
			for( ListOption<Object> listOption : optionList )
			{
				Option<Object> option = listOption.getOption();
				HtmlBooleanState state = listOption.getBooleanState();
				state.setLabel(new TextLabel(option.getName(), true));
				loopVars[0] = new BeanModel(option, wrapper);
				loopVars[1] = new BeanModel(state, wrapper);
				try
				{
					body.render(writer);
				}
				catch( TemplateException e )
				{
					throw Throwables.propagate(e);
				}
			}
		}

		@Override
		public JSExpression createGetExpression()
		{
			return renderer.createGetExpression();
		}

		@Override
		public JSCallable createSetFunction()
		{
			return renderer.createSetFunction();
		}

		@Override
		public JSCallable createResetFunction()
		{
			return renderer.createResetFunction();
		}

		@Override
		public JSCallable createDisableFunction()
		{
			return renderer.createDisableFunction();
		}

		@Override
		public JSExpression createNotEmptyExpression()
		{
			return renderer.createNotEmptyExpression();
		}

		@Override
		public JSExpression createGetNameExpression()
		{
			return renderer.createGetNameExpression();
		}

		@Override
		public JSCallable createSetAllFunction()
		{
			return renderer.createSetAllFunction();
		}
	}
}
