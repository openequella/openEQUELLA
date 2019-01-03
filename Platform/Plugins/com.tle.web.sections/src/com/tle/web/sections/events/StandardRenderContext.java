/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.WrappedSectionInfo;
import com.tle.web.sections.header.BodyTag;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.header.HeaderHelper;
import com.tle.web.sections.header.StandardHeaderHelper;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.OutputResultListener;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public class StandardRenderContext extends WrappedSectionInfo implements PreRenderContext
{
	private boolean dontBookmark;
	private String modalId;
	private String semiModalId;
	private SectionRenderable renderedResponse;
	private SectionResult renderedBody;
	private RenderResultListener rootResultListener;
	@Nullable
	private Set<String> extraBookmarkContexts;
	private final HeaderHelper helper = new StandardHeaderHelper();
	private final IdentityHashMap<PreRenderable, PreRenderable> preRendered = new IdentityHashMap<PreRenderable, PreRenderable>();
	private final Set<String> jsFiles = new LinkedHashSet<String>();
	private final Set<CssInclude> cssFiles = new LinkedHashSet<CssInclude>();
	private final List<JSStatements> statements = new ArrayList<JSStatements>();
	private final List<JSStatements> footerStatements = new ArrayList<JSStatements>();
	private final List<JSStatements> readyStatements = new ArrayList<JSStatements>();
	private final StringBuilder headerMarkup = new StringBuilder();
	private final FormTag form = new FormTag();
	private final BodyTag body = new BodyTag();
	private JSCallAndReference bindW3CFunction;
	private JSCallAndReference bindFunction;
	private PreRenderContext topLevel;

	private static Set<String> GLOBAL_EVENTS = new HashSet<String>(Arrays.asList(JSHandler.EVENT_BEFOREUNLOAD,
		JSHandler.EVENT_PRESUBMIT, JSHandler.EVENT_VALIDATE));

	public StandardRenderContext(SectionInfo info)
	{
		super(info);
		info.setAttribute(StandardRenderContext.class, this);
		rootResultListener = new OutputResultListener(this);
		this.topLevel = this;
	}

	@Override
	public void setRootRenderContext(RenderContext renderContext)
	{
		this.topLevel = (PreRenderContext) renderContext;
		info.setRootRenderContext(renderContext);
	}

	public void addExtraBookmarkContext(String context)
	{
		if( extraBookmarkContexts == null )
		{
			extraBookmarkContexts = new HashSet<String>();
		}
		extraBookmarkContexts.add(context);
	}

	@Override
	public String getModalId()
	{
		return modalId;
	}

	@Override
	public void setModalId(String modalId)
	{
		this.modalId = modalId;
	}

	@Override
	public SectionResult getRenderedBody()
	{
		return renderedBody;
	}

	@Override
	public void setRenderedBody(SectionResult renderedBody)
	{
		this.renderedBody = renderedBody;
		info.preventGET();
		info.renderNow();
	}

	@Override
	public String getSemiModalId()
	{
		return semiModalId;
	}

	@Override
	public void setSemiModalId(String semiModalId)
	{
		this.semiModalId = semiModalId;
	}

	@Override
	public RenderResultListener getRootResultListener()
	{
		return rootResultListener;
	}

	@Override
	public void setRootResultListener(RenderResultListener rootResultListener)
	{
		this.rootResultListener = rootResultListener;
	}

	@Override
	public SectionRenderable getRenderedResponse()
	{
		return renderedResponse;
	}

	@Override
	public void setRenderedResponse(SectionRenderable renderedResponse)
	{
		this.renderedResponse = renderedResponse;
		info.preventGET();
		info.renderNow();
	}

	public Set<String> getExtraBookmarkContexts()
	{
		return extraBookmarkContexts;
	}

	public boolean isDontBookmark()
	{
		return dontBookmark;
	}

	public void setDontBookmark(boolean dontBookmark)
	{
		this.dontBookmark = dontBookmark;
	}

	@Override
	public HeaderHelper getHelper()
	{
		return helper;
	}

	@Override
	public void preRender(@Nullable Collection<? extends PreRenderable> preRenderers)
	{
		if( preRenderers == null )
		{
			return;
		}
		for( PreRenderable preRenderable : preRenderers )
		{
			topLevel.preRender(preRenderable);
		}
	}

	@Override
	public void preRender(@Nullable PreRenderable preRenderer)
	{
		if( preRenderer != null )
		{
			if( !preRendered.containsKey(preRenderer) )
			{
				preRendered.put(preRenderer, preRenderer);
				preRenderer.preRender(topLevel);
			}
		}
	}

	@Override
	public void preRender(@Nullable PreRenderable... preRenderers)
	{
		if( preRenderers == null )
		{
			return;
		}
		for( PreRenderable preRenderable : preRenderers )
		{
			topLevel.preRender(preRenderable);
		}
	}

	@Override
	public boolean isRenderHeader()
	{
		return true;
	}

	@Override
	public void addCss(String src)
	{
		cssFiles.add(new CssInclude(src));
	}

	@Override
	public void addCss(CssInclude css)
	{
		cssFiles.add(css);
	}

	@Override
	public void addJs(String src)
	{
		jsFiles.add(src);
	}

	@Override
	public void addStatements(JSStatements statements)
	{
		this.statements.add(statements);
	}

	@Override
	public void addFooterStatements(JSStatements statements)
	{
		footerStatements.add(statements);
	}

	@Override
	public void addReadyStatements(JSStatements statements)
	{
		readyStatements.add(statements);
	}

	@Override
	public void addHeaderMarkup(String head)
	{
		headerMarkup.append(head);
	}

	@Override
	public FormTag getForm()
	{
		return form;
	}

	@Override
	public BodyTag getBody()
	{
		return body;
	}

	@Override
	public void bindHandler(String event, @Nullable Map<String, String> attrs, JSHandler handler)
	{
		PreRenderContext context = (PreRenderContext) getRootRenderContext();
		if( event.equals(JSHandler.EVENT_READY) )
		{
			context.addReadyStatements(handler);
		}
		else if( GLOBAL_EVENTS.contains(event) )
		{
			// FIXME: There should be an EquellaRenderContext which does this
			if( event.equals(JSHandler.EVENT_BEFOREUNLOAD) && System.getProperty("equella.autotest") != null )
			{
				return;
			}

			JSCallAndReference w3cHandler = handler.getW3CHandler();
			String idFor = null;
			if( attrs != null )
			{
				idFor = attrs.get("id"); //$NON-NLS-1$
			}
			if( w3cHandler != null )
			{
				context.addReadyStatements(new FunctionCallStatement(bindW3CFunction, event, idFor, w3cHandler));
			}
			else
			{
				JSAssignable handlerFunc = handler.getHandlerFunction();
				if( handlerFunc == null )
				{
					handlerFunc = new AnonymousFunction(handler);
				}
				context.addReadyStatements(new FunctionCallStatement(bindFunction, event, idFor, handlerFunc));
			}
		}
		else
		{
			context.preRender(handler);
			String inlineStatements = handler.getStatements(context);
			if( handler.isOverrideDefault() )
			{
				inlineStatements += "return false;"; //$NON-NLS-1$
			}
			if( attrs == null )
			{
				throw new Error("Can't bind non-ready event without an attribute map");
			}
			attrs.put("on" + event, inlineStatements); //$NON-NLS-1$
		}
	}

	@Override
	public PreRenderContext getPreRenderContext()
	{
		return topLevel;
	}

	public List<JSStatements> dequeueStatements()
	{
		List<JSStatements> statements = new ArrayList<JSStatements>(this.statements);
		this.statements.clear();
		return statements;
	}

	public List<JSStatements> dequeueFooterStatements()
	{
		List<JSStatements> statements = new ArrayList<JSStatements>(this.footerStatements);
		this.footerStatements.clear();
		return statements;
	}

	public List<JSStatements> dequeueReadyStatements()
	{
		List<JSStatements> statements = new ArrayList<JSStatements>(this.readyStatements);
		this.readyStatements.clear();
		return statements;
	}

	public String getHeaderMarkup()
	{
		return headerMarkup.toString();
	}

	public List<String> getJsFiles()
	{
		return new ArrayList<String>(jsFiles);
	}

	public List<CssInclude> getCssFiles()
	{
		return new ArrayList<CssInclude>(cssFiles);
	}

	public void setBindW3CFunction(JSCallAndReference bindW3CFunction)
	{
		this.bindW3CFunction = bindW3CFunction;
	}

	public void setBindFunction(JSCallAndReference bindFunction)
	{
		this.bindFunction = bindFunction;
	}

}
