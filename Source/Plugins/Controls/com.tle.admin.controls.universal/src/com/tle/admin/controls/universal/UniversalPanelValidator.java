package com.tle.admin.controls.universal;

import com.tle.common.applet.client.ClientService;
import com.tle.common.wizard.controls.universal.UniversalControl;

public interface UniversalPanelValidator
{
	String doValidation(UniversalControl control, ClientService clientService);

	String getValidatorType();

}
