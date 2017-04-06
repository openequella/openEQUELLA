package com.tle.admin.usermanagement.standard;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JTextField;

import com.tle.admin.gui.EditorException;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.beans.usermanagement.standard.wrapper.CASConfiguration;
import com.tle.common.i18n.CurrentLocale;

public class CASWrapper extends GeneralPlugin<CASConfiguration>
{
	private JTextField url;
	private JTextField logoutUrl;

	public CASWrapper()
	{
		setup();
	}

	private void setup()
	{
		url = new JTextField();
		logoutUrl = new JTextField();
		addNameAndComponent(CurrentLocale.get("com.tle.admin.usermanagement.caswrapper.url"), //$NON-NLS-1$
			url);
		addNameAndComponent(CurrentLocale.get("com.tle.admin.usermanagement.caswrapper.logouturl"), //$NON-NLS-1$
			logoutUrl);
	}

	@Override
	public boolean save(CASConfiguration config)
	{
		try
		{
			config.setUrl(new URL(url.getText()));
			config.setLogoutUrl(new URL(logoutUrl.getText()));
		}
		catch( MalformedURLException e )
		{
			throw new RuntimeException(e);
		}

		return true;
	}

	@Override
	public void validation() throws EditorException
	{
		String login = url.getText();
		if( login == null || login.trim().length() == 0 )
		{
			throw new EditorException("You must specify a login URL");
		}

		String logout = logoutUrl.getText();
		if( logout == null || logout.trim().length() == 0 )
		{
			throw new EditorException("You must specify a logout URL");
		}
	}

	@Override
	public void load(CASConfiguration config)
	{
		URL configUrl = config.getUrl();
		url.setText((configUrl == null ? "" : configUrl.toString())); //$NON-NLS-1$

		configUrl = config.getLogoutUrl();
		logoutUrl.setText((configUrl == null ? "" : configUrl.toString())); //$NON-NLS-1$
	}
}