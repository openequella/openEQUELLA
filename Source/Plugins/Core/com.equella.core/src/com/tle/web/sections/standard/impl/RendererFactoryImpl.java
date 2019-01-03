/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.standard.impl;

import static com.tle.web.sections.standard.RendererConstants.BOOLEANLIST;
import static com.tle.web.sections.standard.RendererConstants.BUTTON;
import static com.tle.web.sections.standard.RendererConstants.CALENDAR;
import static com.tle.web.sections.standard.RendererConstants.CHECKBOX;
import static com.tle.web.sections.standard.RendererConstants.CHECKLIST;
import static com.tle.web.sections.standard.RendererConstants.CODE_MIRROR;
import static com.tle.web.sections.standard.RendererConstants.DIALOG;
import static com.tle.web.sections.standard.RendererConstants.DIV;
import static com.tle.web.sections.standard.RendererConstants.DROPDOWN;
import static com.tle.web.sections.standard.RendererConstants.FILE;
import static com.tle.web.sections.standard.RendererConstants.FILEDROP;
import static com.tle.web.sections.standard.RendererConstants.IMAGE_BUTTON;
import static com.tle.web.sections.standard.RendererConstants.IMAGE_CHECKBOX;
import static com.tle.web.sections.standard.RendererConstants.IP;
import static com.tle.web.sections.standard.RendererConstants.LINK;
import static com.tle.web.sections.standard.RendererConstants.NUMBERFIELD;
import static com.tle.web.sections.standard.RendererConstants.PAGER;
import static com.tle.web.sections.standard.RendererConstants.POPUP_BUTTON;
import static com.tle.web.sections.standard.RendererConstants.POPUP_LINK;
import static com.tle.web.sections.standard.RendererConstants.RADIO_CHECKBOX;
import static com.tle.web.sections.standard.RendererConstants.SHUFFLEBOX;
import static com.tle.web.sections.standard.RendererConstants.SPAN;
import static com.tle.web.sections.standard.RendererConstants.STAR_RATING;
import static com.tle.web.sections.standard.RendererConstants.TABS;
import static com.tle.web.sections.standard.RendererConstants.TEXTAREA;
import static com.tle.web.sections.standard.RendererConstants.TEXTFIELD;
import static com.tle.web.sections.standard.RendererConstants.TEXT_CHECKBOX;
import static com.tle.web.sections.standard.RendererConstants.TOGGLE_CHECKBOX;
import static com.tle.web.sections.standard.RendererConstants.TREE;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.google.common.collect.Lists;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererFactoryAware;
import com.tle.web.sections.standard.RendererFactoryExtension;
import com.tle.web.sections.standard.dialog.model.DialogState;
import com.tle.web.sections.standard.model.CodeMirrorState;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlCalendarState;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlFileDropState;
import com.tle.web.sections.standard.model.HtmlFileUploadState;
import com.tle.web.sections.standard.model.HtmlIpAddressInputState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.HtmlMutableListState;
import com.tle.web.sections.standard.model.HtmlNumberFieldState;
import com.tle.web.sections.standard.model.HtmlPagerState;
import com.tle.web.sections.standard.model.HtmlTabState;
import com.tle.web.sections.standard.model.HtmlTextFieldState;
import com.tle.web.sections.standard.model.HtmlTreeState;
import com.tle.web.sections.standard.model.HtmlValueState;
import com.tle.web.sections.standard.renderers.ButtonRenderer;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.FileDropRenderer;
import com.tle.web.sections.standard.renderers.FileRenderer;
import com.tle.web.sections.standard.renderers.ImageButtonRenderer;
import com.tle.web.sections.standard.renderers.IpAddressInputRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.NumberFieldRenderer;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.sections.standard.renderers.TextAreaRenderer;
import com.tle.web.sections.standard.renderers.TextFieldRenderer;
import com.tle.web.sections.standard.renderers.calendar.CalendarRenderer;
import com.tle.web.sections.standard.renderers.codemirror.CodeMirrorRenderer;
import com.tle.web.sections.standard.renderers.fancybox.FancyBoxDialogRenderer;
import com.tle.web.sections.standard.renderers.list.BooleanListRenderer;
import com.tle.web.sections.standard.renderers.list.CheckListRenderer;
import com.tle.web.sections.standard.renderers.list.DropDownRenderer;
import com.tle.web.sections.standard.renderers.list.ShuffleBoxRenderer;
import com.tle.web.sections.standard.renderers.list.StarRatingListRenderer;
import com.tle.web.sections.standard.renderers.pager.PagerRenderer;
import com.tle.web.sections.standard.renderers.popup.PopupButtonRenderer;
import com.tle.web.sections.standard.renderers.popup.PopupLinkRenderer;
import com.tle.web.sections.standard.renderers.tabs.JQueryTabsRenderer;
import com.tle.web.sections.standard.renderers.toggle.CheckboxRenderer;
import com.tle.web.sections.standard.renderers.toggle.ImageTogglerRenderer;
import com.tle.web.sections.standard.renderers.toggle.JQueryUITogglerRenderer;
import com.tle.web.sections.standard.renderers.toggle.RadioButtonRenderer;
import com.tle.web.sections.standard.renderers.toggle.TextTogglerRenderer;
import com.tle.web.sections.standard.renderers.tree.TreeViewRenderer;

@Bind(RendererFactory.class)
@Singleton
public class RendererFactoryImpl implements RendererFactory
{
	private final Map<RendererKey, RendererFactoryExtension> mappings = new HashMap<RendererKey, RendererFactoryExtension>();
	private PluginTracker<RendererFactoryExtension> rendererTracker;

	@Override
	public SectionRenderable[] convertToRenderers(Object... objects)
	{
		SectionRenderable[] renderers = new SectionRenderable[objects.length];
		for( int i = 0; i < objects.length; i++ )
		{
			renderers[i] = convertToRenderer(objects[i]);
		}
		return renderers;
	}

	@Override
	public List<SectionRenderable> convertToRenderers(Collection<?> objects)
	{
		List<SectionRenderable> out = Lists.newArrayList();
		for( Object obj : objects )
		{
			out.add(convertToRenderer(obj));
		}
		return out;
	}

	@Override
	public SectionRenderable convertToRenderer(Object... objects)
	{
		return CombinedRenderer.combineMultipleResults(convertToRenderers(objects));
	}

	@Override
	public SectionRenderable convertToRenderer(Object object)
	{
		if( object instanceof HtmlComponentState )
		{
			return new RenderFactoryRenderer((HtmlComponentState) object, this);
		}
		return SectionUtils.convertToRenderer(object);
	}

	public static class ConstructorRendererFactory implements RendererFactoryExtension
	{
		private final Constructor<? extends SectionRenderable> constructor;

		public ConstructorRendererFactory(Constructor<? extends SectionRenderable> constructor)
		{
			this.constructor = constructor;
		}

		@Override
		public SectionRenderable getRenderer(RendererFactory rendererFactory, SectionInfo info, String renderer,
			HtmlComponentState state)
		{
			try
			{
				return constructor.newInstance(state);
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public SectionRenderable getRenderer(SectionInfo info, HtmlComponentState state)
	{
		String defaultRenderer = state.getDefaultRenderer();
		String type = state.getRendererType();
		SectionRenderable result = null;
		if( defaultRenderer == null )
		{
			defaultRenderer = LINK;
		}
		if( type != null )
		{
			result = getRendererInternal(info, state, type + '_' + defaultRenderer);
			if( result == null )
			{
				result = getRendererInternal(info, state, type);
			}
		}
		if( result != null )
		{
			return result;
		}
		return getRendererInternal(info, state, defaultRenderer);
	}

	private SectionRenderable getRendererInternal(SectionInfo info, HtmlComponentState state, String renderer)
	{
		Class<? extends HtmlComponentState> clazz = state.getClassForRendering();
		RendererFactoryExtension factory = getMappings().get(new RendererKey(renderer, clazz));
		if( factory == null )
		{
			return null;
		}
		SectionRenderable sr = factory.getRenderer(this, info, renderer, state);
		if( sr instanceof RendererFactoryAware )
		{
			((RendererFactoryAware) sr).setRenderFactory(this);
		}
		return sr;
	}

	public static class RendererKey
	{
		private final String type;
		private final Class<?> clazz;

		public RendererKey(String type, Class<?> clazz)
		{
			this.type = type;
			this.clazz = clazz;
		}

		@Override
		public int hashCode()
		{
			return type.hashCode() + clazz.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if( this == obj )
			{
				return true;
			}

			if( !(obj instanceof RendererKey) )
			{
				return false;
			}

			RendererKey other = (RendererKey) obj;
			return type.equals(other.type) && clazz == other.clazz;
		}
	}

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		rendererTracker = new PluginTracker<RendererFactoryExtension>(pluginService, "com.tle.web.sections.standard",
			"rendererFactory", null, new PluginTracker.ExtensionParamComparator("order"));
		rendererTracker.setBeanKey("class");
	}

	public void addDefaultRenderers()
	{
		addMapping(BUTTON, HtmlComponentState.class, ButtonRenderer.class);
		addMapping(LINK, HtmlLinkState.class, LinkRenderer.class);
		addMapping(LINK, HtmlComponentState.class, LinkRenderer.class);
		addMapping(DIALOG, DialogState.class, FancyBoxDialogRenderer.class);
		addMapping(POPUP_LINK, HtmlComponentState.class, PopupLinkRenderer.class);
		addMapping(POPUP_LINK, HtmlLinkState.class, PopupLinkRenderer.class);
		addMapping(POPUP_BUTTON, HtmlComponentState.class, PopupButtonRenderer.class);
		addMapping(POPUP_BUTTON, HtmlLinkState.class, PopupButtonRenderer.class);
		addMapping(IMAGE_BUTTON, HtmlComponentState.class, ImageButtonRenderer.class);
		addMapping(DROPDOWN, HtmlListState.class, DropDownRenderer.class);
		addMapping(DROPDOWN, HtmlMutableListState.class, DropDownRenderer.class);
		addMapping(TEXTFIELD, HtmlValueState.class, TextFieldRenderer.class);
		addMapping(TEXTFIELD, HtmlTextFieldState.class, TextFieldRenderer.class);
		addMapping(TEXTAREA, HtmlValueState.class, TextAreaRenderer.class);
		addMapping(TEXTAREA, HtmlTextFieldState.class, TextAreaRenderer.class);
		addMapping(CHECKBOX, HtmlBooleanState.class, CheckboxRenderer.class);
		addMapping(RADIO_CHECKBOX, HtmlBooleanState.class, RadioButtonRenderer.class);
		addMapping(TEXT_CHECKBOX, HtmlBooleanState.class, TextTogglerRenderer.class);
		addMapping(IMAGE_CHECKBOX, HtmlBooleanState.class, ImageTogglerRenderer.class);
		addMapping(TOGGLE_CHECKBOX, HtmlBooleanState.class, JQueryUITogglerRenderer.class);
		addMapping(CHECKLIST, HtmlListState.class, CheckListRenderer.class);
		addMapping(SHUFFLEBOX, HtmlListState.class, ShuffleBoxRenderer.class);
		addMapping(CALENDAR, HtmlCalendarState.class, CalendarRenderer.class);
		addMapping(FILE, HtmlFileUploadState.class, FileRenderer.class);
		addMapping(FILEDROP, HtmlFileDropState.class, FileDropRenderer.class);
		addMapping(PAGER, HtmlPagerState.class, PagerRenderer.class);
		addMapping(TABS, HtmlTabState.class, JQueryTabsRenderer.class);
		addMapping(TREE, HtmlTreeState.class, TreeViewRenderer.class);
		addMapping(DROPDOWN + '_' + STAR_RATING, HtmlListState.class, StarRatingListRenderer.class);
		addMapping(DIV, HtmlComponentState.class, DivRenderer.class);
		addMapping(SPAN, HtmlComponentState.class, SpanRenderer.class);
		// addMapping(TABLE, TableState.class, TableRenderer.class);
		addMapping(NUMBERFIELD, HtmlNumberFieldState.class, NumberFieldRenderer.class);
		addMapping(BOOLEANLIST, HtmlListState.class, BooleanListRenderer.class);
		addMapping(IP, HtmlIpAddressInputState.class, IpAddressInputRenderer.class);
		addMapping(CODE_MIRROR, CodeMirrorState.class, CodeMirrorRenderer.class);
	}

	protected void addMapping(String rendererType, Class<?> stateClass, Class<? extends SectionRenderable> renderClass)
	{
		RendererKey key = new RendererKey(rendererType, stateClass);
		Constructor<? extends SectionRenderable> constructor;
		try
		{
			constructor = renderClass.getConstructor(stateClass);
			mappings.put(key, new ConstructorRendererFactory(constructor));
		}
		catch( NoSuchMethodException e )
		{
			SectionUtils.throwRuntime(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<RendererKey, RendererFactoryExtension> getMappings()
	{
		synchronized( mappings )
		{
			if( rendererTracker.needsUpdate() )
			{
				mappings.clear();

				addDefaultRenderers();

				for( Extension extension : rendererTracker.getExtensions() )
				{
					RendererFactoryExtension factoryBean = null;
					Parameter rendererParam = extension.getParameter("renderer"); //$NON-NLS-1$
					Class<? extends SectionRenderable> renderClazz = null;
					if( rendererParam == null )
					{
						factoryBean = rendererTracker.getBeanByExtension(extension);
					}
					else
					{
						renderClazz = (Class<? extends SectionRenderable>) rendererTracker.getClassForName(extension,
							rendererParam.valueAsString());
					}
					Collection<Parameter> renderIds = extension.getParameters("rendererId"); //$NON-NLS-1$
					for( Parameter renderIdParam : renderIds )
					{
						Collection<Parameter> stateClasses = extension.getParameters("stateClassName"); //$NON-NLS-1$
						String renderId = renderIdParam.valueAsString();
						for( Parameter stateClassParam : stateClasses )
						{
							try
							{
								String stateClass = stateClassParam.valueAsString();
								final Class<? extends HtmlComponentState> clazz = (Class<? extends HtmlComponentState>) rendererTracker
									.getClassForName(extension, stateClass);
								final RendererKey key = new RendererKey(renderId, clazz);
								if( renderClazz != null )
								{
									factoryBean = new ConstructorRendererFactory(renderClazz.getConstructor(clazz));
								}
								mappings.put(key, factoryBean);
							}
							catch( NoSuchMethodException e )
							{
								throw new SectionsRuntimeException(e);
							}
						}
					}
				}
			}
			return mappings;
		}
	}

}
