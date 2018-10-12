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

package com.tle.web.sections.generic;

import java.util.EventListener;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.SectionEventFilter;

@NonNullByDefault
public abstract class WrappedSectionInfo implements SectionInfo
{
	protected SectionInfo info;

	public WrappedSectionInfo(SectionInfo info)
	{
		this.info = info;
	}

	@Override
	public void addEventFilter(SectionEventFilter filter)
	{
		info.addEventFilter(filter);
	}

	@Override
	public void clearModel(SectionId id)
	{
		info.clearModel(id);
	}

	@Override
	public boolean containsId(String id)
	{
		return info.containsId(id);
	}

	@Override
	public SectionInfo createForward(String path, Map<Object, Object> attributes)
	{
		return info.createForward(path, attributes);
	}

	@Override
	public SectionInfo createForward(String path)
	{
		return info.createForward(path);
	}

	@Override
	public void forward(SectionInfo forward)
	{
		info.forward(forward);
	}

	@Override
	public void forwardAsBookmark(SectionInfo forward)
	{
		info.forwardAsBookmark(forward);
	}

	@Override
	public void forwardToUrl(String url, int code)
	{
		info.forwardToUrl(url, code);
	}

	@Override
	public void forwardToUrl(String url)
	{
		info.forwardToUrl(url);
	}

	@Nullable
	@Override
	public <T> T getAttribute(Object name)
	{
		return info.<T>getAttribute(name);
	}

	@Nullable
	@Override
	public <T> T getAttributeForClass(Class<T> clazz)
	{
		return info.<T>getAttributeForClass(clazz);
	}

	@Nullable
	@Override
	public <T extends SectionId, S extends T> S lookupSection(Class<T> clazz)
	{
		return info.lookupSection(clazz);
	}

	@Override
	public <T extends SectionId, S extends T> List<S> lookupSections(Class<T> clazz)
	{
		return info.lookupSections(clazz);
	}

	@Override
	public <T> T getAttributeSafe(Object name, Class<?> clazz)
	{
		return info.<T>getAttributeSafe(name, clazz);
	}

	@Override
	public Bookmark getPublicBookmark()
	{
		return info.getPublicBookmark();
	}

	@Override
	public boolean getBooleanAttribute(Object name)
	{
		return info.getBooleanAttribute(name);
	}

	@Override
	@Deprecated
	public SectionContext getContextForId(String id)
	{
		return info.getContextForId(id);
	}

	@Override
	public <T> T getLayout(String id)
	{
		return info.<T>getLayout(id);
	}

	@Override
	public void removeListeners(String target)
	{
		info.removeListeners(target);
	}

	@Override
	public <T> T getModelForId(String id)
	{
		return info.<T>getModelForId(id);
	}

	@Override
	public RenderContext getRootRenderContext()
	{
		return info.getRootRenderContext();
	}

	@Nullable
	@Override
	public HttpServletRequest getRequest()
	{
		return info.getRequest();
	}

	@Nullable
	@Override
	public HttpServletResponse getResponse()
	{
		return info.getResponse();
	}

	@Override
	public String getRootId()
	{
		return info.getRootId();
	}

	@Nullable
	@Override
	public <T> T getTreeAttribute(Object key)
	{
		return info.<T>getTreeAttribute(key);
	}

	@Nullable
	@Override
	public <T> T getTreeData(SectionId id)
	{
		return info.<T>getTreeData(id);
	}

	@Override
	public <T> List<T> getTreeListAttribute(Object key)
	{
		return info.<T>getTreeListAttribute(key);
	}

	@Override
	public String getWrappedRootId(SectionId id)
	{
		return info.getWrappedRootId(id);
	}

	@Override
	public boolean isRendered()
	{
		return info.isRendered();
	}

	@Override
	public void preventGET()
	{
		info.preventGET();
	}

	@Override
	public <L extends EventListener> void processEvent(SectionEvent<L> event, SectionTree tree)
	{
		info.processEvent(event, tree);
	}

	@Override
	public <L extends EventListener> void processEvent(SectionEvent<L> event)
	{
		info.processEvent(event);
	}

	@Override
	public <L extends EventListener> void queueEvent(SectionEvent<L> event)
	{
		info.queueEvent(event);
	}

	@Override
	public void queueTreeEvents(SectionTree tree)
	{
		info.queueTreeEvents(tree);
	}

	@Override
	public void renderNow()
	{
		info.renderNow();
	}

	@Override
	public void setAttribute(Object name, @Nullable Object attribute)
	{
		info.setAttribute(name, attribute);
	}

	@Override
	public void setRendered()
	{
		info.setRendered();
	}

	@Nullable
	@Override
	public SectionId getSectionForId(SectionId id)
	{
		return info.getSectionForId(id);
	}

	@Override
	public void setRootRenderContext(RenderContext renderContext)
	{
		info.setRootRenderContext(renderContext);
	}

	@Override
	public boolean isForceRender()
	{
		return info.isForceRender();
	}

	@Override
	public void forceRedirect()
	{
		info.forceRedirect();
	}

	@Override
	public boolean isForceRedirect()
	{
		return info.isForceRedirect();
	}

	@Override
	public List<SectionId> getChildIds(SectionId id)
	{
		return info.getChildIds(id);
	}

	@Override
	public List<SectionId> getAllChildIds(SectionId id)
	{
		return info.getAllChildIds(id);
	}

	@Override
	public boolean isReal()
	{
		return info.isReal();
	}

	@Override
	public boolean isErrored()
	{
		return info.isErrored();
	}

	@Override
	public void setErrored()
	{
		info.setErrored();
	}

	@Override
	public Map<String, String[]> getParameterMap()
	{
		return info.getParameterMap();
	}
}
