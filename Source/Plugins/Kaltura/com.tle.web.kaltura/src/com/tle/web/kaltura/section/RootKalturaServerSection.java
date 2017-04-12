package com.tle.web.kaltura.section;

import static com.tle.core.kaltura.KalturaConstants.PRIV_CREATE_KALTURA;
import static com.tle.core.kaltura.KalturaConstants.PRIV_EDIT_KALTURA;

import java.util.Arrays;

import javax.inject.Inject;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.kaltura.settings.KalturaSettingsLinkSection;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@Bind
public class RootKalturaServerSection extends OneColumnLayout<OneColumnLayoutModel>
{
	@PlugKey("kaltura.page.title")
	private static Label TITLE_LABEL;

	@Inject
	private TLEAclManager aclService;

	private boolean canView()
	{
		return !aclService.filterNonGrantedPrivileges(Arrays.asList(PRIV_EDIT_KALTURA, PRIV_CREATE_KALTURA)).isEmpty();
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView() )
		{
			throw new AccessDeniedException(CurrentLocale.get("com.tle.web.kaltura.error.noaccess"));
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
			crumbs.add(KalturaSettingsLinkSection.getShowKalturaServerLink(info));

			SectionId section = info.getSectionForId(modalSection);
			if( section instanceof ModalKalturaServerSection )
			{
				((ModalKalturaServerSection) section).addBreadcrumbsAndTitle(info, decorations, crumbs);
				return;
			}
		}
		decorations.setTitle(TITLE_LABEL);
		decorations.setContentBodyClass("connectors");

	}

	@Override
	public Class<OneColumnLayoutModel> getModelClass()
	{
		return OneColumnLayoutModel.class;
	}

}
