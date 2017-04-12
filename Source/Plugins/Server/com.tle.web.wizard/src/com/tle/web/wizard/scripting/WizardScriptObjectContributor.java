package com.tle.web.wizard.scripting;

import java.util.Map;

/**
 * @author aholland
 */
public interface WizardScriptObjectContributor /*
												 * extends
												 * ScriptObjectContributor
												 */
{
	void addWizardScriptObjects(Map<String, Object> objects, WizardScriptContextCreationParams params);
}
