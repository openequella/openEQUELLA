/**
 * 
 */
package com.tle.web.quickcontributeandversion;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

/**
 * @author larry
 */
@Bind
public class QuickContributeAndVersionSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	/**
	 * The string id expression to SettingsTarget constructor is exactly as
	 * spelled in the old invocation to construct a SystemSettings in
	 * SettingsPrivilegeTreeProvider::gatherChildTargets. Accordingly
	 * inconsistency with regard to camel-case/lower case etc across different
	 * sub-classes of AbstractSettingsPrivilegeTreeProvider is a legacy. In this
	 * case, the schizoid names of "quick contribute" and "one click submit"
	 * persist.
	 */
	public QuickContributeAndVersionSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(
			QuickContributeAndVersionSettingsPrivilegeTreeProvider.class).key(
			"securitytree.quickcontributeandversionsettings"), new SettingsTarget("selectionSessions"));
	}
}
