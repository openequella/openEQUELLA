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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.NoTreeForIdException;
import com.tle.web.sections.PublicBookmarkFactory;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.SimpleSectionId;
import com.tle.web.sections.events.BeforeEventsEvent;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.InfoEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ReadyToRespondEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RespondingEvent;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.SectionEventFilter;
import com.tle.web.sections.events.StandardRenderContext;

@NonNullByDefault
public class DefaultSectionInfo implements MutableSectionInfo
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSectionInfo.class);

	protected final SectionTrees trees = new SectionTrees();
	protected final Map<String, Object> modelMap = new HashMap<String, Object>();
	protected final Queue<SectionEvent<EventListener>> eventQueue = new PriorityQueue<SectionEvent<EventListener>>();
	private final Map<Object, Object> attributeMap = new HashMap<Object, Object>();
	private final List<ParametersEvent> parametersEvents = Lists.newArrayList();
	private final SectionsController controller;

	@Nullable
	protected HttpServletResponse response;
	@Nullable
	protected HttpServletRequest request;
	@Nullable
	private List<SectionEventFilter> eventFilters;
	@Nullable
	private RenderContext renderContext;
	@Nullable
	private Bookmark publicBookmark;

	private boolean rendered;
	private boolean preventGET;
	private boolean parametersFinished;
	private boolean responding;
	private boolean forceRedirect;
	private boolean errored;

	public DefaultSectionInfo(SectionsController controller)
	{
		this.controller = controller;
		attributeMap.put(MutableSectionInfo.class, this);
	}

	@Override
	public boolean isRendered()
	{
		return rendered;
	}

	@Override
	public void setRendered()
	{
		if( rendered )
		{
			throw new SectionsRuntimeException("Already rendered!"); //$NON-NLS-1$
		}
		this.rendered = true;
		processEvent(new RespondingEvent());
		eventQueue.clear();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getTreeData(SectionId id)
	{
		return (T) getTreeForId(id).getData(id.getSectionId());
	}

	@Nullable
	private <T> SectionTree getTreeForId(String id, boolean fatal)
	{
		for( SectionTree tree : trees )
		{
			if( tree.containsId(id) )
			{
				return tree;
			}
		}
		if( !fatal )
		{
			return null;
		}
		throw new NoTreeForIdException("No tree for id:" + id); //$NON-NLS-1$
	}

	@Override
	public boolean containsId(String id)
	{
		return getTreeForId(id, false) != null;
	}

	public Map<String, Object> getModelMap()
	{
		return modelMap;
	}

	@Override
	public SectionContext getContextForId(String id)
	{
		SectionTree tree = getTreeForId(id, true);
		// Not possible
		if( tree == null )
		{
			throw new Error("Null tree");
		}
		Section section = tree.getSectionForId(id);
		return new DefaultSectionContext(section, this, tree, id);
	}

	@Override
	public String getRootId()
	{
		return trees.getRootId();
	}

	private <T extends EventListener> List<Object> getListeners(@Nullable String target, Class<T> clazz)
	{
		List<Object> listenerList = new ArrayList<Object>();
		for( SectionTree tree : trees )
		{
			listenerList.addAll(tree.getListeners(target, clazz));
		}
		return listenerList;
	}

	@Override
	public void removeListeners(String target)
	{
		for( SectionTree tree : trees )
		{
			tree.removeListeners(target);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <L extends EventListener> void queueEvent(@Nullable SectionEvent<L> event)
	{
		if( event != null )
		{
			eventQueue.add((SectionEvent<EventListener>) event);
		}
	}

	@Override
	public void setAttribute(Object name, @Nullable Object attribute)
	{
		attributeMap.put(name, attribute);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> T getAttribute(Object name)
	{
		return (T) attributeMap.get(name);
	}

	@Override
	public boolean getBooleanAttribute(Object name)
	{
		Boolean b = getAttribute(name);
		if( b == null )
		{
			return false;
		}
		return b;
	}

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public <T> T getAttributeSafe(Object name, Class<?> clazz)
	{
		T obj = (T) attributeMap.get(name);
		if( obj == null )
		{
			try
			{
				obj = (T) clazz.newInstance();
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
			attributeMap.put(name, obj);
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> T getTreeAttribute(Object name)
	{
		for( SectionTree tree : trees )
		{
			if( tree.containsKey(name) )
			{
				return (T) tree.getAttribute(name);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getTreeListAttribute(Object name)
	{
		List<T> list = new ArrayList<T>();
		for( SectionTree tree : trees )
		{
			if( tree.containsKey(name) )
			{
				list.addAll((Collection<? extends T>) tree.getAttribute(name));
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T extends SectionId, S extends T> S lookupSection(Class<T> clazz)
	{
		S result = null;
		for( SectionTree tree : trees )
		{
			T section = tree.lookupSection(clazz, null);
			if( section != null )
			{
				// FIXME: This should REALLY chuck an error.
				// We have no guarantee at all that we have the right one
				// if( result != null )
				// {
				// throw new Error("Multiple instances of " + clazz +
				// " found in trees");
				// }
				result = (S) section;
				// FIXME: remove!
				return result;
			}
		}
		return result;
	}

	@Override
	public <T extends SectionId, S extends T> List<S> lookupSections(Class<T> clazz)
	{
		final List<S> result = new ArrayList<>();
		for( SectionTree tree : trees )
		{
			result.addAll(tree.lookupSections(clazz));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> T getAttributeForClass(Class<T> clazz)
	{
		return (T) getAttribute(clazz);
	}

	public void putAttributes(Map<?, ?> properties)
	{
		attributeMap.putAll(properties);
	}

	@Nullable
	@Override
	public HttpServletRequest getRequest()
	{
		return request;
	}

	@Override
	public void setRequest(@Nullable HttpServletRequest request)
	{
		this.request = request;
	}

	@Nullable
	@Override
	public HttpServletResponse getResponse()
	{
		return response;
	}

	@Override
	public void setResponse(@Nullable HttpServletResponse response)
	{
		this.response = response;
	}

	@Override
	public void queueTreeEvents(SectionTree tree)
	{
		List<SectionEvent<? extends EventListener>> appEvents = tree.getApplicationEvents();
		for( SectionEvent<? extends EventListener> sectionEvent : appEvents )
		{
			queueEvent(sectionEvent);
		}
	}

	@Override
	public void addTreeToBottom(SectionTree tree, boolean processParamters)
	{
		trees.add(0, tree);
		putAllAttributes(tree.getRuntimeAttributes());
		processEvent(new InfoEvent(false, processParamters), tree);
		queueTreeEvents(tree);
		if( processParamters )
		{
			for( ParametersEvent paramEvent : parametersEvents )
			{
				processEvent(paramEvent, tree);
			}
		}
		if( parametersFinished )
		{
			processEvent(new BeforeEventsEvent(), tree);
		}
		if( responding )
		{
			processQueue();
		}
	}

	@Override
	public void addTree(SectionTree tree)
	{
		trees.add(tree);
		putAllAttributes(tree.getRuntimeAttributes());
		processEvent(new InfoEvent(false, false), tree);
	}

	private void putAllAttributes(Map<Object, Object> attributes)
	{
		attributeMap.putAll(attributes);
	}

	@Override
	public String getWrappedRootId(SectionId id)
	{
		SectionTree tree = getTreeForId(id);
		int treeIndex = trees.indexOf(tree);
		return trees.get(treeIndex - 1).getRootId();
	}

	private SectionTree getTreeForId(SectionId sectionId)
	{
		SectionTree tree = sectionId.getTree();
		if( tree == null )
		{
			tree = getTreeForId(sectionId.getSectionId(), true);
			if( tree == null )
			{
				// Not possible
				throw new Error("Null tree");
			}
		}
		return tree;
	}

	@Override
	public void removeTree(SectionTree tree)
	{
		trees.remove(tree);
		processEvent(new InfoEvent(true, false), tree);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getModelForId(String id)
	{
		Object model = modelMap.get(id);
		if( model == null )
		{
			try
			{
				SectionTree tree = getTreeForId(id, true);
				if( tree == null )
				{
					// Not possible
					throw new Error("Null tree");
				}
				Section section = tree.getSectionForId(id);
				model = section.instantiateModel(this);
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
			modelMap.put(id, model);
		}
		return (T) model;

	}

	@Override
	public void processQueue()
	{
		while( !eventQueue.isEmpty() )
		{
			SectionEvent<EventListener> event = eventQueue.remove();
			processEvent(event);
		}
	}

	@Override
	public <L extends EventListener> void processEvent(SectionEvent<L> event)
	{
		processEvent(event, null);
	}

	@Override
	public List<SectionId> getChildIds(SectionId id)
	{
		return getTreeForId(id).getChildIds(id.getSectionId());
	}

	@Override
	public List<SectionId> getAllChildIds(SectionId id)
	{
		return getTreeForId(id).getAllChildIds(id.getSectionId());
	}

	@Override
	public void renderNow()
	{
		eventQueue.clear();
	}

	@SuppressWarnings({"unchecked", "nls"})
	@Override
	public <L extends EventListener> void processEvent(SectionEvent<L> event, @Nullable SectionTree tree)
	{
		try
		{
			Class<? extends EventListener> listenerClass = event.getListenerClass();
			if( listenerClass == null )
			{
				event.beforeFiring(this, null);
				event.fire(event.getForSectionId(), this, null);
				if( !event.isAbortProcessing() )
				{
					event.finishedFiring(this, null);
				}
				return;
			}
			event.beforeFiring(this, tree);
			if( event.isAbortProcessing() )
			{
				return;
			}
			List<Object> listenersList;
			String listenerId = event.getListenerId();
			if( tree != null )
			{
				listenersList = tree.getListeners(listenerId, listenerClass);
			}
			else
			{
				listenersList = getListeners(listenerId, listenerClass);
			}
			boolean abort = false;
			Exception firstThrowable = null;
			if( !event.isStopProcessing() )
			{
				for( Object listenerTarget : listenersList )
				{
					L listener = null;
					SectionId sectionId = event.getForSectionId();
					if( listenerTarget instanceof String )
					{
						sectionId = new SimpleSectionId((String) listenerTarget);
						listener = (L) getSectionForId(sectionId);
					}
					else
					{
						if( listenerTarget instanceof SectionId )
						{
							sectionId = ((SectionId) listenerTarget);
						}
						listener = (L) listenerTarget;
					}
					if( eventFilters != null )
					{
						for( SectionEventFilter filter : eventFilters )
						{
							if( !filter.shouldFire(sectionId, event, listener) )
							{
								continue;
							}
						}
					}

					if( event.isContinueAfterException() )
					{
						try
						{
							event.fire(sectionId, this, listener);
						}
						catch( Exception t )
						{
							if( firstThrowable == null )
							{
								firstThrowable = t;
							}
							else
							{
								LOGGER.error("More than one exception thrown from event handling:", t);
							}
						}
					}
					else
					{
						event.fire(sectionId, this, listener);
					}
					if( event.isAbortProcessing() )
					{
						abort = true;
						break;
					}
					if( event.isStopProcessing() )
					{
						break;
					}
				}
			}
			if( firstThrowable != null )
			{
				throw firstThrowable;
			}
			if( abort )
			{
				return;
			}
			event.finishedFiring(this, tree);
		}
		catch( Exception e )
		{
			controller.handleException(this, e, event);
		}
		return;
	}

	@Override
	public void forward(SectionInfo forward)
	{
		controller.forward(this, forward);
	}

	@Override
	public void forwardToUrl(String url, int code)
	{
		controller.forwardToUrl(this, url, code);
	}

	@Override
	public void forwardToUrl(String url)
	{
		controller.forwardToUrl(this, url, 302);
	}

	@SuppressWarnings("nls")
	@Override
	public void forwardAsBookmark(SectionInfo forward)
	{
		if( forward.getAttributeForClass(MutableSectionInfo.class) == this )
		{
			throw new SectionsRuntimeException("You can't forward to yourself");
		}
		controller.forwardAsBookmark(this, forward);
	}

	@Override
	public SectionInfo createForward(String path)
	{
		return createForward(path, null);
	}

	@Override
	public SectionInfo createForward(String path, @Nullable Map<Object, Object> attributes)
	{
		String thisPath = getAttribute(SectionInfo.KEY_PATH);
		return controller.createInfo(SectionUtils.relativePath(thisPath, path), request, response, this, null,
			attributes);
	}

	@Override
	public void addEventFilter(SectionEventFilter filter)
	{
		if( eventFilters == null )
		{
			eventFilters = new ArrayList<SectionEventFilter>();
		}
		eventFilters.add(filter);
	}

	@Override
	public Bookmark getPublicBookmark()
	{
		if( publicBookmark == null )
		{
			PublicBookmarkFactory factory = lookupSection(PublicBookmarkFactory.class);
			if( factory != null )
			{
				publicBookmark = factory.getPublicBookmark(this);
			}
			else
			{
				BookmarkEvent bookmarkEvent = new BookmarkEvent();
				bookmarkEvent.setContexts(BookmarkEvent.CONTEXT_BROWSERURL);
				publicBookmark = new InfoBookmark(this, bookmarkEvent);
			}
		}
		return publicBookmark;
	}

	@Override
	public void clearModel(SectionId id)
	{
		modelMap.remove(id.getSectionId());
	}

	@Override
	public void preventGET()
	{
		preventGET = true;
	}

	@Override
	public RenderContext getRootRenderContext()
	{
		if( renderContext == null )
		{
			renderContext = new StandardRenderContext(this);
		}
		return renderContext;
	}

	@Override
	public void setRootRenderContext(RenderContext renderContext)
	{
		this.renderContext = renderContext;
	}

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public <T> T getLayout(String id)
	{
		return (T) getTreeForId(id, true).getLayout(id);
	}

	/**
	 * Yeah, I only wrote this for the 'toString' method :)
	 * 
	 * @author aholland
	 */
	protected static class SectionTrees implements Iterable<SectionTree>
	{
		private final List<SectionTree> trees = new ArrayList<SectionTree>();

		public void add(SectionTree tree)
		{
			trees.add(tree);
		}

		public void add(int index, SectionTree tree)
		{
			trees.add(index, tree);
		}

		public SectionTree get(int index)
		{
			return trees.get(index);
		}

		public boolean remove(SectionTree tree)
		{
			return trees.remove(tree);
		}

		public int indexOf(SectionTree tree)
		{
			return trees.indexOf(tree);
		}

		@SuppressWarnings("nls")
		@Override
		public String toString()
		{
			StringBuilder s = new StringBuilder();
			boolean first = true;
			for( SectionTree tree : trees )
			{
				if( !first )
				{
					s.append("\n");
				}
				s.append("Tree '" + tree.getRootId() + "'\n");
				s.append(tree.toString());
				first = false;
			}

			return s.toString();
		}

		public String getRootId()
		{
			return trees.get(trees.size() - 1).getRootId();
		}

		@Override
		public Iterator<SectionTree> iterator()
		{
			return trees.iterator();
		}

		public List<SectionTree> getList()
		{
			return trees;
		}
	}

	@Override
	public Section getSectionForId(SectionId id)
	{
		Section section = id.getSectionObject();
		if( section == null )
		{
			return getTreeForId(id).getSectionForId(id.getSectionId());
		}
		return section;
	}

	@Override
	public boolean isForceRender()
	{
		return preventGET;
	}

	@SuppressWarnings("null")
	@Override
	public SectionTree getRootTree()
	{
		return getTreeForId(getRootId(), true);
	}

	@Override
	public void fireBeforeEvents()
	{
		if( !parametersFinished )
		{
			parametersFinished = true;
			processEvent(new BeforeEventsEvent());
		}
	}

	@Override
	public void fireReadyToRespond(boolean redirect)
	{
		if( !responding )
		{
			responding = true;
			processEvent(new ReadyToRespondEvent(redirect));
		}
	}

	@Override
	public boolean isForceRedirect()
	{
		return forceRedirect;
	}

	@Override
	public void forceRedirect()
	{
		this.forceRedirect = true;
	}

	@Override
	public List<SectionId> getRootIds()
	{
		List<SectionId> ids = new ArrayList<SectionId>();
		Iterator<SectionTree> iter = trees.iterator();
		while( iter.hasNext() )
		{
			ids.add(new SimpleSectionId(iter.next().getRootId()));
		}
		return ids;
	}

	@Override
	public List<SectionTree> getTrees()
	{
		return trees.getList();
	}

	@Override
	public void addParametersEvent(ParametersEvent event)
	{
		parametersEvents.add(event);
	}

	@Override
	public boolean isReal()
	{
		return true;
	}

	@Override
	public boolean isErrored()
	{
		return errored;
	}

	@Override
	public void setErrored()
	{
		errored = true;
	}

  @Override
  public Map<String, String[]> getParameterMap()
  {
    Map<String, String[]> paramMap = new HashMap<>();
	for( ParametersEvent paramEvent : parametersEvents )
	{
	   paramMap.putAll(paramEvent.getParameterMap());
	}
	return paramMap;
  }
}
