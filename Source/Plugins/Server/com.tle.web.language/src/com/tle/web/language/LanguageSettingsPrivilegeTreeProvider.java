/**
 * 
 */
package com.tle.web.language;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

/**
 * @author larry
 */
@Bind
public class LanguageSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public LanguageSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(LanguageSettingsPrivilegeTreeProvider.class).key(
			"securitytree.languages"), new SettingsTarget("languages"));
	}
}
