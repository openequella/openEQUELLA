package com.tle.web.wizard.page;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.wizard.LERepository;

@Bind(WizardPageService.class)
@Singleton
public class WizardPageServiceImpl implements WizardPageService
{
	private PluginTracker<WizardPageFactory> tracker;

	@Override
	public WizardPage createSimplePage(DefaultWizardPage wizardPage, PropBagEx docxml, WebWizardPageState state,
		boolean expert)
	{

		if( docxml == null )
		{
			docxml = new PropBagEx();
		}
		WizardPageFactory pageFactory = tracker.getBeanList().get(0);
		WizardPage page = pageFactory.createWizardPage();
		page.setWizardPage(wizardPage);
		page.setState(state);
		LERepository repos = pageFactory.createRepository(docxml, expert);
		page.setRepository(repos);
		return page;
	}

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		tracker = new PluginTracker<WizardPageFactory>(pluginService, getClass(), "pageFactory", null)
			.setBeanKey("bean");
	}
}
