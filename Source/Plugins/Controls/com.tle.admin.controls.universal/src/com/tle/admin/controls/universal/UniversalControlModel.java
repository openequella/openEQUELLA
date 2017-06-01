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

package com.tle.admin.controls.universal;

import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.admin.wizard.model.CustomControlModel;
import com.tle.admin.Driver;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.universal.UniversalControl;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

@SuppressWarnings("nls")
public class UniversalControlModel extends CustomControlModel<UniversalControl>
{
	private PluginTracker<UniversalPanelValidator> extensions;

	public UniversalControlModel(ControlDefinition definition)
	{
		super(definition);
	}

	private PluginTracker<UniversalPanelValidator> getExtensions(PluginService pluginService)
	{
		if( extensions == null )
		{
			extensions = new PluginTracker<UniversalPanelValidator>(pluginService, "com.tle.admin.controls.universal",
				"universalvalidator", "id").setBeanKey("class");
		}
		return extensions;
	}

	@Override
	public String doValidation(ClientService clientService)
	{

		String error = Validation.hasTitle(this);
		if( error != null )
		{
			return error;
		}

		error = Validation.hasTarget(getControl());
		if( error != null )
		{
			return error;
		}

		UniversalControl control = getControl();
		@SuppressWarnings("unchecked")
		Set<String> types = (Set<String>) control.getAttributes().get("AttachmentTypes");
		if( Check.isEmpty(types) )
		{
			String[] buttons = {s("notype.confirm.yes"), s("notype.confirm.no")};

			final int choice = JOptionPane.showOptionDialog(getEditor(), s("notype.confirm.message"),
				s("notype.confirm.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttons,
				buttons[0]);

			if( choice == JOptionPane.NO_OPTION )
			{
				return s("notype.confirm.no.message");
			}
			else if( choice == JOptionPane.YES_OPTION )
			{
				return null;
			}
		}

		List<UniversalPanelValidator> panels = getExtensions(Driver.instance().getPluginService()).getBeanList();
		for( UniversalPanelValidator upv : panels )
		{
			if( types.contains(upv.getValidatorType()) )
			{
				String validatePanel = upv.doValidation(control, clientService);
				if( !Check.isEmpty(validatePanel) )
				{
					return validatePanel;
				}
			}
		}

		return null;
	}

	private String s(String postfix)
	{
		return CurrentLocale.get("com.tle.admin.controls.universal." + postfix);
	}

}
