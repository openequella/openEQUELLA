package com.tle.web.payment.storefront.section.workflow;

import javax.inject.Inject;

import com.tle.core.payment.storefront.privileges.ApprovalsSettingsPrivilegeTreeProvider;
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
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@TreeIndexed
public class RootApprovalsSection extends OneColumnLayout<OneColumnLayoutModel>
{
	@PlugKey("approvals.title")
	private static Label TITLE_LABEL;
	@PlugKey("approvals.error.noaccess")
	private static Label LABEL_NOACCESS;

	@Inject
	private ApprovalsSettingsPrivilegeTreeProvider securityProvider;

	private boolean canView()
	{
		return securityProvider.isAuthorised();
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView() )
		{
			throw new AccessDeniedException(LABEL_NOACCESS.getText());
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
			crumbs.add(ApprovalsLinkSection.getShowApprovalsLink(info));

			SectionId section = info.getSectionForId(modalSection);
			if( section instanceof ModalApprovalSection )
			{
				((ModalApprovalSection) section).addBreadcrumbsAndTitle(info, decorations, crumbs);
				return;
			}
		}
		decorations.setTitle(TITLE_LABEL);
		decorations.setContentBodyClass("approval");
	}

	@Override
	public Class<OneColumnLayoutModel> getModelClass()
	{
		return OneColumnLayoutModel.class;
	}

}
