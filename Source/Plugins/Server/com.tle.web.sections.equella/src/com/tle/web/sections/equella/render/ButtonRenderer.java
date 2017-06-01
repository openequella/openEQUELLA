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

package com.tle.web.sections.equella.render;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.tle.common.Check;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.DefaultDisableFunction;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.AbstractElementRenderer;

@SuppressWarnings("nls")
public class ButtonRenderer extends AbstractElementRenderer implements JSDisableable
{
	// @formatter:off
	public enum ButtonType
	{
		ADD(null, Icon.ADD), 
		DELETE(ButtonTrait.WARNING, Icon.DELETE), 
		EDIT(null, Icon.EDIT), 
		SELECT(ButtonTrait.SUCCESS, Icon.SELECT),
		CANCEL(null, null),
		UNSELECT(null, Icon.UNSELECT), 
		SEARCH(null, Icon.SEARCH), 
		GOTO(null, Icon.NEXT), 
		NEXT(null, Icon.NEXT), 
		PREV(null, Icon.PREV), 
		SELECT_USER(null, Icon.USER), 
		SAVE(ButtonTrait.SUCCESS, Icon.SAVE), 
		EMAIL(null, Icon.EMAIL), 
		ACCEPT(ButtonTrait.SUCCESS, Icon.THUMBS_UP),
		REJECT(ButtonTrait.WARNING, Icon.THUMBS_DOWN), 
		VERIFY(ButtonTrait.INFO, Icon.WRENCH), 
		DANGEROUS(ButtonTrait.DANGER, Icon.WARNING), 
		DOWNLOAD(ButtonTrait.SUCCESS, Icon.DOWNLOAD),
		UPLOAD(null, Icon.UPLOAD),
		BLUE(ButtonTrait.PRIMARY, null),
		NAV(ButtonTrait.INVERSE, null),
		PLUS(ButtonTrait.PRIMARY,Icon.ADD),
		MINUS(null, Icon.MINUS);
		
		// @formatter:on

		private final ButtonTrait trait;
		private final Icon icon;

		private ButtonType(ButtonTrait trait, Icon icon)
		{
			this.trait = trait;
			this.icon = icon;
		}

		public ButtonRenderer apply(ButtonRenderer r)
		{
			return r.setTrait(trait).setIcon(icon);
		}
	}

	public enum ButtonTrait
	{
		DEFAULT("btn-equella"), PRIMARY("btn-primary"), INFO("btn-info"), SUCCESS("btn-success"),
		WARNING("btn-warning"), DANGER("btn-danger"), INVERSE("btn-inverse");

		private final String cssClass;

		private ButtonTrait(String cssClass)
		{
			this.cssClass = cssClass;
		}

		public String getCssClass()
		{
			return cssClass;
		}
	}

	public enum ButtonSize
	{
		SMALL("btn-mini"), MEDIUM(null), LARGE("btn-large");

		private final String cssClass;

		private ButtonSize(String cssClass)
		{
			this.cssClass = cssClass;
		}

		public String getCssClass()
		{
			return cssClass;
		}
	}

	private Icon icon;
	private ButtonSize size;
	private ButtonTrait trait;
	private boolean iconOnly;
	private String ariaLabelValue;

	public ButtonRenderer(HtmlComponentState state)
	{
		super(state);
		addClass("btn");
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(Bootstrap.PRERENDER);
		super.preRender(info);
	}

	@Override
	protected Map<String, String> prepareAttributes(SectionWriter writer) throws IOException
	{
		Map<String, String> as = super.prepareAttributes(writer);

		if( iconOnly )
		{
			as.put("title", getLabelText());
		}

		as.put("type", "button");
		if( ariaLabelValue != null )
		{
			// Making accessible icon button
			// http://www.nczonline.net/blog/2013/04/01/making-accessible-icon-buttons/
			as.put("aria-label", ariaLabelValue);
		}
		return as;
	}

	@Override
	protected Set<String> getStyleClasses()
	{
		Set<String> rv = super.getStyleClasses();

		// Add trait classes
		if( trait == null )
		{
			trait = state.getAttribute(ButtonTrait.class);
			if( trait == null )
			{
				trait = ButtonTrait.DEFAULT;
			}
		}
		rv.add(trait.getCssClass());

		// Add size classes
		if( size == null )
		{
			size = state.getAttribute(ButtonSize.class);
			if( size == null )
			{
				size = ButtonSize.SMALL;
			}
		}
		if( size.getCssClass() != null )
		{
			rv.add(size.getCssClass());
		}

		return rv;
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		if( icon == null )
		{
			icon = state.getAttribute(Icon.class);
		}

		if( iconOnly )
		{
			writer.render(new LabelRenderer(new IconLabel(icon, null)));
			return;
		}

		SectionRenderable nr = getNestedRenderable();
		if( icon == null )
		{
			writer.render(nr);
			return;
		}

		writer.render(new LabelRenderer(new IconLabel(icon,
			new TextLabel(SectionUtils.renderToString(writer, nr), true))));
	}

	@Override
	protected void processHandler(SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler)
	{
		if( !isStillAddClickHandler() && isDisabled() && JSHandler.EVENT_CLICK.equals(event) )
		{
			return;
		}

		if( getHtmlState().isCancel() )
		{
			handler = Js.handler(
				Js.statement(JQuerySelector.methodCallExpression(Type.ID, getResetId(), Js.function("click"))),
				handler.getStatements());
		}

		super.processHandler(writer, attrs, event, handler);
	}

	private String getResetId()
	{
		return getId() + "_reset";
	}

	@Override
	protected boolean isStillAddClickHandler()
	{
		return true;
	}

	public ButtonRenderer setSize(ButtonSize size)
	{
		this.size = size;
		return this;
	}

	public ButtonRenderer setTrait(ButtonTrait type)
	{
		this.trait = type;
		return this;
	}

	public ButtonRenderer setIcon(Icon icon)
	{
		this.icon = icon;
		return this;
	}

	public ButtonRenderer showAs(ButtonType type)
	{
		return type.apply(this);
	}

	public void setIconOnly(boolean iconOnly)
	{
		this.iconOnly = iconOnly;
	}

	public void freemarkerShowAs(String type)
	{
		if( !Check.isEmpty(type) )
		{
			showAs(ButtonType.valueOf(type.toUpperCase()));
		}
	}

	public void freemarkerTrait(String trait)
	{
		if( !Check.isEmpty(trait) )
		{
			this.trait = ButtonTrait.valueOf(trait.toUpperCase());
		}
	}

	public void freemarkerIcon(String icon)
	{
		if( !Check.isEmpty(icon) )
		{
			setIcon(Icon.valueOf(icon.toUpperCase()));
		}
	}

	public void freemarkerSize(String size)
	{
		if( !Check.isEmpty(size) )
		{
			setSize(ButtonSize.valueOf(size.toUpperCase()));
		}
	}

	/*
	 * Covariant return to make fluent API work
	 */
	@Override
	public ButtonRenderer addClass(String extraClass)
	{
		super.addClass(extraClass);
		return this;
	}

	@Override
	protected String getTag()
	{
		return "button";
	}

	@Override
	public JSCallable createDisableFunction()
	{
		return new DefaultDisableFunction(this);
	}

	// //////////////////////////////////////
	//
	// Predefined buttons. These methods get called by name from Freemarker too
	//
	// //////////////////////////////////////

	public ButtonRenderer add(ButtonSize size)
	{
		return setSize(size).setIcon(Icon.ADD);
	}

	public ButtonRenderer delete(ButtonSize size, HtmlComponentState state)
	{
		return setTrait(ButtonTrait.WARNING).setSize(size).setIcon(Icon.DELETE);
	}

	public ButtonRenderer edit(ButtonSize size, HtmlComponentState state)
	{
		return setSize(size).setIcon(Icon.EDIT);
	}

	public ButtonRenderer select(ButtonSize size, HtmlComponentState state)
	{
		return setTrait(ButtonTrait.SUCCESS).setSize(size).setIcon(Icon.SELECT);
	}

	public ButtonRenderer unselect(ButtonSize size, HtmlComponentState state)
	{
		return setSize(size).setIcon(Icon.UNSELECT);
	}

	public String getAriaLabelValue()
	{
		return ariaLabelValue;
	}

	public void setAriaLabelValue(String ariaLabelValue)
	{
		this.ariaLabelValue = ariaLabelValue;
	}
}
