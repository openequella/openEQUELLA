package com.tle.web.oauth.servlet;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;

/**
 * @author Aaron
 */
@Bind
public class OAuthDefaultRedirectSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations.getDecorations(context).setMenuMode(MenuMode.HIDDEN);
		Decorations.setTitle(context, new TextLabel("OAuth Response"));
		return viewFactory.createResult("oauthdefaultredirect.ftl", this);
	}
}
