package com.tle.mycontent;

import java.util.List;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemId;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author aholland
 */
public interface ContentHandler
{
	HtmlLinkState decorate(SectionInfo info, StandardItemListEntry itemEntry);

	void contribute(SectionInfo info, ItemDefinition collection);

	boolean canEdit(SectionInfo info, ItemId id);

	void edit(SectionInfo info, ItemId id);

	SectionRenderable render(RenderContext context);

	List<HtmlComponentState> getMajorActions(RenderContext context);

	List<HtmlComponentState> getMinorActions(RenderContext context);

	Label getTitle(SectionInfo info);

	void addTrees(SectionInfo info, boolean parameters);

	boolean isRawFiles();

	void addCrumbs(SectionInfo info, Decorations decorations, Breadcrumbs crumbs);
}
