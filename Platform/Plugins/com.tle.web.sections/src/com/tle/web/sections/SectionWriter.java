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

package com.tle.web.sections;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.Utils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderResultListener;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.SectionEventFilter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.header.BodyTag;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.header.HeaderHelper;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;

/**
 * A thin wrapper around a {@code java.io.PrintWriter} which extends it with
 * convenience methods for writing out HTML tags.
 * <p>
 * An example:
 * 
 * <pre>
 * SectionWriter writer;
 * ...
 * writer.writeTag("div", "class", "info");
 * writer.writeText("&lt;Hello>");
 * writer.endTag("div");
 * </pre>
 * 
 * Will write:
 * <p>
 * <code>&lt;div class="info">&amp;lt;Hello&amp;gt;&lt;/div></code>
 * 
 * @author jmaginnis
 */
public class SectionWriter extends PrintWriter implements PreRenderContext
{
	private final PreRenderContext renderContext;

	public SectionWriter(Writer out, RenderContext info)
	{
		super(out);
		if( info instanceof SectionWriter )
		{
			this.renderContext = (PreRenderContext) ((SectionWriter) info).getInfo();
		}
		else
		{
			this.renderContext = info.getPreRenderContext();
		}
	}

	@Deprecated
	public RenderContext getInfo()
	{
		return renderContext;
	}

	/**
	 * Write an opening tag with no attributes. <br>
	 * 
	 * @param tag
	 * @throws IOException
	 */
	public void writeTag(String tag) throws IOException
	{
		Map<String, String> empty = Collections.emptyMap();
		writeTag(tag, empty);
	}

	/**
	 * Write an end tag.
	 * 
	 * @param tag The tag to end
	 * @throws IOException
	 */
	public void endTag(String tag) throws IOException
	{
		out.write("</"); //$NON-NLS-1$
		out.write(tag);
		out.write('>');
	}

	/**
	 * Write out text which will be entity encoded.
	 * 
	 * @param text
	 * @throws IOException
	 */
	public void writeText(String text) throws IOException
	{
		out.write(SectionUtils.ent(text));
	}

	/**
	 * Write an opening tag with the given map of attributes.
	 * 
	 * @param tag The tag to write
	 * @param attributes The attributes as a map
	 * @throws IOException
	 */
	public void writeTag(String tag, Map<String, String> attributes) throws IOException
	{
		writeTag(tag, attributes, false);
	}

	/**
	 * Write a tag with the given varargs attributes.
	 * 
	 * @param tag The tag to write
	 * @param attributes A vararg list of Name/Value's.
	 * @throws IOException
	 */
	public void writeTag(String tag, String... attributes) throws IOException
	{
		out.write('<');
		out.write(tag);
		for( int i = 0; i < attributes.length; i++ )
		{
			String name = attributes[i];
			String value = attributes[++i];
			if( value != null )
			{
				out.append(' ');
				out.append(Utils.ent(name));
				out.append("=\""); //$NON-NLS-1$
				out.append(Utils.ent(value));
				out.append('"');
			}
		}
		out.write('>');
	}

	/**
	 * Write a tag with the option of making it an empty (self closing) tag.
	 * 
	 * @param tag The tag
	 * @param attributes The map of attributes
	 * @param empty Whether or not to write an empty tag
	 * @throws IOException
	 */
	public void writeTag(String tag, Map<String, String> attributes, boolean empty) throws IOException
	{
		out.write('<');
		out.write(tag);
		out.write(SectionUtils.mapToAttributes(attributes));
		if( empty )
		{
			out.write('/');
		}
		out.write('>');
	}

	@Override
	public void addEventFilter(SectionEventFilter filter)
	{
		renderContext.addEventFilter(filter);
	}

	@Override
	public void clearModel(SectionId id)
	{
		renderContext.clearModel(id);
	}

	@Override
	public boolean containsId(String id)
	{
		return renderContext.containsId(id);
	}

	@Override
	public SectionInfo createForward(String path, Map<Object, Object> attributes)
	{
		return renderContext.createForward(path, attributes);
	}

	@Override
	public SectionInfo createForward(String path)
	{
		return renderContext.createForward(path);
	}

	@Override
	public void forward(SectionInfo forward)
	{
		renderContext.forward(forward);
	}

	@Override
	public void forwardAsBookmark(SectionInfo forward)
	{
		renderContext.forwardAsBookmark(forward);
	}

	@Override
	public void forwardToUrl(String url, int code)
	{
		renderContext.forwardToUrl(url, code);
	}

	@Override
	public void forwardToUrl(String url)
	{
		renderContext.forwardToUrl(url);
	}

	@Override
	public List<SectionId> getChildIds(SectionId id)
	{
		return renderContext.getChildIds(id);
	}

	@Override
	public List<SectionId> getAllChildIds(SectionId id)
	{
		return renderContext.getAllChildIds(id);
	}

	@Override
	public <T> T getAttribute(Object name)
	{
		return renderContext.<T>getAttribute(name);
	}

	@Override
	public <T> T getAttributeForClass(Class<T> clazz)
	{
		return renderContext.<T>getAttributeForClass(clazz);
	}

	@Override
	public <T> T getAttributeSafe(Object name, Class<?> clazz)
	{
		return renderContext.<T>getAttributeSafe(name, clazz);
	}

	@Override
	public Bookmark getPublicBookmark()
	{
		return renderContext.getPublicBookmark();
	}

	@Override
	public boolean getBooleanAttribute(Object name)
	{
		return renderContext.getBooleanAttribute(name);
	}

	@Override
	@Deprecated
	public SectionContext getContextForId(String id)
	{
		return renderContext.getContextForId(id);
	}

	@Override
	public HeaderHelper getHelper()
	{
		return renderContext.getHelper();
	}

	@Override
	public <T> T getLayout(String id)
	{
		return renderContext.<T>getLayout(id);
	}

	@Override
	public void removeListeners(String target)
	{
		renderContext.removeListeners(target);
	}

	@Override
	public String getModalId()
	{
		return renderContext.getModalId();
	}

	@Override
	public <T> T getModelForId(String id)
	{
		return renderContext.<T>getModelForId(id);
	}

	@Override
	public RenderContext getRootRenderContext()
	{
		return renderContext.getRootRenderContext();
	}

	@Override
	public SectionResult getRenderedBody()
	{
		return renderContext.getRenderedBody();
	}

	@Override
	public SectionRenderable getRenderedResponse()
	{
		return renderContext.getRenderedResponse();
	}

	@Override
	public HttpServletRequest getRequest()
	{
		return renderContext.getRequest();
	}

	@Override
	public HttpServletResponse getResponse()
	{
		return renderContext.getResponse();
	}

	@Override
	public String getRootId()
	{
		return renderContext.getRootId();
	}

	@Override
	public RenderResultListener getRootResultListener()
	{
		return renderContext.getRootResultListener();
	}

	@Override
	public SectionId getSectionForId(SectionId id)
	{
		return renderContext.getSectionForId(id);
	}

	@Override
	public String getSemiModalId()
	{
		return renderContext.getSemiModalId();
	}

	@Override
	public <T> T getTreeAttribute(Object key)
	{
		return renderContext.<T>getTreeAttribute(key);
	}

	// @Override
	// public <T> T getTreeAttributeForClass(Class<T> clazz)
	// {
	// return renderContext.<T> getTreeAttributeForClass(clazz);
	// }

	@Override
	public <T extends SectionId, S extends T> S lookupSection(Class<T> clazz)
	{
		return renderContext.lookupSection(clazz);
	}

	@Override
	public <T extends SectionId, S extends T> List<S> lookupSections(Class<T> clazz)
	{
		return renderContext.lookupSections(clazz);
	}

	@Override
	public <T> T getTreeData(SectionId id)
	{
		return renderContext.<T>getTreeData(id);
	}

	@Override
	public <T> List<T> getTreeListAttribute(Object key)
	{
		return renderContext.<T>getTreeListAttribute(key);
	}

	@Override
	public String getWrappedRootId(SectionId id)
	{
		return renderContext.getWrappedRootId(id);
	}

	@Override
	public boolean isRendered()
	{
		return renderContext.isRendered();
	}

	@Override
	public void preRender(Collection<? extends PreRenderable> preRenderers)
	{
		renderContext.preRender(preRenderers);
	}

	@Override
	public void preRender(PreRenderable... preRenderers)
	{
		renderContext.preRender(preRenderers);
	}

	@Override
	public void preRender(PreRenderable preRenderer)
	{
		renderContext.preRender(preRenderer);
	}

	@Override
	public void preventGET()
	{
		renderContext.preventGET();
	}

	@Override
	public <L extends EventListener> void processEvent(SectionEvent<L> event, SectionTree tree)
	{
		renderContext.processEvent(event, tree);
	}

	@Override
	public <L extends EventListener> void processEvent(SectionEvent<L> event)
	{
		renderContext.processEvent(event);
	}

	@Override
	public <L extends EventListener> void queueEvent(SectionEvent<L> event)
	{
		renderContext.queueEvent(event);
	}

	@Override
	public void queueTreeEvents(SectionTree tree)
	{
		renderContext.queueTreeEvents(tree);
	}

	@Override
	public void renderNow()
	{
		renderContext.renderNow();
	}

	@Override
	public void setAttribute(Object name, Object attribute)
	{
		renderContext.setAttribute(name, attribute);
	}

	@Override
	public void setModalId(String modalId)
	{
		renderContext.setModalId(modalId);
	}

	@Override
	public void setRendered()
	{
		renderContext.setRendered();
	}

	@Override
	public void setRenderedBody(SectionResult renderedBody)
	{
		renderContext.setRenderedBody(renderedBody);
	}

	@Override
	public void setRenderedResponse(SectionRenderable renderedResponse)
	{
		renderContext.setRenderedResponse(renderedResponse);
	}

	@Override
	public void setRootResultListener(RenderResultListener rootResultListener)
	{
		renderContext.setRootResultListener(rootResultListener);
	}

	@Override
	public void setSemiModalId(String semiModalId)
	{
		renderContext.setSemiModalId(semiModalId);
	}

	@Override
	public void setRootRenderContext(RenderContext renderContext)
	{
		this.renderContext.setRootRenderContext(renderContext);
	}

	@Override
	public boolean isRenderHeader()
	{
		return renderContext.isRenderHeader();
	}

	@Override
	public void addCss(String src)
	{
		renderContext.addCss(src);
	}

	@Override
	public void addCss(CssInclude css)
	{
		renderContext.addCss(css);
	}

	@Override
	public void addFooterStatements(JSStatements statements)
	{
		renderContext.addFooterStatements(statements);
	}

	@Override
	public void addHeaderMarkup(String head)
	{
		renderContext.addHeaderMarkup(head);
	}

	@Override
	public void addJs(String src)
	{
		renderContext.addJs(src);
	}

	@Override
	public void addReadyStatements(JSStatements statements)
	{
		renderContext.addReadyStatements(statements);
	}

	@Override
	public void addStatements(JSStatements statements)
	{
		renderContext.addStatements(statements);
	}

	@Override
	public void bindHandler(String event, Map<String, String> attrs, JSHandler handler)
	{
		renderContext.bindHandler(event, attrs, handler);
	}

	@Override
	public BodyTag getBody()
	{
		return renderContext.getBody();
	}

	@Override
	public FormTag getForm()
	{
		return renderContext.getForm();
	}

	@Override
	public PreRenderContext getPreRenderContext()
	{
		return renderContext.getPreRenderContext();
	}

	public void render(SectionRenderable renderable) throws IOException
	{
		renderContext.preRender(renderable);
		if( renderable != null )
		{
			renderable.realRender(this);
		}
	}

	@Override
	public boolean isForceRender()
	{
		return renderContext.isForceRender();
	}

	@Override
	public void forceRedirect()
	{
		renderContext.forceRedirect();
	}

	@Override
	public boolean isForceRedirect()
	{
		return renderContext.isForceRedirect();
	}

	@Override
	public void close()
	{
		// nothing to close
	}

	@Override
	public boolean isReal()
	{
		return true;
	}

	@Override
	public boolean isErrored()
	{
		return renderContext.isErrored();
	}

	@Override
	public void setErrored()
	{
		renderContext.setErrored();
	}
}
