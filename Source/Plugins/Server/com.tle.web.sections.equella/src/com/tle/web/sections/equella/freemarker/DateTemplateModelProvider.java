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

package com.tle.web.sections.equella.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.SectionsTemplateModelProvider;
import com.tle.web.freemarker.methods.SectionsTemplateModel;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.equella.render.DateRenderer;
import com.tle.web.sections.equella.render.DateRendererFactory;

import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class DateTemplateModelProvider implements SectionsTemplateModelProvider
{
	@Inject
	private DateRendererFactory dateRendererFactory;

	@Override
	public SectionsTemplateModel getTemplateModel(Object object)
	{
		return new DateTemplateModel((Date) object, dateRendererFactory);
	}

	public static class DateTemplateModel extends SectionsTemplateModel
		implements
			TemplateScalarModel,
			TemplateMethodModelEx
	{
		private final Date date;
		private DateRendererFactory dateRendererFactory;

		public DateTemplateModel(Date date, DateRendererFactory dateRendererFactory)
		{
			this.date = date;
			this.dateRendererFactory = dateRendererFactory;
		}

		@Override
		public String getAsString() throws TemplateModelException
		{
			DateRenderer d = dateRendererFactory.createDateRenderer(date);

			SectionWriter context = getSectionWriter();
			d.preRender(context);

			try
			{
				StringWriter s = new StringWriter();
				SectionWriter n = new SectionWriter(s, context);
				d.realRender(n);
				return s.toString();
			}
			catch( IOException e )
			{
				throw new TemplateModelException(e);
			}
		}

		@Override
		public Object exec(List arguments) throws TemplateModelException
		{
			// You can use it as a function to prevent the time-ago ness
			return new TemplateDateModel()
			{
				@Override
				public Date getAsDate() throws TemplateModelException
				{
					return date;
				}

				@Override
				public int getDateType()
				{
					return TemplateDateModel.DATETIME;
				}
			};
		}
	}
}
