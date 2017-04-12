/**
 * 
 */
package com.tle.admin.fedsearch.standard;

import javax.swing.JLabel;
import javax.swing.JTextField;

import com.tle.admin.fedsearch.SearchPlugin;
import com.tle.beans.search.SRUSettings;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author larry
 */
public class SRUPlugin extends SearchPlugin<SRUSettings>
{
	private JTextField urlField;
	private JTextField schemaIdField;

	public SRUPlugin()
	{
		super(SRUSettings.class);
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

	/**
	 * locale strings shared with the SRW version.
	 * 
	 * @param keyPart
	 * @return
	 */
	private static String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.fedsearch.standard.srwplugin." + keyPart);
	}

	@Override
	public void load(SRUSettings settings)
	{
		urlField.setText(settings.getUrl());
		schemaIdField.setText(settings.getSchemaId());
	}

	@Override
	public void save(SRUSettings settings)
	{
		settings.setUrl(urlField.getText());
		settings.setSchemaId(schemaIdField.getText());
	}
}
