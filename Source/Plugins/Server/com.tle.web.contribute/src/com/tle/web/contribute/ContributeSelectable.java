package com.tle.web.contribute;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.AbstractSelectionNavAction;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;
import com.tle.web.wizard.WebWizardService;
import com.tle.web.wizard.WizardConstants;

@Bind
@Singleton
public class ContributeSelectable extends AbstractSelectionNavAction
{
	static
	{
		PluginResourceHandler.init(ContributeSelectable.class);
	}

	@PlugKey("selectable.home")
	private static Label LABEL_CONTRIBUTE;

	@Inject
	private WebWizardService webWizardService;

	@Override
	public SectionInfo createSectionInfo(final SectionInfo info, final SelectionSession session)
	{
		final Set<String> collections = session.getContributionCollectionIds();
		if( collections.size() == 1 )
		{
			String xml = session.getInitialItemXml();
			PropBagEx bagXml = null;
			if( !Check.isEmpty(xml) )
			{
				try
				{
					bagXml = new PropBagEx(xml);
				}
				catch( Exception e )
				{
					SectionUtils.throwRuntime(e);
					return null;
				}
			}
			return webWizardService.getNewItemWizardForward(info, collections.iterator().next(), bagXml, null, false);
		}
		return info.createForward(WizardConstants.CONTRIBUTE_URL);
	}

	@Override
	public boolean isActionAvailable(SectionInfo info, SelectionSession session)
	{
		if( !super.isActionAvailable(info, session) )
		{
			return false;
		}
		Layout layout = session.getLayout();
		if( layout == Layout.NORMAL || layout == Layout.COURSE )
		{
			return (session.isAllContributionCollections() || !session.getContributionCollectionIds().isEmpty());
		}
		return false;
	}

	@Override
	public Label getLabelForNavAction(SectionInfo info)
	{
		return LABEL_CONTRIBUTE;
	}

	@Override
	public SectionInfo createForwardForNavAction(SectionInfo fromInfo, SelectionSession session)
	{
		return createSectionInfo(fromInfo, session);
	}

	@Override
	public String getActionType()
	{
		return "contribute";
	}

	@Override
	public boolean isShowBreadcrumbs()
	{
		return true;
	}
}
