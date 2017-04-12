package com.tle.web.sections.equella.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tle.web.sections.render.OrderedRenderer;
import com.tle.web.sections.render.OrderedRenderer.RendererOrder;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;

public abstract class AbstractCombinedRenderer
{
	public static final String AREA_LEFT = "left"; //$NON-NLS-1$
	public static final String AREA_MIDDLE = "middle"; //$NON-NLS-1$
	public static final String AREA_RIGHT = "right"; //$NON-NLS-1$
	public static final String AREA_SEPARATOR = "separator"; //$NON-NLS-1$
	private static final RendererOrder SORT = new OrderedRenderer.RendererOrder();

	private SectionRenderable renderable;
	private final Map<String, List<OrderedRenderer>> renderers = new HashMap<String, List<OrderedRenderer>>();
	private Map<Object, Object> attributes = new HashMap<Object, Object>();

	public List<? extends SectionRenderable> getRendererList(String key)
	{
		List<OrderedRenderer> list = renderers.get(key);
		if( list != null )
		{
			Collections.sort(list, SORT);
			return list;
		}
		return Collections.emptyList();
	}

	public void addRenderer(String key, PreRenderable renderer)
	{
		addRenderer(key, renderer, 0);
	}

	public void addRenderer(String key, PreRenderable renderer, int order)
	{
		List<OrderedRenderer> list = renderers.get(key);
		if( list == null )
		{
			list = new ArrayList<OrderedRenderer>();
			renderers.put(key, list);
		}
		list.add(new OrderedRenderer(order, renderer));
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(Object key)
	{
		return (T) attributes.get(key);
	}

	public void setAttribute(Object key, Object value)
	{
		attributes.put(key, value);
	}

	public SectionRenderable getRenderable()
	{
		return renderable;
	}

	public void setRenderable(SectionRenderable renderable)
	{
		this.renderable = renderable;
	}

	public Map<Object, Object> getAttributes()
	{
		return attributes;
	}

}
