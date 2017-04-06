package com.tle.reporting.oda.webservice;

import java.util.Properties;

import org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSourceWizardPage;
import org.eclipse.swt.widgets.Composite;

public class WebserviceSelectionWizardPage extends DataSourceWizardPage
{
	private WebserviceSelectionPageHelper pageHelper;
	private Properties folderProperties;

	public WebserviceSelectionWizardPage(String pageName)
	{
		super(pageName);
	}

	@Override
	public void createPageCustomControl(Composite parent)
	{
		if( pageHelper == null )
		{
			pageHelper = new WebserviceSelectionPageHelper(this);
		}

		pageHelper.createCustomControl(parent);
		pageHelper.initCustomControl(folderProperties); // in case init was
														// called before create
		this.setPingButtonVisible(false);
	}

	@Override
	public void setInitialProperties(Properties dataSourceProps)
	{
		folderProperties = dataSourceProps;
		if( pageHelper != null )
		{
			pageHelper.initCustomControl(folderProperties);
		}
	}

	@Override
	public Properties collectCustomProperties()
	{
		if( pageHelper != null )
		{
			return pageHelper.collectCustomProperties(folderProperties);
		}
		return (folderProperties != null) ? folderProperties : new Properties();
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		getControl().setFocus();
	}
}
