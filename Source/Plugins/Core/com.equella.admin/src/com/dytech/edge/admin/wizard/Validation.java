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

package com.dytech.edge.admin.wizard;

import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public final class Validation
{
	/**
	 * Validates a control against the validation rules in the control
	 * definition.
	 */
	public static boolean validateControl(Control control, ClientService clientService)
	{
		// Clear any existing error message
		control.setErrorMessage(null);

		if( control.getDefinition() != null )
		{
			String error = control.doValidation(clientService);
			if( error != null )
			{
				control.setErrorMessage(error);
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a control has at least one target.
	 */
	public static String hasTarget(WizardControl control)
	{
		if( control.getTargetnodes().isEmpty() )
		{
			return CurrentLocale.get("wizard.validation.needstarget");
		}
		else
		{
			return null;
		}
	}

	public static String noAttributeTargets(WizardControl control)
	{
		for( TargetNode tn : control.getTargetnodes() )
		{
			if( tn.isAttribute() )
			{
				return CurrentLocale.get("wizard.validation.noattributetargets");
			}
		}
		return null;
	}

	/**
	 * Checks if the control has a title.
	 */
	public static String hasTitle(Control control)
	{
		return LangUtils.isEmpty(control.getTitle()) ? CurrentLocale.get("wizard.validation.needstitle") : null;
	}

	/**
	 * Checks if the control has at least one child.
	 */
	public static String hasChildren(Control control)
	{
		if( Check.isEmpty(control.getChildren()) )
		{
			return CurrentLocale.get("wizard.validation.needschildren");
		}
		else
		{
			return null;
		}
	}

	private Validation()
	{
		throw new Error();
	}
}
