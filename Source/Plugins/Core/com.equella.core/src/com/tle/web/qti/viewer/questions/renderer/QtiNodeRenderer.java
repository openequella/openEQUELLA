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

package com.tle.web.qti.viewer.questions.renderer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import uk.ac.ed.ph.jqtiplus.attribute.Attribute;
import uk.ac.ed.ph.jqtiplus.attribute.AttributeList;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.QuestionRenderers;
import com.tle.web.qti.viewer.questions.freemarker.QuestionFreemarkerFactory;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagProcessor;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class QtiNodeRenderer implements NestedRenderable
{
	private final QtiNode model;
	@Nullable
	private final QtiViewerContext context;
	private boolean preProcessed;

	@Nullable
	private SectionRenderable topRenderable;
	@Nullable
	private SectionRenderable nestedRenderable;
	@Nullable
	private SectionRenderable bottomRenderable;

	private List<SectionRenderable> childRenderables = Lists.newArrayList();

	@Inject
	protected QuestionRenderers qfac;
	@Inject
	protected QuestionFreemarkerFactory view;

	protected QtiNodeRenderer(QtiNode model, QtiViewerContext context)
	{
		this.model = model;
		this.context = context;
	}

	public void preProcess()
	{
		if( !preProcessed )
		{
			final Iterator<QtiNode> childIterator = getChildIterator();
			while( childIterator.hasNext() )
			{
				final QtiNode node = childIterator.next();
				final QtiNodeRenderer childRenderer = qfac.chooseRenderer(node, getContext());
				childRenderables.add(childRenderer);
			}
			preProcessed = true;
		}
		else
		{
			throw new Error("Already preProcessed!");
		}
	}

	protected boolean isNestedTop()
	{
		return true;
	}

	@Nullable
	protected final SectionRenderable getTopRenderable()
	{
		if( topRenderable == null )
		{
			topRenderable = createTopRenderable();
			if( topRenderable != null && isNestedTop() )
			{
				if( topRenderable instanceof NestedRenderable )
				{
					((NestedRenderable) topRenderable).setNestedRenderable(getNestedRenderable());
				}
			}
		}
		return topRenderable;
	}

	@Nullable
	@Override
	public SectionRenderable getNestedRenderable()
	{
		if( nestedRenderable == null )
		{
			nestedRenderable = createNestedRenderable();
		}
		return nestedRenderable;
	}

	@Nullable
	protected final SectionRenderable getBottomRenderable()
	{
		if( bottomRenderable == null )
		{
			bottomRenderable = createBottomRenderable();
		}
		return bottomRenderable;
	}

	@Nullable
	protected SectionRenderable createTopRenderable()
	{
		final String tagName = getTagName();
		final TagState tagState = new TagState();

		final Map<String, String> attrs = Maps.newHashMap();
		addAttributes(attrs);
		tagState.addTagProcessor(new AttributeCopyingProcessor(attrs));

		return new TagRenderer(tagName, tagState);
	}

	protected String getTagName()
	{
		return model.getQtiClassName();
	}

	protected void addAttributes(Map<String, String> attrs)
	{
		final AttributeList attributes = model.getAttributes();
		for( Attribute<?> attribute : attributes )
		{
			addAttribute(attrs, attribute);
		}
	}

	protected void addAttribute(Map<String, String> attrs, Attribute<?> attribute)
	{
		final Object computedValue = attribute.getComputedValue();
		if( computedValue != null )
		{
			if( computedValue instanceof Collection )
			{
				final Collection<?> c = (Collection<?>) computedValue;
				final StringBuilder sb = new StringBuilder();
				boolean first = true;
				for( Object o : c )
				{
					if( !first )
					{
						sb.append(" ");
					}
					sb.append(o.toString());
					first = false;
				}
				attrs.put(attribute.getLocalName(), sb.toString());
			}
			else
			{
				attrs.put(attribute.getLocalName(), computedValue.toString());
			}
		}
	}

	@Nullable
	protected SectionRenderable createNestedRenderable()
	{
		return CombinedRenderer.combineMultipleResults(childRenderables);
	}

	@SuppressWarnings("unchecked")
	protected <T extends QtiNode> Iterator<T> getChildIterator()
	{
		return (Iterator<T>) model.iterator();
	}

	@Nullable
	protected SectionRenderable createBottomRenderable()
	{
		return null;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		final SectionRenderable top = getTopRenderable();
		if( top != null )
		{
			top.preRender(info);
		}
		if( !isNestedTop() )
		{
			final SectionRenderable nested = getNestedRenderable();
			if( nested != null )
			{
				nested.preRender(info);
			}
		}
		final SectionRenderable bottom = getBottomRenderable();
		if( bottom != null )
		{
			bottom.preRender(info);
		}
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		final SectionRenderable top = getTopRenderable();
		if( top != null )
		{
			top.realRender(writer);
		}
		if( !isNestedTop() )
		{
			final SectionRenderable nested = getNestedRenderable();
			if( nested != null )
			{
				nested.realRender(writer);
			}
		}
		final SectionRenderable bottom = getBottomRenderable();
		if( bottom != null )
		{
			bottom.realRender(writer);
		}
	}

	/**
	 * @param nested
	 * @return Itself
	 */
	@Override
	public NestedRenderable setNestedRenderable(@Nullable SectionRenderable nestedRenderable)
	{
		this.nestedRenderable = nestedRenderable;
		return this;
	}

	protected String renderToText(@Nullable SectionRenderable renderable)
	{
		if( renderable == null )
		{
			return "";
		}

		// This is sub ottstimal
		final StringWriter sw = new StringWriter();
		try (SectionWriter writer = new SectionWriter(sw, getContext().getRenderContext()))
		{
			renderable.preRender(writer);
			renderable.realRender(writer);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
		return sw.toString();
	}

	@Nullable
	protected String id(@Nullable Identifier ident)
	{
		if( ident == null )
		{
			return null;
		}
		return ident.toString();
	}

	protected QtiViewerContext getContext()
	{
		return context;
	}

	public static class AttributeCopyingProcessor implements TagProcessor
	{
		private final Map<String, String> attrs;

		public AttributeCopyingProcessor(Map<String, String> attrs)
		{
			this.attrs = attrs;
		}

		@Override
		public void preRender(PreRenderContext info)
		{
		}

		@Override
		public void processAttributes(SectionWriter writer, Map<String, String> attrs)
		{
			attrs.putAll(this.attrs);
		}
	}
}
