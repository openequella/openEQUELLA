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

package com.tle.web.controls.advancedscript;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.PropBagWrapper;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.scripting.ScriptException;
import com.tle.common.scripting.types.XmlScriptType;
import com.tle.common.util.Logger;
import com.tle.common.wizard.controls.advancedscript.AdvancedScriptControl;
import com.tle.core.guice.Bind;
import com.tle.core.javascript.JavascriptModule;
import com.tle.core.javascript.JavascriptService;
import com.tle.core.services.LoggingService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.controls.advancedscript.scripting.AdvancedScriptWebControlConstants;
import com.tle.web.freemarker.BasicFreemarkerFactory;
import com.tle.web.freemarker.FreemarkerSectionResult;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSFunction;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.controls.AbstractSimpleWebControl;
import com.tle.web.wizard.controls.CCustomControl;
import com.tle.web.wizard.impl.WebRepository;

@SuppressWarnings("nls")
@Bind
public class AdvancedScriptWebControl extends AbstractSimpleWebControl
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(AdvancedScriptWebControl.class);
	private static final JSCallable HANDLE_SUBMIT_FUNCTION = new ExternallyDefinedFunction("handleSubmit",
		new IncludeFile(resources.url("scripts/asc.js")));

	private static final String SUBMITTER_NAME_KEY = "AdvancedScriptWebControl.SUBMITTER_NAME";
	private static final String SUBMITTER_VALUE_KEY = "AdvancedScriptWebControl.SUBMITTER_VALUE";

	private AdvancedScriptControl definitionControl;
	private CCustomControl storageControl;

	@Inject
	private JavascriptService javascriptService;
	@Inject
	private WizardService wizardService;
	@Inject
	private UserSessionService sessionService;
	private Logger logger;

	@EventFactory
	private EventGenerator events;
	private JSFunction submitFunction;

	@Inject
	private BasicFreemarkerFactory custFactory;

	@Override
	public SectionResult renderHtml(final RenderEventContext context) throws Exception
	{
		final ScriptContext scriptContext = getScriptContext(context, false, false);

		// Server side On-Load script execution. Cannot be called in doReads
		// since there is no guarantee on how many times it will execute
		final String r = definitionControl.getReadJs();
		execServerSideScript(r, resources.getString("script.load", getTitle()), scriptContext);

		final ScriptContext markupScriptContext = getScriptContext(context, false, true);

		TagState divState = new TagState();

		// On-load JS
		final String onLoadJs = evaluateMarkUp(context.getRootRenderContext(), definitionControl.getOnLoadJs(),
			markupScriptContext);
		divState.addReadyStatements(new ScriptStatement(onLoadJs));
		if( !Check.isEmpty(onLoadJs) )
		{
			// stops pressing up against the next script statement. it works
			// anyway, but looks ugly :)
			divState.addReadyStatements(new ScriptStatement("\n"));
		}

		// On-submit JS
		final String onSubmitJs = evaluateMarkUp(context.getRootRenderContext(), definitionControl.getOnSubmitJs(),
			markupScriptContext);
		if( !Check.isEmpty(onSubmitJs) )
		{
			final JSCallable preSubmit = new SimpleFunction("onsub" + getFormName(), new ScriptStatement(onSubmitJs));
			divState.addEventStatements(JSHandler.EVENT_PRESUBMIT,
				Js.iff(Js.not(Js.call(preSubmit)), new ReturnStatement(false)));
		}
		else
		{
			// if there is no onsubmit markup, just return true (Redmine #7037)
			divState.addEventStatements(JSHandler.EVENT_PRESUBMIT, new ReturnStatement(true));
		}

		// JS Libraries
		final List<String> jsLibs = definitionControl.getJsLibs();
		for( String setting : jsLibs )
		{
			if( setting.contains(".") ) // as it must
			{
				String[] libAndModule = setting.split("\\.");
				if( libAndModule.length == 2 ) // as it must
				{
					JavascriptModule module = javascriptService.getJavascriptModule(libAndModule[0], libAndModule[1]);
					if( module != null )
					{
						Object preRenderer = module.getPreRenderer();
						if( preRenderer instanceof PreRenderable )
						{
							divState.addPreRenderable((PreRenderable) preRenderer);
						}
					}
					else
					{
						logger.warn("Javascript module " + setting + " not found");
					}
				}
			}
		}

		return new DivRenderer(divState, new SimpleSectionResult(
			evaluateMarkUp(context.getRootRenderContext(), definitionControl.getMarkUp(), markupScriptContext)));
	}

	private String evaluateMarkUp(RenderContext context, String markUp, ScriptContext scriptContext) throws Exception
	{
		try
		{
			final FreemarkerSectionResult inner = custFactory.createResult("advanced", new StringReader(markUp),
				context);

			// all the script context objects are available in the freemarker
			// templates
			for( Map.Entry<String, Object> entry : scriptContext.getScriptObjects().entrySet() )
			{
				inner.addExtraObject(entry.getKey(), entry.getValue());
			}

			StringWriter outbuf = new StringWriter();
			inner.realRender(new SectionWriter(outbuf, context));
			return outbuf.toString();
		}
		catch( SectionsRuntimeException runtime )
		{
			Throwable cause = runtime.getCause();
			if( cause instanceof Exception )
			{
				throw (Exception) cause;
			}
			throw runtime;
		}
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		submitFunction = new SubmitValuesFunction(events.getEventHandler("handleSubmit"));
	}

	@EventHandlerMethod
	public void handleSubmit(SectionInfo info, String submitterName, String submitterValue)
	{
		if( !Check.isEmpty(submitterName) )
		{
			info.setAttribute(SUBMITTER_NAME_KEY, submitterName);
			info.setAttribute(SUBMITTER_VALUE_KEY, new String[]{submitterValue});
		}
	}

	@Override
	public void setWrappedControl(final HTMLControl control)
	{
		definitionControl = new AdvancedScriptControl((CustomControl) control.getControlBean());
		storageControl = (CCustomControl) control;
		super.setWrappedControl(control);
	}

	@Override
	public boolean isEmpty()
	{
		return storageControl.getValues().size() == 0;
	}

	/**
	 * Server side On-Submit script execution
	 */
	@Override
	public void doEdits(final SectionInfo info)
	{
		// evaluate the on submit script
		execServerSideScript(definitionControl.getStoreJs(), resources.getString("script.save", getTitle()),
			getScriptContext(info, true, false));
	}

	private void execServerSideScript(String script, String scriptName, ScriptContext context)
	{
		if( !Check.isEmpty(script) )
		{
			try
			{
				if( context != null )
				{
					wizardService.executeScript(script, scriptName, context, false);
				}
			}
			catch( ScriptException e )
			{
				throwScriptError(scriptName, e);
			}
		}
	}

	private void throwScriptError(String scriptName, Exception e)
	{
		throw new RuntimeException(resources.getString("error.script") + scriptName + "\n" + e.getMessage());
	}

	/**
	 * @return null if there is no staging folder
	 */
	private ScriptContext getScriptContext(SectionInfo info, boolean request, boolean clientSide)
	{
		final Map<String, Object> extra = new HashMap<String, Object>();
		extra.put(AdvancedScriptWebControlConstants.PREFIX, getFormName());
		if( clientSide )
		{
			final RenderContext context = (RenderContext) info;
			final ScriptVariable thisVar = new ScriptVariable("this");
			final OverrideHandler submitHandler = new OverrideHandler(HANDLE_SUBMIT_FUNCTION, thisVar, submitFunction);
			extra.put(AdvancedScriptWebControlConstants.SUBMIT_JS,
				submitHandler.getStatements(context) + " return false;");
			// required!
			context.preRender(submitHandler);
		}
		if( request )
		{
			Map<Object, Object> paramMap = new HashMap<>();
			paramMap.putAll(info.getRequest().getParameterMap());
			String submitter = info.getAttribute(SUBMITTER_NAME_KEY);
			if( !Check.isEmpty(submitter) )
			{
				paramMap.put(submitter, info.getAttribute(SUBMITTER_VALUE_KEY));
			}
			extra.put(AdvancedScriptWebControlConstants.REQUEST_MAP, paramMap);
		}
		final WebRepository repo = (WebRepository) getRepository();
		extra.put(AdvancedScriptWebControlConstants.WIZARD_ID, repo.getWizid());

		// add the magic 'attributes' bag (this passes data between
		// scripts/templates)
		final String attributesKey = repo.getWizid() + "_" + getFormName() + "_attributes";
		XmlScriptType att = sessionService.getAttribute(attributesKey);
		if( att == null )
		{
			att = new PropBagWrapper(new PropBagEx());

		}
		sessionService.setAttribute(attributesKey, att);
		extra.put(AdvancedScriptWebControlConstants.ATTRIBUTES, att);

		final WizardState wizState = repo.getState();
		final ScriptContext scriptContext = wizardService.createScriptContext(wizState, storageControl.getWizardPage(),
			storageControl, extra);

		scriptContext.setLogger(logger);

		PropBagWrapper xml = scriptContext.getXml();
		xml.clearOverrides();
		Deque<Pair<String, Integer>> pathOverrides = wizState.getPathOverrides();
		while( !pathOverrides.isEmpty() )
		{
			Pair<String, Integer> override = pathOverrides.removeLast();
			xml.pushOverride(override.getFirst(), override.getSecond());
		}

		return scriptContext;
	}

	@Inject
	public void setLoggingService(LoggingService loggingService)
	{
		logger = loggingService.getLogger(AdvancedScriptWebControl.class);
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}
}
