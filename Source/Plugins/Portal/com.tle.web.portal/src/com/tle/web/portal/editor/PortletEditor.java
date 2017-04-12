package com.tle.web.portal.editor;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

public interface PortletEditor
{
	void create(SectionInfo info, String type, boolean admin);

	void edit(SectionInfo info, String portletUuid, boolean admin);

	SectionRenderable render(RenderContext info);

	void saveToSession(SectionInfo info);

	void loadFromSession(SectionInfo info);

	void restore(SectionInfo info);

	void register(SectionTree tree, String parentId);

	void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs);

	SectionRenderable renderHelp(RenderContext context);
}
