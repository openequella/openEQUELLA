package com.tle.web.entities.section;

import com.tle.exceptions.AccessDeniedException;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author Aaron
 */
@TreeIndexed
@SuppressWarnings("nls")
public abstract class AbstractRootEntitySection<M extends OneColumnLayoutModel> extends OneColumnLayout<M>
{
	@PlugKey("error.noaccess")
	private static Label LABEL_NO_ACCESS;

	protected abstract boolean canView(SectionInfo info);

	protected abstract Label getTitleLabel(SectionInfo info);

	protected abstract HtmlLinkState getShowEntitiesLink(SectionInfo info);

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView(context) )
		{
			throw new AccessDeniedException(LABEL_NO_ACCESS.getText());
		}

		return super.renderHtml(context);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		OneColumnLayoutModel model = getModel(info);
		SectionId modalSection = model.getModalSection();
		crumbs.add(SettingsUtils.getBreadcrumb());

		if( modalSection != null )
		{
			crumbs.add(getShowEntitiesLink(info));

			SectionId section = info.getSectionForId(modalSection);
			if( section instanceof AbstractEntityContributeSection )
			{
				((AbstractEntityContributeSection<?, ?, ?>) section).addBreadcrumbsAndTitle(info, decorations, crumbs);
			}
			return;
		}
		decorations.setTitle(getTitleLabel(info));
		decorations.setContentBodyClass("entities");
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new OneColumnLayoutModel();
	}
}
