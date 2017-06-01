/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
