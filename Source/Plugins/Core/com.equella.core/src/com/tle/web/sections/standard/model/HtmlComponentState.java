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

package com.tle.web.sections.standard.model;

import java.util.Comparator;

import com.dytech.common.text.NumberStringComparator;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.RendererCallback;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.RendererSelectable;

/**
 * The base "State" class for all Standard Component Library Renderers and
 * Sections.
 * <p>
 * This class extends the {@link TagState} with properties specific to the
 * Standard Component Library, such as:
 * <ul>
 * <li>An optional label</li>
 * <li>Renderer type strings (default, override, markup selected)</li>
 * <li>A form name - not always the same as the element id</li>
 * <li>Flags for disabling and not displaying at all</li>
 * <li>Flags for keeping track of whether or not it was rendered</li>
 * </ul>
 * <p>
 * This class itself is useful for {@code Section}s and Renderers which only
 * need event processing and don't store any other state, such as {@link Button}.
 * 
 * @author jmaginnis
 */
public class HtmlComponentState extends TagState implements RendererSelectable
{
	private static final String RENDERER_CALLBACK = "RendererCallback"; //$NON-NLS-1$

	private Label label;
	private String rendererType;
	private String overrideRendererType;
	private String defaultRenderer;
	private boolean disabled;
	private boolean displayed = true;
	private boolean beenRendered;
	private String name;
	private boolean cancel;
	private Label title;

	public HtmlComponentState()
	{
		// nothing
	}

	public HtmlComponentState(Label label, JSHandler clickHandler)
	{
		this(RendererConstants.BUTTON, clickHandler);
		setLabel(label);
	}

	public HtmlComponentState(JSHandler clickHandler)
	{
		this(RendererConstants.BUTTON, clickHandler);
	}

	public HtmlComponentState(String defaultRenderer)
	{
		this.defaultRenderer = defaultRenderer;
	}

	public HtmlComponentState(String defaultRenderer, JSHandler clickHandler)
	{
		this(defaultRenderer);
		setClickHandler(clickHandler);
	}

	public void setDefaultRenderer(String defaultRenderer)
	{
		this.defaultRenderer = defaultRenderer;
	}

	public boolean hasBeenRendered()
	{
		return beenRendered;
	}

	public void setBeenRendered(boolean beenRendered)
	{
		this.beenRendered = beenRendered;
	}

	public boolean isDisabled()
	{
		return disabled;
	}

	public HtmlComponentState setDisabled(boolean disabled)
	{
		this.disabled = disabled;
		return this;
	}

	public boolean isDisplayed()
	{
		return displayed;
	}

	public HtmlComponentState setDisplayed(boolean displayed)
	{
		this.displayed = displayed;
		return this;
	}

	public Label getLabel()
	{
		return label;
	}

	public HtmlComponentState setLabel(Label label)
	{
		this.label = label;
		return this;
	}

	public String getRendererType()
	{
		if( overrideRendererType != null )
		{
			return overrideRendererType + '_' + (rendererType == null ? defaultRenderer : rendererType);
		}
		return rendererType;
	}

	public HtmlComponentState setRendererType(String rendererType)
	{
		this.rendererType = rendererType;
		return this;
	}

	public void setOverrideRendererType(String overrideRendererType)
	{
		this.overrideRendererType = overrideRendererType;
	}

	public String getDefaultRenderer()
	{
		return defaultRenderer;
	}

	@Override
	public void setRendererType(SectionInfo info, String type)
	{
		this.rendererType = type;
	}

	@SuppressWarnings("unchecked")
	public <T extends HtmlComponentState> Class<T> getClassForRendering()
	{
		return (Class<T>) getClass();
	}

	public LabelRenderer createLabelRenderer()
	{
		return new LabelRenderer(label);
	}

	public String getLabelText()
	{
		if( label != null )
		{
			return label.getText();
		}
		return ""; //$NON-NLS-1$
	}

	public static final Comparator<HtmlComponentState> LABEL_COMPARATOR = new NumberStringComparator<HtmlComponentState>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String convertToString(HtmlComponentState t)
		{
			return t.getLabelText();
		}
	};

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isCancel()
	{
		return cancel;
	}

	public void setCancel(boolean cancel)
	{
		this.cancel = cancel;
	}

	public void addRendererCallback(RendererCallback callback)
	{
		RendererCallback oldCallback = getAttribute(RENDERER_CALLBACK);
		if( oldCallback != null && !callback.equals(oldCallback) )
		{
			throw new SectionsRuntimeException("Only one callback supported currently"); //$NON-NLS-1$
		}
		setAttribute(RENDERER_CALLBACK, callback);
	}

	public void fireRendererCallback(RenderContext info, SectionRenderable renderer)
	{
		RendererCallback callback = getAttribute(RENDERER_CALLBACK);
		if( callback != null )
		{
			callback.rendererSelected(info, renderer);
		}
	}

	public Label getTitle()
	{
		return title;
	}

	public void setTitle(Label title)
	{
		this.title = title;
	}
}
