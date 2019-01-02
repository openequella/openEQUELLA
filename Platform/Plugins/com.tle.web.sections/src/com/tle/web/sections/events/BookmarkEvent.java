/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.events;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.tle.web.sections.BookmarkContextHolder;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.generic.InfoBookmark;

/**
 * Event for creating {@code Bookmarks}.
 * <p>
 * {@code BookmarkEvent}'s are used for generating URLs (or Name Value maps)
 * from a {@link SectionInfo}. They are used by the system for 2 particular
 * cases:
 * <ul>
 * <li>Generating a URL to forward to for <a
 * href="http://en.wikipedia.org/wiki/Post/Redirect/Get">PRG</a>.
 * <p>
 * After a POST request comes in and all events have been processed, a URL is
 * generated to redirect to, by posting a {@code BookmarkEvent}.</li>
 * <li>Generating a Name/Value map for creating hidden {@code <input>} form
 * fields.
 * <p>
 * At the end of the render process, a {@code BookmarkEvent} is posted with the
 * {@code BookmarkEvent#isRendering()} flag set, which will collect Names/Values
 * for any {@code Model} state which is not being rendered to {@code <input>}
 * tags.</li>
 * </ul>
 * {@link BookmarkEventListener}'s are passed the {@code BookmarkEvent} and make
 * calls to {@link #setParam(String, String)} or
 * {@link #setParams(String, Collection)} to modify the Name/Value map. A URL
 * can be created from the Name/Value map ({@link #getParams()}) by URL encoding
 * and adding a URL path/host component.
 * 
 * @see BookmarkEventListener
 * @see Bookmarked
 * @see InfoBookmark
 * @see PathGenerator
 * @see ParametersEvent
 * @author jmaginnis
 */
public class BookmarkEvent extends AbstractTargettedEvent<BookmarkEvent, BookmarkEventListener>
{
	public static final String CONTEXT_BROWSERURL = "browserUrl"; //$NON-NLS-1$
	public static final String CONTEXT_SESSION = "session"; //$NON-NLS-1$
	public static final String CONTEXT_SUPPORTED = "supported"; //$NON-NLS-1$
	public static final String CONTEXT_SITEWIDE = "site"; //$NON-NLS-1$
	private static final String[] NO_VALS = new String[]{};

	private final Map<String, String[]> params;
	private final BookmarkContextHolder contextHolder;
	private Set<String> existing;
	private final boolean rendering;

	/**
	 * Create this event with the rendering flag set to {@code false}.
	 */
	public BookmarkEvent()
	{
		this(null, false, null);
	}

	@SuppressWarnings("unchecked")
	public BookmarkEvent(SectionId sectionId, boolean rendering, SectionInfo info)
	{
		super(sectionId);
		params = new LinkedHashMap<String, String[]>();
		contextHolder = new BookmarkContextHolder();
		this.rendering = rendering;

		if( info != null )
		{
			this.existing = info.getRequest().getParameterMap().keySet();
		}
		else
		{
			this.existing = null;
		}
	}

	public BookmarkEvent(String... mustContexts)
	{
		this();
		Set<String> mustSet = new HashSet<String>();
		Collections.addAll(mustSet, mustContexts);
		contextHolder.setOnlyForContext(mustSet);
	}

	public Map<String, String[]> getBookmarkState()
	{
		return params;
	}

	/**
	 * Gets the rendering flag.
	 * 
	 * @return Whether or not this is a rendering event
	 */
	public boolean isRendering()
	{
		return rendering;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, BookmarkEventListener listener) throws Exception
	{
		listener.bookmark(info, this);
	}

	@Override
	public Class<BookmarkEventListener> getListenerClass()
	{
		return BookmarkEventListener.class;
	}

	/**
	 * Set a single parameter value. If the value is {@code null} ignore, else
	 * set the single string value as an array of 1 string containing just the
	 * value.
	 * 
	 * @param id The key/id
	 * @param value The value
	 */
	public void setParam(String id, String value)
	{
		if( value != null )
		{
			params.put(id, new String[]{value});
		}
		else if( existing != null && existing.contains(id) )
		{
			params.put(id, NO_VALS);
		}
		else
		{
			params.remove(id);
		}
	}

	public void setParams(String paramId, String[] vals)
	{
		if( vals == null )
		{
			vals = NO_VALS;
		}
		if( vals.length == 0 && (existing == null || !existing.contains(paramId)) )
		{
			params.remove(paramId);
		}
		else
		{
			params.put(paramId, vals);
		}
	}

	/**
	 * Set a collection of strings into the parameter map. If the collection is
	 * {@code null} or {@code empty} ignore, else {@code toArray} the collection
	 * and set that as the value in the map.
	 * 
	 * @param id The key/id
	 * @param vals The collection to set
	 */
	public void setParams(String id, Collection<String> vals)
	{
		if( vals != null && !vals.isEmpty() )
		{
			params.put(id, vals.toArray(new String[vals.size()]));
		}
		else if( existing != null && existing.contains(id) )
		{
			params.put(id, NO_VALS);
		}
		else
		{
			params.remove(id);
		}
	}

	public boolean isAllowedInThisContext(BookmarkContextHolder contextHolder)
	{
		return this.contextHolder.matches(contextHolder);
	}

	public void setIgnoredContexts(String... contexts)
	{
		Set<String> contextSet = new HashSet<String>();
		Collections.addAll(contextSet, contexts);
		contextHolder.setIgnoreForContext(contextSet);
	}

	public void setContexts(String... contexts)
	{
		Set<String> contextSet = new HashSet<String>();
		Collections.addAll(contextSet, contexts);
		contextHolder.setContexts(contextSet);
	}

	public boolean isPartial()
	{
		return existing != null;
	}
}
