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

package com.tle.web.sections.render;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.ElementId;

/**
 * A {@link SectionRenderable} class that can render a {@link TagState}
 * instance.
 * <p>
 * Styles and attributes can be overridden in the renderer, but the TagRenderer
 * must <b>NOT</b> modify the {@code TagState} instance, as the {@code TagState}
 * could be rendered by more than one renderer, or used in more than one web
 * request.
 * 
 * @author jmaginnis
 */
@NonNullByDefault
public class TagRenderer extends AbstractWrappedElementId implements NestedRenderable, StyleableRenderer
{
	protected TagState tagState;
	@Nullable
	protected Map<Object, Object> attrs;
	@Nullable
	protected Map<String, String> data;
	@Nullable
	protected SectionRenderable nestedRenderable;
	@Nullable
	protected String style;
	@Nullable
	protected Set<String> styleClasses;
	private String tag;

	public TagRenderer(String tag, TagState state)
	{
		super(state);
		this.tag = tag;
		this.tagState = state;
	}

	public TagRenderer(String tag, TagState state, SectionRenderable nestedRenderable)
	{
		this(tag, state);
		this.nestedRenderable = nestedRenderable;
	}

	public ElementId getVisibleElementId()
	{
		return this;
	}

	@Override
	public TagRenderer addClass(@Nullable String extraClass)
	{
		if( extraClass != null )
		{
			if( styleClasses == null )
			{
				styleClasses = new LinkedHashSet<String>();
			}
			styleClasses.add(extraClass);
		}
		return this;
	}

	@SuppressWarnings("nls")
	public static void addClass(Map<String, String> attrs, String styleClass)
	{
		String clazz = attrs.get("class");
		if( clazz == null )
		{
			clazz = styleClass;
		}
		else
		{
			clazz = clazz + ' ' + styleClass;
		}
		attrs.put("class", clazz);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(Object key)
	{
		if( attrs != null && attrs.containsKey(key) )
		{
			return (T) attrs.get(key);
		}
		return (T) tagState.getAttribute(key);
	}

	public void setAttribute(Object key, Object value)
	{
		if( attrs == null )
		{
			attrs = new HashMap<Object, Object>();
		}
		attrs.put(key, value);
	}

	@Nullable
	private Map<String, String> getData()
	{
		Map<String, String> stateData = tagState.getData();
		if( stateData != null )
		{
			if( data == null )
			{
				data = Maps.newHashMap();
			}
			data.putAll(stateData);
		}
		return data;
	}

	/**
	 * @param key
	 * @return
	 */
	public String getData(String key)
	{
		if( data != null && data.containsKey(key) )
		{
			return data.get(key);
		}
		return tagState.getData(key);
	}

	/**
	 * Used for data-xxx attributes. They should always be rendered.
	 * 
	 * @param key Unprefixed key, e.g. xxx will spit out an attribute of
	 *            data-xxx
	 * @param value
	 */
	public void setData(String key, String value)
	{
		if( data == null )
		{
			data = new HashMap<String, String>();
		}
		data.put(key, value);
	}

	/**
	 * Pre-render the following {@code PreRenderable}s.
	 * <p>
	 * <ul>
	 * <li>The nested renderable</li>
	 * <li>All the {@code TagProcessor}s</li>
	 * <li>All the Event handlers ({@link JSHandler}).
	 * </ul>
	 */
	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(tagState.getPreRenderables());
		info.preRender(tagState.getProcessors());
	}

	/**
	 * Render the tag to a {@link SectionWriter}.
	 * <p>
	 * This calls the following methods in order:
	 * <ol>
	 * <li>{@link #prepareAttributes(SectionWriter)}</li>
	 * <li>{@link #writeStart(SectionWriter, Map)}</li>
	 * <li>{@link #writeMiddle(SectionWriter)}</li>
	 * <li>{@link #writeEnd(SectionWriter)}</li> </ul>
	 */
	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		Map<String, String> attrs = prepareAttributes(writer);
		writeStart(writer, attrs);
		writeMiddle(writer);
		writeEnd(writer);
	}

	/**
	 * Write the closing tag.
	 * <p>
	 * Some tags don't close (such as &lt;input>) and thus some renderers will
	 * override this with a no-op.
	 * 
	 * @param writer The writer
	 * @throws IOException
	 */
	protected void writeEnd(SectionWriter writer) throws IOException
	{
		writer.endTag(getTag());
	}

	/**
	 * Write the contents of the tag.
	 * <p>
	 * If this renderer has a Nested Renderable, render that, otherwise do
	 * nothing.
	 * 
	 * @param writer The writer
	 * @throws IOException
	 */
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		SectionRenderable renderable = getNestedRenderable();
		if( renderable != null )
		{
			writer.render(renderable);
		}
	}

	@Nullable
	@Override
	public SectionRenderable getNestedRenderable()
	{
		return nestedRenderable;
	}

	@SuppressWarnings("null")
	@Override
	public void setStyles(@Nullable String style, @Nullable String styleClass, @Nullable String id)
	{
		if( !Check.isEmpty(style) )
		{
			this.style = style;
		}
		if( !Check.isEmpty(styleClass) )
		{
			addClasses(styleClass);
		}
		if( !Check.isEmpty(id) )
		{
			setId(id);
		}
	}

	public TagRenderer addClasses(String styleClass)
	{
		String[] classes = styleClass.split("\\s+"); //$NON-NLS-1$
		for( String clazz : classes )
		{
			addClass(clazz);
		}
		return this;
	}

	/**
	 * Prepare the attributes for this tag.
	 * <p>
	 * First of all the "id", "class" and "style" attributes are added to the
	 * map, then {@link #prepareFirstAttributes(SectionWriter, Map)} is called,
	 * followed by calling
	 * {@link #processHandler(SectionWriter, Map, String, JSHandler)} on each
	 * event handler, then each registered {@code TagProcessor} is called in
	 * turn, followed by a final call to
	 * {@link #prepareLastAttributes(SectionWriter, Map)}.
	 * 
	 * @param writer The writer
	 * @return A map containing all the attributes to write out
	 * @throws IOException
	 */
	@SuppressWarnings("nls")
	protected Map<String, String> prepareAttributes(SectionWriter writer) throws IOException
	{
		Map<String, String> attrs = new LinkedHashMap<String, String>();

		ElementId elementId = getVisibleElementId();
		if( elementId.isElementUsed() )
		{
			attrs.put("id", elementId.getElementId(writer));
		}
		Set<String> clazzes = getStyleClasses();
		if( !Check.isEmpty(clazzes) )
		{
			StringBuilder sbuf = new StringBuilder();
			boolean first = true;
			for( String clazz : clazzes )
			{
				if( !first )
				{
					sbuf.append(' ');
				}
				sbuf.append(clazz);
				first = false;
			}
			attrs.put("class", sbuf.toString());
		}
		attrs.put("style", getStyle());
		final Map<String, String> allData = getData();
		if( allData != null )
		{
			for( Entry<String, String> d : allData.entrySet() )
			{
				attrs.put("data-" + d.getKey(), d.getValue());
			}
		}
		prepareFirstAttributes(writer, attrs);
		processHandlers(writer, attrs);
		List<TagProcessor> processors = tagState.getProcessors();
		if( processors != null )
		{
			for( TagProcessor tagProcessor : processors )
			{
				tagProcessor.processAttributes(writer, attrs);
			}
		}
		prepareLastAttributes(writer, attrs);
		return attrs;
	}

	protected void processHandlers(SectionWriter writer, Map<String, String> attrs)
	{
		Set<String> eventKeys = tagState.getEventKeys();
		for( String event : eventKeys )
		{
			processHandler(writer, attrs, event, tagState.getHandler(event));
		}
	}

	protected Set<String> getStyleClasses()
	{
		Set<String> clazzes = tagState.getStyleClasses();
		if( clazzes != null )
		{
			clazzes = new HashSet<String>(clazzes);
			if( styleClasses != null )
			{
				clazzes.addAll(styleClasses);
			}
		}
		else
		{
			clazzes = styleClasses;
		}
		return clazzes;
	}

	/**
	 * Process an indivial handler.
	 * <p>
	 * Some renderers might need to alter the way event handling is done. E.g.
	 * when a list is rendered as a Checkbox List it might need to turn "change"
	 * events into "click" events in order to retain the semantics of "change".
	 * This method allows you to treat each handler differently.
	 * 
	 * @see JSHandler
	 * @param writer The writer
	 * @param attrs The attributes to modify
	 * @param event The event type
	 * @param handler The handler mapped to the event
	 */
	protected void processHandler(SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler)
	{
		writer.bindHandler(event, attrs, handler);
	}

	protected void prepareLastAttributes(SectionWriter writer, Map<String, String> attrs)
	{
		// nothing by default
	}

	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		// nothing by default
	}

	/**
	 * Write the start of the tag, including the attributes.
	 * 
	 * @param writer The writer
	 * @param attrs The attributes to write
	 * @throws IOException
	 */
	protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		writer.writeTag(getTag(), attrs);
	}

	protected String getTag()
	{
		return tag;
	}

	protected void setTag(String tag)
	{
		this.tag = tag;
	}

	/**
	 * The {@code SectionRenderable} to render as the content of this tag.
	 */
	@Override
	public TagRenderer setNestedRenderable(@Nullable SectionRenderable nested)
	{
		this.nestedRenderable = nested;
		return this;
	}

	public String getStyle()
	{
		if( style == null )
		{
			return tagState.getStyle();
		}
		return style;
	}

	public void addStyle(Map<String, String> attrs, String style)
	{
		String curStyle = attrs.get("style"); //$NON-NLS-1$
		if( curStyle != null )
		{
			style = curStyle + style;
		}
		attrs.put("style", style); //$NON-NLS-1$
	}

	public boolean getBooleanAttribute(Object key)
	{
		Boolean attr = getAttribute(key);
		return attr != null ? attr : false;
	}

	public TagState getTagState()
	{
		return tagState;
	}
}
