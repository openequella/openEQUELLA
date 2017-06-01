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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.tle.admin.fedsearch.SearchPlugin;
import com.tle.admin.gui.EditorException;
import com.tle.beans.search.MerlotSettings;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public class MerlotPlugin extends SearchPlugin<MerlotSettings>
{
	private JCheckBox advanced;
	private JTextField licenceKey;

	public MerlotPlugin()
	{
		super(MerlotSettings.class);
	}

	@Override
	public void initGUI()
	{
		licenceKey = new JTextField();
		panel.add(new JLabel(s("label.licencekey")));
		panel.add(licenceKey);

		advanced = new JCheckBox(s("label.advanced"));
		panel.add("span 3", advanced);
	}

	@Override
	public void load(MerlotSettings settings)
	{
		advanced.setSelected(settings.isAdvancedApi());
		licenceKey.setText(settings.getLicenceKey());
	}

	@Override
	public void save(MerlotSettings settings)
	{
		settings.setAdvancedApi(advanced.isSelected());
		settings.setLicenceKey(licenceKey.getText().trim());
	}

	@Override
	public void validation() throws EditorException
	{
		if( Check.isEmpty(licenceKey.getText()) )
		{
			throw new EditorException(s("validation.licencekey"));
		}
	}

	private static String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.fedsearch.standard.merlot." + keyPart);
	}
}
