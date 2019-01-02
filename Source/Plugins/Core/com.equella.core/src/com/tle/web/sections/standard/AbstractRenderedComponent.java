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

package com.tle.web.sections.standard;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.HandlerMap;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagProcessor;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.LabelTagRenderer;

@NonNullByDefault
public abstract class AbstractRenderedComponent<S extends HtmlComponentState> extends AbstractHtmlComponent<S>
	implements
		HtmlRenderer,
		RendererSelectable,
		ElementId,
		RendererCallback
{
	@Inject
	protected RendererFactory renderFactory;

	private boolean used = true;
	private boolean disabled;
	private boolean displayed = true;
	@Nullable
	private HandlerMap handlerMap;
	@Nullable
	private List<PreRenderable> preRenderables;
	@Nullable
	private List<TagProcessor> processors;

	protected Label label;
	@Nullable
	protected String styleClass;
	@Nullable
	protected String style;
	protected String defaultRenderer;

	public AbstractRenderedComponent(String defaultRenderer)
	{
		this.defaultRenderer = defaultRenderer;
	}

	public String getDefaultRenderer()
	{
		return defaultRenderer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object instantiateModel(SectionInfo info)
	{
		Object state = super.instantiateModel(info);
		if( info.isReal() )
		{
			setupState(info, (S) state);
		}
		return state;
	}

	protected S setupState(SectionInfo info, S state)
	{
		state.setDisabled(disabled);
		state.setDisplayed(displayed);
		state.setLabel(label);
		state.setStyle(style);
		if( handlerMap != null )
		{
			state.getHandlerMap().setFallbackMap(handlerMap);
		}
		if( processors != null )
		{
			for( TagProcessor processor : processors )
			{
				state.addTagProcessor(processor);
			}
		}
		return state;
	}

	public JSHandler getClickHandler()
	{
		return handlerMap.getHandler(JSHandler.EVENT_CLICK);
	}

	public void setClickHandler(JSCallable callable, Object... args)
	{
		setEventHandler(JSHandler.EVENT_CLICK, new StatementHandler(callable, args));
	}

	public void setClickHandler(JSHandler handler)
	{
		setEventHandler(JSHandler.EVENT_CLICK, handler);
	}

	public void addClickStatements(JSStatements... statements)
	{
		addEventStatements(JSHandler.EVENT_CLICK, statements);
	}

	public void addReadyStatements(JSStatements... statements)
	{
		addEventStatements(JSHandler.EVENT_READY, statements);
	}

	public void addReadyStatements(JSCallable callable, Object... args)
	{
		addEventStatements(JSHandler.EVENT_READY, new StatementHandler(callable, args));
	}

	public void setEventHandler(String event, JSHandler handler)
	{
		ensureBuildingTree();
		ensureHandlerMap();
		handlerMap.setEventHandler(event, handler);
	}

	public void addEventStatements(String event, JSStatements... statements)
	{
		ensureBuildingTree();
		ensureHandlerMap();
		handlerMap.addEventStatements(event, statements);
	}

	private void ensureHandlerMap()
	{
		if( handlerMap == null )
		{
			handlerMap = new HandlerMap();
		}
	}

	public void setClickHandler(SectionInfo info, JSCallable callable, Object... args)
	{
		getState(info).setClickHandler(new StatementHandler(callable, args));
	}

	public void addClickStatements(SectionInfo info, JSStatements... statements)
	{
		getState(info).addEventStatements(JSHandler.EVENT_CLICK, statements);
	}

	public void setEventHandler(SectionInfo info, String event, JSHandler handler)
	{
		getState(info).setEventHandler(event, handler);
	}

	public void addEventStatements(SectionInfo info, String event, JSStatements... statements)
	{
		getState(info).addEventStatements(event, statements);
	}

	public void setClickHandler(SectionInfo info, JSHandler handler)
	{
		getState(info).setClickHandler(handler);
	}

	public void addReadyStatements(SectionInfo info, JSStatements... statements)
	{
		getState(info).addReadyStatements(statements);
	}

	public void addReadyStatements(SectionInfo info, JSCallable callable, Object... args)
	{
		getState(info).addReadyStatements(callable, args);
	}

	@Override
	public void rendererSelected(RenderContext info, SectionRenderable renderer)
	{
		// nothing
	}

	public void setDisplayed(boolean b)
	{
		ensureBuildingTree();
		this.displayed = b;
	}

	@Nullable
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		S state = getState(context);
		state.setDefaultRenderer(getDefaultRenderer());
		if( state.isDisplayed() )
		{
			state.addRendererCallback(this);
			state.setName(getParameterId());

			if( !state.hasIdBeenSet() )
			{
				state.setElementId(this);
			}
			else if( used )
			{
				state.registerUse();
			}

			if( !state.isClassesSet() && styleClass != null )
			{
				state.addClasses(styleClass);
			}

			if( !Check.isEmpty(preRenderables) )
			{
				for( PreRenderable pr : preRenderables )
				{
					state.addPreRenderable(pr);
				}
			}

			prepareModel(context);
			if( "null".equals(state.getRendererType()) ) //$NON-NLS-1$
			{
				return null;
			}
			return chooseRenderer(context, state);
		}
		return null;
	}

	public boolean isDisplayed(SectionInfo info)
	{
		return getState(info).isDisplayed();
	}

	public void setDisplayed(SectionInfo info, boolean displayed)
	{
		getState(info).setDisplayed(displayed);
	}

	public void setLabel(SectionInfo info, Label label)
	{
		getState(info).setLabel(label);
	}

	public void setLabel(Label label)
	{
		ensureBuildingTree();
		this.label = label;
	}

	public Label getLabel(SectionInfo info)
	{
		return getState(info).getLabel();
	}

	protected void prepareModel(RenderContext info)
	{
		// nothing
	}

	public S getState(SectionInfo info)
	{
		return getModel(info);
	}

	public S getState(SectionContext context)
	{
		return getModel(context);
	}

	protected SectionRenderable chooseRenderer(RenderContext info, S state)
	{
		SectionRenderable renderer = renderFactory.getRenderer(info, state);
		state.fireRendererCallback(info, renderer);
		return renderer;
	}

	@Override
	public void setRendererType(SectionInfo info, String type)
	{
		getState(info).setRendererType(type);
	}

	public void setRenderFactory(RendererFactory renderFactory)
	{
		this.renderFactory = renderFactory;
	}

	@Override
	public String getElementId(SectionInfo info)
	{
		return getParameterId();
	}

	public boolean isDisabled(SectionInfo info)
	{
		return getState(info).isDisabled();
	}

	public boolean isDisabled()
	{
		ensureBuildingTree();
		return disabled;
	}

	public void enable(SectionInfo info)
	{
		setDisabled(info, false);
	}

	public void disable(SectionInfo info)
	{
		setDisabled(info, true);
	}

	public void show(SectionInfo info)
	{
		setDisplayed(info, true);
	}

	public void hide(SectionInfo info)
	{
		setDisplayed(info, false);
	}

	public void setDisabled(SectionInfo info, boolean disabled)
	{
		getState(info).setDisabled(disabled);
	}

	public void setDisabled(boolean disabled)
	{
		ensureBuildingTree();
		this.disabled = disabled;
	}

	public void setStyleClass(String styleClass)
	{
		ensureBuildingTree();
		this.styleClass = styleClass;
	}

	public void setStyle(String style)
	{
		ensureBuildingTree();
		this.style = style;
	}

	public void setDefaultRenderer(String defaultRenderer)
	{
		ensureBuildingTree();
		this.defaultRenderer = defaultRenderer;
	}

	@Override
	public void registerUse()
	{
		used = true;
	}

	@Override
	public boolean isElementUsed()
	{
		return used;
	}

	@Override
	public boolean isStaticId()
	{
		return true;
	}

	@Deprecated
	public ElementByIdExpression getJSElement()
	{
		if( !used )
		{
			throw new SectionsRuntimeException("Must be registered first"); //$NON-NLS-1$
		}
		return new ElementByIdExpression(this);
	}

	@Override
	protected boolean addToThisBookmark(SectionInfo info, BookmarkEvent event)
	{
		if( !super.addToThisBookmark(info, event) )
		{
			return false;
		}
		if( event.isRendering() && getState(info).hasBeenRendered() )
		{
			event.setParam(getParameterId(), null);
			return false;
		}
		return true;
	}

	public void addPrerenderables(PreRenderable pr)
	{
		if( preRenderables == null )
		{
			preRenderables = new ArrayList<PreRenderable>();
		}
		preRenderables.add(pr);
	}

	public List<TagProcessor> getProcessors()
	{
		return processors;
	}

	public void addTagProcessor(TagProcessor processor)
	{
		if( processors == null )
		{
			processors = new ArrayList<TagProcessor>();
		}
		processors.add(processor);
	}

	public LabelTagRenderer getLabelRenderer()
	{
		return new LabelTagRenderer(this, null, label);
	}
}
