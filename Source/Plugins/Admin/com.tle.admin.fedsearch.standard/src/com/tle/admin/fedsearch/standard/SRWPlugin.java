package com.tle.admin.fedsearch.standard;

import javax.swing.JLabel;
import javax.swing.JTextField;

import com.tle.admin.fedsearch.SearchPlugin;
import com.tle.beans.search.SRWSettings;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author cofarrell
 */
public class SRWPlugin extends SearchPlugin<SRWSettings>
{
	private JTextField urlField;
	private JTextField schemaIdField;

	public SRWPlugin()
	{
		super(SRWSettings.class);
	}

	@Override
	public void initGUI()
	{
		urlField = new JTextField();
		schemaIdField = new JTextField();

		panel.add(new JLabel(s("url")));
		panel.add(urlField);
		panel.add(new JLabel(s("schemaId")));
		panel.add(schemaIdField);
	}

	private static String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.fedsearch.standard.srwplugin." + keyPart);
	}

	@Override
	public void load(SRWSettings settings)
	{
		urlField.setText(settings.getUrl());
		schemaIdField.setText(settings.getSchemaId());
	}

	@Override
	public void save(SRWSettings settings)
	{
		settings.setUrl(urlField.getText());
		settings.setSchemaId(schemaIdField.getText());
	}
}
