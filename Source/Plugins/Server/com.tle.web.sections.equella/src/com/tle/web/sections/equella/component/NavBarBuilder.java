package com.tle.web.sections.equella.component;

import java.util.List;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.component.model.NavBarState.NavBarElement;
import com.tle.web.sections.equella.render.BootstrapDropDownRenderer;
import com.tle.web.sections.equella.render.BootstrapSplitDropDownRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.model.HtmlLinkState;

public class NavBarBuilder
{
	private final NavBarElement DIVIDER_ELEMENT = new NavBarElement("divider-vertical", null);

	private final List<NavBarElement> elems;
	private final RendererFactory rendererFactory;

	public NavBarBuilder(List<NavBarElement> elems, RendererFactory rendererFactory)
	{
		this.elems = elems;
		this.rendererFactory = rendererFactory;
	}

	public NavBarBuilder divider()
	{
		elems.add(DIVIDER_ELEMENT);
		return this;
	}

	public NavBarBuilder text(Label label)
	{
		elems.add(new NavBarElement(null, SectionUtils.convertToRenderer(new TextLabel("<p class=\"navbar-text\">",
			true), label, new TextLabel("</p>", true))));
		return this;
	}

	public NavBarBuilder content(Object content)
	{
		SectionRenderable renderable = rendererFactory.convertToRenderer(content);
		elems.add(new NavBarElement(null, renderable));
		return this;
	}

	public NavBarBuilder action(Link action)
	{
		return action(null, action);
	}

	public NavBarBuilder action(String clazz, Link action)
	{
		elems.add(new NavBarElement(clazz, action));
		return this;
	}

	public NavBarBuilder action(String clazz, HtmlLinkState action)
	{
		elems.add(new NavBarElement(clazz, action));
		return this;
	}

	public NavBarBuilder dropDown(Label textLabel, SingleSelectionList<?> list, boolean active)
	{
		list.setLabel(textLabel);
		list.setDefaultRenderer(active ? BootstrapDropDownRenderer.ACTIVE_RENDER_CONSTANT
			: BootstrapDropDownRenderer.RENDER_CONSTANT);
		elems.add(new NavBarElement("dropdown", list));
		return this;
	}

	public NavBarBuilder splitDropDown(Label textLabel, SingleSelectionList<?> list)
	{
		list.setLabel(textLabel);
		list.setDefaultRenderer(BootstrapSplitDropDownRenderer.SPLIT_NAVBAR_RENDER_CONSTANT);
		elems.add(new NavBarElement("dropdown", list));
		return this;
	}
}
