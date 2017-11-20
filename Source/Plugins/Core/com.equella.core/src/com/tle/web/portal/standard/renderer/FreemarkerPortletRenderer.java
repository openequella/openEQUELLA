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

package com.tle.web.portal.standard.renderer;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.PropBagWrapper;
import com.dytech.edge.common.ScriptContext;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.core.guice.Bind;
import com.tle.core.scripting.service.StandardScriptContextParams;
import com.tle.web.freemarker.BasicFreemarkerFactory;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.FreemarkerSectionResult;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@Bind
public class FreemarkerPortletRenderer
	extends
		PortletContentRenderer<FreemarkerPortletRenderer.FreemarkerPortletRendererModel>
{
	private static final Logger LOGGER = Logger.getLogger(FreemarkerPortletRenderer.class);

	private static final JSCallable HANDLE_SUBMIT_FUNCTION = new ExternallyDefinedFunction("handleSubmit",
		new IncludeFile(ResourcesService.getResourceHelper(FreemarkerPortletRenderer.class).url(
			"scripts/freemarkerportlet.js")));

	private static final String SUBMITTER_NAME_KEY = "FreemarkerPortletRenderer.SUBMITTER_NAME";
	private static final String SUBMITTER_VALUE_KEY = "FreemarkerPortletRenderer.SUBMITTER_VALUE";

	@PlugKey("freemarker.error.script")
	private static String ERROR_SCRIPT_KEY;
	@PlugKey("freemarker.error.markup")
	private static String ERROR_MARKUP_KEY;
	@PlugKey("freemarker.error.onload")
	private static String ERROR_ONLOAD_KEY;
	@PlugKey("freemarker.error.javascript")
	private static String KEY_ERROR_JS;

	@Inject
	private ScriptingService scriptService;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@AjaxFactory
	private AjaxGenerator ajax;
	@Component
	private Div portletDiv;

	@Inject
	private BasicFreemarkerFactory custFactory;

	private UpdateDomFunction reloadPortletHandler;
	private UpdateDomFunction reloadPortletWithSpinnerHandler;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		reloadPortletHandler = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("handleSubmit"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), id + "freemarkerPortlet");
		reloadPortletWithSpinnerHandler = ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("handleSubmit"), ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), id
				+ "freemarkerPortlet");
	}

	@EventHandlerMethod(priority = SectionEvent.PRIORITY_HIGH)
	public void handleSubmit(SectionInfo info, String submitterName, String submitterValue)
	{
		if( !Check.isEmpty(submitterName) )
		{
			info.setAttribute(SUBMITTER_NAME_KEY, submitterName);
			info.setAttribute(SUBMITTER_VALUE_KEY, new String[]{submitterValue});
		}
	}

	@Override
	public SectionRenderable renderHtml(RenderEventContext context) throws Exception
	{
		String markup = "";
		String clientJs = "";
		List<String> jsIncludes = new ArrayList<String>();
		List<String> cssIncludes = new ArrayList<String>();
		String config = portlet.getConfig();
		if( !Check.isEmpty(config) )
		{
			PropBagEx xml = new PropBagEx(config);
			markup = xml.getNode("markup");
			jsIncludes.addAll(xml.getNodeList("external-javascript"));
			cssIncludes.addAll(xml.getNodeList("external-css"));
			clientJs = xml.getNode("clientscript");
		}

		final ScriptContext scriptContext = getScriptContext(context);
		final ScriptVariable thisVar = new ScriptVariable("this");
		final OverrideHandler submitHandler = new OverrideHandler(HANDLE_SUBMIT_FUNCTION, thisVar, reloadPortletHandler);
		scriptContext.addScriptObject("submitJavascript", submitHandler.getStatements(context) + " return false;");
		final OverrideHandler submitWithLoadingHandler = new OverrideHandler(HANDLE_SUBMIT_FUNCTION, thisVar,
			reloadPortletWithSpinnerHandler);
		scriptContext.addScriptObject("submitWithLoadingJavascript", submitWithLoadingHandler.getStatements(context)
			+ " return false;");

		FreemarkerSectionResult result = null;
		try
		{
			result = custFactory.createResult("portletFreemarker", new StringReader(markup), context);
		}
		catch( Exception e )
		{
			LOGGER.warn("Error while running a Scripted portlet " + portlet.getUuid(), e);
			return new LabelRenderer(new KeyLabel(ERROR_MARKUP_KEY, e.getMessage()));
		}

		for( Map.Entry<String, Object> entry : scriptContext.getScriptObjects().entrySet() )
		{
			result.addExtraObject(entry.getKey(), entry.getValue());
		}

		FreemarkerSectionResult scriptResult = null;
		try
		{
			scriptResult = custFactory.createResult("portletFreemarkerScript", new StringReader(clientJs), context);
		}
		catch( Exception e )
		{
			LOGGER.warn("Error while running a Scripted portlet " + portlet.getUuid(), e);
			return new LabelRenderer(new KeyLabel(ERROR_ONLOAD_KEY, e.getMessage()));
		}

		for( Map.Entry<String, Object> entry : scriptContext.getScriptObjects().entrySet() )
		{
			scriptResult.addExtraObject(entry.getKey(), entry.getValue());
		}

		if( !jsIncludes.isEmpty() )
		{
			portletDiv.addPrerenderables(new IncludeFile(jsIncludes.toArray(new String[jsIncludes.size()])));
		}

		getModel(context).setMarkup(result);

		// required!
		portletDiv.addPrerenderables(submitHandler);
		portletDiv.addPrerenderables(submitWithLoadingHandler);
		if( !Check.isEmpty(clientJs) )
		{
			StringWriter out = new StringWriter();
			SectionWriter writer = new SectionWriter(out, context);
			scriptResult.realRender(writer);

			String clientScript = out.toString();
			if( !Check.isEmpty(clientScript) )
			{
				String errorTitle = JSUtils.escape(
					CurrentLocale.get(KEY_ERROR_JS, CurrentLocale.get(portlet.getName())), true);

				// TODO: put into a freemarker template
				clientScript = "setTimeout(function(){ try{ eval(" + JSUtils.escape(clientJs, true)
					+ "\n);  } catch (e) { throw (" + errorTitle + " + ':\\n' + e); }  }, 1);";
			}
			ScriptStatement scriptStatement = new ScriptStatement(clientScript);
			portletDiv.addReadyStatements(context, scriptStatement);
		}

		if( !cssIncludes.isEmpty() )
		{
			for( String css : cssIncludes )
			{
				portletDiv.addPrerenderables(CssInclude.include(css).make());
			}

		}

		return view.createResult("freemarkerportlet.ftl", context);
	}

	private ScriptContext getScriptContext(SectionInfo info)
	{
		StandardScriptContextParams params = new StandardScriptContextParams(null, null, false, null);
		ScriptContext scriptContext = scriptService.createScriptContext(params);
		FreemarkerPortletRendererModel model = getModel(info);

		PropBagEx bag;

		if( Check.isEmpty(model.getXml()) )
		{
			bag = new PropBagEx();
		}
		else
		{
			bag = new PropBagEx(model.getXml());
		}

		scriptContext.addScriptObject("attributes", new PropBagWrapper(bag));

		Map<Object, Object> paramMap = new HashMap<Object, Object>();
		paramMap.putAll(info.getRequest().getParameterMap());
		String submitter = info.getAttribute(SUBMITTER_NAME_KEY);
		if( !Check.isEmpty(submitter) )
		{
			paramMap.put(submitter, info.getAttribute(SUBMITTER_VALUE_KEY));
		}
		scriptContext.addScriptObject("request", new RequestMapScriptWrapper(getSectionId(), paramMap));
		scriptContext.addScriptObject("prefix", getSectionId());

		return scriptContext;
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void onLoadScript(SectionInfo info)
	{
		FreemarkerPortletRendererModel model = getModel(info);

		String script = null;
		String config = portlet.getConfig();
		if( !Check.isEmpty(config) )
		{
			PropBagEx xml = new PropBagEx(config);
			script = xml.getNode("script");
		}

		try
		{
			execScript(info, script, getScriptContext(info));
		}
		catch( Exception e )
		{
			LOGGER.warn("Error while running a Scripted portlet " + portlet.getUuid(), e);
			model.setError(new LabelRenderer(new KeyLabel(ERROR_SCRIPT_KEY, e.getMessage())));
		}

	}

	private void execScript(SectionInfo info, String script, ScriptContext context)
	{
		if( !Check.isEmpty(script) )
		{
			FreemarkerPortletRendererModel model = getModel(info);
			try
			{
				scriptService.executeScript(script, "portletScript", context, false);
			}
			catch( Exception e )
			{
				LOGGER.warn("Error while running a Scripted portlet " + portlet.getUuid(), e);
				model.setError(new LabelRenderer(new KeyLabel(ERROR_SCRIPT_KEY, e.getMessage())));
			}
			model.setXml(((PropBagWrapper) context.getScriptObjects().get("attributes")).asString());
		}
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "pfm";
	}

	@Override
	public Class<FreemarkerPortletRendererModel> getModelClass()
	{
		return FreemarkerPortletRendererModel.class;
	}

	public Div getPortletDiv()
	{
		return portletDiv;
	}

	public static class FreemarkerPortletRendererModel
	{
		private String xml;
		private SectionRenderable error;
		private SectionRenderable markup;

		public void setXml(String xml)
		{
			this.xml = xml;
		}

		public String getXml()
		{
			return xml;
		}

		public void setError(SectionRenderable error)
		{
			this.error = error;
		}

		public SectionRenderable getError()
		{
			return error;
		}

		public void setMarkup(SectionRenderable markup)
		{
			this.markup = markup;
		}

		public SectionRenderable getMarkup()
		{
			return markup;
		}
	}

	/**
	 * TODO: this is duped with the advanced script request wrapper and does not
	 * implement the RequestMapScriptObject interface. The interface should be
	 * moved somewhere common.
	 */
	public static class RequestMapScriptWrapper implements ScriptObject // implements
	// RequestMapScriptObject
	{
		private static final long serialVersionUID = 1L;

		final String prefix;
		final Map<Object, Object> requestMap;

		public RequestMapScriptWrapper(String prefix, Map<Object, Object> requestMap)
		{
			this.prefix = prefix;
			this.requestMap = requestMap;
		}

		public String get(String key)
		{
			String[] values = getList(key);
			if( values != null && values.length > 0 )
			{
				return values[0];
			}
			return null;
		}

		public String[] getList(String key)
		{
			return (String[]) requestMap.get(prefix + key);
		}

		@Override
		public void scriptEnter()
		{
			// Nothing by default
		}

		@Override
		public void scriptExit()
		{
			// Nothing by default
		}
	}
}
