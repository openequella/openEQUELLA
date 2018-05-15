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

package com.tle.web.sections.ajax.handler;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dytech.common.io.DevNullWriter;
import com.google.common.collect.Lists;
import com.tle.annotation.Nullable;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.ajax.AjaxCaptureRenderer;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.FullAjaxCaptureResult;
import com.tle.web.sections.ajax.FullDOMResult;
import com.tle.web.sections.ajax.JSONRenderer;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.ajax.SimpleDOMResult;
import com.tle.web.sections.ajax.exception.AjaxException;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderResultListener;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.generic.WrappedSectionInfo;
import com.tle.web.sections.header.BodyTag;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.header.HeaderHelper;
import com.tle.web.sections.header.InfoFormAction;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.WrappedNestedRenderable;
import com.tle.web.template.RenderNewTemplate;

public class StandardAjaxRenderContext extends WrappedSectionInfo implements AjaxRenderContext, RenderResultListener
{
	private boolean capturing;

	private Capture currentCapture;
	private final Set<String> ajaxDivs = new HashSet<String>();
	private final Map<String, FullAjaxCaptureResult> captureMap = new HashMap<String, FullAjaxCaptureResult>();
	private final Map<String, List<FullAjaxCaptureResult>> lists = new HashMap<String, List<FullAjaxCaptureResult>>();
	private final IdentityHashMap<PreRenderable, PreRenderable> preRenderedCapture = new IdentityHashMap<PreRenderable, PreRenderable>();
	private final PreRenderContext innerContext;
	private JSONResponseCallback responseCallback;
	private final Set<CssInclude> cssFiles = new LinkedHashSet<CssInclude>();
	private final Set<String> jsFiles = new LinkedHashSet<String>();
	private final List<JSStatements> headerStatements = new ArrayList<JSStatements>();
	private boolean bodyCapturing;
	private BookmarkEvent formBookmarkEvent;

	public StandardAjaxRenderContext(RenderContext renderContext)
	{
		super(renderContext);
		renderContext.setRootRenderContext(this);
		renderContext.setAttribute(AjaxRenderContext.class, this);
		this.innerContext = (PreRenderContext) renderContext;
	}

	@Override
	public HeaderHelper getHelper()
	{
		return innerContext.getHelper();
	}

	@Override
	public String getModalId()
	{
		return innerContext.getModalId();
	}

	@Override
	public SectionResult getRenderedBody()
	{
		return innerContext.getRenderedBody();
	}

	@Override
	public SectionRenderable getRenderedResponse()
	{
		return innerContext.getRenderedResponse();
	}

	@Override
	public RenderResultListener getRootResultListener()
	{
		return this;
	}

	@Override
	public String getSemiModalId()
	{
		return innerContext.getSemiModalId();
	}

	@Override
	public void preRender(Collection<? extends PreRenderable> preRenderers)
	{
		innerContext.preRender(preRenderers);
	}

	@Override
	public void preRender(PreRenderable... preRenderers)
	{
		innerContext.preRender(preRenderers);
	}

	@Override
	public void preRender(@Nullable PreRenderable preRenderer)
	{
		if( preRenderer == null )
		{
			return;
		}
		if( capturing )
		{
			if( !preRenderedCapture.containsKey(preRenderer) )
			{
				preRenderedCapture.put(preRenderer, preRenderer);
				preRenderer.preRender(this);
			}
		}
		else
		{
			innerContext.preRender(preRenderer);
		}
	}

	@Override
	public void setModalId(String modalId)
	{
		innerContext.setModalId(modalId);
	}

	@Override
	public void setRenderedBody(SectionResult renderedBody)
	{
		innerContext.setRenderedBody(renderedBody);
	}

	@Override
	public void setRenderedResponse(SectionRenderable renderedResponse)
	{
		innerContext.setRenderedResponse(renderedResponse);
	}

	@Override
	public void setRootResultListener(RenderResultListener rootResultListener)
	{
		innerContext.setRootResultListener(rootResultListener);
	}

	@Override
	public void setSemiModalId(String semiModalId)
	{
		innerContext.setSemiModalId(semiModalId);
	}

	@Override
	@SuppressWarnings("nls")
	public FullDOMResult getFullDOMResult()
	{
		FullDOMResult domResult = new FullDOMResult();
		StringBuilder sbuf = new StringBuilder();
		capturing = true;
		for( JSStatements statement : preRenderPageScripts() )
		{
			sbuf.append(statement.getStatements(this));
		}
		domResult.setScript(sbuf.toString());
		domResult.setHtml(captureMap);
		domResult.setLists(lists);
		List<String> css = Lists.newArrayList();
		boolean rtl = CurrentLocale.isRightToLeft();
		boolean newLayout = RenderNewTemplate.isNewLayout(this);
		for( CssInclude ci : cssFiles )
		{
			if( rtl && ci.isHasRtl() )
			{
				css.add(ci.getRtlHref(this));
			}
			else
			{
				css.add(ci.getHref(this));
			}
		}
		domResult.setCss(css);
		domResult.setJs(new ArrayList<String>(jsFiles));
		if( formBookmarkEvent != null )
		{
			InfoFormAction bookmark = new InfoFormAction(new InfoBookmark(this, formBookmarkEvent));
			Map<String, Object> formParams = new HashMap<String, Object>();
			FormTag formTag = getForm();
			formParams.put("action", bookmark.getFormAction());
			formParams.put("state", bookmark.getHiddenState());
			formParams.put("partial", formBookmarkEvent.isPartial());
			if( formTag.getEncoding() != null )
			{
				formParams.put("encoding", formTag.getEncoding());
			}
			formParams.put("id", formTag.getElementId(this));
			domResult.setFormParams(formParams);
		}
		return domResult;
	}

	private List<JSStatements> dequeStatements()
	{
		List<JSStatements> allStatements = new ArrayList<JSStatements>(headerStatements);
		headerStatements.clear();
		return allStatements;
	}

	@SuppressWarnings("nls")
	private List<JSStatements> preRenderPageScripts()
	{
		LinkedList<JSStatements> renderedStatements = new LinkedList<JSStatements>();
		int iterations = 0;
		List<JSStatements> origStatements = dequeStatements();
		while( !origStatements.isEmpty() )
		{
			renderedStatements.addAll(0, origStatements);
			preRender(origStatements);
			origStatements = dequeStatements();

			if( ++iterations > 10 )
			{
				throw new SectionsRuntimeException("10 looks like infinity");
			}
		}
		return renderedStatements;
	}

	public SimpleDOMResult getSimpleDOMResult()
	{
		FullDOMResult fullDOMResult = getFullDOMResult();
		FullAjaxCaptureResult first = captureMap.values().iterator().next();
		return new SimpleDOMResult(fullDOMResult, first.getHtml(), first.getScript());
	}

	@Override
	public boolean isCurrentlyCapturing()
	{
		return currentCapture != null;
	}

	@Override
	public Writer startCapture(Writer writer, String divId, Map<String, Object> params, boolean collection)
	{
		if( ajaxDivs.contains(divId) )
		{
			if( currentCapture != null )
			{
				throw new SectionsRuntimeException("Can't capture recursively"); //$NON-NLS-1$
			}
			StringWriter swriter = new StringWriter();
			currentCapture = new Capture(divId, swriter, collection, params);
			capturing = true;
			return swriter;
		}
		return writer;
	}

	@Override
	public void endCapture(String divId)
	{
		if( currentCapture == null || !currentCapture.getDivId().equals(divId) )
		{
			return;
		}
		List<JSStatements> statements = currentCapture.getStatements();
		StringBuilder sbuf = new StringBuilder();
		preRender(statements);
		// Comments in the statements can ruin everything
		for( JSStatements statement : statements )
		{
			sbuf.append(statement.getStatements(this));
			sbuf.append("\n");
		}
		assert (currentCapture.getStatements().isEmpty());
		FullAjaxCaptureResult result = new FullAjaxCaptureResult(currentCapture.getWriter().toString(),
			sbuf.toString(), divId, currentCapture.getParams());
		if( currentCapture.isCollection() )
		{
			List<FullAjaxCaptureResult> col = lists.get(divId);
			if( col == null )
			{
				col = new ArrayList<FullAjaxCaptureResult>();
				lists.put(divId, col);
			}
			col.add(result);
		}
		else
		{
			captureMap.put(divId, result);
		}
		capturing = false;
		currentCapture = null;
	}

	@Override
	public void captureResources(PreRenderable... preRenderables)
	{
		boolean oldCapture = capturing;
		capturing = true;
		preRender(preRenderables);
		capturing = oldCapture;
	}

	@Override
	public Map<String, FullAjaxCaptureResult> getAllCaptures()
	{
		return captureMap;
	}

	private static class Capture
	{
		private final Writer writer;
		private final boolean collection;
		private final Map<String, Object> params;
		private final List<JSStatements> statements = new ArrayList<JSStatements>();
		private final String divId;

		public Capture(String divId, Writer writer, boolean collection, Map<String, Object> params)
		{
			this.divId = divId;
			this.writer = writer;
			this.collection = collection;
			this.params = params;
		}

		public Writer getWriter()
		{
			return writer;
		}

		public boolean isCollection()
		{
			return collection;
		}

		public Map<String, Object> getParams()
		{
			return params;
		}

		public void addStatement(JSStatements statements)
		{
			this.statements.add(statements);
		}

		public List<JSStatements> getStatements()
		{
			List<JSStatements> retList = new ArrayList<JSStatements>(statements);
			statements.clear();
			return retList;
		}

		public String getDivId()
		{
			return divId;
		}
	}

	@Override
	public boolean isRenderHeader()
	{
		return false;
	}

	@Override
	public <L extends EventListener> void processEvent(SectionEvent<L> event)
	{
		try
		{
			info.processEvent(event);
		}
		catch( Exception e )
		{
			throw new AjaxException(e);
		}
	}

	@Override
	public void returnResult(SectionResult result, String fromId)
	{
		try
		{
			RenderResultListener rootResultListener = innerContext.getRootResultListener();
			if( responseCallback == null )
			{
				rootResultListener.returnResult(result, fromId);
			}
			else
			{
				SectionUtils.renderToWriter(this, (SectionRenderable) result, new DevNullWriter());
				Object responseObject = responseCallback.getResponseObject(this);
				if( responseObject == null )
				{
					return;
				}
				if( responseObject instanceof SectionRenderable )
				{
					rootResultListener.returnResult((SectionResult) responseObject, fromId);
				}
				else
				{
					rootResultListener.returnResult(new JSONRenderer(responseObject, true), fromId);
				}
			}
		}
		catch( IOException e )
		{
			throw new AjaxException(e);
		}
	}

	@Override
	public void setJSONResponseCallback(JSONResponseCallback responseCallback)
	{
		this.responseCallback = responseCallback;
	}

	@Override
	public void addAjaxDivs(String... ajaxIds)
	{
		addAjaxDivs(Arrays.asList(ajaxIds));

	}

	@Override
	public void addAjaxDivs(Collection<String> ajaxIds)
	{
		ajaxDivs.addAll(ajaxIds);
		if( !bodyCapturing && ajaxDivs.contains(AjaxGenerator.AJAXID_BODY) )
		{
			bodyCapturing = true;
			BodyTag bodyTag = getBody();
			bodyTag.setRenderer(new WrappedNestedRenderable(bodyTag.getRenderer())
			{
				@Override
				public NestedRenderable setNestedRenderable(SectionRenderable nested)
				{
					return super.setNestedRenderable(new AjaxCaptureRenderer(AjaxGenerator.AJAXID_BODY, nested));
				}
			});
			InnerBodyEvent.ensureRegistered(info);
		}
	}

	@Override
	public void addCss(String src)
	{
		if( capturing )
		{
			cssFiles.add(new CssInclude(src));
		}
		else
		{
			innerContext.addCss(src);
		}
	}

	@Override
	public void addCss(CssInclude css)
	{
		if( capturing )
		{
			cssFiles.add(css);
		}
		else
		{
			innerContext.addCss(css);
		}
	}

	@Override
	public void addJs(String src)
	{
		if( capturing )
		{
			jsFiles.add(src);
		}
		else
		{
			innerContext.addJs(src);
		}
	}

	@Override
	public void addStatements(JSStatements statements)
	{
		if( capturing )
		{
			headerStatements.add(statements);
		}
	}

	@Override
	public void addReadyStatements(JSStatements statements)
	{
		if( capturing )
		{
			currentCapture.addStatement(statements);
		}
	}

	@Override
	public void addFooterStatements(JSStatements statements)
	{
		if( capturing )
		{
			currentCapture.addStatement(statements);
		}
	}

	@Override
	public void addHeaderMarkup(String head)
	{
		innerContext.addHeaderMarkup(head);
	}

	@Override
	public void bindHandler(String event, Map<String, String> attrs, JSHandler handler)
	{
		innerContext.bindHandler(event, attrs, handler);
	}

	@Override
	public BodyTag getBody()
	{
		return innerContext.getBody();
	}

	@Override
	public FormTag getForm()
	{
		return innerContext.getForm();
	}

	@Override
	public PreRenderContext getPreRenderContext()
	{
		return innerContext.getPreRenderContext();
	}

	@Override
	public boolean isRenderingAjaxDiv(String divId)
	{
		return ajaxDivs.contains(divId);
	}

	@Override
	public void setFormBookmarkEvent(BookmarkEvent event)
	{
		this.formBookmarkEvent = event;
	}
}
