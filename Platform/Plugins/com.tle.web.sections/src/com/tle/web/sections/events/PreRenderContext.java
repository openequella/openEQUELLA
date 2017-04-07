package com.tle.web.sections.events;

import java.util.Collection;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.PreRenderable;

@NonNullByDefault
public interface PreRenderContext extends RenderContext
{
	@Override
	void preRender(@Nullable Collection<? extends PreRenderable> preRenderers);

	@Override
	void preRender(PreRenderable preRenderer);

	@Override
	void preRender(PreRenderable... preRenderers);

	void addJs(String src);

	void addCss(String src);

	void addCss(CssInclude css);

	void addStatements(JSStatements statements);

	void addFooterStatements(JSStatements statements);

	void addReadyStatements(JSStatements statements);

	void addHeaderMarkup(String head);

	void bindHandler(String event, Map<String, String> attrs, JSHandler handler);
}
