package com.tle.web.portal.renderer;

import com.tle.common.portal.entity.Portlet;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;

public abstract class PortletContentRenderer<M> extends AbstractPrototypeSection<M>
	implements
		HtmlRenderer,
		ViewableChildInterface
{
	protected Portlet portlet;

	public void setPortlet(Portlet portlet)
	{
		this.portlet = portlet;
		// Dodgy call to prevent #5714
		this.portlet.getAttributes();
	}
}
