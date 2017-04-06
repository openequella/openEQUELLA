package com.tle.reporting.oda.webservice;

import java.util.Properties;

import org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSourceEditorPage;
import org.eclipse.swt.widgets.Composite;

/**
 * @author nread
 */
public class WebservicePropertyPage extends DataSourceEditorPage
{
	private WebserviceSelectionPageHelper pageHelper;

	public WebservicePropertyPage()
	{
		super();
	}

	@Override
	public Properties collectCustomProperties(Properties profileProps)
	{
		if( pageHelper == null )
		{
			return profileProps;
		}
		return pageHelper.collectCustomProperties(profileProps);
	}

	@Override
	protected void createAndInitCustomControl(Composite parent, Properties profileProps)
	{
		if( pageHelper == null )
		{
			pageHelper = new WebserviceSelectionPageHelper(this);
		}
		pageHelper.createCustomControl(parent);
		this.setPingButtonVisible(false);
		pageHelper.initCustomControl(profileProps);
	}
}
