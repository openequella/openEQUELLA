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

package com.tle.web.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.freemarker.methods.SectionsTemplateModel;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.expression.ScriptExpression;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

@Bind
@Singleton
public class SectionsBeansWrapper extends BeansWrapper
{
	private PluginTracker<SectionsTemplateModelProvider> tracker;
	private volatile Map<String, SectionsTemplateModelProvider> modelMap;
	private Object modelMapLock = new Object();

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		tracker = new PluginTracker<SectionsTemplateModelProvider>(pluginService, "com.tle.web.freemarker",
			"templateModelProvider", "modelClass");
		tracker.setBeanKey("class");
	}

	private Map<String, SectionsTemplateModelProvider> getModelMap()
	{
		if( modelMap == null )
		{
			synchronized( modelMapLock )
			{
				if( modelMap == null )
				{
					modelMap = tracker.getBeanMap();
				}
			}
		}
		return modelMap;
	}

	@Override
	public TemplateModel wrap(Object object) throws TemplateModelException
	{
		if( object instanceof JSExpression || object instanceof JSStatements || object instanceof JSCallable )
		{
			return new JSTemplateModel(object);
		}

		if( object instanceof TemplateResult )
		{
			return new TemplateResultModel((TemplateResult) object);
		}

		if( object instanceof Label )
		{
			return new LabelTemplateModel((Label) object);
		}
		if( object instanceof SectionRenderable )
		{
			return new RenderableModel((SectionRenderable) object);
		}

		if( object != null )
		{
			SectionsTemplateModelProvider m = getModelMap().get(object.getClass().getName());
			if( m != null )
			{
				return m.getTemplateModel(object);
			}
		}

		return super.wrap(object);
	}

	public static class LabelTemplateModel extends SectionsTemplateModel implements TemplateScalarModel
	{
		private final Label label;

		public LabelTemplateModel(Label label)
		{
			this.label = label;
		}

		@Override
		public String getAsString() throws TemplateModelException
		{
			try
			{
				final SectionWriter sectionWriter = getSectionWriter();
				if( sectionWriter != null )
				{
					final StringWriter sw = new StringWriter();
					final SectionRenderable renderable = SectionUtils.convertToRenderer(label);
					final PreRenderContext preRenderContext = sectionWriter.getPreRenderContext();
					renderable.preRender(preRenderContext);
					renderable.realRender(new SectionWriter(sw, preRenderContext));
					return sw.toString();
				}
				else
				{
					if( label.isHtml() )
					{
						return label.getText();
					}
					return SectionUtils.ent(label.getText());
				}
			}
			catch( IOException e )
			{
				throw new TemplateModelException(e);
			}
		}
	}

	public static class JSTemplateModel extends SectionsTemplateModel
		implements
			TemplateScalarModel,
			AdapterTemplateModel,
			TemplateMethodModelEx
	{
		private final Object jsObject;

		public JSTemplateModel(Object object)
		{
			this.jsObject = object;
		}

		@Override
		public Object exec(@SuppressWarnings("rawtypes") List args) throws TemplateModelException
		{
			JSCallable function = (JSCallable) jsObject;
			RenderContext context = getSectionWriter();
			context.preRender(function);
			Object[] exprArgs = new Object[args.size()];
			int i = 0;
			for( Object obj : args )
			{
				if( obj instanceof AdapterTemplateModel )
				{
					Object adapted = ((AdapterTemplateModel) obj).getAdaptedObject(Object.class);
					exprArgs[i] = adapted;
				}
				else
				{
					exprArgs[i] = new ScriptExpression(obj.toString());
				}
				i++;
			}
			return function.getExpressionForCall(context, JSUtils.convertExpressions(exprArgs));
		}

		@Override
		public String getAsString() throws TemplateModelException
		{
			RenderContext info = getSectionWriter();
			SectionUtils.preRender(info, (PreRenderable) jsObject);
			if( jsObject instanceof JSExpression )
			{
				return ((JSExpression) jsObject).getExpression(info);
			}
			else
			{
				return ((JSStatements) jsObject).getStatements(info);
			}
		}

		@Override
		public Object getAdaptedObject(@SuppressWarnings("rawtypes") Class arg0)
		{
			return jsObject;
		}
	}

	public class TemplateResultModel extends SectionsTemplateModel implements TemplateHashModel, AdapterTemplateModel
	{
		private final TemplateResult template;

		public TemplateResultModel(TemplateResult template)
		{
			this.template = template;
		}

		@Override
		public TemplateModel get(String name) throws TemplateModelException
		{
			RenderContext info = getSectionWriter();
			return new RenderableModel(template.getNamedResult(info, name));
		}

		@Override
		public boolean isEmpty() throws TemplateModelException
		{
			return false;
		}

		@Override
		public Object getAdaptedObject(@SuppressWarnings("rawtypes") Class class1)
		{
			return template;
		}
	}

	public class RenderableModel extends BeanModel implements TemplateScalarModel
	{
		private final SectionRenderable renderable;

		public SectionWriter getSectionWriter()
		{
			try
			{
				return (SectionWriter) ((AdapterTemplateModel) Environment.getCurrentEnvironment().getGlobalVariable(
					"_info")) //$NON-NLS-1$
					.getAdaptedObject(SectionWriter.class);
			}
			catch( TemplateModelException e )
			{
				throw new SectionsRuntimeException(e);
			}
		}

		public RenderableModel(SectionRenderable renderable)
		{
			super(renderable, SectionsBeansWrapper.this);
			this.renderable = renderable;
		}

		@Override
		public Object getAdaptedObject(@SuppressWarnings("rawtypes") Class hint)
		{
			return renderable;
		}

		@Override
		public String getAsString() throws TemplateModelException
		{
			return SectionUtils.renderToString(getSectionWriter(), renderable);
		}

	}
}
